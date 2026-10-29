package miproyecto2026;

/**
 * Arbol AVL que indexa predicciones por id de solicitud.
 *
 * Invariantes:
 *  - es un ABB: izquierda < nodo < derecha (String.compareTo), claves unicas
 *  - factor de balance de todo nodo en {-1, 0, 1}
 *  - altura(n) = 1 + max(altura(izq), altura(der)), con altura(null) = 0
 */
public class AVLResultados {
    private NodoArbol raiz;
    private int cantidad;
    private int rotaciones; // contador para el analisis

    public AVLResultados() {
        this.raiz = null;
        this.cantidad = 0;
        this.rotaciones = 0;
    }

    public boolean estaVacio() { return raiz == null; }

    public int cantidad() { return cantidad; }

    public int getRotaciones() { return rotaciones; }

    /** Altura del arbol en O(1) (vacio = 0). */
    public int altura() { return altura(raiz); }

    /** Id de la raiz, o null si esta vacio (util para verificar rotaciones). */
    public String raizId() { return raiz == null ? null : raiz.clave(); }

    // ---------- utilidades de balanceo ----------

    private int altura(NodoArbol n) { return n == null ? 0 : n.altura; }

    private void actualizarAltura(NodoArbol n) {
        n.altura = 1 + Math.max(altura(n.izq), altura(n.der));
    }

    private int factor(NodoArbol n) { return n == null ? 0 : altura(n.izq) - altura(n.der); }

    private NodoArbol rotarDerecha(NodoArbol y) {
        NodoArbol x = y.izq;
        y.izq = x.der;
        x.der = y;
        actualizarAltura(y);
        actualizarAltura(x);
        rotaciones++;
        return x;
    }

    private NodoArbol rotarIzquierda(NodoArbol x) {
        NodoArbol y = x.der;
        x.der = y.izq;
        y.izq = x;
        actualizarAltura(x);
        actualizarAltura(y);
        rotaciones++;
        return y;
    }

    private NodoArbol balancear(NodoArbol n) {
        actualizarAltura(n);
        int f = factor(n);
        if (f > 1) {                               // desbalance a la izquierda
            if (factor(n.izq) < 0) {               // caso izquierda-derecha
                n.izq = rotarIzquierda(n.izq);
            }
            return rotarDerecha(n);
        }
        if (f < -1) {                              // desbalance a la derecha
            if (factor(n.der) > 0) {               // caso derecha-izquierda
                n.der = rotarDerecha(n.der);
            }
            return rotarIzquierda(n);
        }
        return n;
    }

    // ---------- operaciones del TAD ----------

    /** Pre: p != null y no existe el id. Post: existe(id), cantidad' = cantidad + 1, sigue siendo AVL. */
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
        return balancear(n);
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

    /** Post: si existia, deja de existir, cantidad' = cantidad - 1 y sigue siendo AVL. */
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
            if (n.izq == null) return n.der;
            if (n.der == null) return n.izq;
            NodoArbol sucesor = n.der;
            while (sucesor.izq != null) sucesor = sucesor.izq;
            n.dato = sucesor.dato;
            n.der = eliminarRec(n.der, sucesor.clave());
        }
        return balancear(n);
    }

    // ---------- consulta y verificacion ----------

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

    /** Comprueba orden BST, alturas almacenadas y balance de todo el arbol. O(n). */
    public boolean esAVLValido() {
        return validar(raiz, null, null) >= 0;
    }

    /** Retorna la altura real o -1 si hay alguna violacion. */
    private int validar(NodoArbol n, String min, String max) {
        if (n == null) return 0;
        String k = n.clave();
        if (min != null && k.compareTo(min) <= 0) return -1;
        if (max != null && k.compareTo(max) >= 0) return -1;
        int hi = validar(n.izq, min, k);
        int hd = validar(n.der, k, max);
        if (hi < 0 || hd < 0) return -1;
        if (Math.abs(hi - hd) > 1) return -1;
        int h = 1 + Math.max(hi, hd);
        if (n.altura != h) return -1;
        return h;
    }
}
