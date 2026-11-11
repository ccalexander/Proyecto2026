"""
Reporte de evaluacion a partir de results/predictions.csv.

Uso (desde la raiz del proyecto), DESPUES de export_predictions.py:
    python python/evaluate_model.py
    python python/evaluate_model.py results/predictions.csv

Salidas:
    results/classification_report.txt   precision/recall/F1 por intencion
    results/confusion_matrix.png        imagen de la matriz de confusion

Nota: no vuelve a ejecutar el modelo. Usa las mismas predicciones (split
"validation") que consume Java, asi todas las metricas son consistentes.
"""
import os
import sys

import matplotlib
matplotlib.use("Agg")  # sin ventana: solo guarda la imagen
import matplotlib.pyplot as plt
import pandas as pd
from sklearn.metrics import accuracy_score, classification_report, confusion_matrix, f1_score

path = sys.argv[1] if len(sys.argv) > 1 else "results/predictions.csv"
if not os.path.exists(path):
    sys.exit(f"No existe {path}. Ejecuta primero export_predictions.py")

df = pd.read_csv(path, dtype={"intent_real": str, "intent_pred": str})
real = df["intent_real"]
pred = df["intent_pred"]

labels = sorted(set(real) | set(pred), key=lambda v: (0, int(v)) if v.lstrip("-").isdigit() else (1, v))

acc = accuracy_score(real, pred)
f1 = f1_score(real, pred, average="macro", zero_division=0)
print(f"Muestras evaluadas: {len(df)}")
print(f"Accuracy: {acc:.4f} | F1 macro: {f1:.4f}")

report = classification_report(real, pred, labels=labels, zero_division=0)
print("\n--- REPORTE DE CLASIFICACION ---")
print(report)

os.makedirs("results", exist_ok=True)
with open("results/classification_report.txt", "w", encoding="utf-8") as f:
    f.write(f"accuracy={acc:.4f} f1_macro={f1:.4f} n={len(df)}\n\n{report}")

cm = confusion_matrix(real, pred, labels=labels)
k = len(labels)
fig, ax = plt.subplots(figsize=(10, 8))
im = ax.imshow(cm, cmap="Blues")
fig.colorbar(im, ax=ax)
ax.set_title("Matriz de confusion - Clasificador de intenciones (MASSIVE es-ES, validation)")
ax.set_xlabel("Prediccion")
ax.set_ylabel("Valor real")
ax.set_xticks(range(k))
ax.set_yticks(range(k))
ax.set_xticklabels(labels, rotation=90, fontsize=6)
ax.set_yticklabels(labels, fontsize=6)
if k <= 20:  # con 60 clases los numeros no se leen
    for i in range(k):
        for j in range(k):
            ax.text(j, i, cm[i, j], ha="center", va="center", fontsize=7)
fig.tight_layout()
fig.savefig("results/confusion_matrix.png", dpi=150)
print("\nGuardado: results/classification_report.txt y results/confusion_matrix.png")
