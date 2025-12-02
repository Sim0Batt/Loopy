import mysql.connector
import numpy as np
import pandas as pd
import datetime
import time

from db_utils import DB_CONFIG, leggi_dati_grezzi

ANALISI_DA_MEZZANOTTE = True


def analizza_attivita_e_stress(dati_grezzi: dict, rhr: int):
    """
    Analisi attività diurna E i livelli di stress.
    Classifica ogni minuto usando un punteggio pesato (0-5).
    """
    print("  [ALGO] Avvio Analisi Attività E Stress (Algoritmo Pesato)...")

    # Dizionari per i risultati finali
    totali_attivita = {'attivita_sedentaria_minuti': 0, 'attivita_leggera_minuti': 0, 
                       'attivita_moderata_minuti': 0, 'attivita_intensa_minuti': 0}
    totali_stress = {'stress_calmo_minuti': 0, 'stress_medio_minuti': 0, 'stress_alto_minuti': 0}
    
    try:
        # --- 1. PARSING DATI (Rimane quasi uguale, ma gestiamo meglio i formati) ---
        # Accelerometro
        acc_x_str = dati_grezzi.get('acc_x', '')
        ts_acc_str = dati_grezzi.get('timestampsAccelerometer', '')
        
        if not acc_x_str or not ts_acc_str:
             return totali_attivita, "{}", totali_stress, "{}"

        # Parsing vettorizzato (più veloce e sicuro)
        ts_acc = np.array([int(t) for t in ts_acc_str.split(',') if t.strip()])
        acc_x = np.array([float(v) for v in acc_x_str.split(',') if v.strip()])
        acc_y = np.array([float(v) for v in dati_grezzi.get('acc_y', '').split(',') if v.strip()])
        acc_z = np.array([float(v) for v in dati_grezzi.get('acc_z', '').split(',') if v.strip()])
        
        # Allineamento lunghezze
        min_len = min(len(acc_x), len(acc_y), len(acc_z), len(ts_acc))
        df_acc = pd.DataFrame({
            'x': acc_x[:min_len], 'y': acc_y[:min_len], 'z': acc_z[:min_len]
        }, index=pd.to_datetime(ts_acc[:min_len], unit='ms'))

        # CALCOLO MOVIMENTO NETTO (Rimuove Gravità)
        # 1. Calcola Magnitudo totale
        df_acc['mag'] = np.sqrt(df_acc['x']**2 + df_acc['y']**2 + df_acc['z']**2)
        # 2. Rimuovi la componente statica (gravità) sottraendo la media mobile
        # Questo lascia solo i "picchi" di movimento vero
        df_acc['movement'] = np.abs(df_acc['mag'] - df_acc['mag'].rolling('10s').mean())

        # PPG (Battito)
        hr_str = dati_grezzi.get('heartRates', '')
        ts_hr_str = dati_grezzi.get('timestampsPPG', '')
        if hr_str:
            hr_vals = [int(v) for v in hr_str.split(',') if v.strip()]
            ts_hr = [int(t) for t in ts_hr_str.split(',') if t.strip()]
            df_hr = pd.DataFrame({'hr': hr_vals}, index=pd.to_datetime(ts_hr, unit='ms'))
        else:
            df_hr = pd.DataFrame({'hr': rhr}, index=df_acc.index) # Fallback

        # Elettrodi (Sudorazione)
        sw_str = dati_grezzi.get('sweatings', '')
        ts_sw_str = dati_grezzi.get('timestampsElectrodes', '')
        if sw_str:
            sw_vals = [float(v) for v in sw_str.split(',') if v.strip()]
            ts_sw = [int(t) for t in ts_sw_str.split(',') if t.strip()]
            df_sweat = pd.DataFrame({'sweat': sw_vals}, index=pd.to_datetime(ts_sw, unit='ms'))
        else:
            df_sweat = pd.DataFrame({'sweat': 0.1}, index=df_acc.index) # Fallback basso

        # --- 2. RESAMPLING AL MINUTO (Creiamo la tabella unificata) ---
        df = pd.concat([
            df_acc['movement'].resample('1min').mean(), # Media del movimento netto
            df_hr['hr'].resample('1min').mean(),
            df_sweat['sweat'].resample('1min').mean()
        ], axis=1).interpolate().dropna()

        if df.empty: return totali_attivita, "{}", totali_stress, "{}"

        # --- 3. LOGICA DI CLASSIFICAZIONE (Pesi e Score 0-5) ---
        
        def calcola_score(row):
            mov, hr, sweat = row['movement'], row['hr'], row['sweat']
            
            # A. Normalizzazione (Portiamo tutto da 0 a 1)
            # Movimento: 0.02g è fermo, >0.5g è attività intensa
            norm_mov = np.clip((mov - 0.02) / (0.5 - 0.02), 0, 1)
            
            # Cuore: Usiamo la riserva cardiaca (HRR). Max stimato = 190.
            # (HR - RHR) / (Max - RHR)
            hr_max = 190 
            norm_hr = np.clip((hr - rhr) / (hr_max - rhr), 0, 1)
            
            # Sudore: 0.5uS base, 5.0uS massimo
            norm_sweat = np.clip((sweat - 0.5) / (5.0 - 0.5), 0, 1)

            # B. Calcolo Score Attività (0-5)
            # Pesi: 50% Movimento, 40% Cuore, 10% Sudore
            raw_score = (norm_mov * 0.5) + (norm_hr * 0.4) + (norm_sweat * 0.1)
            act_score = int(round(raw_score * 5)) # Arrotonda a intero 0-5
            
            # Mappatura categorie per i totali
            cat_attivita = 'SEDENTARIO' # 0
            if act_score == 1: cat_attivita = 'LEGGERO'
            elif act_score == 2: cat_attivita = 'MODERATO'
            elif act_score >= 3: cat_attivita = 'INTENSO'

            # C. Calcolo Stress (Solo se attività è bassa)
            cat_stress = 'CALMO'
            if act_score <= 1: # Se sei fermo o quasi
                # Stress Score: HR elevato a riposo + Sudorazione
                # Diamo molto peso all'HRV (qui approssimato da HR/RHR) e Sudore
                stress_index = (norm_hr * 0.6) + (norm_sweat * 0.4)
                
                if stress_index > 0.6: cat_stress = 'ALTO'
                elif stress_index > 0.3: cat_stress = 'MEDIO'
            
            return cat_attivita, cat_stress

        # Applica la logica riga per riga
        df[['zona_attivita', 'zona_stress']] = df.apply(calcola_score, axis=1, result_type='expand')

        # --- 4. OUTPUT (Conteggi e JSON) ---
        counts_att = df['zona_attivita'].value_counts()
        totali_attivita = {
            'attivita_sedentaria_minuti': int(counts_att.get('SEDENTARIO', 0)),
            'attivita_leggera_minuti': int(counts_att.get('LEGGERO', 0)),
            'attivita_moderata_minuti': int(counts_att.get('MODERATO', 0)),
            'attivita_intensa_minuti': int(counts_att.get('INTENSO', 0))
        }
        
        counts_stress = df['zona_stress'].value_counts()
        totali_stress = {
            'stress_calmo_minuti': int(counts_stress.get('CALMO', 0)),
            'stress_medio_minuti': int(counts_stress.get('MEDIO', 0)),
            'stress_alto_minuti': int(counts_stress.get('ALTO', 0))
        }

       
        # Ritorniamo il DataFrame originale per il salvataggio SQL
        return totali_attivita, totali_stress, df

    except Exception as e:
        print(f"  [ERRORE ANALISI DIURNA] {e}")
        return totali_attivita, "{}", totali_stress, "{}"
    
    

def leggi_rhr_attuale(cursor, user_id) -> int:
    """ Legge l'RHR calcolato stanotte """
    query = "SELECT rhr_a_riposo FROM daily_summary WHERE userId = %s ORDER BY data DESC LIMIT 1"
    cursor.execute(query, (user_id,))
    risultato = cursor.fetchone()
    if risultato and risultato[0] is not None: return int(risultato[0])
    else: return 55 # Default

def salva_riepilogo_attivita_e_stress(cursor, user_id, metriche_att, metriche_stress, df_timeline):
    """
    Salva:
    1. I TOTALI nella tua tabella 'daily_summary'.
    2. Le RIGHE DELLA TIMELINE nelle tabelle 'Activity' e 'Stress' del tuo amico.
    """
    print(f"  [DB] Salvataggio Totali e Timeline per user_id {user_id}...")
    oggi = datetime.date.today()
    oggi_str = oggi.strftime('%Y-%m-%d') # Per cancellare i vecchi dati

    query_totali = """
        INSERT INTO daily_summary (
            userId, data,
            attivita_sedentaria_minuti, attivita_leggera_minuti,
            attivita_moderata_minuti, attivita_intensa_minuti,
            stress_calmo_minuti, stress_medio_minuti, stress_alto_minuti
        )
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
        ON DUPLICATE KEY UPDATE
            attivita_sedentaria_minuti = VALUES(attivita_sedentaria_minuti),
            attivita_leggera_minuti = VALUES(attivita_leggera_minuti),
            attivita_moderata_minuti = VALUES(attivita_moderata_minuti),
            attivita_intensa_minuti = VALUES(attivita_intensa_minuti),
            stress_calmo_minuti = VALUES(stress_calmo_minuti),
            stress_medio_minuti = VALUES(stress_medio_minuti),
            stress_alto_minuti = VALUES(stress_alto_minuti)
    """
    cursor.execute(query_totali, (
        user_id, oggi,
        metriche_att.get('attivita_sedentaria_minuti'), metriche_att.get('attivita_leggera_minuti'),
        metriche_att.get('attivita_moderata_minuti'), metriche_att.get('attivita_intensa_minuti'),
        metriche_stress.get('stress_calmo_minuti'), metriche_stress.get('stress_medio_minuti'),
        metriche_stress.get('stress_alto_minuti')
    ))


    # Mappatura Stringhe
    map_attivita = {'SEDENTARIO': 0, 'LEGGERO': 1, 'MODERATO': 2, 'INTENSO': 3}
    map_stress = {'CALMO': 0, 'MEDIO': 1, 'ALTO': 2}

    # Cancello i dati di oggi
    cursor.execute(f"DELETE FROM Activity WHERE userId = %s AND timestamp LIKE '{oggi_str}%'", (user_id,))
    cursor.execute(f"DELETE FROM Stress WHERE userId = %s AND timestamp LIKE '{oggi_str}%'", (user_id,))

    valori_activity = []
    valori_stress = []

    for index, row in df_timeline.iterrows():
        ts_str = index.strftime('%Y-%m-%dT%H:%M:%SZ')

        livello_att = map_attivita.get(row['zona_attivita'], 0)
        livello_stress = map_stress.get(row['zona_stress'], 0)

        valori_activity.append((livello_att, user_id, ts_str))
        valori_stress.append((livello_stress, user_id, ts_str))


    if valori_activity:
        cursor.executemany("INSERT INTO Activity (activity_level, userId, timestamp) VALUES (%s, %s, %s)", valori_activity)

    if valori_stress:
        cursor.executemany("INSERT INTO Stress (stress_level, userId, timestamp) VALUES (%s, %s, %s)", valori_stress)

    print(f"  [DB] Inserite {len(valori_activity)} righe nelle tabelle grafici.")


def main():
    start_time = time.time()
    print(f"--- AVVIO CALCOLO ATTIVITA' DIURNA ({datetime.datetime.now()}) ---")

    now = datetime.datetime.now()
    start_of_day = datetime.datetime(now.year, now.month, now.day, 0, 0, 0)
    ora_fine_ms = int(now.timestamp() * 1000)
    ora_inizio_ms = int(start_of_day.timestamp() * 1000)

    conn = None
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor()
        cursor.execute("SELECT id FROM User")
        lista_utenti = cursor.fetchall()

        for (user_id,) in lista_utenti:
            try:
                print(f"[USER: {user_id}] Calcolo Attività...")

                rhr_attuale = leggi_rhr_attuale(cursor, user_id)
                tabelle_necessarie = ['PPG', 'Accelerometro', 'Elettrodi']
                dati_grezzi = leggi_dati_grezzi(cursor, user_id, ora_inizio_ms, ora_fine_ms, tabelle_necessarie)

                if not dati_grezzi.get('acc_x') or not dati_grezzi.get('heartRates') or not dati_grezzi.get('sweatings'):
                    print(f"[USER: {user_id}] Dati (PPG, ACC o Elettrodi) mancanti. Salto.")
                    continue

                risultati_att, risultati_stress, df_grafici = analizza_attivita_e_stress(dati_grezzi, rhr_attuale)

                if risultati_att:
                    salva_riepilogo_attivita_e_stress(cursor, user_id, risultati_att, risultati_stress, df_grafici)
                    conn.commit()
                    print(f"[USER: {user_id}] Riepilogo e Grafici salvati.")
            except Exception as e:
                print(f"!!! ERRORE (utente {user_id}): {e} !!!")
                conn.rollback()

    except Exception as err:
        print(f"!!! ERRORE DB GLOBALE: {err} !!!")
    finally:
        if conn: conn.close()

    end_time = time.time()
    print(f"--- CALCOLO ATTIVITA' TERMINATO (Durata: {end_time - start_time:.2f} sec) ---")

if __name__ == "__main__":
    main()