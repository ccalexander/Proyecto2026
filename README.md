# TransformerDS

Proyecto final de **Algoritmos y Estructuras de Datos** (UTP).
Un Transformer clasifica intenciones de un asistente inteligente; las estructuras de datos, construidas a mano en Java, organizan el sistema alrededor del modelo.

- **Autor (trabajo individual):** Alexander Cayo Canqui
- **Repositorio:** https://github.com/ccalexander/Proyecto2026

## 1. Track y justificación

**Track D · Smart Assistant — MASSIVE** (`AmazonScience/massive`, configuración `es-ES`).

Se eligió porque el reto (reconocer intenciones de un asistente tipo Alexa/Siri) justifica de forma natural las estructuras del curso:

- las órdenes del usuario llegan como solicitudes que esperan su turno → **cola**;
- las últimas predicciones se consultan de la más reciente a la más antigua → **pila**;
- cada resultado se recupera por el id de su solicitud → **ABB / AVL**;
- las muestras del dataset se administran dinámicamente → **TAD + lista enlazada**.

## 2. Modelo base

**`distilbert-base-multilingual-cased`** (Hugging Face), con fine-tuning para clasificación de secuencias.

Justificación:

- Es multilingüe, por lo que funciona con texto en español.
- Es una versión destilada de BERT (6 capas), más pequeña y rápida de entrenar que BERT completo, lo que permite hacer el fine-tuning en un equipo personal.
- No se reimplementa Self-Attention: tokenizer, embeddings, encoder y clasificador vienen de la librería (PyTorch / Hugging Face), tal como permite la consigna.

Configuración de entrenamiento: 1000 muestras del split `train` (mezcladas con semilla 42), lotes de 8 y **8 épocas** (1000 pasos; el último checkpoint es `checkpoint-1000`). `train_model.py` acepta el número de épocas como argumento.

## 3. Arquitectura

```
Dataset CSV → RepositorioMuestras (lista enlazada) → arreglos (labels)
   → Tokenizer + Transformer (Python) → results/predictions.csv
   → ColaInferencia → Prediccion ─┬→ PilaHistorial
                                  ├→ AVLResultados (índice por id)
                                  ├→ MatrizConfusion (int[k][k])
                                  └→ ResultadosLote (arreglos 1D)
```

Python y Java se comunican por archivos CSV (no hay llamada en tiempo real). El detalle y el diagrama completo están en [`docs/arquitectura.md`](docs/arquitectura.md).

| Estructura | Clase(s) | Uso en el sistema |
|---|---|---|
| TAD + lista enlazada | `RepositorioMuestras`, `Nodo`, `Muestra` | Almacena las muestras del dataset (ids únicos) |
| Cola FIFO | `ColaInferencia`, `NodoGenerico<T>`, `Solicitud` | Solicitudes pendientes de inferencia |
| Pila LIFO | `PilaHistorial`, `Prediccion` | Historial de predicciones |
| ABB / AVL | `ABBResultados`, `AVLResultados`, `NodoArbol` | Índice de resultados por id de solicitud |
| Arreglos 1D | `ResultadosLote` | `real[]`, `pred[]`, `score[]`, histograma de scores |
| Matriz 2D | `MatrizConfusion` | Matriz de confusión, accuracy, precisión, recall, F1 |
| Integración | `PipelineEvaluacion`, `MiProyecto2026` | Conecta todo y escribe `results/confusion_matrix.csv` |

No se usan `ArrayList`, `LinkedList`, `Stack`, `Queue` ni `TreeMap` en las estructuras evaluadas. `TreeMap` aparece solo en una prueba, como referencia comparativa.

## 4. Estructura del repositorio

```
Proyecto2026/
├── README.md
├── .gitignore
├── build.xml, manifest.mf, nbproject/   (proyecto NetBeans / Ant)
├── data/        README.md, massive_es_train.csv
├── python/      prepare_dataset.py, preprocess.py, train_model.py,
│                evaluate_model.py, test_inference.py,
│                export_predictions.py, requirements.txt
├── src/miproyecto2026/    código Java de las estructuras y el pipeline
├── test/miproyecto2026/   pruebas Java
├── docs/        tad_informal.md, tad_formal.md, tad_cola_pila.md,
│                tad_indice.md, arquitectura.md, complejidad.md
└── results/     predictions.csv, metrics.json, confusion_matrix.csv
```

Nota: la consigna propone una estructura recomendada con `java/src/...`. Aquí se mantiene el diseño estándar de NetBeans (`src/` y `test/` en la raíz) porque el proyecto se desarrolló y se ejecuta en NetBeans.

Los pesos del modelo (`models/`) no se suben al repositorio; se regeneran con `train_model.py`.

## 5. Cómo reproducir de punta a punta

Requisitos: Python 3, JDK y NetBeans.

```bash
pip install -r python/requirements.txt
python python/prepare_dataset.py     # descarga MASSIVE es-ES y genera data/
python python/preprocess.py
python python/train_model.py 8       # fine-tuning, 8 épocas (genera models/)
python python/export_predictions.py 3000  # genera results/predictions.csv y metrics.json (todo validation)
python python/evaluate_model.py      # reporte por clase y matriz de confusión (png)
```

Luego, en NetBeans, ejecutar `MiProyecto2026` (F6). Lee `data/massive_es_train.csv` y `results/predictions.csv`, y escribe `results/confusion_matrix.csv`. También acepta argumentos opcionales: CSV de entrenamiento, CSV de predicciones y carpeta de salida.

## 6. Pruebas (NetBeans)

No se usa JUnit: cada archivo es una clase con `main`. Se ejecutan con **Shift+F6** sobre el archivo.

| Archivo | Verifica | Resultado esperado |
|---|---|---|
| `PruebasRepositorioMuestras` | Lista enlazada y contrato del TAD | 35 correctas |
| `PruebasColaPila` | Cola FIFO y pila LIFO | 26 correctas |
| `PruebasArboles` | ABB, AVL, 4 rotaciones, comparación con `TreeMap`, altura ABB degenerado vs AVL | 37 correctas |
| `PruebasEvaluacion` | CSV, matriz de confusión, lote y pipeline | 38 correctas |

## 7. Datos y splits

Ver [`data/README.md`](data/README.md). Resumen: se usa `train` para el fine-tuning y `validation` para evaluar; el split `test` se mantiene fuera del entrenamiento y del ajuste.

## 8. Resultados

Evaluación sobre el split `validation` de MASSIVE es-ES (2033 muestras, ninguna omitida; el modelo vio las 60 intenciones en el entrenamiento). Las métricas de Python (`results/metrics.json`) y las de Java (`MiProyecto2026`) coinciden.

| Intento | Épocas | Accuracy | F1 macro |
|---|---|---|---|
| 1 | 3 | 0.4737 | 0.2389 |
| 2 (**final**) | 8 | **0.6783** | **0.5377** |

El intento 2 es el modelo guardado (`checkpoint-1000`). Se ajustó solo el número de épocas, comparando en `validation`; el split `test` no se usó.

Observaciones del pipeline Java sobre el modelo final:

- Score promedio: 0.7417 (0.8251 en aciertos y 0.5656 en errores). 416 de 2033 predicciones tienen confianza menor a 0.5.
- Índice AVL: 2033 nodos, altura 12, validado con `esAVLValido()`.
- Confusiones más frecuentes (intención real → predicha, por número de clase): 2→42 (20 casos), 50→32 (13), 31→18 (11), 12→49 (11), 49→12 (11).
- Java cuenta 59 clases presentes en los datos de validación; el modelo distingue 60.

Archivos generados: `results/metrics.json`, `results/predictions.csv`, `results/confusion_matrix.csv` (Java), y `results/classification_report.txt` y `results/confusion_matrix.png` (`evaluate_model.py`).

Antes de usar la muestra mezclada, con las primeras 1000 muestras solo aparecían 34 intenciones y se omitía casi la mitad de `validation`; esos números no son comparables con los de la tabla.

## 9. Documentación

- [`docs/tad_informal.md`](docs/tad_informal.md) y [`docs/tad_formal.md`](docs/tad_formal.md): TAD `RepositorioMuestras` (operaciones, pre/postcondiciones, invariantes).
- [`docs/tad_cola_pila.md`](docs/tad_cola_pila.md) y [`docs/tad_indice.md`](docs/tad_indice.md): cola, pila y árboles.
- [`docs/arquitectura.md`](docs/arquitectura.md): ubicación de las estructuras en el pipeline.
- [`docs/complejidad.md`](docs/complejidad.md): análisis Big O.

## 10. Limitaciones

- El fine-tuning usa solo 1000 muestras (unas 17 por intención), por lo que el accuracy (0.6783) y sobre todo el F1 macro (0.5377) son moderados. Se mantiene ese tamaño por la regla de subconjunto comparable de la consigna.
- La muestra es aleatoria con semilla fija, no estratificada: la distribución de intenciones puede estar desbalanceada.
- La conexión Java ↔ Python es por CSV, no en tiempo real.
- `RepositorioMuestras.insertar` es O(n) porque valida la unicidad del id recorriendo la lista (decisión documentada en `docs/complejidad.md`).
- El índice de árboles ordena los ids de forma lexicográfica (`String.compareTo`), no numérica.
- Las intenciones aparecen como números de clase en los resultados, no con su nombre.
