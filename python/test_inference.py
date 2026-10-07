import torch
import numpy as np
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import pandas as pd
import os

print("1. Cargando metadatos y etiquetas del dataset...")
df = pd.read_csv("data/massive_es_train.csv")
unique_intents = df["intent"].unique()

print("2. Cargando el tokenizador y el modelo...")
model_name = "distilbert-base-multilingual-cased"
tokenizer = AutoTokenizer.from_pretrained(model_name)

# Buscar si hay un checkpoint guardado dentro de models/intent_classifier
model_path = "models/intent_classifier"
checkpoints = [os.path.join(model_path, d) for d in os.listdir(model_path) if d.startswith("checkpoint-")] if os.path.exists(model_path) else []

if checkpoints:
    latest_checkpoint = max(checkpoints, key=os.path.getmtime)
    print(f"-> Cargando desde el checkpoint: {latest_checkpoint}")
    model = AutoModelForSequenceClassification.from_pretrained(latest_checkpoint)
else:
    print("-> Usando modelo base multilingüe.")
    model = AutoModelForSequenceClassification.from_pretrained(model_name, num_labels=len(unique_intents))

model.eval()

print("\n--- ¡MODELO LISTO PARA PRUEBAS INTERACTIVAS! ---")
print("Escribe una frase para que el asistente prediga su intención (escribe 'salir' para terminar):\n")

while True:
    frase_prueba = input("Tú: ")
    if frase_prueba.lower() == 'salir':
        print("¡Saliendo de la prueba interactiva!")
        break
    
    if not frase_prueba.strip():
        continue

    inputs = tokenizer(frase_prueba, return_tensors="pt", padding=True, truncation=True, max_length=128)

    with torch.no_grad():
        outputs = model(**inputs)
        logits = outputs.logits
        pred_id = torch.argmax(logits, dim=-1).item()

    intent_predicho = unique_intents[pred_id] if pred_id < len(unique_intents) else "Desconocido"
    print(f"   -> Intención predicha: [ID: {pred_id}] --> {intent_predicho}\n")
