package miproyecto2026;

/**
 * TAD RepositorioMuestras implementado con lista simplemente enlazada.
 *
 * Invariantes:
 *  - cantidad >= 0
 *  - los IDs son unicos
 *  - cantidad == 0  <=>  inicio == null
 */
public class RepositorioMuestras {
    private Nodo inicio;
    private int cantidad;

    public RepositorioMuestras() {
        this.inicio = null;
        this.cantidad = 0;
    }

    public boolean estaVacio() {
        return this.inicio == null;
    }

    public int cantidad() {
        return this.cantidad;
    }

    /** Pre: m != null y no existe m.id. Post: existe(m.id), cantidad' = cantidad + 1. */
    public void insertar(Muestra m) {
        if (m == null) {
            throw new IllegalArgumentException("La muestra no puede ser null");
        }
        if (existe(m.getId())) {
            throw new IllegalArgumentException("Ya existe una muestra con id " + m.getId());
        }
        Nodo nuevo = new Nodo(m);
        nuevo.siguiente = inicio;
        inicio = nuevo;
        cantidad++;
    }

    public boolean existe(String id) {
        return buscar(id) != null;
    }

    public Muestra buscar(String id) {
        Nodo actual = inicio;
        while (actual != null) {
            if (actual.dato.getId().equals(id)) {
                return actual.dato;
            }
            actual = actual.siguiente;
        }
        return null;
    }

    public void recorrer() {
        Nodo actual = inicio;
        while (actual != null) {
            System.out.println(actual.dato);
            actual = actual.siguiente;
        }
    }

    public boolean eliminar(String id) {
        if (estaVacio()) return false;
        if (inicio.dato.getId().equals(id)) {
            inicio = inicio.siguiente;
            cantidad--;
            return true;
        }
        Nodo actual = inicio;
        while (actual.siguiente != null) {
            if (actual.siguiente.dato.getId().equals(id)) {
                actual.siguiente = actual.siguiente.siguiente;
                cantidad--;
                return true;
            }
            actual = actual.siguiente;
        }
        return false;
    }

    public void vaciar() {
        inicio = null;
        cantidad = 0;
    }
}
