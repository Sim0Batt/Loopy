import mysql.connector
import numpy as np
import pandas as pd
import datetime
import time
import json

from db_utils import DB_CONFIG, leggi_dati_grezzi


ANALISI_SONNO_ORE = 48

def analizza_sonno_e_metriche(dati_grezzi: dict) -> (dict, str | None):
    """
    Analizza Fasi Sonno, HRV (RMSSD), RHR e Recupero.
    Restituisce: (Dizionario Totali, Stringa JSON Grafico)
    """
    print("Avvio Analisi Completa Sonno (Fasi, HRV, RHR)...")

    # Valori di default se qualcosa va storto
    risultati_vuoti = ({
        'hrv': None, 'rhr': None, 'recupero': None,
        'sonno_totale_minuti': 0, 'sonno_profondo_minuti': 0,
        'sonno_leggero_minuti': 0, 'sonno_rem_minuti': 0,
        'sonno_sveglio_minuti': 0
    }, None)

    try:
        # Accelerometro
        ts_acc_str = dati_grezzi.get('timestampsAccelerometer', '')
        acc_x_str = dati_grezzi.get('acc_x', '')
        acc_y_str = dati_grezzi.get('acc_y', '')
        acc_z_str = dati_grezzi.get('acc_z', '')

        if not ts_acc_str or not acc_x_str:
             print("  [SONNO] Dati Accelerometro (timestamp o assi X,Y,Z) mancanti.")
             return risultati_vuoti

        ts_acc = [int(t.strip()) for t in ts_acc_str.split(',') if t.strip()]
        acc_x = np.array([float(v.strip()) for v in acc_x_str.split(',') if v.strip()])
        acc_y = np.array([float(v.strip()) for v in acc_y_str.split(',') if v.strip()])
        acc_z = np.array([float(v.strip()) for v in acc_z_str.split(',') if v.strip()])

        min_len = min(len(acc_x), len(acc_y), len(acc_z), len(ts_acc))
        if min_len == 0:
             print("  [SONNO] Dati Accelerometro vuoti dopo il parsing.")
             return risultati_vuoti

        acc_x, acc_y, acc_z, ts_acc = acc_x[:min_len], acc_y[:min_len], acc_z[:min_len], ts_acc[:min_len]

        # Calcolo Magnitudo (movimento totale) e filtro gravita (1g)
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
            print("  [SONNO] Dati PPG mancanti.")
            return risultati_vuoti

        ts_hr = [int(t.strip()) for t in ts_hr_str.split(',') if t.strip()]
        hr_vals = [int(h.strip()) for h in hr_vals_str.split(',') if h.strip()]
        df_hr = pd.DataFrame(
            {'hr': hr_vals},
            index=pd.to_datetime(ts_hr, unit='ms', errors='coerce')
        ).dropna()

        # HRV
        print("  [ALGO] Calcolo HRV (RMSSD)...")
        timestamps_hrv = np.array(ts_hr, dtype=np.int64)
        intervals = np.diff(timestamps_hrv)
        intervals = intervals[(intervals > 300) & (intervals < 2000)] # filtro 30-220 bpm

        risultato_hrv = None
        if len(intervals) >= 2:
            successive_diffs = np.diff(intervals)
            rmssd = np.sqrt(np.mean(np.square(successive_diffs))) # formula RMSSD
            risultato_hrv = int(np.round(rmssd))
            print(f"Calcolo (RMSSD) completato: {risultato_hrv} ms")
        else:
            print("Dati HRV insufficienti per RMSSD.")

        #divido a blocchi di un minuto
        df_acc_min = df_acc.resample('1Min').sum() # Somma movimenti
        df_hr_min = df_hr.resample('1Min').mean() # Media HR
        df_hr_std = df_hr.resample('1Min').std()  # HRV

        df = pd.concat([df_acc_min, df_hr_min, df_hr_std.rename(columns={'hr': 'hr_std'})], axis=1)
        df = df.interpolate(method='time').fillna(method='bfill').fillna(method='ffill')

        if df.empty:
            print("  [SONNO] DataFrame vuoto dopo il resampling.")
            return risultati_vuoti

        # TODO: soglia da tarare con i sensori effettivi
        MOV_THRESHOLD = 0.5
        df['is_asleep'] = df['movement'] <= MOV_THRESHOLD

        df['is_asleep_stable'] = (df['is_asleep'].rolling(window=15, min_periods=1).min() > 0)

        df_sonno = df[df['is_asleep_stable'] == True]

        if df_sonno.empty:
            print("  [SONNO] Nessun sonno stabile rilevato.")
            return {'hrv': risultato_hrv}, None # Ritorno almeno l'HRV

        # calcolo rhr
        resting_hr = df_sonno['hr'].quantile(0.10) # RHR (10° percentile)
        risultato_rhr = int(np.round(resting_hr))
        print(f"  [ALGO] RHR calcolato: {risultato_rhr} bpm")

        # calcolo recupero
        risultato_recupero = None
        if risultato_hrv is not None and risultato_rhr is not None:
            # Formula (TODO: da tarare)
            score_hrv = int(np.clip((risultato_hrv - 20) / (100 - 20) * 100, 0, 100))
            score_rhr = 100 - int(np.clip((risultato_rhr - 50) / (80 - 50) * 100, 0, 100))
            risultato_recupero = int((score_hrv * 0.7) + (score_rhr * 0.3))
            print(f"Recupero calcolato: {risultato_recupero}%")

        # stima fasi sonno
        resting_hr_std = df_sonno['hr_std'].quantile(0.25) # Variabilità a riposo

        def classifica_fase(riga):
            if not riga['is_asleep_stable']:
                return 'SVEGLIO'
            if riga['hr'] < (resting_hr * 1.08) and riga['hr_std'] < (resting_hr_std * 1.5):
                return 'PROFONDO'
            elif riga['hr_std'] > (resting_hr_std * 2.0) and riga['hr'] > resting_hr:
                return 'REM'
            else:
                return 'LEGGERO'

        df['fase'] = df.apply(classifica_fase, axis=1)

        #calcolo finale
        conteggio_fasi = df['fase'].value_counts()
        sonno_totale = int(conteggio_fasi.get('PROFONDO', 0) + conteggio_fasi.get('LEGGERO', 0) + conteggio_fasi.get('REM', 0))

        risultati_finali = {
            'hrv': risultato_hrv,
            'rhr': risultato_rhr,
            'recupero': risultato_recupero,
            'sonno_totale_minuti': sonno_totale,
            'sonno_profondo_minuti': int(conteggio_fasi.get('PROFONDO', 0)),
            'sonno_leggero_minuti': int(conteggio_fasi.get('LEGGERO', 0)),
            'sonno_rem_minuti': int(conteggio_fasi.get('REM', 0)),
            'sonno_sveglio_minuti': int(conteggio_fasi.get('SVEGLIO', 0))
        }

        # creo json
        df_grafico = df[df['is_asleep_stable'] == True][['fase']].copy()
        df_grafico.index = df_grafico.index.strftime('%Y-%m-%d %H:%M')
        grafico_json_str = df_grafico['fase'].to_json(orient='index')

        print(f"  [SONNO] Analisi completata: {risultati_finali}")
        return risultati_finali, grafico_json_str

    except Exception as e:
        print(f"  [SONNO] !!! ERRORE GRAVE durante l'analisi: {e} !!!")
        return risultati_vuoti, None

def salva_riepilogo_sonno(cursor, user_id, metriche: dict):
    """
    Salva tutte le metriche notturne (HRV, Recupero, Fasi Sonno E GRAFICO)
    """
    print(f"  [DB] Salvataggio Riepilogo Sonno Completo per user_id {user_id}...")
    oggi = datetime.date.today()

    query = """
        INSERT INTO daily_summary (
            userId, data, hrv_rmssd, recupero_score, rhr_a_riposo,
            sonno_totale_minuti, sonno_profondo_minuti, sonno_leggero_minuti, sonno_rem_minuti, sonno_sveglio_minuti,
            sonno_grafico_json
        )
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        ON DUPLICATE KEY UPDATE
            hrv_rmssd = VALUES(hrv_rmssd),
            recupero_score = VALUES(recupero_score),
            rhr_a_riposo = VALUES(rhr_a_riposo),
            sonno_totale_minuti = VALUES(sonno_totale_minuti),
            sonno_profondo_minuti = VALUES(sonno_profondo_minuti),
            sonno_leggero_minuti = VALUES(sonno_leggero_minuti),
            sonno_rem_minuti = VALUES(sonno_rem_minuti),
            sonno_sveglio_minuti = VALUES(sonno_sveglio_minuti),
            sonno_grafico_json = VALUES(sonno_grafico_json)
    """
    dati_da_salvare = (
        user_id,
        oggi,
        metriche.get('hrv'), metriche.get('recupero'), metriche.get('rhr'),
        metriche.get('sonno_totale_minuti'), metriche.get('sonno_profondo_minuti'),
        metriche.get('sonno_leggero_minuti'), metriche.get('sonno_rem_minuti'),
        metriche.get('sonno_sveglio_minuti'),
        metriche.get('sonno_grafico_json')
    )
    cursor.execute(query, dati_da_salvare)


def main():
    start_time = time.time()
    print(f"--- AVVIO CALCOLO SONNO/RECUPERO ({datetime.datetime.now()}) ---")

    ora_fine_ms = int(datetime.datetime.now().timestamp() * 1000)
    ora_inizio_ms = ora_fine_ms - (ANALISI_SONNO_ORE * 60 * 60 * 1000)

    conn = None
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor()
        cursor.execute("SELECT id, age FROM User")
        lista_utenti = cursor.fetchall()
        print(f"Trovati {len(lista_utenti)} utenti.")

        for (user_id, user_age) in lista_utenti:
            try:
                print(f"[USER: {user_id}] Calcolo Sonno...")

                # Leggo Dati (PPG e Accelerometro)
                tabelle_necessarie = ['PPG', 'Accelerometro']
                dati_grezzi = leggi_dati_grezzi(cursor, user_id, ora_inizio_ms, ora_fine_ms, tabelle_necessarie)

                if not dati_grezzi.get('timestampsPPG') or not dati_grezzi.get('acc_x'):
                    print(f"[USER: {user_id}] Dati notturni (PPG o ACC) mancanti. Salto.")
                    continue

                risultati_totali, grafico_json = analizza_sonno_e_metriche(dati_grezzi)

                # aggiungo il json
                risultati_totali['sonno_grafico_json'] = grafico_json

                # Ora calcolo recupero (basato sui risultati di HRV e RHR)
                risultati_totali['recupero'] = calcola_recupero(
                    risultati_totali.get('hrv'),
                    risultati_totali.get('rhr')
                )

                # risultati totali contiene tutte le robe calcolate
                salva_riepilogo_sonno(cursor, user_id, risultati_totali)
                conn.commit()
                print(f"[USER: {user_id}] Riepilogo Sonno salvato.")
            except Exception as e:
                print(f"!!! ERRORE (utente {user_id}): {e} !!!")
                conn.rollback()

    except Exception as err:
        print(f"!!! ERRORE DB GLOBALE: {err} !!!")
    finally:
        if conn: conn.close()

    end_time = time.time()
    print(f"--- CALCOLO SONNO TERMINATO (Durata: {end_time - start_time:.2f} sec) ---")

if __name__ == "__main__":
    main()