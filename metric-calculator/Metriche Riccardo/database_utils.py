import mysql.connector
import datetime

# Assicurati che il nome del DB sia corretto come da tuo screenshot
DB_CONFIG = {
    'user': 'root',
    'password': 'Simone04',
    'host': 'localhost',
    'database': 'LoopyDB_local' 
}

def leggi_dati_grezzi(cursor, user_id: int, start_time_ms: int, end_time_ms: int, tabelle: list) -> dict:
    dati_grezzi = {}

    # Configurazione: Mappiamo i nomi delle tabelle alle colonne e alle chiavi del dizionario
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

    # 1. CONVERSIONE INPUT: Da Millisecondi (Python) a Stringa ISO (Database)
    # Il DB usa il formato '2025-12-02T10:00:00Z', quindi dobbiamo cercare usando stringhe
    start_dt = datetime.datetime.fromtimestamp(start_time_ms / 1000.0, datetime.timezone.utc)
    end_dt = datetime.datetime.fromtimestamp(end_time_ms / 1000.0, datetime.timezone.utc)
    
    start_str = start_dt.strftime('%Y-%m-%dT%H:%M:%SZ')
    end_str = end_dt.strftime('%Y-%m-%dT%H:%M:%SZ')

    for tabella_nome in tabelle:
        if tabella_nome not in query_config:
            print(f"  [DB_Util] Attenzione: Tabella '{tabella_nome}' non configurata.")
            continue

        config = query_config[tabella_nome]
        colonne_str = ", ".join(config['cols'])

        # Query usando le stringhe per il confronto temporale
        query = f"""
            SELECT {colonne_str}
            FROM {tabella_nome}
            WHERE userId = %s
              AND timestamp BETWEEN %s AND %s
            ORDER BY timestamp ASC
        """

        cursor.execute(query, (user_id, start_str, end_str))
        rows = cursor.fetchall()
        
        # Se non ci sono dati, riempiamo con stringhe vuote per evitare errori dopo
        if not rows:
            for k in config['keys']:
                dati_grezzi[k] = ""
            continue

        # 2. CONVERSIONE OUTPUT: Da Stringa ISO (Database) a Millisecondi (Python)
        # I calcolatori si aspettano numeri interi separati da virgola.
        
        col_data = {k: [] for k in config['keys']} # Dizionario temporaneo liste
        
        for row in rows:
            for i, key_name in enumerate(config['keys']):
                val = row[i]
                
                # Se è la colonna timestamp (che nel DB è stringa '2025-12...'), la convertiamo in numero
                if 'timestamp' in key_name.lower():
                    try:
                        # Parsiamo la data ISO
                        if isinstance(val, str):
                            dt_obj = datetime.datetime.strptime(val, '%Y-%m-%dT%H:%M:%SZ')
                            # Convertiamo in millisecondi
                            val = int(dt_obj.replace(tzinfo=datetime.timezone.utc).timestamp() * 1000)
                    except Exception as e:
                        # Fallback se il formato è strano, proviamo a passare il valore così com'è o 0
                        print(f"Errore conversione data {val}: {e}")
                        val = 0
                
                col_data[key_name].append(str(val))

        # Uniamo le liste in stringhe separate da virgola
        for key_name in config['keys']:
            dati_grezzi[key_name] = ", ".join(col_data[key_name])

    return dati_grezzi