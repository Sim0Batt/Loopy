import mysql.connector
import numpy as np
import pandas as pd
import datetime
import time
from db_utils import DB_CONFIG, leggi_dati_grezzi

ANALISI_SONNO_ORE = 48


def analizza_sonno_e_metriche(dati_grezzi: dict):

    print("  [ALGO] Avvio Analisi Completa Sonno...")

    risultati_vuoti = ({'hrv': None, 'rhr': None, 'recupero': None, 'sonno_totale_minuti': 0, 'sonno_profondo_minuti': 0, 'sonno_leggero_minuti': 0, 'sonno_rem_minuti': 0, 'sonno_sveglio_minuti': 0}, None)

    try:
        ts_acc_str = dati_grezzi.get('timestampsAccelerometer', '')
        acc_x_str = dati_grezzi.get('acc_x', '')
        acc_y_str = dati_grezzi.get('acc_y', '')
        acc_z_str = dati_grezzi.get('acc_z', '')

        if not ts_acc_str or not acc_x_str: return risultati_vuoti

        ts_acc = [int(t.strip()) for t in ts_acc_str.split(',') if t.strip()]
        acc_x = np.array([float(v.strip()) for v in acc_x_str.split(',') if v.strip()])
        acc_y = np.array([float(v.strip()) for v in acc_y_str.split(',') if v.strip()])
        acc_z = np.array([float(v.strip()) for v in acc_z_str.split(',') if v.strip()])

        min_len = min(len(acc_x), len(acc_y), len(acc_z), len(ts_acc))
        if min_len == 0: return risultati_vuoti
        acc_x, acc_y, acc_z, ts_acc = acc_x[:min_len], acc_y[:min_len], acc_z[:min_len], ts_acc[:min_len]

        magnitudo_grezza = np.sqrt(acc_x**2 + acc_y**2 + acc_z**2)
        magnitudo_filtrata = np.abs(magnitudo_grezza - np.mean(magnitudo_grezza))

        df_acc = pd.DataFrame({'movement': magnitudo_filtrata}, index=pd.to_datetime(ts_acc, unit='ms', errors='coerce')).dropna()

        ts_hr_str = dati_grezzi.get('timestampsPPG', '')
        hr_vals_str = dati_grezzi.get('heartRates', '')
        if not ts_hr_str or not hr_vals_str: return risultati_vuoti

        ts_hr = [int(t.strip()) for t in ts_hr_str.split(',') if t.strip()]
        hr_vals = [int(h.strip()) for h in hr_vals_str.split(',') if h.strip()]
        df_hr = pd.DataFrame({'hr': hr_vals}, index=pd.to_datetime(ts_hr, unit='ms', errors='coerce')).dropna()

        # CALCOLO HRV (RMSSD) sui dati grezzi
        timestamps_hrv = np.array(ts_hr, dtype=np.int64)
        intervals = np.diff(timestamps_hrv)
        intervals = intervals[(intervals > 300) & (intervals < 2000)]
        risultato_hrv = None
        if len(intervals) >= 2:
            rmssd = np.sqrt(np.mean(np.square(np.diff(intervals))))
            risultato_hrv = int(np.round(rmssd))

        df_acc_min = df_acc.resample('1Min').sum()
        df_hr_min = df_hr.resample('1Min').mean()
        df_hr_std = df_hr.resample('1Min').std()

        df = pd.concat([df_acc_min, df_hr_min, df_hr_std.rename(columns={'hr': 'hr_std'})], axis=1)
        df = df.interpolate(method='time').fillna(method='bfill').fillna(method='ffill')
        if df.empty: return risultati_vuoti

        MOV_THRESHOLD = 0.5
        df['is_asleep'] = df['movement'] <= MOV_THRESHOLD
        df['is_asleep_stable'] = (df['is_asleep'].rolling(window=15, min_periods=1).min() > 0)

        df_sonno = df[df['is_asleep_stable'] == True]
        if df_sonno.empty: return {'hrv': risultato_hrv}, None

        resting_hr = df_sonno['hr'].quantile(0.10)
        risultato_rhr = int(np.round(resting_hr))

        risultato_recupero = None
        if risultato_hrv and risultato_rhr:
            score_hrv = int(np.clip((risultato_hrv - 20) / 80 * 100, 0, 100))
            score_rhr = 100 - int(np.clip((risultato_rhr - 50) / 30 * 100, 0, 100))
            risultato_recupero = int((score_hrv * 0.7) + (score_rhr * 0.3))

        resting_hr_std = df_sonno['hr_std'].quantile(0.25)
        def classifica_fase(riga):
            if not riga['is_asleep_stable']: return 'SVEGLIO'
            if riga['hr'] < (resting_hr * 1.08) and riga['hr_std'] < (resting_hr_std * 1.5): return 'PROFONDO'
            elif riga['hr_std'] > (resting_hr_std * 2.0) and riga['hr'] > resting_hr: return 'REM'
            return 'LEGGERO'

        df['fase'] = df.apply(classifica_fase, axis=1)

        conteggio = df['fase'].value_counts()
        sonno_tot = int(conteggio.get('PROFONDO', 0) + conteggio.get('LEGGERO', 0) + conteggio.get('REM', 0))

        risultati = {
            'hrv': risultato_hrv, 'rhr': risultato_rhr, 'recupero': risultato_recupero,
            'sonno_totale_minuti': sonno_tot,
            'sonno_profondo_minuti': int(conteggio.get('PROFONDO', 0)),
            'sonno_leggero_minuti': int(conteggio.get('LEGGERO', 0)),
            'sonno_rem_minuti': int(conteggio.get('REM', 0)),
            'sonno_sveglio_minuti': int(conteggio.get('SVEGLIO', 0))
        }

        return risultati, df

    except Exception as e:
        print(f"  [SONNO] Errore: {e}")
        return risultati_vuoti



def salva_tutto(cursor, user_id, metriche, df_timeline):
    print(f"  [DB] Salvataggio dati Notturni per user_id {user_id}...")
    oggi = datetime.datetime.now().today()
    oggi_str = oggi.strftime('%Y-%m-%d')

    query_totali = """
        INSERT INTO daily_summary (
            userId, data, hrv_rmssd, recupero_score, rhr_a_riposo,
            sonno_totale_minuti, sonno_profondo_minuti, sonno_leggero_minuti,
            sonno_rem_minuti, sonno_sveglio_minuti
        )
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        ON DUPLICATE KEY UPDATE
            hrv_rmssd = VALUES(hrv_rmssd), recupero_score = VALUES(recupero_score),
            rhr_a_riposo = VALUES(rhr_a_riposo), sonno_totale_minuti = VALUES(sonno_totale_minuti),
            sonno_profondo_minuti = VALUES(sonno_profondo_minuti), sonno_leggero_minuti = VALUES(sonno_leggero_minuti),
            sonno_rem_minuti = VALUES(sonno_rem_minuti), sonno_sveglio_minuti = VALUES(sonno_sveglio_minuti)
    """
    cursor.execute(query_totali, (
        user_id, oggi, metriche.get('hrv'), metriche.get('recupero'), metriche.get('rhr'),
        metriche.get('sonno_totale_minuti'), metriche.get('sonno_profondo_minuti'),
        metriche.get('sonno_leggero_minuti'), metriche.get('sonno_rem_minuti'), metriche.get('sonno_sveglio_minuti')
    ))

    if df_timeline is not None:
        # Mappatura Fasi
        map_sonno = {'SVEGLIO': 0, 'LEGGERO': 1, 'REM': 2, 'PROFONDO': 3}

        df_grafico = df_timeline[df_timeline['is_asleep_stable'] == True]

        valori_sleep = []
        for index, row in df_grafico.iterrows():
            ts_str = index.strftime('%Y-%m-%dT%H:%M:%SZ')
            livello = map_sonno.get(row['fase'], 0)
            valori_sleep.append((livello, user_id, ts_str))

        if valori_sleep:
            start_ts = valori_sleep[0][2]
            end_ts = valori_sleep[-1][2]
            cursor.execute(f"DELETE FROM Sleep WHERE userId = %s AND timestamp BETWEEN %s AND %s", (user_id, start_ts, end_ts))

            cursor.executemany("INSERT INTO Sleep (sleep_level, userId, timestamp) VALUES (%s, %s, %s)", valori_sleep)
            print(f"  [DB] Inserite {len(valori_sleep)} righe nel grafico Sleep.")


def main():
    start_time = time.time()
    print(f"--- AVVIO CALCOLO NOTTURNO ({datetime.datetime.now()}) ---")

    now = datetime.datetime.now()
    ora_fine_ms = int(now.timestamp() * 1000)
    ora_inizio_ms = ora_fine_ms - (ANALISI_SONNO_ORE * 60 * 60 * 1000)

    conn = None
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor()
        cursor.execute("SELECT id FROM User")
        lista_utenti = cursor.fetchall()

        for (user_id,) in lista_utenti:
            try:
                print(f"[USER: {user_id}] Calcolo...")
                tabelle = ['PPG', 'Accelerometro']
                dati = leggi_dati_grezzi(cursor, user_id, ora_inizio_ms, ora_fine_ms, tabelle)

                if not dati.get('timestampsPPG') or not dati.get('acc_x'): continue

                risultati, df_timeline = analizza_sonno_e_metriche(dati)

                salva_tutto(cursor, user_id, risultati, df_timeline)
                conn.commit()
                print(f"[USER: {user_id}] Salvato.")
            except Exception as e:
                print(f"ERROR USER {user_id}: {e}")
                conn.rollback()
    except Exception as e:
        print(f"ERROR DB: {e}")
    finally:
        if conn: conn.close()
    print(f"--- TERMINATO ({time.time() - start_time:.2f}s) ---")

if __name__ == "__main__":
    main()