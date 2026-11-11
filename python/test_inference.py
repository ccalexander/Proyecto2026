"""
Prueba interactiva del modelo entrenado.

Uso (desde la raiz del proyecto):
    python python/test_inference.py
"""
import os

import pandas as pd
import torch
from transformers import AutoModelForSequenceClassification, AutoTokenizer

MODEL_NAME = "distilbert-base-multilingual-cased"
MODEL_DIR = "models/intent_classifier"

print("1. Cargando metadatos y etiquetas del dataset...")
df = pd.read_csv("data/massive_es_train.csv")
unique_intents = list(df["intent"].unique())  # mismo orden que preprocess.py

# Nombres legibles (alarm_set, etc.). Necesita el dataset en cache o internet; si falla, se muestra el numero.
nombres = None
try:
    from datasets import load_dataset_builder
    nombres = load_dataset_builder("AmazonScience/massive", "es-ES").info.features["intent"].names
except Exception:
    print("   (no se pudieron obtener los nombres de las intenciones; se mostrara el numero)")

print("2. Cargando el tokenizador y el modelo...")
tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)

if os.path.exists(os.path.join(MODEL_DIR, "config.json")):
    ruta = MODEL_DIR                                     # modelo final (save_model)
else:
    checkpoints = [d for d in os.listdir(MODEL_DIR) if d.startswith("checkpoint-")] if os.path.exists(MODEL_DIR) else []
    if not checkpoints:
        raise SystemExit("No hay modelo entrenado. Ejecuta primero train_model.py")
    ruta = os.path.join(MODEL_DIR, max(checkpoints, key=lambda d: int(d.split("-")[-1])))
print(f"-> Cargando desde: {ruta}")
model = AutoModelForSequenceClassification.from_pretrained(ruta)
model.eval()

print("\n--- MODELO LISTO PARA PRUEBAS INTERACTIVAS ---")
print("Escribe una frase (o 'salir' para terminar):\n")

while True:
    frase = input("Tu: ")
    if frase.lower() == "salir":
        break
    if not frase.strip():
        continue

    inputs = tokenizer(frase, return_tensors="pt", padding=True, truncation=True, max_length=128)
    with torch.no_grad():
        prob = torch.softmax(model(**inputs).logits, dim=-1)
    conf, pred_id = prob.max(dim=-1)
    pred_id = pred_id.item()

    valor = unique_intents[pred_id] if pred_id < len(unique_intents) else None
    if valor is None:
        etiqueta = "Desconocido"
    elif nombres is not None and str(valor).isdigit() and int(valor) < len(nombres):
        etiqueta = f"{nombres[int(valor)]} ({valor})"
    else:
        etiqueta = str(valor)
    print(f"   -> Intencion: {etiqueta} | confianza: {conf.item():.2f}\n")
