import mysql.connector
import datetime
import random
from db_utils import DB_CONFIG

# --- CONFIGURAZIONE ---
USER_ID = 1

# Usiamo OGGI in UTC per coerenza con il resto del sistema
OGGI_UTC = datetime.datetime.now(datetime.timezone.utc).date()

# Orari chiave per la simulazione (in UTC)
ORA_INIZIO_GIORNO = datetime.datetime.combine(
    OGGI_UTC, datetime.time(0, 0, tzinfo=datetime.timezone.utc)
)
ORA_FINE_GIORNO = ORA_INIZIO_GIORNO + datetime.timedelta(days=1)

ORA_INIZIO_SONNO = datetime.datetime.combine(
    OGGI_UTC, datetime.time(2, 0, tzinfo=datetime.timezone.utc)
)
ORA_FINE_SONNO = datetime.datetime.combine(
    OGGI_UTC, datetime.time(8, 0, tzinfo=datetime.timezone.utc)
)
ORA_STRESS_INIZIO = datetime.datetime.combine(
    OGGI_UTC, datetime.time(11, 0, tzinfo=datetime.timezone.utc)
)
ORA_STRESS_FINE = ORA_STRESS_INIZIO + datetime.timedelta(minutes=30)
ORA_SPORT_INIZIO = datetime.datetime.combine(
    OGGI_UTC, datetime.time(18, 0, tzinfo=datetime.timezone.utc)
)
ORA_SPORT_FINE = ORA_SPORT_INIZIO + datetime.timedelta(minutes=45)


# Connessione DB
conn = mysql.connector.connect(**DB_CONFIG)
cursor = conn.cursor()


def insert_batch(table, cols, data):
    """Insert bulk rows into a table."""
    if not data:
        return
    placeholders = ", ".join(["%s"] * len(cols))
    col_names = ", ".join(cols)
    sql = f"INSERT INTO {table} ({col_names}) VALUES ({placeholders})"
    try:
        cursor.executemany(sql, data)
        conn.commit()
        print(f"  -> Inserite {len(data)} righe in {table}")
    except Exception as e:
        print(f"  ❌ Errore insert {table}: {e}")
        conn.rollback()


def pulisci_db():
    print("🧹 Pulizia dati vecchi per User 1...")
    tables = ["Accelerometro", "PPG", "Elettrodi", "Activity", "Stress", "Sleep", "daily_summary"]
    for t in tables:
        try:
            cursor.execute(f"DELETE FROM {t} WHERE userId = %s", (USER_ID,))
        except Exception as e:
            print(f"  ⚠️ Errore cancellando {t}: {e}")
    conn.commit()


def genera_dati():
    pulisci_db()
    print("🚀 Inizio generazione dati realistici (1 riga al secondo per 24h)...")

    data_acc = []
    data_ppg = []
    data_eda = []

    current_time = ORA_INIZIO_GIORNO

    while current_time < ORA_FINE_GIORNO:
        ts_str = current_time.strftime('%Y-%m-%dT%H:%M:%SZ')

        # -------------------------
        # SCELTA DELLO SCENARIO
        # -------------------------
        if ORA_INIZIO_SONNO <= current_time < ORA_FINE_SONNO:
            # NOTTE: 02:00–08:00
            # Cicli di ~90min per fasi sonno
            minuto_ciclo = int((current_time - ORA_INIZIO_SONNO).total_seconds() // 60) % 90

            # Default: sonno leggero
            hr = random.randint(58, 65)
            acc_x = random.uniform(0.0, 0.02)
            acc_y = random.uniform(-0.02, 0.02)
            acc_z = random.uniform(0.98, 1.02)
            eda = 0.2

            if 10 < minuto_ciclo < 40:
                # FASE PROFONDA (30 min)
                hr = random.randint(50, 55)
                acc_x = random.uniform(-0.005, 0.005)
                acc_y = random.uniform(-0.005, 0.005)
                acc_z = random.uniform(0.99, 1.01)
            elif 70 < minuto_ciclo < 90:
                # FASE REM (20 min)
                hr = random.randint(60, 75)
                acc_x = random.uniform(0.0, 0.02)
                acc_y = random.uniform(-0.02, 0.02)
                acc_z = random.uniform(0.98, 1.02)
                eda = 0.25

        elif ORA_STRESS_INIZIO <= current_time < ORA_STRESS_FINE:
            # STRESS DA SEDUTO: 11:00–11:30
            hr = random.randint(85, 100)          # battito altino da seduto
            acc_x = random.uniform(0.0, 0.02)     # quasi fermo
            acc_y = random.uniform(-0.02, 0.02)
            acc_z = random.uniform(0.98, 1.02)
            eda = random.uniform(2.0, 4.0)        # sudorazione presente

        elif ORA_SPORT_INIZIO <= current_time < ORA_SPORT_FINE:
            # SPORT INTENSO: 18:00–18:45
            hr = random.randint(130, 165)         # 70–90% HRR
            # movimento intenso: magnitudo ben sopra 1g
            acc_x = random.uniform(1.5, 2.5)
            acc_y = random.uniform(-1.0, 1.0)
            acc_z = random.uniform(-0.5, 1.5)
            eda = random.uniform(3.0, 8.0)        # sudorazione alta

        else:
            # ALTRI PERIODI: distinzione grossolana tra:
            # - pre/post sonno / sera tardi → relax
            # - resto della giornata → attività leggera/moderata
            t = current_time.time()

            if t < datetime.time(2, 0) or t >= datetime.time(23, 0):
                # PRE-SONNO / TARDA SERA: sedentario rilassato
                hr = random.randint(60, 75)
                acc_x = random.uniform(0.0, 0.03)
                acc_y = random.uniform(-0.03, 0.03)
                acc_z = random.uniform(0.97, 1.03)
                eda = random.uniform(0.2, 0.6)
            elif datetime.time(8, 0) <= t < datetime.time(11, 0):
                # MATTINA: lavoro leggero / colazione / spostamenti
                hr = random.randint(70, 90)
                acc_x = random.uniform(0.02, 0.15)
                acc_y = random.uniform(-0.1, 0.1)
                acc_z = random.uniform(0.9, 1.1)
                eda = random.uniform(0.3, 1.0)
            elif datetime.time(11, 30) <= t < datetime.time(18, 0):
                # GIORNO PIENO: camminata, lavoro in piedi, ecc.
                hr = random.randint(75, 105)
                acc_x = random.uniform(0.05, 0.3)
                acc_y = random.uniform(-0.2, 0.2)
                acc_z = random.uniform(0.85, 1.15)
                eda = random.uniform(0.4, 1.5)
            else:
                # SERA (dopo sport o prima di cena): attività leggera / divano
                hr = random.randint(65, 85)
                acc_x = random.uniform(0.01, 0.08)
                acc_y = random.uniform(-0.05, 0.05)
                acc_z = random.uniform(0.9, 1.1)
                eda = random.uniform(0.3, 0.9)

        # -------------------------
        # COSTRUZIONE RIGHE
        # -------------------------
        spo2 = random.randint(96, 99)  # ossigenazione realistica

        data_acc.append((acc_x, acc_y, acc_z, ts_str, USER_ID))
        data_ppg.append((hr, spo2, ts_str, USER_ID))
        data_eda.append((eda, ts_str, USER_ID))

        # passo di 1 secondo
        current_time += datetime.timedelta(seconds=1)

    # INSERT FINALE (bulk)
    insert_batch("Accelerometro", ["acc_x", "acc_y", "acc_z", "timestamp", "userId"], data_acc)
    insert_batch("PPG", ["battito", "ossigenazione", "timestamp", "userId"], data_ppg)
    insert_batch("Elettrodi", ["sudorazione", "timestamp", "userId"], data_eda)

    print("✅ DATI INIETTATI CORRETTAMENTE (24h a 1 Hz)!")


if __name__ == "__main__":
    try:
        genera_dati()
    finally:
        conn.close()
