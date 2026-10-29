# Análisis de complejidad: RepositorioMuestras (lista simplemente enlazada)

Sea **n** la cantidad de muestras almacenadas. La lista guarda solo la referencia
`inicio` (no hay `fin`) y un contador `cantidad`.

## Tabla resumen

| Operación      | Mejor caso | Peor caso | Justificación |
|----------------|-----------|-----------|---------------|
| `estaVacio()`  | O(1)      | O(1)      | Compara `inicio == null`. |
| `cantidad()`   | O(1)      | O(1)      | Devuelve el contador, no recorre. |
| `existe(id)`   | O(1)      | O(n)      | Usa `buscar`. Mejor caso: el id está en el primer nodo. |
| `buscar(id)`   | O(1)      | O(n)      | Recorrido lineal; peor caso: el id está al final o no existe. |
| `insertar(m)`  | O(n)      | O(n)      | La inserción al inicio es O(1), pero antes se verifica que el id no exista (`existe`), que recorre toda la lista cuando el id es nuevo. |
| `eliminar(id)` | O(1)      | O(n)      | Mejor caso: el id está en el primer nodo. Peor caso: está al final o no existe. |
| `recorrer()`   | O(n)      | O(n)      | Visita cada nodo exactamente una vez. |
| `vaciar()`     | O(1)      | O(1)      | Pone `inicio = null`; el recolector de basura libera los nodos. |

## Detalle

### insertar(m)
```
existe(m.id)        -> O(n)   (invariante: IDs únicos)
crear nodo + enlazar al inicio + cantidad++  -> O(1)
Total: O(n) + O(1) = O(n)
```
Sin la validación de unicidad sería O(1). Se paga O(n) para cumplir el
invariante del TAD. Si el id ya existe, el recorrido termina al encontrarlo y
se lanza la excepción.

### buscar(id) / eliminar(id)
Ambas recorren nodo por nodo comparando ids con `equals`:
- Mejor caso: 1 comparación → O(1).
- Peor caso: n comparaciones → O(n).
- Caso promedio (id presente en posición uniforme): ~n/2 comparaciones → O(n).

`eliminar` solo reenlaza referencias una vez encontrado el nodo (O(1)), por lo
que el costo dominante es la búsqueda.

### recorrer()
Un `while` con n iteraciones y trabajo constante por nodo (imprimir). Θ(n).

## Espacio
- Cada muestra usa un nodo extra con una referencia (`siguiente`): O(n) en total.
- Las operaciones usan solo variables auxiliares (`actual`, `nuevo`): O(1) de
  espacio adicional.

## Comparación con las alternativas

| Estructura                 | Acceso por índice | Insertar al inicio | Buscar por id |
|----------------------------|-------------------|--------------------|---------------|
| Lista enlazada (este TAD)  | O(n)              | O(1) sin validar   | O(n)          |
| Arreglo / `ArrayList`      | O(1)              | O(n)               | O(n)          |

Conclusión: la lista enlazada evita reservar memoria contigua y redimensionar,
pero la búsqueda por id es lineal. Esto justifica el índice ABB/AVL del
pipeline de inferencia, que reduce la búsqueda por clave a O(log n).

---

# Complejidad: ColaInferencia y PilaHistorial

La cola mantiene referencias a `frente` y `fin`; la pila, solo a `tope`. Por eso
ninguna operación necesita recorrer la estructura, salvo `recorrer()`.

| Operación                      | Cola       | Pila       | Justificación |
|--------------------------------|------------|------------|---------------|
| `encolar(s)` / `apilar(p)`     | O(1)       | O(1)       | Enlaza un nodo en `fin` (cola) o en `tope` (pila). |
| `desencolar()` / `desapilar()` | O(1)       | O(1)       | Avanza `frente` o `tope` una posición. |
| `frente()` / `tope()`          | O(1)       | O(1)       | Lee la referencia, no recorre. |
| `estaVacia()` / `cantidad()`   | O(1)       | O(1)       | Compara con null / lee el contador. |
| `recorrer()`                   | O(n)       | O(n)       | Visita cada nodo una vez. |

Espacio: O(n) por los nodos; las operaciones usan O(1) de espacio adicional.

La cola necesita la referencia `fin` para que `encolar` sea O(1); sin ella
habría que recorrer toda la lista (O(n)) para llegar al último nodo.

---

# Complejidad: ABB y AVL (índice por id)

Sea **n** la cantidad de predicciones y **h** la altura del árbol.

| Operación            | ABB (mejor / promedio) | ABB (peor caso) | AVL (todos los casos) |
|----------------------|------------------------|-----------------|-----------------------|
| `insertar`           | O(log n)               | O(n)            | O(log n)              |
| `buscar` / `existe`  | O(log n)               | O(n)            | O(log n)              |
| `eliminar`           | O(log n)               | O(n)            | O(log n)              |
| `altura()`           | O(n)                   | O(n)            | O(1)                  |
| `ids()` / `recorrer` | O(n)                   | O(n)            | O(n)                  |
| `esAVLValido()`      | -                      | -               | O(n)                  |

## Justificación
- Cada operación sigue un único camino desde la raíz, así que cuesta O(h).
- **ABB:** h depende del orden de llegada. Con ids ya ordenados el árbol se vuelve
  una lista y h = n, es decir O(n). Medido en las pruebas con 1000 ids ordenados:
  **altura 1000**.
- **AVL:** el balance garantiza h <= 1.44 · log2(n + 2), por lo que todo cuesta
  O(log n). Medido con los mismos 1000 ids ordenados: **altura 10**.
- Las rotaciones son O(1) (solo reasignan referencias). Insertar necesita a lo
  sumo una rotación simple o doble; eliminar puede necesitar una por nivel, es
  decir O(log n) en total, sin cambiar el orden de crecimiento.
- `insertar` y `eliminar` llaman primero a `existe` (O(log n)) para validar la
  precondición; esto no cambia el orden de complejidad.

## Espacio
O(n) por los nodos. Las operaciones son recursivas, con profundidad h:
O(log n) de pila en el AVL y O(n) en el peor caso del ABB.

## Comparación con la lista enlazada
| Estructura                  | Buscar por id |
|-----------------------------|---------------|
| Lista enlazada              | O(n)          |
| AVL                         | O(log n)      |

Con n = 1,000,000 predicciones, buscar en la lista requeriría hasta un millón de
comparaciones, y en el AVL cerca de 20.
