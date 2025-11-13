import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.dates import DateFormatter
import matplotlib.dates as mdates
import numpy as np
import mysql.connector 
from sqlalchemy import create_engine
import matplotlib.patches as mpatches # Per la legenda
import sys


# === CONFIGURAZIONE CONNESSIONE DATABASE ===
DB_USER = 'root'
DB_PASS = 'Simone04'
DB_HOST = '127.0.0.1' 
DB_PORT = '3306'
DB_NAME = 'LoopyDB' 

try:
    db_connection_str = f'mysql+mysqlconnector://{DB_USER}:{DB_PASS}@{DB_HOST}:{DB_PORT}/{DB_NAME}'
    db_engine = create_engine(db_connection_str)
    print("Connessione al database MySQL... Riuscita.")
except Exception as e:
    print(f"❌ ERRORE: Connessione al database fallita. Controlla che MySQL sia attivo. Dettagli: {e}")
    exit()
# ==========================================

BACKGROUND_COLOR = '#F3F5F6'
TEXT_COLOR = 'black'

def get_zone_color(value, threshold_high, threshold_mod, threshold_low):
    if value >= threshold_high: return '#c0392b'  # Rosso
    elif value >= threshold_mod: return '#f39c12'  # Arancione
    elif value >= threshold_low: return '#27ae60'  # Verde
    else: return '#3498db'  # Blu

# =========================================================================
# 1. GRAFICO STRESS
# =========================================================================
def genera_grafico_stress(user_id):
    print("Inizio generazione Grafico Stress... (lettura dati ACCURATI da DB)")
    
    query_stress = f"""
    SELECT
      p.timestamp AS timestamp,
      ROUND(AVG( (p.battito - 60) + (e.sudorazione * 50) ), 2) AS stress_avg
    FROM PPG p
    JOIN Elettrodi e ON p.userId = e.userId AND p.timestamp = e.timestamp
    JOIN Accelerometro a ON p.userId = a.userId AND p.timestamp = a.timestamp
    WHERE p.userId = {user_id}
      AND DATE(p.timestamp) = CURDATE()
      AND a.movimento = 0 
    GROUP BY p.timestamp 
    ORDER BY p.timestamp;
    """
    df = pd.read_sql_query(query_stress, db_engine)
    
    if df.empty:
        print("❌ Dati Stress (a riposo) non trovati per oggi. Grafico non generato.")
        return

    df.columns = ['timestamp', 'stress_avg'] 
    df['stress_positivo'] = df['stress_avg'] 
    df['timestamp'] = pd.to_datetime(df['timestamp'])
    
    df_agg = df.set_index('timestamp').resample('30min').mean().reset_index()
    df_agg['stress_liscio'] = df_agg['stress_positivo'].rolling(window=1, min_periods=1).mean()
    df_agg['color'] = df_agg['stress_liscio'].apply(lambda x: get_zone_color(x, 40.0, 20.0, 10.0))

    # Plotting
    plt.style.use('default')
    fig, ax = plt.subplots(figsize=(16, 7), facecolor=BACKGROUND_COLOR)
    ax.set_facecolor(BACKGROUND_COLOR)

    ax.bar(df_agg['timestamp'], df_agg['stress_liscio'],
           width=pd.Timedelta(minutes=30),
           color=df_agg['color'],
           edgecolor=TEXT_COLOR,
           linewidth=0.8,
           label='Daily Stress Level',
           align='edge')

    # Allineamento Asse X e Pulizia
    if not df_agg.empty:
        start_day = df_agg['timestamp'].min().normalize()
        end_day = df_agg['timestamp'].max().normalize() + pd.Timedelta(days=1)
        ax.set_xlim(start_day, end_day)
        ax.xaxis.set_major_locator(mdates.HourLocator(interval=3))
        ax.xaxis.set_major_formatter(DateFormatter('%H:%M'))

    ax.set_title('Daily Stress', color=TEXT_COLOR, fontsize=20, pad=20)
    ax.set_yticklabels([]); ax.tick_params(axis='y', length=0)
    ax.tick_params(axis='x', colors=TEXT_COLOR)
    ax.grid(axis='y', linestyle='--', alpha=0.5, color='gray')

    # Legenda
    zona_alta = mpatches.Patch(color='#c0392b', label='Stress Alto')
    zona_media = mpatches.Patch(color='#f39c12', label='Stress Moderato')
    zona_bassa = mpatches.Patch(color='#27ae60', label='Stress Basso')
    zona_recupero = mpatches.Patch(color='#3498db', label='Recupero')
    ax.legend(handles=[zona_alta, zona_media, zona_bassa, zona_recupero], 
              loc='upper left', frameon=False, ncol=2, fontsize=10)

    plt.tight_layout()
    output_path = '/home/ubuntu/GraphicGeneratorLogic/graphs/grafico_stress_finale_barre.png'
    plt.savefig(output_path, dpi=200, facecolor=BACKGROUND_COLOR)
    print(f"✅ Grafico stress (accurato) salvato in: {output_path}")


# =========================================================================
# 2. GRAFICO ATTIVITA'
# =========================================================================
def genera_grafico_attivita(user_id):
    print("Inizio generazione Grafico Attività... (lettura dati ACCURATI da DB)")
    query_activity = f"""
    SELECT
      p.timestamp AS timestamp,
      ROUND(AVG(
        CASE 
          WHEN a.movimento = 1 AND p.battito > 65 AND e.sudorazione < 0.8
          THEN (p.battito - 65)
          ELSE 0 
        END
      ), 2) AS combined_score
    FROM PPG p
    JOIN Accelerometro a ON p.userId = a.userId AND p.timestamp = a.timestamp
    JOIN Elettrodi e ON p.userId = e.userId AND p.timestamp = e.timestamp
    WHERE p.userId = {user_id} AND DATE(p.timestamp) = CURDATE()
    GROUP BY p.timestamp 
    ORDER BY p.timestamp;
    """
    df = pd.read_sql_query(query_activity, db_engine)
    if df.empty:
        print("❌ Dati Attività (filtrati) non trovati per oggi. Grafico non generato.")
        return
    df.columns = ['timestamp', 'combined_score']
    df['timestamp'] = pd.to_datetime(df['timestamp'])
    df_agg = df.set_index('timestamp').resample('5min').mean().reset_index()
    df_agg['score_liscio'] = df_agg['combined_score'].rolling(window=1, min_periods=1).mean()
    df_agg['color'] = df_agg['score_liscio'].apply(lambda x: get_zone_color(x, 30.0, 15.0, 5.0))
    plt.style.use('default')
    fig, ax = plt.subplots(figsize=(16, 7), facecolor=BACKGROUND_COLOR)
    ax.set_facecolor(BACKGROUND_COLOR)
    ax.bar(df_agg['timestamp'], df_agg['score_liscio'],
           width=pd.Timedelta(minutes=5), color=df_agg['color'],
           edgecolor=TEXT_COLOR, linewidth=0.8, label='Daily Activity Score', align='edge')
    
    if not df_agg.empty:
        start_day = df_agg['timestamp'].min().normalize()
        end_day = df_agg['timestamp'].max().normalize() + pd.Timedelta(days=1)
        ax.set_xlim(start_day, end_day)
        ax.xaxis.set_major_locator(mdates.HourLocator(interval=3))
        ax.xaxis.set_major_formatter(DateFormatter('%H:%M'))

    ax.set_title('Daily Activity', color=TEXT_COLOR, fontsize=20, pad=20)
    ax.set_yticklabels([]); ax.tick_params(axis='y', length=0)
    ax.tick_params(axis='x', colors=TEXT_COLOR)
    ax.grid(axis='y', linestyle='--', alpha=0.5, color='gray')
    zona_alta = mpatches.Patch(color='#c0392b', label='Intenso (Zona 4)')
    zona_media = mpatches.Patch(color='#f39c12', label='Moderato (Zona 3)')
    zona_bassa = mpatches.Patch(color='#27ae60', label='Leggero (Zona 2)')
    zona_recupero = mpatches.Patch(color='#3498db', label='Riposo (Zona 1)')
    ax.legend(handles=[zona_alta, zona_media, zona_bassa, zona_recupero], 
              loc='upper left', frameon=False, ncol=2, fontsize=10)
    plt.tight_layout()
    output_path = '/home/ubuntu/GraphicGeneratorLogic/graphs/grafico_attivita_finale.png'
    plt.savefig(output_path, dpi=200, facecolor=BACKGROUND_COLOR)
    print(f"✅ Grafico attività (accurato) salvato in: {output_path}")

# =========================================================================
# 3. GRAFICO SONNO
# =========================================================================
def genera_grafico_sonno(user_id):
    print("Inizio generazione Grafico Sonno... (lettura dati ACCURATI da DB)")
    # metto la f prima delle """ per fare una f-string
    
    query_sleep = f""" 
    SELECT
      p.timestamp AS timestamp,
      p.battito,
      a.movimento
    FROM PPG p
    JOIN Accelerometro a ON p.userId = a.userId AND p.timestamp = a.timestamp
    WHERE p.userId ={user_id}
      AND DATE(p.timestamp) = CURDATE()
      AND (HOUR(p.timestamp) >= 21 OR HOUR(p.timestamp) <= 9) 
    ORDER BY p.timestamp;
    """
    df = pd.read_sql_query(query_sleep, db_engine)
    
    if df.empty:
        print("❌ Dati Sonno (grezzi) non trovati per oggi. Grafico non generato.")
        return

    df['timestamp'] = pd.to_datetime(df['timestamp'])
    
    # [INVARIATO]: Calcolo HRV Proxy
    df = df.set_index('timestamp')
    df['hrv_proxy'] = df['battito'].rolling(window='5min', min_periods=1).std()
    df = df.reset_index().fillna(0) 

    def classifica_fase_livello(row):
        HR_DEEP_THRESHOLD = 58   # Battito molto basso
        HR_LIGHT_THRESHOLD = 65  # Battito a riposo normale
        HRV_LOW_THRESHOLD = 1.5  # Stabile (per Sonno Profondo)
        HRV_HIGH_THRESHOLD = 2.5 # Variabile (per Sonno REM)
        
        if row['movimento'] > 0: 
            return 3 # Livello 3 = Sveglio
        
        # Sonno Profondo: HR basso E stabile (HRV bassa)
        # [MODIFICA]: Reso più flessibile. Ora cerchiamo HR sotto la soglia (65) E HRV bassa (stabile).
        if row['battito'] <= HR_LIGHT_THRESHOLD and row['hrv_proxy'] <= HRV_LOW_THRESHOLD: 
            return 0 # Livello 0 = Profondo
            
        # Sonno REM: HR variabile (HRV alta), anche se il battito è basso
        if row['hrv_proxy'] >= HRV_HIGH_THRESHOLD and row['battito'] < (HR_LIGHT_THRESHOLD + 5): # Un po' più variabile
            return 2 # Livello 2 = REM
            
        # Tutto il resto è Sonno Leggero
        return 1 # Livello 1 = Leggero
            
    df['livello_fase'] = df.apply(classifica_fase_livello, axis=1)

    # [INVARIATO]: Mappiamo i colori ai livelli
    color_map = {
        3: '#f39c12',  # Arancione (Sveglio)
        2: '#e74c3c',  # Rosso (REM)
        1: '#3498db',  # Blu (Leggero)
        0: '#5e35b1'   # Viola (Profondo)
    }
    df['color'] = df['livello_fase'].map(color_map)

    # [INVARIATO]: Contiamo i minuti per fase
    minuti_per_fase = df['livello_fase'].value_counts()
    
    # === PLOTTING: IPNOGRAMMA (Grafico a Livelli) ===
    # [PERCHÉ]: Questo è il design standard e intuitivo per il sonno.
    plt.style.use('default')
    fig, ax = plt.subplots(figsize=(16, 7), facecolor=BACKGROUND_COLOR)
    ax.set_facecolor(BACKGROUND_COLOR) 

    # [PERCHÉ]: Disegniamo barre da 1 minuto senza bordi per creare un blocco visivo.
    # L'altezza (Y) è il livello della fase (0=Profondo, 1=Leggero, 2=REM, 3=Sveglio).
    ax.bar(df['timestamp'], df['livello_fase'] + 1, # +1 per evitare che 'Profondo' (0) sia invisibile
           width=pd.Timedelta(minutes=1),
           color=df['color'],
           edgecolor=df['color'], # Stesso colore del bordo (look pieno)
           linewidth=0.5,
           align='edge')

    # Allineamento Asse X (Finestra di sonno)
    if not df.empty:
        start_day = df['timestamp'].min().normalize()
        end_day = df['timestamp'].max().normalize() + pd.Timedelta(days=1)
        ax.set_xlim(start_day, end_day)
        ax.xaxis.set_major_locator(mdates.HourLocator(interval=3))
        ax.xaxis.set_major_formatter(DateFormatter('%H:%M'))

    # Pulizia Visiva (Asse Y con i nomi delle Fasi)
    ax.set_title('Sleep Phases Estimation (Hypnogram)', color=TEXT_COLOR, fontsize=20, pad=20)
    
    ax.set_yticks([1, 2, 3, 4])
    ax.set_yticklabels(['Profondo', 'Leggero', 'REM', 'Sveglio'], color=TEXT_COLOR, fontsize=12)
    ax.tick_params(axis='y', length=0)
    ax.tick_params(axis='x', colors=TEXT_COLOR)
    ax.grid(axis='y', linestyle='--', alpha=0.5, color='gray')
    
    plt.tight_layout()
    
    output_path = '/home/ubuntu/GraphicGeneratorLogic/graphs/grafico_sonno_finale.png'
    plt.savefig(output_path, dpi=200, facecolor=BACKGROUND_COLOR)
    print(f"✅ Grafico sonno (Ipnogramma) salvato in: {output_path}")


if __name__ == "__main__":
    user_id =sys.argv[1] # Prende l'ID utente come argomento da linea di comando in modo dinamico
    genera_grafico_stress (user_id)
    genera_grafico_attivita(user_id)
    genera_grafico_sonno(user_id)
    print("\n[Operazione Completata] Tutti e 3 i grafici sono stati generati.") 