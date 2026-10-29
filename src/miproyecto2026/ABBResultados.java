package miproyecto2026;

/**
 * Arbol binario de busqueda (sin balanceo) que indexa predicciones por id de solicitud.
 * Se usa como linea base para comparar con el AVL.
 *
 * Invariante: para todo nodo, las claves del subarbol izquierdo son menores y las del
 * derecho son mayores (orden segun String.compareTo). Las claves son unicas.
 */
public class ABBResultados {
    private NodoArbol raiz;
    private int cantidad;

    public ABBResultados() {
        this.raiz = null;
        this.cantidad = 0;
    }

    public boolean estaVacio() { return raiz == null; }

    public int cantidad() { return cantidad; }

    /** Pre: p != null y no existe el id. Post: existe(id), cantidad' = cantidad + 1. */
    public void insertar(Prediccion p) {
        if (p == null) {
            throw new IllegalArgumentException("La prediccion no puede ser null");
        }
        String id = p.getSolicitud().getId();
        if (existe(id)) {
            throw new IllegalArgumentException("Ya existe una prediccion con id " + id);
        }
        raiz = insertarRec(raiz, p);
        cantidad++;
    }

    private NodoArbol insertarRec(NodoArbol n, Prediccion p) {
        if (n == null) return new NodoArbol(p);
        if (p.getSolicitud().getId().compareTo(n.clave()) < 0) {
            n.izq = insertarRec(n.izq, p);
        } else {
            n.der = insertarRec(n.der, p);
        }
        return n;
    }

    public Prediccion buscar(String id) {
        NodoArbol actual = raiz;
        while (actual != null) {
            int c = id.compareTo(actual.clave());
            if (c == 0) return actual.dato;
            actual = (c < 0) ? actual.izq : actual.der;
        }
        return null;
    }

    public boolean existe(String id) { return buscar(id) != null; }

    public boolean eliminar(String id) {
        if (!existe(id)) return false;
        raiz = eliminarRec(raiz, id);
        cantidad--;
        return true;
    }

    private NodoArbol eliminarRec(NodoArbol n, String id) {
        if (n == null) return null;
        int c = id.compareTo(n.clave());
        if (c < 0) {
            n.izq = eliminarRec(n.izq, id);
        } else if (c > 0) {
            n.der = eliminarRec(n.der, id);
        } else {
            if (n.izq == null) return n.der;   // 0 hijos o solo derecho
            if (n.der == null) return n.izq;   // solo izquierdo
            NodoArbol sucesor = n.der;          // 2 hijos: menor del subarbol derecho
            while (sucesor.izq != null) sucesor = sucesor.izq;
            n.dato = sucesor.dato;
            n.der = eliminarRec(n.der, sucesor.clave());
        }
        return n;
    }

    /** Altura real del arbol (vacio = 0). Recorre todo el arbol: O(n). */
    public int altura() { return alturaRec(raiz); }

    private int alturaRec(NodoArbol n) {
        if (n == null) return 0;
        return 1 + Math.max(alturaRec(n.izq), alturaRec(n.der));
    }

    /** Ids en orden ascendente, en un arreglo. */
    public String[] ids() {
        String[] res = new String[cantidad];
        llenar(raiz, res, new int[]{0});
        return res;
    }

    private void llenar(NodoArbol n, String[] res, int[] pos) {
        if (n == null) return;
        llenar(n.izq, res, pos);
        res[pos[0]++] = n.clave();
        llenar(n.der, res, pos);
    }

    public void recorrerInorden() { inorden(raiz); }

    private void inorden(NodoArbol n) {
        if (n == null) return;
        inorden(n.izq);
        System.out.println(n.dato);
        inorden(n.der);
    }
}
