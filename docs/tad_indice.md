# TAD IndiceResultados (ABB / AVL)

## Propósito
Indexar las predicciones del Transformer **por id de solicitud**, para recuperar
el resultado de una consulta concreta sin recorrer todo el historial.

```
Solicitud -> ColaInferencia -> Transformer -> Prediccion -+-> PilaHistorial
                                                          +-> AVLResultados (indice por id)
```

La pila responde "¿cuál fue la última predicción?"; el índice responde
"¿qué se predijo para la solicitud X?".

## Clave y orden
- Clave: `id` de la solicitud (`String`), comparada con `String.compareTo`.
- Orden **lexicográfico**: "10" < "2". Es consistente para el índice (no afecta la
  búsqueda), pero el recorrido inorden no sigue el orden numérico. Si se quisiera
  orden numérico, habría que usar ids con ceros a la izquierda ("0002").

## Especificación formal

```
TAD IndiceResultados

Datos:    A : conjunto de Prediccion con clave id unica, cantidad : entero

Invariantes:
    I1: cantidad == |A|
    I2: ABB: para todo nodo n, claves(n.izq) < clave(n) < claves(n.der)
    I3: (solo AVL) |altura(n.izq) - altura(n.der)| <= 1 para todo nodo n
    I4: (solo AVL) altura(n) = 1 + max(altura(n.izq), altura(n.der)); altura(null) = 0

insertar(p : Prediccion)
    Pre:  p != null  y  no existe(p.solicitud.id)
    Post: existe(p.solicitud.id); cantidad' = cantidad + 1; se mantienen I2 (e I3, I4 en el AVL)

buscar(id : String) -> Prediccion
    Pre:  id != null
    Post: retorna la prediccion con ese id, o null si no existe; el indice no cambia

existe(id : String) -> boolean
    Post: retorna (buscar(id) != null)

eliminar(id : String) -> boolean
    Pre:  id != null
    Post: si existia: retorna true, no existe'(id), cantidad' = cantidad - 1
          si no:      retorna false, A' = A
          se mantienen I2 (e I3, I4 en el AVL)

cantidad() -> entero     Post: retorna |A|
estaVacio() -> boolean   Post: retorna (cantidad == 0)
altura() -> entero       Post: altura del arbol (vacio = 0)
ids() -> String[]        Post: arreglo con todos los ids en orden ascendente
```

## Errores controlados
- `insertar(null)` o `insertar` con id repetido: `IllegalArgumentException`.
- `buscar` inexistente: `null`. `eliminar` inexistente: `false`.

## Balanceo del AVL
Tras cada inserción o eliminación se recalcula la altura de cada nodo del camino
y se aplica una de cuatro rotaciones cuando el factor de balance sale de {-1, 0, 1}:

| Caso               | Condición                          | Solución                     |
|--------------------|------------------------------------|------------------------------|
| Izquierda-izquierda| factor(n) > 1 y factor(n.izq) >= 0 | rotación derecha             |
| Derecha-derecha    | factor(n) < -1 y factor(n.der) <= 0| rotación izquierda           |
| Izquierda-derecha  | factor(n) > 1 y factor(n.izq) < 0  | rotar izq. el hijo + rot. der.|
| Derecha-izquierda  | factor(n) < -1 y factor(n.der) > 0 | rotar der. el hijo + rot. izq.|

Eliminación de un nodo con dos hijos: se reemplaza por su **sucesor inorden**
(el menor del subárbol derecho) y luego se elimina ese sucesor.
