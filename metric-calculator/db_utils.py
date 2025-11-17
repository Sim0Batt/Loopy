import mysql.connector
import datetime

DB_CONFIG = {
    'user': 'root',
    'password': 'Simone04',
    'host': 'localhost',
    'database': 'LoopyDB'
}


def leggi_dati_grezzi(cursor, user_id: int, start_time_ms: int, end_time_ms: int, tabelle: list) -> dict:

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
              'cols': ['acc_x', 'acc_y', 'acc_z', 'timestamp'],
              'keys': ['acc_x', 'acc_y', 'acc_z', 'timestampsAccelerometer']
        }
    }

    for tabella_nome in tabelle:
        if tabella_nome not in query_config:
            print(f"  [DB_Util] Attenzione: Tabella '{tabella_nome}' non configurata.")
            continue

        config = query_config[tabella_nome]
        colonne_str = ", ".join(config['cols'])

        query = f"""
            SELECT {colonne_str}
            FROM {tabella_nome}
            WHERE userId = %s
              AND CAST(timestamp AS UNSIGNED) BETWEEN %s AND %s
            ORDER BY id ASC
        """

        cursor.execute(query, (user_id, start_time_ms, end_time_ms))
        rows = cursor.fetchall()

        for i, key_name in enumerate(config['keys']):
            dati_grezzi[key_name] = ", ".join([str(row[i]) for row in rows])

    return dati_grezzi