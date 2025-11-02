import pandas as pd
import numpy as np
from sklearn.impute import KNNImputer
import joblib
import os
import sys
import mysql.connector
from datetime import datetime


def preprocess(df: pd.DataFrame) -> pd.DataFrame:
    # Applichiamo la stessa preprocessing usata in training
    # Se il CSV non contiene header (es. file con solo eda,hr,temp), proviamo ad assegnare i nomi
    expected_cols = {'hr', 'eda', 'temp'}
    if not expected_cols.issubset(set(df.columns)):
        # If columns are numeric (0,1,2...) try to infer mapping based on number of columns
        if df.shape[1] == 3:
            # assumiamo ordine: eda, hr, temp
            df.columns = ['eda', 'hr', 'temp']
        elif df.shape[1] == 4:
            # assumiamo ordine: glucose, eda, hr, temp
            df.columns = ['glucose', 'eda', 'hr', 'temp']
        else:
            # fall back: raise a clear error explaining expected format
            raise ValueError("CSV deve contenere le colonne 'eda','hr','temp' (con o senza 'glucose') o avere 3/4 colonne senza header.")

    features_originali = [col for col in df.columns if col != 'glucose']
    target = 'glucose'

    X = df[features_originali]
    # Se glucose esiste, la teniamo; altrimenti creiamo una colonna dummy
    if target in df.columns:
        y = df[target]
    else:
        y = None

    # Ensure n_neighbors is not greater than available samples (avoids errors for very small CSVs)
    n_samples = X.shape[0]
    n_neighbors = min(5, max(1, n_samples - 1))
    imputer = KNNImputer(n_neighbors=n_neighbors)
    X_imputed = imputer.fit_transform(X)

    df_imputed = pd.DataFrame(X_imputed, columns=features_originali)
    if y is not None:
        df_imputed[target] = y.values
    df = df_imputed.copy()

    window_size_5min = 5
    window_size_15min = 15

    # Use min_periods=1 so we can compute features on short files as well
    df['hr_mean_5min'] = df['hr'].rolling(window=window_size_5min, min_periods=1).mean()
    df['eda_mean_5min'] = df['eda'].rolling(window=window_size_5min, min_periods=1).mean()
    df['temp_mean_15min'] = df['temp'].rolling(window=window_size_15min, min_periods=1).mean()

    # diffs produce NaN on the first row; fill with 0 (no change)
    df['hr_diff_1min'] = df['hr'].diff(periods=1).fillna(0)
    df['temp_diff_1min'] = df['temp'].diff(periods=1).fillna(0)

    # For std with a single value, use ddof=0 so std=0 rather than NaN
    df['hr_std_15min'] = df['hr'].rolling(window=window_size_15min, min_periods=1).std(ddof=0)

    df_cleaned = df.dropna()
    return df_cleaned


def save_to_database(glicemia: float, user_id: int) -> None:
    """Salva il valore di glicemia nel database MySQL"""
    try:
        connection = mysql.connector.connect(
            host='localhost',
            user='root',
            password='Simone04',
            database='LoopyDB',
            connection_timeout=5
        )
        cursor = connection.cursor()
        
        timestamp = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        
        query = "INSERT INTO Glucosio (glicemia, userId, timestamp) VALUES (%s, %s, %s)"
        values = (glicemia, user_id, timestamp)
        
        cursor.execute(query, values)
        connection.commit()
        
        cursor.close()
        connection.close()
    except mysql.connector.Error as err:
        print(f"ERROR: Errore nel salvataggio nel database: {err}", file=sys.stderr)
        sys.exit(2)


def predict_last_value(model_path: str = './model_rf.pkl', csv_path: str = './result.csv') -> float:
    if not os.path.exists(model_path):
        raise FileNotFoundError(f"Modello non trovato: {model_path}. Esegui prima train.py")
    if not os.path.exists(csv_path):
        raise FileNotFoundError(f"CSV non trovato: {csv_path}")

    try:
        model = joblib.load(model_path)
    except Exception as e:
        raise RuntimeError(f"Impossibile caricare il modello. File corrotto o versione incompatibile: {e}")
    
    df = pd.read_csv(csv_path)
    df_cleaned = preprocess(df)

    features_engineered = [
        'eda', 'hr', 'temp',
        'hr_mean_5min', 'eda_mean_5min', 'temp_mean_15min',
        'hr_diff_1min', 'temp_diff_1min',
        'hr_std_15min'
    ]

    if df_cleaned.shape[0] == 0:
        raise ValueError("Dopo preprocessing non ci sono righe utili per la predizione.")

    X = df_cleaned[features_engineered]
    # Prendiamo l'ultima riga disponibile e prediciamo
    x_last = X.tail(1)
    y_pred = model.predict(x_last)

    # Restituiamo il valore numerico (float)
    return float(y_pred[0])


if __name__ == '__main__':
    user_id = sys.argv[1]
    try:
        model_path = "/home/ubuntu/MLLoopy/models/model_rf.pkl"
        csv_path = f"/home/ubuntu/MLLoopy/csvs/result{user_id}.csv"

        val = predict_last_value(model_path, csv_path)
        save_to_database(val, int(user_id))
    except Exception as e:
        print(f"ERROR: {e}", file=sys.stderr)
        sys.exit(2)
