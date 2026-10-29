package miproyecto2026;

/**
 * Cola FIFO de solicitudes pendientes de inferencia, con nodos propios.
 *
 * Invariantes:
 *  - cantidad >= 0
 *  - cantidad == 0  <=>  frente == null  <=>  fin == null
 *  - si cantidad > 0, fin.siguiente == null
 */
public class ColaInferencia {
    private NodoGenerico<Solicitud> frente;
    private NodoGenerico<Solicitud> fin;
    private int cantidad;

    public ColaInferencia() {
        this.frente = null;
        this.fin = null;
        this.cantidad = 0;
    }

    public boolean estaVacia() { return frente == null; }

    public int cantidad() { return cantidad; }

    /** Pre: s != null. Post: s queda al final de la cola, cantidad' = cantidad + 1. */
    public void encolar(Solicitud s) {
        if (s == null) {
            throw new IllegalArgumentException("La solicitud no puede ser null");
        }
        NodoGenerico<Solicitud> nuevo = new NodoGenerico<>(s);
        if (estaVacia()) {
            frente = nuevo;
        } else {
            fin.siguiente = nuevo;
        }
        fin = nuevo;
        cantidad++;
    }

    /** Pre: !estaVacia(). Post: retorna y quita la solicitud mas antigua, cantidad' = cantidad - 1. */
    public Solicitud desencolar() {
        if (estaVacia()) {
            throw new IllegalStateException("La cola de inferencia esta vacia");
        }
        Solicitud s = frente.dato;
        frente = frente.siguiente;
        if (frente == null) {
            fin = null;
        }
        cantidad--;
        return s;
    }

    /** Pre: !estaVacia(). Retorna la solicitud mas antigua sin quitarla. */
    public Solicitud frente() {
        if (estaVacia()) {
            throw new IllegalStateException("La cola de inferencia esta vacia");
        }
        return frente.dato;
    }

    public void recorrer() {
        NodoGenerico<Solicitud> actual = frente;
        while (actual != null) {
            System.out.println(actual.dato);
            actual = actual.siguiente;
        }
    }
}
