# TAD RepositorioMuestras (especificación informal)

## Propósito
Almacenar y administrar de forma dinámica las muestras (`Muestra`) del dataset
MASSIVE en español (Track D), antes de que pasen al preprocesamiento y al
Transformer. Permite insertar, eliminar, buscar y recorrer muestras sin conocer
cómo se guardan internamente (lista simplemente enlazada).

## Elemento almacenado: `Muestra`
| Campo    | Tipo   | Descripción                                   |
|----------|--------|-----------------------------------------------|
| `id`     | String | Identificador único de la muestra             |
| `utt`    | String | Texto del comando del usuario (utterance)     |
| `intent` | String | Intención etiquetada (clase)                  |

## Datos
Colección dinámica de `Muestra`, sin límite fijo de tamaño y con IDs únicos.

## Operaciones
| Operación       | Qué hace                                                        |
|-----------------|-----------------------------------------------------------------|
| `insertar(m)`   | Agrega la muestra `m` al repositorio                            |
| `eliminar(id)`  | Quita la muestra con ese `id`; indica si la encontró            |
| `buscar(id)`    | Devuelve la muestra con ese `id`, o `null` si no existe         |
| `existe(id)`    | Indica si hay una muestra con ese `id`                          |
| `cantidad()`    | Devuelve cuántas muestras hay                                   |
| `estaVacio()`   | Indica si no hay muestras                                       |
| `recorrer()`    | Visita todas las muestras en orden e imprime cada una           |
| `vaciar()`      | Elimina todas las muestras                                      |

## Invariantes
1. `cantidad >= 0`.
2. Los IDs son únicos: no hay dos muestras con el mismo `id`.
3. Si `cantidad == 0`, entonces `inicio == null` (y viceversa).
4. `cantidad` coincide con el número de nodos alcanzables desde `inicio`.

## Errores controlados
- `insertar(null)` o `insertar` con `id` repetido lanza `IllegalArgumentException`.
- `buscar` de un `id` inexistente devuelve `null`; `eliminar` devuelve `false`.
