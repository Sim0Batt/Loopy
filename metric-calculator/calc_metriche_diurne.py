import mysql.connector
import numpy as np
import pandas as pd
import datetime
import time

from db_utils import DB_CONFIG, leggi_dati_grezzi

ANALISI_DA_MEZZANOTTE = True


def analizza_attivita_e_stress(dati_grezzi: dict, rhr: int):
    """
    Analizza l'attività diurna E i livelli di stress.
    Classifica ogni minuto (usando Movimento, HR, Sudorazione).

    RESTITUISCE 3 COSE:
    1. Dizionario Totali Attività
    2. Dizionario Totali Stress
    3. DataFrame Pandas COMPLETO (la timeline minuto per minuto)
    """
    print("  [ALGO] Avvio Analisi Attività E Stress...")

    risultati_vuoti = ({'attivita_sedentaria_minuti': 0, 'attivita_leggera_minuti': 0, 'attivita_moderata_minuti': 0, 'attivita_intensa_minuti': 0},
                       {'stress_calmo_minuti': 0, 'stress_medio_minuti': 0, 'stress_alto_minuti': 0},
                       None)

    try:

        # Accelerometro (X,Y,Z)
        ts_acc_str = dati_grezzi.get('timestampsAccelerometer', '')
        acc_x_str = dati_grezzi.get('acc_x', '')
        acc_y_str = dati_grezzi.get('acc_y', '')
        acc_z_str = dati_grezzi.get('acc_z', '')

        if not ts_acc_str or not acc_x_str:
             print("  [ATTIVITA] Dati Accelerometro mancanti.")
             return risultati_vuoti

        ts_acc = [int(t.strip()) for t in ts_acc_str.split(',') if t.strip()]
        acc_x = np.array([float(v.strip()) for v in acc_x_str.split(',') if v.strip()])
        acc_y = np.array([float(v.strip()) for v in acc_y_str.split(',') if v.strip()])
        acc_z = np.array([float(v.strip()) for v in acc_z_str.split(',') if v.strip()])

        min_len = min(len(acc_x), len(acc_y), len(acc_z), len(ts_acc))
        if min_len == 0: return risultati_vuoti
        acc_x, acc_y, acc_z, ts_acc = acc_x[:min_len], acc_y[:min_len], acc_z[:min_len], ts_acc[:min_len]

        # Calcolo Magnitudo (movimento totale) e filtro la gravità (1g)
        magnitudo_grezza = np.sqrt(acc_x**2 + acc_y**2 + acc_z**2)
        magnitudo_filtrata = np.abs(magnitudo_grezza - np.mean(magnitudo_grezza))

        df_acc = pd.DataFrame(
            {'movement': magnitudo_filtrata},
            index=pd.to_datetime(ts_acc, unit='ms', errors='coerce')
        ).dropna()

        # PPG
        ts_hr_str = dati_grezzi.get('timestampsPPG', '')
        hr_vals_str = dati_grezzi.get('heartRates', '')
        if not ts_hr_str or not hr_vals_str:
            print("  [ATTIVITA] Dati PPG mancanti.")
            return risultati_vuoti

        ts_hr = [int(t.strip()) for t in ts_hr_str.split(',') if t.strip()]
        hr_vals = [int(h.strip()) for h in hr_vals_str.split(',') if h.strip()]
        df_hr = pd.DataFrame(
            {'hr': hr_vals},
            index=pd.to_datetime(ts_hr, unit='ms', errors='coerce')
        ).dropna()

        # Elettrodi (Sudorazione)
        ts_sweat_str = dati_grezzi.get('timestampsElectrodes', '')
        sweat_vals_str = dati_grezzi.get('sweatings', '')
        if not ts_sweat_str or not sweat_vals_str:
            print("  [ATTIVITA] Dati Sudorazione mancanti.")
            return risultati_vuoti

        ts_sweat = [int(t.strip()) for t in ts_sweat_str.split(',') if t.strip()]
        sweat_vals = [float(s.strip()) for s in sweat_vals_str.split(',') if s.strip()]
        df_sweat = pd.DataFrame(
            {'sweat': sweat_vals},
            index=pd.to_datetime(ts_sweat, unit='ms', errors='coerce')
        ).dropna()

        df_acc_min = df_acc.resample('1Min').sum()
        df_hr_min = df_hr.resample('1Min').mean()
        df_sweat_min = df_sweat.resample('1Min').mean()

        df = pd.concat([df_acc_min, df_hr_min, df_sweat_min], axis=1)
        df = df.interpolate(method='time').fillna(method='bfill').fillna(method='ffill')

        if df.empty:
            print("  [ATTIVITA] DataFrame vuoto dopo il resampling.")
            return risultati_vuoti


        # TODO: tarare movimenti
        SOGLIA_MOV_SEDENTARIO = 0.5
        SOGLIA_MOV_LEGGERO = 5.0
        SOGLIA_HR_LEGGERO = rhr + 20
        SOGLIA_HR_MODERATO = rhr + 50

        SOGLIA_HR_STRESS = rhr + 15
        SOGLIA_SUDORE_STRESS = 2.0

        def classifica_dati(riga):
            mov = riga['movement']
            hr = riga['hr']
            sweat = riga['sweat']

            zona_attivita = 'LEGGERO' # Default
            if mov <= SOGLIA_MOV_SEDENTARIO:
                zona_attivita = 'SEDENTARIO'
            elif hr >= SOGLIA_HR_MODERATO:
                zona_attivita = 'INTENSO'
            elif hr >= SOGLIA_HR_LEGGERO and mov > SOGLIA_MOV_LEGGERO:
                zona_attivita = 'MODERATO'

            zona_stress = 'CALMO' # Default
            if zona_attivita != 'INTENSO':
                if mov <= SOGLIA_MOV_SEDENTARIO:
                    if hr > SOGLIA_HR_STRESS and sweat > SOGLIA_SUDORE_STRESS:
                        zona_stress = 'ALTO'
                    elif hr > SOGLIA_HR_STRESS or sweat > SOGLIA_SUDORE_STRESS:
                        zona_stress = 'MEDIO'

            return zona_attivita, zona_stress

        df[['zona_attivita', 'zona_stress']] = df.apply(classifica_dati, axis=1, result_type='expand')

        conteggio_zone_attivita = df['zona_attivita'].value_counts()
        risultati_totali_attivita = {
            'attivita_sedentaria_minuti': int(conteggio_zone_attivita.get('SEDENTARIO', 0)),
            'attivita_leggera_minuti': int(conteggio_zone_attivita.get('LEGGERO', 0)),
            'attivita_moderata_minuti': int(conteggio_zone_attivita.get('MODERATO', 0)),
            'attivita_intensa_minuti': int(conteggio_zone_attivita.get('INTENSO', 0))
        }

        conteggio_zone_stress = df['zona_stress'].value_counts()
        risultati_totali_stress = {
            'stress_calmo_minuti': int(conteggio_zone_stress.get('CALMO', 0)),
            'stress_medio_minuti': int(conteggio_zone_stress.get('MEDIO', 0)),
            'stress_alto_minuti': int(conteggio_zone_stress.get('ALTO', 0))
        }

        print(f"  [ATTIVITA] Analisi completata: {risultati_totali_attivita}")
        return risultati_totali_attivita, risultati_totali_stress, df

    except Exception as e:
        print(f"  [ATTIVITA] !!! ERRORE GRAVE durante l'analisi: {e} !!!")
        return risultati_vuoti

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
    oggi_str = today.strftime('%Y-%m-%d') # Per cancellare i vecchi dati

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
        ts_str = index.strftime('%Y-%m-%d %H:%M:%S')

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