package miproyecto2026;

/** Pruebas de ColaInferencia (FIFO), PilaHistorial (LIFO) y su integracion. */
public class PruebasColaPila {

    private static int ok = 0;
    private static int fallos = 0;

    private static void verificar(boolean condicion, String nombre) {
        if (condicion) {
            ok++;
            System.out.println("[OK]    " + nombre);
        } else {
            fallos++;
            System.out.println("[FALLO] " + nombre);
        }
    }

    private static Solicitud s(String id) {
        return new Solicitud(id, "texto " + id);
    }

    private static Prediccion p(String id) {
        return new Prediccion(s(id), "intent_" + id, 0.9);
    }

    // ---------- COLA ----------
    static void pruebaColaVacia() {
        ColaInferencia c = new ColaInferencia();
        verificar(c.estaVacia() && c.cantidad() == 0, "cola vacia: estaVacia y cantidad 0");
        boolean lanzo = false;
        try { c.desencolar(); } catch (IllegalStateException e) { lanzo = true; }
        verificar(lanzo, "cola vacia: desencolar lanza IllegalStateException");
        lanzo = false;
        try { c.frente(); } catch (IllegalStateException e) { lanzo = true; }
        verificar(lanzo, "cola vacia: frente lanza IllegalStateException");
    }

    static void pruebaColaFifo() {
        ColaInferencia c = new ColaInferencia();
        c.encolar(s("1"));
        c.encolar(s("2"));
        c.encolar(s("3"));
        verificar(c.cantidad() == 3, "cola: cantidad 3 tras encolar 3");
        verificar(c.frente().getId().equals("1"), "cola: frente es el primero en llegar");
        verificar(c.desencolar().getId().equals("1"), "cola FIFO: sale 1");
        verificar(c.desencolar().getId().equals("2"), "cola FIFO: sale 2");
        verificar(c.desencolar().getId().equals("3"), "cola FIFO: sale 3");
        verificar(c.estaVacia() && c.cantidad() == 0, "cola: queda vacia tras sacar todo");
    }

    static void pruebaColaReutilizable() {
        ColaInferencia c = new ColaInferencia();
        c.encolar(s("1"));
        c.desencolar();
        c.encolar(s("2")); // fin debia reiniciarse al vaciarse
        verificar(c.cantidad() == 1 && c.frente().getId().equals("2"),
                "cola: se puede encolar tras vaciarse (fin se reinicia)");
        c.encolar(s("3"));
        verificar(c.desencolar().getId().equals("2") && c.desencolar().getId().equals("3"),
                "cola: orden correcto tras reutilizarse");
    }

    static void pruebaColaNull() {
        ColaInferencia c = new ColaInferencia();
        boolean lanzo = false;
        try { c.encolar(null); } catch (IllegalArgumentException e) { lanzo = true; }
        verificar(lanzo && c.cantidad() == 0, "cola: encolar(null) lanza excepcion y no cambia nada");
    }

    // ---------- PILA ----------
    static void pruebaPilaVacia() {
        PilaHistorial h = new PilaHistorial();
        verificar(h.estaVacia() && h.cantidad() == 0, "pila vacia: estaVacia y cantidad 0");
        boolean lanzo = false;
        try { h.desapilar(); } catch (IllegalStateException e) { lanzo = true; }
        verificar(lanzo, "pila vacia: desapilar lanza IllegalStateException");
        lanzo = false;
        try { h.tope(); } catch (IllegalStateException e) { lanzo = true; }
        verificar(lanzo, "pila vacia: tope lanza IllegalStateException");
    }

    static void pruebaPilaLifo() {
        PilaHistorial h = new PilaHistorial();
        h.apilar(p("1"));
        h.apilar(p("2"));
        h.apilar(p("3"));
        verificar(h.cantidad() == 3, "pila: cantidad 3 tras apilar 3");
        verificar(h.tope().getSolicitud().getId().equals("3"), "pila: tope es el ultimo apilado");
        verificar(h.desapilar().getSolicitud().getId().equals("3"), "pila LIFO: sale 3");
        verificar(h.desapilar().getSolicitud().getId().equals("2"), "pila LIFO: sale 2");
        verificar(h.desapilar().getSolicitud().getId().equals("1"), "pila LIFO: sale 1");
        verificar(h.estaVacia(), "pila: queda vacia tras sacar todo");
    }

    static void pruebaPilaNull() {
        PilaHistorial h = new PilaHistorial();
        boolean lanzo = false;
        try { h.apilar(null); } catch (IllegalArgumentException e) { lanzo = true; }
        verificar(lanzo && h.cantidad() == 0, "pila: apilar(null) lanza excepcion y no cambia nada");
    }

    // ---------- INTEGRACION (pipeline de inferencia) ----------
    static void pruebaIntegracion() {
        ColaInferencia cola = new ColaInferencia();
        PilaHistorial historial = new PilaHistorial();

        cola.encolar(new Solicitud("a", "pon una alarma a las 7"));
        cola.encolar(new Solicitud("b", "que clima hace hoy"));
        cola.encolar(new Solicitud("c", "reproduce musica"));

        // Se procesan en orden de llegada; cada resultado va al historial.
        while (!cola.estaVacia()) {
            Solicitud atendida = cola.desencolar();
            historial.apilar(new Prediccion(atendida, "intent_de_" + atendida.getId(), 0.95));
        }

        verificar(cola.estaVacia(), "integracion: la cola quedo sin pendientes");
        verificar(historial.cantidad() == 3, "integracion: el historial tiene 3 predicciones");
        verificar(historial.tope().getSolicitud().getId().equals("c"),
                "integracion: la ultima atendida (c) esta arriba del historial");
        historial.desapilar();
        verificar(historial.tope().getSolicitud().getId().equals("b"),
                "integracion: deshacer devuelve la anterior (b)");
    }

    public static void main(String[] args) {
        pruebaColaVacia();
        pruebaColaFifo();
        pruebaColaReutilizable();
        pruebaColaNull();
        pruebaPilaVacia();
        pruebaPilaLifo();
        pruebaPilaNull();
        pruebaIntegracion();

        System.out.println("\nResultado: " + ok + " correctas, " + fallos + " fallidas");
        if (fallos > 0) {
            System.exit(1);
        }
    }
}
