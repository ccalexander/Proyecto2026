
package miproyecto2026;

public class RepositorioDoble {
    private NodoDoble inicio;
    private NodoDoble fin;
    private int cantidad;

    public RepositorioDoble() {
        this.inicio = null;
        this.fin = null;
        this.cantidad = 0;
    }

    public boolean estaVacio() {
        return this.inicio == null;
    }

    public int getCantidad() {
        return this.cantidad;
    }

    public void insertar(Muestra m) {
        if (m == null) return;
        NodoDoble nuevo = new NodoDoble(m);
        
        if (estaVacio()) {
            inicio = nuevo;
            fin = nuevo;
        } else {
            fin.siguiente = nuevo;
            nuevo.anterior = fin;
            fin = nuevo;
        }
        cantidad++;
    }

    public void recorrerAdelante() {
        NodoDoble actual = inicio;
        while (actual != null) {
            System.out.println(actual.dato);
            actual = actual.siguiente;
        }
    }

    public void recorrerAtras() {
        NodoDoble actual = fin;
        while (actual != null) {
            System.out.println(actual.dato);
            actual = actual.anterior;
        }
    }
}
