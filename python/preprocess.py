import pandas as pd
import numpy as np
import os

print("1. Cargando el dataset CSV...")
df = pd.read_csv("data/massive_es_train.csv")

# Preprocesamiento básico del texto (minúsculas y limpieza de espacios)
print("2. Preprocesando textos (utterances)...")
df["utt"] = df["utt"].astype(str).str.lower().str.strip()

# Creación de arreglos auxiliares y mapeo de etiquetas (int[] labels)
print("3. Generando arreglos auxiliares y mapeo de etiquetas...")
unique_intents = df["intent"].unique()

# Diccionarios de conversión
intent2id = {intent: idx for idx, intent in enumerate(unique_intents)}
id2intent = {idx: intent for intent, idx in intent2id.items()}

# Convertir la columna de intenciones a un arreglo numérico de enteros (equivalente a int[] labels)
labels_array = np.array([intent2id[intent] for intent in df["intent"]], dtype=int)

# Mostrar resultados del preprocesamiento
print(f"-> Total de muestras procesadas: {len(df)}")
print(f"-> Clases (intents) únicas detectadas: {len(unique_intents)}")
print(f"-> Primeros 5 elementos del arreglo numérico (int[] labels): {labels_array[:5]}")

# Guardar los metadatos o el dataset procesado si es necesario
os.makedirs("data", exist_ok=True)
np.save("data/labels_array.npy", labels_array)
print("¡Preprocesamiento completado y arreglo de etiquetas guardado con éxito!")
