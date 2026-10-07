import os
import pandas as pd
from datasets import load_dataset

print("Cargando dataset MASSIVE en español...")
dataset = load_dataset("AmazonScience/massive", "es-ES", split="train")

# Seleccionemos los campos clave para nuestras estructuras de datos en Java
# id, utt (texto del comando), intent (intención)
print("Procesando y filtrando muestras...")

# Podemos tomar un subconjunto manejable para las pruebas (por ejemplo, las primeras 1000 muestras)
subset = dataset.select(range(1000))

data_list = []
for item in subset:
    data_list.append({
        "id": item["id"],
        "utt": item["utt"],
        "intent": item["intent"]
    })

df = pd.DataFrame(data_list)

# Crear la carpeta data si no existe
os.makedirs("data", exist_ok=True)
output_path = "data/massive_es_train.csv"

# Guardar a CSV
df.to_csv(output_path, index=False, encoding="utf-8")
print(f"¡Dataset exportado con éxito en: {output_path}!")
