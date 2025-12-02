import mysql.connector
import time
import datetime
import random
from db_utils import DB_CONFIG

# --- CONFIGURAZIONE ---
USER_ID = 1
OGGI = datetime.date.today()

# Orari chiave per la simulazione
ORA_INIZIO_SONNO = datetime.datetime.combine(OGGI, datetime.time(2, 0)) # 02:00 di notte
ORA_FINE_SONNO = datetime.datetime.combine(OGGI, datetime.time(8, 0))   # 08:00 di mattina
ORA_SPORT = datetime.datetime.combine(OGGI, datetime.time(18, 0))       # 18:00
ORA_STRESS = datetime.datetime.combine(OGGI, datetime.time(11, 0))      # 11:00

# Connessione DB
conn = mysql.connector.connect(**DB_CONFIG)
cursor = conn.cursor()

def insert_batch(table, cols, data):
    if not data: return
    placeholders = ", ".join(["%s"] * len(cols))
    col_names = ", ".join(cols)
    sql = f"INSERT INTO {table} ({col_names}) VALUES ({placeholders})"
    try:
        cursor.executemany(sql, data)
        conn.commit()
        print(f"  -> Inserite {len(data)} righe in {table}")
    except Exception as e:
        print(f"  ❌ Errore insert {table}: {e}")

def pulisci_db():
    print("🧹 Pulizia dati vecchi per User 1...")
    tables = ["Accelerometro", "PPG", "Elettrodi", "Activity", "Stress", "Sleep", "daily_summary"]
    for t in tables:
        try:
            cursor.execute(f"DELETE FROM {t} WHERE userId = {USER_ID}")
            # Se daily_summary non ha userId ma id, gestiscilo, ma nel tuo schema ha userId.
        except:
            pass
    conn.commit()

# --- GENERAZIONE DATI ---

def genera_dati():
    pulisci_db()
    print("🚀 Inizio generazione dati realistici (1 riga al minuto)...")

    data_acc, data_ppg, data_eda = [], [], []

    # 1. NOTTE (02:00 - 08:00) -> 6 ORE
    # Simuliamo cicli: Profondo, Leggero, REM
    print("Generating Sleep (02:00 - 08:00)...")
    current_time = ORA_INIZIO_SONNO
    while current_time < ORA_FINE_SONNO:
        ts_str = current_time.strftime('%Y-%m-%dT%H:%M:%SZ')
        
        # Logica Fasi Sonno (Ciclo di 90 min circa)
        minuto_ciclo = (current_time.minute + (current_time.hour * 60)) % 90
        
        # Default: Sonno Leggero
        hr = random.randint(58, 65) 
        acc_x = 0.02 # Quasi fermo
        
        if 10 < minuto_ciclo < 40: # FASE PROFONDA (30 min)
            hr = random.randint(50, 55) # Battito molto basso
            acc_x = 0.00 # Immobile
        elif 70 < minuto_ciclo < 90: # FASE REM (20 min)
            hr = random.randint(60, 75) # Battito irregolare/alto
            acc_x = 0.01 
            
        # Insert
        data_acc.append((acc_x, 0.01, 0.98, ts_str, USER_ID))
        data_ppg.append((hr, 98, ts_str, USER_ID))
        data_eda.append((0.2, ts_str, USER_ID)) # Sudorazione nulla
        
        current_time += datetime.timedelta(minutes=1)

    # 2. SPORT (18:00 - 18:45) -> 45 MINUTI
    print("Generating Sport (18:00 - 18:45)...")
    current_time = ORA_SPORT
    fine_sport = ORA_SPORT + datetime.timedelta(minutes=45)
    while current_time < fine_sport:
        ts_str = current_time.strftime('%Y-%m-%dT%H:%M:%SZ')
        
        hr = random.randint(130, 165) # Battito alto (70-90% HRR)
        acc_x = random.uniform(1.5, 2.5) # Movimento intenso (>1.5g)
        eda = random.uniform(3.0, 8.0) # Sudorazione alta
        
        data_acc.append((acc_x, 0.5, 0.5, ts_str, USER_ID))
        data_ppg.append((hr, 97, ts_str, USER_ID))
        data_eda.append((eda, ts_str, USER_ID))
        
        current_time += datetime.timedelta(minutes=1)

    # 3. STRESS (11:00 - 11:30) -> 30 MINUTI
    print("Generating Stress (11:00 - 11:30)...")
    current_time = ORA_STRESS
    fine_stress = ORA_STRESS + datetime.timedelta(minutes=30)
    while current_time < fine_stress:
        ts_str = current_time.strftime('%Y-%m-%dT%H:%M:%SZ')
        
        hr = random.randint(85, 100) # Battito altino per essere seduti
        acc_x = 0.02 # Seduto (fermo)
        eda = random.uniform(2.0, 4.0) # Sudorazione presente ("sudori freddi")
        
        data_acc.append((acc_x, 0.01, 0.98, ts_str, USER_ID))
        data_ppg.append((hr, 98, ts_str, USER_ID))
        data_eda.append((eda, ts_str, USER_ID))
        
        current_time += datetime.timedelta(minutes=1)

    # INSERT FINALE
    insert_batch("Accelerometro", ["acc_x", "acc_y", "acc_z", "timestamp", "userId"], data_acc)
    insert_batch("PPG", ["battito", "ossigenazione", "timestamp", "userId"], data_ppg)
    insert_batch("Elettrodi", ["sudorazione", "timestamp", "userId"], data_eda)
    print("✅ DATI INIETTATI CORRETTAMENTE!")

if __name__ == "__main__":
    try:
        genera_dati()
    finally:
        conn.close()