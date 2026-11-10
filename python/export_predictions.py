"""
Exporta las predicciones del modelo entrenado para que las consuma Java.

Uso (desde la raiz del proyecto):
    python python/export_predictions.py            # 500 muestras de validacion
    python python/export_predictions.py 2000       # cantidad personalizada

Entradas:
    data/massive_es_train.csv       (de prepare_dataset.py)
    models/intent_classifier/checkpoint-*   (de train_model.py)

Salidas:
    results/predictions.csv   id, utt, intent_real, intent_pred, score
    results/metrics.json      accuracy, f1_macro, n evaluadas, checkpoint, split

IMPORTANTE (splits): se evalua con el split "validation" de MASSIVE, que NO se uso
para entrenar. Asi las metricas no estan infladas por haber visto los datos.
"""
import json
import os
import sys

import pandas as pd
import torch
from datasets import load_dataset
from sklearn.metrics import accuracy_score, f1_score
from transformers import AutoModelForSequenceClassification, AutoTokenizer

MODEL_NAME = "distilbert-base-multilingual-cased"
MODEL_DIR = "models/intent_classifier"
TRAIN_CSV = "data/massive_es_train.csv"
SPLIT = "validation"
BATCH = 32

n_eval = int(sys.argv[1]) if len(sys.argv) > 1 else 500

print("1. Reconstruyendo el mapeo intent <-> id (mismo orden que en el entrenamiento)...")
train = pd.read_csv(TRAIN_CSV)
intents = list(train["intent"].unique())      # igual que preprocess.py
intent2id = {str(v): i for i, v in enumerate(intents)}
id2intent = {i: str(v) for i, v in enumerate(intents)}
print(f"   {len(intents)} clases vistas en el entrenamiento")

print("2. Buscando el ultimo checkpoint...")
checkpoints = [d for d in os.listdir(MODEL_DIR) if d.startswith("checkpoint-")]
if not checkpoints:
    sys.exit("No hay checkpoints en models/intent_classifier. Ejecuta primero train_model.py")
latest = max(checkpoints, key=lambda d: int(d.split("-")[-1]))
ckpt_path = os.path.join(MODEL_DIR, latest)
print(f"   Usando {ckpt_path}")

tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)
model = AutoModelForSequenceClassification.from_pretrained(ckpt_path)
model.eval()

print(f"3. Cargando el split '{SPLIT}' de MASSIVE (es-ES)...")
ds = load_dataset("AmazonScience/massive", "es-ES", split=SPLIT)
ds = ds.select(range(min(n_eval, len(ds))))

rows = []
skipped = 0
for item in ds:
    real = str(item["intent"])
    if real not in intent2id:        # intencion que el modelo nunca vio en el subconjunto de entrenamiento
        skipped += 1
        continue
    rows.append({"id": item["id"], "utt": item["utt"], "intent_real": real})
print(f"   {len(rows)} muestras usables, {skipped} omitidas (intencion no vista en el entrenamiento)")

print("4. Prediciendo por lotes...")
preds, scores = [], []
for start in range(0, len(rows), BATCH):
    lote = [r["utt"] for r in rows[start:start + BATCH]]
    enc = tokenizer(lote, padding=True, truncation=True, max_length=128, return_tensors="pt")
    with torch.no_grad():
        logits = model(**enc).logits
    prob = torch.softmax(logits, dim=-1)
    conf, idx = prob.max(dim=-1)
    preds.extend(id2intent[i] for i in idx.tolist())
    scores.extend(round(c, 6) for c in conf.tolist())

df = pd.DataFrame(rows)
df["intent_pred"] = preds
df["score"] = scores
df = df[["id", "utt", "intent_real", "intent_pred", "score"]]

os.makedirs("results", exist_ok=True)
df.to_csv("results/predictions.csv", index=False, encoding="utf-8")

acc = accuracy_score(df["intent_real"], df["intent_pred"])
f1 = f1_score(df["intent_real"], df["intent_pred"], average="macro", zero_division=0)
metrics = {
    "split": SPLIT,
    "checkpoint": ckpt_path.replace("\\", "/"),
    "n_evaluadas": len(df),
    "n_omitidas_intent_no_vista": skipped,
    "accuracy": round(acc, 4),
    "f1_macro": round(f1, 4),
}
with open("results/metrics.json", "w", encoding="utf-8") as f:
    json.dump(metrics, f, indent=2, ensure_ascii=False)

print(f"\nListo: results/predictions.csv ({len(df)} filas) y results/metrics.json")
print(f"Accuracy: {acc:.4f} | F1 macro: {f1:.4f}")
print("Ahora ejecuta la parte Java (MiProyecto2026) y compara el accuracy.")
