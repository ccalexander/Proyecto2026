# TAD ColaInferencia y TAD PilaHistorial

Ambas estructuras se implementan con nodos propios (`NodoGenerico<T>`), sin usar
`Queue`, `Stack` ni `LinkedList`.

## Uso en el pipeline de inferencia

```
Usuario -> Solicitud -> ColaInferencia -> Transformer -> Prediccion -> PilaHistorial
```

- **Cola (FIFO):** las solicitudes se atienden en el orden en que llegan; si el
  modelo está ocupado, las nuevas esperan en la cola.
- **Pila (LIFO):** el historial guarda las predicciones y siempre se consulta o
  deshace primero la más reciente.

---

## TAD ColaInferencia

```
Datos:    secuencia de Solicitud (frente ... fin), cantidad : entero

Invariantes:
    I1: cantidad >= 0
    I2: cantidad == 0  <=>  frente == null  <=>  fin == null
    I3: si cantidad > 0 entonces fin.siguiente == null

encolar(s : Solicitud)
    Pre:  s != null
    Post: s es el ultimo elemento; cantidad' = cantidad + 1

desencolar() -> Solicitud
    Pre:  !estaVacia()
    Post: retorna el primero que llego; cantidad' = cantidad - 1
          si cantidad' == 0 entonces frente' == null y fin' == null

frente() -> Solicitud
    Pre:  !estaVacia()
    Post: retorna el primero sin quitarlo; la cola no cambia

estaVacia() -> boolean      Post: retorna (cantidad == 0)
cantidad()  -> entero       Post: retorna cantidad
recorrer()                  Post: visita de frente a fin; la cola no cambia
```

## TAD PilaHistorial

```
Datos:    secuencia de Prediccion (tope ... base), cantidad : entero

Invariantes:
    I1: cantidad >= 0
    I2: cantidad == 0  <=>  tope == null

apilar(p : Prediccion)
    Pre:  p != null
    Post: tope' = p; cantidad' = cantidad + 1

desapilar() -> Prediccion
    Pre:  !estaVacia()
    Post: retorna la ultima apilada; cantidad' = cantidad - 1

tope() -> Prediccion
    Pre:  !estaVacia()
    Post: retorna la ultima apilada sin quitarla; la pila no cambia

estaVacia() -> boolean      Post: retorna (cantidad == 0)
cantidad()  -> entero       Post: retorna cantidad
recorrer()                  Post: visita de la mas reciente a la mas antigua
```

## Errores controlados
- `encolar(null)` / `apilar(null)`: `IllegalArgumentException`.
- `desencolar()`, `frente()`, `desapilar()`, `tope()` sobre estructura vacia:
  `IllegalStateException`.
