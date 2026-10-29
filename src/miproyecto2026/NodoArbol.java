package miproyecto2026;

/** Nodo del ABB/AVL de resultados. La clave es el id de la solicitud. */
public class NodoArbol {
    public Prediccion dato;
    public NodoArbol izq;
    public NodoArbol der;
    public int altura; // altura del subarbol (hoja = 1); la usa el AVL

    public NodoArbol(Prediccion dato) {
        this.dato = dato;
        this.izq = null;
        this.der = null;
        this.altura = 1;
    }

    public String clave() {
        return dato.getSolicitud().getId();
    }
}
