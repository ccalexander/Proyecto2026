import pandas as pd
import numpy as np
import torch
import matplotlib.pyplot as plt
import seaborn as sns
from transformers import AutoTokenizer, AutoModelForSequenceClassification
from sklearn.metrics import classification_report, confusion_matrix

print("1. Cargando datos y etiquetas...")
df = pd.read_csv("data/massive_es_train.csv")
labels_array = np.load("data/labels_array.npy")

print("2. Cargando el modelo entrenado y el tokenizador...")
model_path = "models/intent_classifier"
# Si guardaste el modelo en otra ruta, asegúrate de ajustarla aquí (ej. models/intent_classifier_final)
tokenizer = AutoTokenizer.from_pretrained("distilbert-base-multilingual-cased")

# Nota: Si no guardaste los pesos con trainer.save_model() en una carpeta específica, 
# usaremos el modelo actual en memoria o el checkpoint. Carguemos el tokenizador y probemos:
try:
    model = AutoModelForSequenceClassification.from_pretrained(model_path)
except:
    # Si prefieres evaluar sobre un subconjunto rápido o el modelo base si no se guardó el checkpoint final:
    print("Aviso: Cargando modelo base para demostración de métricas.")
    model = AutoModelForSequenceClassification.from_pretrained("distilbert-base-multilingual-cased", num_labels=len(np.unique(labels_array)))

model.eval()

print("3. Realizando predicciones en lote...")
texts = df["utt"].tolist()[:100] # Evaluamos las primeras 100 muestras para prueba rápida
true_labels = labels_array[:100]

inputs = tokenizer(texts, padding=True, truncation=True, max_length=128, return_tensors="pt")
with torch.no_grad():
    outputs = model(**inputs)
    predictions = torch.argmax(outputs.logits, dim=-1).numpy()

print("4. Generando reporte de métricas y Matriz de Confusión...")
# Matriz de confusión
cm = confusion_matrix(true_labels, predictions)

print("\n--- REPORTE DE CLASIFICACIÓN ---")
print(classification_report(true_labels, predictions, zero_division=0))

# Guardar gráfica de la matriz de confusión
plt.figure(figsize=(10, 8))
sns.heatmap(cm, annot=True, fmt="d", cmap="Blues")
plt.title("Matriz de Confusión - Clasificador de Intenciones (MASSIVE)")
plt.xlabel("Predicción")
plt.ylabel("Valor Real")

import os
os.makedirs("models", exist_ok=True)
plt.savefig("models/confusion_matrix.png")
print("\n¡Matriz de confusión guardada con éxito en models/confusion_matrix.png!")
