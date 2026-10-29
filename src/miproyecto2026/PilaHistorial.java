package miproyecto2026;

/**
 * Pila LIFO con el historial de predicciones (la mas reciente arriba), con nodos propios.
 *
 * Invariantes:
 *  - cantidad >= 0
 *  - cantidad == 0  <=>  tope == null
 */
public class PilaHistorial {
    private NodoGenerico<Prediccion> tope;
    private int cantidad;

    public PilaHistorial() {
        this.tope = null;
        this.cantidad = 0;
    }

    public boolean estaVacia() { return tope == null; }

    public int cantidad() { return cantidad; }

    /** Pre: p != null. Post: tope' = p, cantidad' = cantidad + 1. */
    public void apilar(Prediccion p) {
        if (p == null) {
            throw new IllegalArgumentException("La prediccion no puede ser null");
        }
        NodoGenerico<Prediccion> nuevo = new NodoGenerico<>(p);
        nuevo.siguiente = tope;
        tope = nuevo;
        cantidad++;
    }

    /** Pre: !estaVacia(). Post: retorna y quita la ultima prediccion, cantidad' = cantidad - 1. */
    public Prediccion desapilar() {
        if (estaVacia()) {
            throw new IllegalStateException("El historial esta vacio");
        }
        Prediccion p = tope.dato;
        tope = tope.siguiente;
        cantidad--;
        return p;
    }

    /** Pre: !estaVacia(). Retorna la ultima prediccion sin quitarla. */
    public Prediccion tope() {
        if (estaVacia()) {
            throw new IllegalStateException("El historial esta vacio");
        }
        return tope.dato;
    }

    /** Recorre de la mas reciente a la mas antigua. */
    public void recorrer() {
        NodoGenerico<Prediccion> actual = tope;
        while (actual != null) {
            System.out.println(actual.dato);
            actual = actual.siguiente;
        }
    }
}
