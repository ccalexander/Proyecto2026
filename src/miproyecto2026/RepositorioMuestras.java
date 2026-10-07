
package miproyecto2026;
public class RepositorioMuestras {
    private Nodo inicio;
    private int cantidad;

    public RepositorioMuestras() {
        this.inicio = null;
        this.cantidad = 0;
    }
    public boolean estaVacio(){
        return this.inicio==null;
    }
    public int getCantidad(){
        return this.cantidad;
    }
    public void insertar(Muestra m){
        if (m == null)return;
        Nodo nuevo = new Nodo(m);
        nuevo.siguiente = inicio;
        inicio = nuevo;
        cantidad++;
    }
    public Muestra buscar(String id){
        Nodo actual = inicio;
        while (actual != null){
            if (actual.dato.getId().equals(id)){
                return actual.dato;
            }
            actual = actual.siguiente;
        }
        return null;
    }
    public void recorrer(){
        Nodo actual = inicio;
        while (actual != null){
            System.out.println(actual.dato);
            actual = actual.siguiente;
        }
    }
    public boolean eliminar(String id){
        if (estaVacio())return false;
        if (inicio.dato.getId().equals(id)){
            inicio = inicio.siguiente;
            cantidad--;
            return true;
        }
        Nodo actual = inicio;
        while (actual.siguiente != null){
            if (actual.siguiente.dato.getId().equals(id)){
                actual.siguiente = actual.siguiente.siguiente;
                cantidad--;
                return true;
            }
            actual = actual.siguiente;
        }
        return false;
    }
    public void vaciar(){
        inicio = null;
        cantidad = 0;
    }
}
