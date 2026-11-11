"""
Fine-tuning de distilbert-base-multilingual-cased con 1000 muestras de MASSIVE es-ES.

Uso (desde la raiz del proyecto):
    python python/train_model.py        # 3 epocas (por defecto)
    python python/train_model.py 8      # numero de epocas personalizado
"""
import sys

import pandas as pd
import numpy as np
import torch
from transformers import AutoTokenizer, AutoModelForSequenceClassification, Trainer, TrainingArguments
from torch.utils.data import Dataset

EPOCHS = int(sys.argv[1]) if len(sys.argv) > 1 else 3

print("1. Cargando datos y etiquetas preprocesadas...")
df = pd.read_csv("data/massive_es_train.csv")
labels_array = np.load("data/labels_array.npy")

print("2. Cargando el Tokenizer...")
model_name = "distilbert-base-multilingual-cased"
tokenizer = AutoTokenizer.from_pretrained(model_name)

print("3. Tokenizando textos...")
texts = df["utt"].tolist()
encodings = tokenizer(texts, padding=True, truncation=True, max_length=128, return_tensors="pt")

class MassiveDataset(Dataset):
    def __init__(self, encodings, labels):
        self.encodings = encodings
        self.labels = labels

    def __getitem__(self, idx):
        item = {key: val[idx] for key, val in self.encodings.items()}
        item["labels"] = torch.tensor(self.labels[idx])
        return item

    def __len__(self):
        return len(self.labels)

dataset = MassiveDataset(encodings, labels_array)

print("4. Cargando el modelo Transformer preentrenado...")
num_labels = len(np.unique(labels_array))
model = AutoModelForSequenceClassification.from_pretrained(model_name, num_labels=num_labels)

print(f"5. Configurando entrenamiento ({EPOCHS} epocas)...")
training_args = TrainingArguments(
    output_dir="models/intent_classifier",
    per_device_train_batch_size=8,
    num_train_epochs=EPOCHS,
    weight_decay=0.01,
    logging_steps=25,
    save_strategy="epoch",
    save_total_limit=1,      # conserva solo el ultimo checkpoint (cada uno pesa ~500 MB)
)

trainer = Trainer(
    model=model,
    args=training_args,
    train_dataset=dataset,
)

print("6. Iniciando el entrenamiento...")
trainer.train()

trainer.save_model("models/intent_classifier")
print("Entrenamiento finalizado y modelo guardado en models/intent_classifier")
