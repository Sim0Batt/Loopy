import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LinearRegression
from sklearn.preprocessing import StandardScaler
from sklearn.impute import KNNImputer
from sklearn.pipeline import Pipeline
import joblib
import os
import sys


def preprocess(df: pd.DataFrame) -> pd.DataFrame:
    # Imputazione
    if 'glucose' not in df.columns:
        raise ValueError("Colonna 'glucose' non trovata nel CSV.")

    features_originali = [col for col in df.columns if col != 'glucose']
    target = 'glucose'

    X = df[features_originali]
    y = df[target]

    # Ensure n_neighbors is not greater than available samples (avoids errors for very small CSVs)
    n_samples = X.shape[0]
    n_neighbors = min(5, max(1, n_samples - 1))
    imputer = KNNImputer(n_neighbors=n_neighbors)
    X_imputed = imputer.fit_transform(X)

    df_imputed = pd.DataFrame(X_imputed, columns=features_originali)
    df_imputed[target] = y.values
    df = df_imputed.copy()

    # Feature engineering (rolling, differenze, std)
    window_size_5min = 5
    window_size_15min = 15

    # Use min_periods=1 so features are computable even on short chunks
    df['hr_mean_5min'] = df['hr'].rolling(window=window_size_5min, min_periods=1).mean()
    df['eda_mean_5min'] = df['eda'].rolling(window=window_size_5min, min_periods=1).mean()
    df['temp_mean_15min'] = df['temp'].rolling(window=window_size_15min, min_periods=1).mean()

    df['hr_diff_1min'] = df['hr'].diff(periods=1).fillna(0)
    df['temp_diff_1min'] = df['temp'].diff(periods=1).fillna(0)

    df['hr_std_15min'] = df['hr'].rolling(window=window_size_15min, min_periods=1).std(ddof=0)

    df_cleaned = df.dropna()

    return df_cleaned


def train_model(csv_path, model_path) -> None:
    if not os.path.exists(csv_path):
        raise FileNotFoundError(f"File non trovato: {csv_path}")

    df = pd.read_csv(csv_path)
    print(f"Caricati {len(df)} record da {csv_path}")

    df_cleaned = preprocess(df)

    features_engineered = [
        'eda', 'hr', 'temp',
        'hr_mean_5min', 'eda_mean_5min', 'temp_mean_15min',
        'hr_diff_1min', 'temp_diff_1min',
        'hr_std_15min'
    ]

    X = df_cleaned[features_engineered]
    y = df_cleaned['glucose']

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

    # Modello leggero: LinearRegression per Raspberry Pi 3B+
    # Occupa ~100KB di memoria e è velocissimo
    pipeline = Pipeline([
        ('scaler', StandardScaler()),
        ('model', LinearRegression())
    ])

    pipeline.fit(X_train, y_train)

    # Salva il pipeline sul disco
    joblib.dump(pipeline, model_path)
    print(f"Modello salvato in: {model_path}")


if __name__ == '__main__':
    CSV_PATH = f"/home/ubuntu/MLLoopy/csvs/result.csv"
    MODEL_PATH = f"/home/ubuntu/MLLoopy/models/model_rf.pkl"

    try:
        train_model(CSV_PATH, MODEL_PATH)
        print("success")
    except Exception as e:
        print(f"failure: {e}")

