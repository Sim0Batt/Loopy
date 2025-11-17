import mysql.connector
import numpy as np
import pandas as pd
import datetime
import time
import json

from db_utils import DB_CONFIG, leggi_dati_grezzi

ANALISI_DA_MEZZANOTTE = True


def analizza_attivita_e_stress(dati_grezzi: dict, rhr: int) -> (dict, str | None, dict, str | None):
    """
    Analisi attività diurna e livelli di stress.
    Classifica ogni minuto (usando Movimento, HR, Sudorazione).
    Restituisce: (Totali Attività, Grafico Attività JSON, Totali Stress, Grafico Stress JSON)
    """
    print("Avvio Analisi Attività E Stress...")

    # valori di default se qualcosa va storto
    risultati_vuoti_att = {'attivita_sedentaria_minuti': 0, 'attivita_leggera_minuti': 0, 'attivita_moderata_minuti': 0, 'attivita_intensa_minuti': 0}
    risultati_vuoti_stress = {'stress_calmo_minuti': 0, 'stress_medio_minuti': 0, 'stress_alto_minuti': 0}
    risultati_vuoti = (risultati_vuoti_att, None, risultati_vuoti_stress, None)

    try:
        # PREPARAZIONE DATI

        # Accelerometro (X,Y,Z)
        ts_acc_str = dati_grezzi.get('timestampsAccelerometer', '')
        acc_x_str = dati_grezzi.get('acc_x', '')
        acc_y_str = dati_grezzi.get('acc_y', '')
        acc_z_str = dati_grezzi.get('acc_z', '')
        if not ts_acc_str or not acc_x_str:
             print("  [ATTIVITA/STRESS] Dati Accelerometro mancanti.")
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
            print("  [ATTIVITA/STRESS] Dati PPG mancanti.")
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
            print("  [ATTIVITA/STRESS] Dati Sudorazione mancanti.")
            return risultati_vuoti

        ts_sweat = [int(t.strip()) for t in ts_sweat_str.split(',') if t.strip()]
        sweat_vals = [float(s.strip()) for s in sweat_vals_str.split(',') if s.strip()]
        df_sweat = pd.DataFrame(
            {'sweat': sweat_vals},
            index=pd.to_datetime(ts_sweat, unit='ms', errors='coerce')
        ).dropna()

        # Raggruppa tutti i sensori in "zone" da 1 minuto
        df_acc_min = df_acc.resample('1Min').sum() # Somma dei movimenti
        df_hr_min = df_hr.resample('1Min').mean() # Media HR
        df_sweat_min = df_sweat.resample('1Min').mean() # Media Sudorazione

        # Unisce tutto in una tabella e riempie i buchi
        df = pd.concat([df_acc_min, df_hr_min, df_sweat_min], axis=1)
        df = df.interpolate(method='time').fillna(method='bfill').fillna(method='ffill')

        if df.empty:
            print("  [ATTIVITA/STRESS] DataFrame vuoto dopo il resampling.")
            return risultati_vuoti


        # SOGLIE (TODO: Valori da tarare in base ai test del sensore)
        SOGLIA_MOV_SEDENTARIO = 0.5 # (es. 0.5g totali di movimento in 1 min)
        SOGLIA_MOV_LEGGERO = 5.0
        SOGLIA_HR_LEGGERO = rhr + 20
        SOGLIA_HR_MODERATO = rhr + 50
        SOGLIA_HR_STRESS = rhr + 15
        SOGLIA_SUDORE_STRESS = 2.0

        def classifica_dati(riga):
            mov, hr, sweat = riga['movement'], riga['hr'], riga['sweat']

            # 1. Classifica Attività
            zona_attivita = 'LEGGERO' # Default
            if mov <= SOGLIA_MOV_SEDENTARIO:
                zona_attivita = 'SEDENTARIO'
            elif hr >= SOGLIA_HR_MODERATO:
                zona_attivita = 'INTENSO'
            elif hr >= SOGLIA_HR_LEGGERO and mov > SOGLIA_MOV_LEGGERO:
                zona_attivita = 'MODERATO'

            # 2. Classifica Stress
            zona_stress = 'CALMO' # Default
            # misuriamo stress solo se non sei in attività intensa
            if zona_attivita != 'INTENSO':
                if mov <= SOGLIA_MOV_SEDENTARIO: # se sei fermo...
                    # ...e hai HR e Sudorazione alti = STRESS
                    if hr > SOGLIA_HR_STRESS and sweat > SOGLIA_SUDORE_STRESS:
                        zona_stress = 'ALTO'
                    elif hr > SOGLIA_HR_STRESS or sweat > SOGLIA_SUDORE_STRESS:
                        zona_stress = 'MEDIO'

            return zona_attivita, zona_stress

        # Applica la funzione a ogni riga (minuto)
        df[['zona_attivita', 'zona_stress']] = df.apply(classifica_dati, axis=1, result_type='expand')

        # CALCOLO STATISTICHE TOTALI
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

        # Ccreo json per i grafici (a intervalli di un minuto... quindi gonzo quando farai grafici considera intervalli di un minuto),
        #vedi te se prendere intervalli piu grandi magari di 15 o 30 minuti e mettere la media di stress e attivita dei valori
        # dalla data/ora prendo solo ora (es. "09:30")
        df.index = df.index.strftime('%H:%M')
        grafico_attivita_json_str = df['zona_attivita'].to_json(orient='index')
        grafico_stress_json_str = df['zona_stress'].to_json(orient='index')

        print(f"  [ATTIVITA] Analisi completata: {risultati_totali_attivita}")
        print(f"  [STRESS] Analisi completata: {risultati_totali_stress}")
        return risultati_totali_attivita, grafico_attivita_json_str, risultati_totali_stress, grafico_stress_json_str

    except Exception as e:
        print(f"  [ATTIVITA/STRESS] !!! ERRORE GRAVE durante l'analisi: {e} !!!")
        return risultati_vuoti




def leggi_rhr_attuale(cursor, user_id) -> int:
    """ Legge l'RHR calcolato stanotte (dallo script del sonno) """
    query = "SELECT rhr_a_riposo FROM daily_summary WHERE userId = %s ORDER BY data DESC LIMIT 1"
    cursor.execute(query, (user_id,))
    risultato = cursor.fetchone()
    # Se non abbiamo uno storico, usiamo 55 come RHR di base
    if risultato and risultato[0] is not None:
        return int(risultato[0])
    else:
        return 55

def salva_riepilogo_attivita_e_stress(cursor, user_id, metriche_attivita: dict, metriche_stress: dict):
    """
    Aggiorno la riga di OGGI con i totali di attività E stress.
    """
    print(f"  [DB] Salvataggio Attività/Stress per user_id {user_id}...")
    oggi = datetime.date.today()

    # Nomi delle colonne SQL (da TabellaRiepilogoGiornalieroTable.kt, mi pare il putno B.1 in  TODO.txt)
    query = """
        INSERT INTO daily_summary (
            userId, data,
            attivita_sedentaria_minuti, attivita_leggera_minuti,
            attivita_moderata_minuti, attivita_intensa_minuti,
            attivita_grafico_json,
            stress_calmo_minuti, stress_medio_minuti, stress_alto_minuti,
            stress_grafico_json
        )
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        ON DUPLICATE KEY UPDATE
            attivita_sedentaria_minuti = VALUES(attivita_sedentaria_minuti),
            attivita_leggera_minuti = VALUES(attivita_leggera_minuti),
            attivita_moderata_minuti = VALUES(attivita_moderata_minuti),
            attivita_intensa_minuti = VALUES(attivita_intensa_minuti),
            attivita_grafico_json = VALUES(attivita_grafico_json),
            stress_calmo_minuti = VALUES(stress_calmo_minuti),
            stress_medio_minuti = VALUES(stress_medio_minuti),
            stress_alto_minuti = VALUES(stress_alto_minuti),
            stress_grafico_json = VALUES(stress_grafico_json)
    """
    dati_da_salvare = (
        user_id,
        oggi,
        metriche_attivita.get('attivita_sedentaria_minuti'),
        metriche_attivita.get('attivita_leggera_minuti'),
        metriche_attivita.get('attivita_moderata_minuti'),
        metriche_attivita.get('attivita_intensa_minuti'),
        metriche_attivita.get('attivita_grafico_json'),
        metriche_stress.get('stress_calmo_minuti'),
        metriche_stress.get('stress_medio_minuti'),
        metriche_stress.get('stress_alto_minuti'),
        metriche_stress.get('stress_grafico_json')
    )
    cursor.execute(query, dati_da_salvare)

def main():
    start_time = time.time()
    print(f"--- AVVIO CALCOLO ATTIVITA' DIURNA ({datetime.datetime.now()}) ---")

    # calcolo la finestra temporale (da mezzanotte a ora)
    now = datetime.datetime.now()
    start_of_day = datetime.datetime(now.year, now.month, now.day, 0, 0, 0)
    ora_fine_ms = int(now.timestamp() * 1000)
    ora_inizio_ms = int(start_of_day.timestamp() * 1000)

    conn = None
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor()
        cursor.execute("SELECT id FROM User") # Nome tabella "User"
        lista_utenti = cursor.fetchall()
        print(f"Trovati {len(lista_utenti)} utenti.")

        for (user_id,) in lista_utenti:
            try:
                print(f"[USER: {user_id}] Calcolo Attività/Stress...")

                # 1. Leggo RHR (calcolato stanotte)
                rhr_attuale = leggi_rhr_attuale(cursor, user_id)

                # 2. Leggo Dati Grezzi (ACC, PPG, Elettrodi di OGGI)
                tabelle_necessarie = ['PPG', 'Accelerometro', 'Elettrodi']
                dati_grezzi = leggi_dati_grezzi(cursor, user_id, ora_inizio_ms, ora_fine_ms, tabelle_necessarie)

                # Controllo che tutti i dati necessari ci siano
                if not dati_grezzi.get('acc_x') or not dati_grezzi.get('heartRates') or not dati_grezzi.get('sweatings'):
                    print(f"[USER: {user_id}] Dati (PPG, ACC o Elettrodi) mancanti. Salto.")
                    continue

                # 3. Calcolo Metriche (Totali e Grafico)
                risultati_att, grafico_att_json, risultati_stress, grafico_stress_json = analizza_attivita_e_stress(dati_grezzi, rhr_attuale)

                # Metto insieme i risultati
                risultati_att['attivita_grafico_json'] = grafico_att_json
                risultati_stress['stress_grafico_json'] = grafico_stress_json

                # Salvo Risultati
                if risultati_att:
                    salva_riepilogo_attivita_e_stress(cursor, user_id, risultati_att, risultati_stress)
                    conn.commit()
                    print(f"[USER: {user_id}] Riepilogo Attività/Stress salvato.")
            except Exception as e:
                print(f"!!! ERRORE (utente {user_id}): {e} !!!")
                conn.rollback()

    except Exception as err:
        print(f"!!! ERRORE DB GLOBALE: {err} !!!")
    finally:
        if conn: conn.close()

    end_time = time.time()
    print(f"CALCOLO ATTIVITA' TERMINATO (Durata: {end_time - start_time:.2f} sec) ---")

if __name__ == "__main__":
    main()