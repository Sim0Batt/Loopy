import sys
import mysql.connector
import numpy as np
import datetime

DB_CONFIG = {
    'user': 'root',
    'password': 'Simone04',
    'host': 'localhost',
    'database': 'LoopyDB'
}

ANALYSIS_WINDOW_HOURS = 24

#TODO: implementare gli algoritmi

def calcola_hrv(timestamps_ppg_str: str) -> int | None:
    print(" Calcolo HRV...")
    return 42

def calcola_passi(movements_str: str) -> int | None:
    print(" Calcolo Passi...") #TODO: qui alla fine mettiamo livello attivita... quindi da cambiare tutto :(
    return 5800

def calcola_stress(heartrates_str: str, sweatings_str: str) -> int | None:
    print(" Calcolo Stress...")
    return 35

def calcola_recupero(hrv: int, stress: int) -> int | None:
    print(" Calcolo Recupero...")
    return 80

def calcola_vo2max(hr: str) -> int | None:
    print(" Calcolo VO2Max...")
    return 45
# TODO: da scrivere anche gli altri come sonno ecc... ma con calma



def leggi_dati_grezzi(cursor, user_id, start_time_ms: int, end_time_ms: int) -> dict:

    dati_grezzi = {}

    query_config = {
        'PPG': {
            'cols': ['battito', 'ossigenazione', 'timestamp'],
            'keys': ['heartRates', 'oxygens', 'timestampsPPG']
        },
        'Elettrodi': {
            'cols': ['sudorazione', 'timestamp'],
            'keys': ['sweatings', 'timestampsElectrodes']
        },
        'Termometro': {
            'cols': ['temperatura', 'timestamp'],
            'keys': ['temperatures', 'timestampsTermometer']
        },
        'Accelerometro': {
            'cols': ['movimento', 'timestamp'],
            'keys': ['movements', 'timestampsAccelerometer']
        }
    }

    for tabella, config in query_config.items():
        colonne_str = ", ".join(config['cols'])

        query = f"""
            SELECT {colonne_str} 
            FROM {tabella} 
            WHERE userId = %s 
              AND CAST(timestamp AS UNSIGNED) BETWEEN %s AND %s
            ORDER BY id ASC
        """

        cursor.execute(query, (user_id, start_time_ms, end_time_ms))
        rows = cursor.fetchall()

        for i, key_name in enumerate(config['keys']):
            dati_grezzi[key_name] = ", ".join([str(row[i]) for row in rows])

    return dati_grezzi


def salva_riepilogo(cursor, user_id, metriche: dict):
    oggi = datetime.date.today()

    query = """
            INSERT INTO daily_summary (
                user_id, data,
                hrv_rmssd, stress_score, passi_totali, recupero_score, vo2max
            )
            VALUES (%s, %s, %s, %s, %s, %s, %s)
                ON DUPLICATE KEY UPDATE
                                     hrv_rmssd = VALUES(hrv_rmssd),
                                     stress_score = VALUES(stress_score),
                                     passi_totali = VALUES(passi_totali),
                                     recupero_score = VALUES(recupero_score),
                                     vo2max = VALUES(vo2max) \
            """
    dati_da_salvare = (
        user_id, oggi,
        metriche.get('hrv'), metriche.get('stress'), metriche.get('passi'),
        metriche.get('recupero'), metriche.get('vo2max')
    )
    cursor.execute(query, dati_da_salvare)


def main():
    """
    qui prendo i dati delle ultime 24 ore, trovo tutti gli utenti e per ogni utente prendo i dati grezzi,
    calcolo le robe con gli algoritmi e poi salvo """

    print(f" AVVIO CALCOLO METRICHE GIORNALIERE ({datetime.datetime.now()}) ---")

    # prendo l'ora di adesso (ms)
    ora_fine_ms = int(datetime.datetime.now().timestamp() * 1000)
    # prendo l'ora di 24 ore fa ( sempre ms)
    ora_inizio_ms = ora_fine_ms - (ANALYSIS_WINDOW_HOURS * 60 * 60 * 1000)
    #TODO: controllare se si vuole fare ogni 24 ore o meno

    print(f"Finestra di analisi (ms): {ora_inizio_ms} -> {ora_fine_ms}")

    conn = None
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor()

        # qui prendo utenti dal db
        cursor.execute("SELECT id FROM User")
        lista_utenti = cursor.fetchall()
        print(f"Trovati {len(lista_utenti)} utenti. Inizio ciclo.")

        # ciclo per calcolare robe per ogni utente skr prpr eskere sto impazzendo sono fuso
        for (user_id,) in lista_utenti:
            try:
                print(f"[USER: {user_id}] Inizio calcolo")

                dati_grezzi = leggi_dati_grezzi(cursor, user_id, ora_inizio_ms, ora_fine_ms)

                if not dati_grezzi.get('timestampsPPG'):
                    print(f"[USER: {user_id}] Nessun dato grezzo trovato, saltato.")
                    continue

                metriche_calcolate = {}
                metriche_calcolate['hrv'] = calcola_hrv(dati_grezzi.get('timestampsPPG', ''))
                metriche_calcolate['passi'] = calcola_passi(dati_grezzi.get('movements', ''))
                metriche_calcolate['stress'] = calcola_stress(dati_grezzi.get('heartRates', ''), dati_grezzi.get('sweatings', ''))
                metriche_calcolate['recupero'] = calcola_recupero(metriche_calcolate['hrv'], metriche_calcolate['stress'])
                metriche_calcolate['vo2max'] = calcola_vo2max(dati_grezzi.get('heartRates', ''))

                print(f"[USER: {user_id}] Metriche calcolate (placeholder): {metriche_calcolate}")

                salva_riepilogo(cursor, user_id, metriche_calcolate)

                conn.commit()
                print(f"[USER: {user_id}] Riepilogo salvato con successo.")

            except Exception as e:
                print(f"ERRORE CRITICO per user_id {user_id}: {e}")
                conn.rollback()

    except mysql.connector.Error as err:
        print(f"ERRORE DATABASE: {err}")
    finally:
        if conn and conn.is_connected():
            cursor.close()
            conn.close()
            print(f"Connessione DB chiusa. Calcoli terminati.")

if __name__ == "__main__":
    main()