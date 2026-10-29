package miproyecto2026;

/** Nodo reutilizable para la pila y la cola (dato + referencia al siguiente). */
public class NodoGenerico<T> {
    public T dato;
    public NodoGenerico<T> siguiente;

    public NodoGenerico(T dato) {
        this.dato = dato;
        this.siguiente = null;
    }
}
