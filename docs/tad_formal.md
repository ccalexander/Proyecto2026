# TAD RepositorioMuestras (especificación formal)

```
TAD RepositorioMuestras

Datos:
    M : conjunto finito de Muestra, con claves id únicas
    cantidad : entero

Invariantes:
    I1: cantidad >= 0
    I2: para todo m1, m2 en M: m1 != m2  =>  m1.id != m2.id
    I3: cantidad == |M|
    I4: cantidad == 0  <=>  inicio == null

Operaciones:

    crear() -> RepositorioMuestras
        Pre:  true
        Post: M' = {} y cantidad' = 0

    insertar(m : Muestra) -> void
        Pre:  m != null  y  no existe(m.id)
        Post: existe(m.id)  y  cantidad' = cantidad + 1
              y  M' = M U {m}

    eliminar(id : String) -> boolean
        Pre:  id != null
        Post: si existe(id):   retorna true,  no existe'(id),  cantidad' = cantidad - 1
              si no existe(id): retorna false, M' = M, cantidad' = cantidad

    buscar(id : String) -> Muestra
        Pre:  id != null
        Post: si existe(id) retorna la m en M con m.id = id; si no, retorna null
              M' = M  (no modifica el estado)

    existe(id : String) -> boolean
        Pre:  id != null
        Post: retorna (buscar(id) != null);  M' = M

    cantidad() -> entero
        Pre:  true
        Post: retorna |M|;  M' = M

    estaVacio() -> boolean
        Pre:  true
        Post: retorna (cantidad == 0);  M' = M

    recorrer() -> void
        Pre:  true
        Post: visita cada muestra de M exactamente una vez;  M' = M

    vaciar() -> void
        Pre:  true
        Post: M' = {}  y  cantidad' = 0
```

## Implementación elegida
Lista simplemente enlazada con nodos propios (`Nodo`), referencia `inicio` y
contador `cantidad`. No se usa `ArrayList` ni `LinkedList`.
