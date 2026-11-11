# Datos del proyecto

## Origen

- **Dataset:** MASSIVE (`AmazonScience/massive`), publicado en Hugging Face.
- **Configuración:** `es-ES` (español de España).
- **Tarea:** clasificación de intenciones (*intent classification*) de comandos de un asistente inteligente. Cada ejemplo es una frase del usuario (`utt`) con su intención (`intent`).
- **Track:** D · Smart Assistant.

El dataset original no se sube al repositorio: se descarga con `datasets.load_dataset` al ejecutar el script de preparación.

## Script de preparación

`python/prepare_dataset.py` (se ejecuta desde la raíz del proyecto):

```bash
python python/prepare_dataset.py
```

Qué hace:

1. Descarga el split `train` de MASSIVE `es-ES`.
2. Mezcla el split con semilla fija (`shuffle(seed=42)`) y toma **1000 muestras** (`select(range(1000))`). Se mezcla porque MASSIVE viene agrupado por intención: las primeras 1000 solo cubrían 34 de las 60.
3. Conserva solo tres campos: `id`, `utt` e `intent`.
4. Guarda el resultado en `data/massive_es_train.csv` (UTF-8, con cabecera).

## Archivos de esta carpeta

| Archivo | Descripción | ¿Se versiona? |
|---|---|---|
| `massive_es_train.csv` | 1000 muestras de entrenamiento (`id`, `utt`, `intent`). Las lee Java para cargar el `RepositorioMuestras`. | Sí (es pequeño y reproducible) |
| `labels_array.npy` | Arreglo de etiquetas generado por el preprocesamiento de Python. | No (`*.npy` está en `.gitignore`; se regenera) |

### Formato del CSV

```
id,utt,intent
<id de la muestra>,<texto del comando>,<intención>
```

Nota: en MASSIVE, `intent` es un `ClassLabel` de Hugging Face, por lo que en el CSV aparece como **número** (índice de la clase) y no como nombre (`alarm_set`, etc.). Los scripts de Python y Java lo tratan como texto de forma consistente. El dataset define 60 intenciones.

## Explicación de los splits

MASSIVE trae tres particiones, y cada una tiene un papel distinto:

| Split | Uso en este proyecto |
|---|---|
| `train` | Fine-tuning del modelo (1000 muestras mezcladas, semilla 42) y carga del `RepositorioMuestras` en Java. |
| `validation` | **Evaluación**: `export_predictions.py` genera `results/predictions.csv` y `metrics.json` con este split. |
| `test` | **No se usa** en el entrenamiento, ni en el ajuste, ni en la evaluación del proyecto. Se reserva para una evaluación final, si el docente la pide. |

Por qué se evalúa en `validation` y no en `train`: medir sobre muestras que el modelo ya vio sobreestima el rendimiento. Mantener `test` fuera de cualquier decisión evita filtrar información de la evaluación final al entrenamiento.

## Reproducir

```bash
pip install -r python/requirements.txt
python python/prepare_dataset.py     # genera data/massive_es_train.csv
python python/preprocess.py          # genera el resto de insumos de entrenamiento
```

Después sigue con el entrenamiento y la exportación de predicciones, como se indica en el README principal.

## Limitaciones

- La muestra es aleatoria con semilla fija, no estratificada, y es pequeña (unas 17 muestras por intención): la distribución de clases puede estar desbalanceada. Con esta muestra aparecen las 60 intenciones.
- Con las primeras 1000 muestras (sin mezclar) solo había 34 intenciones y 987 de las 2033 muestras de `validation` quedaban sin evaluar; por eso se cambió a la muestra mezclada.
- `export_predictions.py` omite las muestras de `validation` cuya intención no esté en el entrenamiento; con la muestra actual no se omite ninguna (2033 evaluadas).
