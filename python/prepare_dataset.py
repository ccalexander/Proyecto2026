import os
import pandas as pd
from datasets import load_dataset

SEED = 42        # semilla fija: misma muestra en cada ejecucion (reproducible)
N_MUESTRAS = 1000

print("Cargando dataset MASSIVE en español...")
dataset = load_dataset("AmazonScience/massive", "es-ES", split="train")

# Los datos vienen agrupados por intencion/escenario, asi que tomar las primeras N
# deja fuera muchas intenciones. Se mezcla con semilla fija y luego se toman N.
print("Procesando y filtrando muestras...")
subset = dataset.shuffle(seed=SEED).select(range(N_MUESTRAS))

# Campos clave para las estructuras de datos en Java: id, utt (texto), intent
data_list = []
for item in subset:
    data_list.append({
        "id": item["id"],
        "utt": item["utt"],
        "intent": item["intent"]
    })

df = pd.DataFrame(data_list)

os.makedirs("data", exist_ok=True)
output_path = "data/massive_es_train.csv"
df.to_csv(output_path, index=False, encoding="utf-8")

print(f"¡Dataset exportado con éxito en: {output_path}!")
print(f"Muestras: {len(df)} | intenciones distintas: {df['intent'].nunique()} (de 60 posibles)")
