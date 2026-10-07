
package miproyecto2026;

public class NodoDoble {
    public Muestra dato;
    public NodoDoble siguiente;
    public NodoDoble anterior;

    public NodoDoble(Muestra dato) {
        this.dato = dato;
        this.siguiente = null;
        this.anterior = null;
    }
}