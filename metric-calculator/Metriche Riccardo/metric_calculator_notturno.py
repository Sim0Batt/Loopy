import mysql.connector
import numpy as np
import pandas as pd
import datetime
import time

from db_utils import DB_CONFIG, leggi_dati_grezzi

# Quante ore indietro guardare per stimare il sonno
ANALISI_SONNO_ORE = 48


def analizza_sonno_e_metriche(dati_grezzi: dict):
    """
    Analisi Sonno Ibrida:
    - Usa RMSSD scientifico (da intervalli RR simulati)
    - Usa filtro di stabilità del sonno (is_asleep_stable)
    - Usa classificazione fasi relativa al RHR notturno

    Ritorna SEMPRE: (metriche: dict, df_timeline | None)
    """
    print("  [ALGO] Avvio Analisi Sonno (Hybrid)...")
    
    res_vuoti = {
        'hrv': 0,
        'rhr': 60,
        'recupero': 0,
        'sonno_totale_minuti': 0,
        'sonno_profondo_minuti': 0,
        'sonno_leggero_minuti': 0,
        'sonno_rem_minuti': 0,
        'sonno_sveglio_minuti': 0
    }

    try:
        # --- 1. PARSING ACC ---
        ts_acc_str = dati_grezzi.get('timestampsAccelerometer', '')
        acc_x_str = dati_grezzi.get('acc_x', '')
        acc_y_str = dati_grezzi.get('acc_y', '')
        acc_z_str = dati_grezzi.get('acc_z', '')

        if not ts_acc_str or not acc_x_str:
            return res_vuoti, None

        ts_acc = [int(t.strip()) for t in ts_acc_str.split(',') if t.strip()]
        acc_x = np.array([float(v.strip()) for v in acc_x_str.split(',') if v.strip()])
        acc_y = np.array([float(v.strip()) for v in acc_y_str.split(',') if v.strip()])
        acc_z = np.array([float(v.strip()) for v in acc_z_str.split(',') if v.strip()])

        min_len = min(len(acc_x), len(acc_y), len(acc_z), len(ts_acc))
        if min_len == 0:
            return res_vuoti, None

        acc_x, acc_y, acc_z, ts_acc = acc_x[:min_len], acc_y[:min_len], acc_z[:min_len], ts_acc[:min_len]

        magnitudo_grezza = np.sqrt(acc_x**2 + acc_y**2 + acc_z**2)
        # Rimuovi componente statica (gravità media)
        magnitudo_filtrata = np.abs(magnitudo_grezza - np.mean(magnitudo_grezza))

        df_acc = pd.DataFrame(
            {'movement': magnitudo_filtrata},
            index=pd.to_datetime(ts_acc, unit='ms', errors='coerce')
        ).dropna()

        # --- 1b. PARSING PPG ---
        ts_hr_str = dati_grezzi.get('timestampsPPG', '')
        hr_vals_str = dati_grezzi.get('heartRates', '')
        if not ts_hr_str or not hr_vals_str:
            return res_vuoti, None

        ts_hr = [int(t.strip()) for t in ts_hr_str.split(',') if t.strip()]
        hr_vals = [int(h.strip()) for h in hr_vals_str.split(',') if h.strip()]

        min_len_hr = min(len(ts_hr), len(hr_vals))
        if min_len_hr == 0:
            return res_vuoti, None

        ts_hr = ts_hr[:min_len_hr]
        hr_vals = hr_vals[:min_len_hr]

        df_hr = pd.DataFrame(
            {'hr': hr_vals},
            index=pd.to_datetime(ts_hr, unit='ms', errors='coerce')
        ).dropna()

        # --- 2. HRV REALE (RMSSD) SU INTERVALLI ---
        timestamps_hrv = np.array(ts_hr, dtype=np.int64)
        intervals = np.diff(timestamps_hrv)
        # Filtra intervalli fuori range battito umano
        intervals = intervals[(intervals > 300) & (intervals < 2000)]

        risultato_hrv_rmssd = 0
        if len(intervals) >= 2:
            rmssd = np.sqrt(np.mean(np.square(np.diff(intervals))))
            risultato_hrv_rmssd = int(np.round(rmssd))

        # --- 3. PREPARAZIONE DATI AL MINUTO ---
        df_hr_std = df_hr.resample('1Min').std().rename(columns={'hr': 'hrv_proxy'})
        
        df = pd.concat([
            df_acc.resample('1Min').mean(),
            df_hr.resample('1Min').mean(),
            df_hr_std
        ], axis=1).interpolate().bfill().ffill()

        if df.empty:
            tmp = dict(res_vuoti)
            tmp['hrv'] = risultato_hrv_rmssd
            return tmp, None

        # --- 4. FILTRO "SONO A LETTO?" ---
        MOV_THRESHOLD = 0.05-0.03
        df['is_low_movement'] = df['movement'] <= MOV_THRESHOLD
        df['is_asleep_stable'] = (df['is_low_movement'].rolling(window=15, min_periods=1).min() > 0)

        df_sonno_reale = df[df['is_asleep_stable'] == True]
        if df_sonno_reale.empty:
            tmp = dict(res_vuoti)
            tmp['hrv'] = risultato_hrv_rmssd
            # ritorno anche df intero per eventuale debug
            return tmp, df

        # --- 5. RHR NOTTURNO (5° percentile sul sonno stabile) ---
        rhr_notturno = np.percentile(df_sonno_reale['hr'], 5)
        print(f"  [SONNO] RHR (Basale Notturno): {rhr_notturno:.1f} bpm | RMSSD: {risultato_hrv_rmssd} ms")

        # --- 6. CLASSIFICAZIONE FASI ---
        def classifica_fase(row):
            # Se non sei in sonno stabile → sveglio
            if not row['is_asleep_stable']:
                return 'SVEGLIO'
            # Se ti muovi troppo anche in sonno stabile → micro-risveglio
            if row['movement'] > 0.2:
                return 'SVEGLIO'
            
            # Profondo: HR vicina al RHR e bassa variabilità
            if (row['hr'] <= (rhr_notturno * 1.05)) and (row['hrv_proxy'] < 2.5):
                return 'PROFONDO'
            # REM: HR sopra RHR e variabilità alta
            elif (row['hr'] > (rhr_notturno * 1.10)) and (row['hrv_proxy'] >= 2.5):
                return 'REM'
            # Default: Leggero
            else:
                return 'LEGGERO'

        df['fase'] = df.apply(classifica_fase, axis=1)

        # --- 7. OUTPUT METRICHE ---
        counts = df['fase'].value_counts()

        minuti_prof = counts.get('PROFONDO', 0)
        minuti_rem = counts.get('REM', 0)
        minuti_leggero = counts.get('LEGGERO', 0)
        minuti_sveglio = counts.get('SVEGLIO', 0)

        minuti_buoni = minuti_prof + minuti_rem
        sonno_totale = minuti_prof + minuti_rem + minuti_leggero

        score_recupero = int((minuti_buoni / sonno_totale) * 100) if sonno_totale > 0 else 0

        risultati = {
            'hrv': risultato_hrv_rmssd,
            'rhr': int(rhr_notturno),
            'recupero': score_recupero,
            'sonno_totale_minuti': int(sonno_totale),
            'sonno_profondo_minuti': int(minuti_prof),
            'sonno_leggero_minuti': int(minuti_leggero),
            'sonno_rem_minuti': int(minuti_rem),
            'sonno_sveglio_minuti': int(minuti_sveglio)
        }

        return risultati, df

    except Exception as e:
        print(f"  [ERRORE SONNO] {e}")
        return res_vuoti, None


def salva_tutto(cursor, user_id, metriche, df_timeline):
    print(f"  [DB] Salvataggio dati Notturni per user_id {user_id}...")
    oggi = datetime.date.today()

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
        # Mappatura Fasi → valore per grafico
        map_sonno = {'SVEGLIO': 0, 'LEGGERO': 33, 'REM': 66, 'PROFONDO': 100}

        df_grafico = df_timeline[df_timeline['is_asleep_stable'] == True]

        valori_sleep = []
        for index, row in df_grafico.iterrows():
            ts_str = index.strftime('%Y-%m-%dT%H:%M:%SZ')
            livello = map_sonno.get(row['fase'], 0)
            valori_sleep.append((livello, user_id, ts_str))

        if valori_sleep:
            start_ts = valori_sleep[0][2]
            end_ts = valori_sleep[-1][2]
            cursor.execute(
                "DELETE FROM Sleep WHERE userId = %s AND timestamp BETWEEN %s AND %s",
                (user_id, start_ts, end_ts)
            )

            cursor.executemany(
                "INSERT INTO Sleep (sleep_level, userId, timestamp) VALUES (%s, %s, %s)",
                valori_sleep
            )
            print(f"  [DB] Inserite {len(valori_sleep)} righe nel grafico Sleep.")


def main():
    start_time = time.time()
    print(f"--- AVVIO CALCOLO NOTTURNO ({datetime.datetime.now()}) ---")

    # Usiamo UTC per coerenza con il resto del sistema
    now = datetime.datetime.now(datetime.timezone.utc)
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

                if not dati.get('timestampsPPG') or not dati.get('acc_x'):
                    print(f"[USER: {user_id}] Dati sonno insufficienti. Salto.")
                    continue

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
        if conn:
            conn.close()

    print(f"--- TERMINATO ({time.time() - start_time:.2f}s) ---")


if __name__ == "__main__":
    main()
