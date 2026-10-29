package miproyecto2026;

import java.util.Random;
import java.util.TreeMap;

/** Pruebas de ABBResultados y AVLResultados (indice por id de solicitud). */
public class PruebasArboles {

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

    private static Prediccion p(String id) {
        return new Prediccion(new Solicitud(id, "texto " + id), "intent_" + id, 0.9);
    }

    private static boolean excepcion(Runnable r) {
        try { r.run(); } catch (IllegalArgumentException e) { return true; }
        return false;
    }

    // ================= ABB =================
    static void pruebasABB() {
        ABBResultados t = new ABBResultados();
        verificar(t.estaVacio() && t.cantidad() == 0 && t.altura() == 0, "ABB vacio: estaVacio, cantidad 0, altura 0");
        verificar(t.buscar("x") == null && !t.eliminar("x"), "ABB vacio: buscar null y eliminar false");

        for (String id : new String[]{"m", "f", "t", "b", "h", "p", "z"}) t.insertar(p(id));
        verificar(t.cantidad() == 7, "ABB: 7 inserciones");
        verificar(String.join("", t.ids()).equals("bfhmptz"), "ABB: inorden ordenado");
        verificar(t.buscar("h") != null && t.buscar("q") == null, "ABB: buscar presente y ausente");
        verificar(excepcion(() -> t.insertar(p("m"))), "ABB: id duplicado lanza excepcion");
        verificar(excepcion(() -> t.insertar(null)), "ABB: insertar(null) lanza excepcion");

        verificar(t.eliminar("b") && String.join("", t.ids()).equals("fhmptz"), "ABB eliminar hoja");
        t.insertar(p("a"));                       // f.izq = a (hijo unico de f)
        verificar(t.eliminar("f") && String.join("", t.ids()).equals("ahmptz"), "ABB eliminar nodo con un hijo");
        verificar(t.eliminar("m") && String.join("", t.ids()).equals("ahptz"), "ABB eliminar raiz con dos hijos");
        verificar(!t.eliminar("m") && t.cantidad() == 5, "ABB eliminar inexistente: false y cantidad igual");

        ABBResultados lineal = new ABBResultados();
        for (int i = 0; i < 1000; i++) lineal.insertar(p(String.format("%04d", i)));
        verificar(lineal.altura() == 1000, "ABB degenerado: ids ordenados dan altura 1000 (como lista)");
    }

    // ================= AVL: rotaciones =================
    static void pruebasRotaciones() {
        AVLResultados t = new AVLResultados();
        t.insertar(p("3")); t.insertar(p("2")); t.insertar(p("1"));
        verificar("2".equals(t.raizId()) && t.getRotaciones() == 1 && t.esAVLValido(), "AVL caso izq-izq: rotacion simple derecha");

        t = new AVLResultados();
        t.insertar(p("1")); t.insertar(p("2")); t.insertar(p("3"));
        verificar("2".equals(t.raizId()) && t.getRotaciones() == 1 && t.esAVLValido(), "AVL caso der-der: rotacion simple izquierda");

        t = new AVLResultados();
        t.insertar(p("3")); t.insertar(p("1")); t.insertar(p("2"));
        verificar("2".equals(t.raizId()) && t.getRotaciones() == 2 && t.esAVLValido(), "AVL caso izq-der: rotacion doble");

        t = new AVLResultados();
        t.insertar(p("1")); t.insertar(p("3")); t.insertar(p("2"));
        verificar("2".equals(t.raizId()) && t.getRotaciones() == 2 && t.esAVLValido(), "AVL caso der-izq: rotacion doble");
    }

    // ================= AVL: operaciones =================
    static void pruebasAVL() {
        AVLResultados t = new AVLResultados();
        verificar(t.estaVacio() && t.cantidad() == 0 && t.altura() == 0 && t.esAVLValido(), "AVL vacio: valido, altura 0");
        verificar(t.buscar("x") == null && !t.eliminar("x"), "AVL vacio: buscar null y eliminar false");
        verificar(excepcion(() -> t.insertar(null)), "AVL: insertar(null) lanza excepcion");

        t.insertar(p("10"));
        verificar(excepcion(() -> t.insertar(p("10"))), "AVL: id duplicado lanza excepcion");
        verificar(t.cantidad() == 1, "AVL: duplicado no cambia la cantidad");

        AVLResultados u = new AVLResultados();
        for (int i = 1; i <= 7; i++) u.insertar(p(String.valueOf(i)));
        verificar("4".equals(u.raizId()) && u.altura() == 3, "AVL: 1..7 queda perfectamente balanceado (raiz 4, altura 3)");
        verificar(String.join("", u.ids()).equals("1234567"), "AVL: inorden ordenado");

        verificar(u.eliminar("1") && u.esAVLValido(), "AVL eliminar hoja sigue valido");
        verificar(u.eliminar("4") && u.esAVLValido() && !u.existe("4"), "AVL eliminar raiz (dos hijos) sigue valido");
        verificar(u.eliminar("2") && u.eliminar("3") && u.esAVLValido(), "AVL eliminar con rotaciones sigue valido");
        verificar(u.cantidad() == 3 && String.join("", u.ids()).equals("567"), "AVL: quedan 5,6,7");
        verificar(!u.eliminar("99") && u.cantidad() == 3, "AVL eliminar inexistente: false");
        u.eliminar("5"); u.eliminar("6"); u.eliminar("7");
        verificar(u.estaVacio() && u.altura() == 0, "AVL: vaciado completo vuelve a estar vacio");
    }

    // ================= AVL vs ABB, carga y aleatorio =================
    static void pruebaCarga() {
        int n = 1000;
        ABBResultados abb = new ABBResultados();
        AVLResultados avl = new AVLResultados();
        for (int i = 0; i < n; i++) {
            String id = String.format("%04d", i);
            abb.insertar(p(id));
            avl.insertar(p(id));
        }
        System.out.println("   altura con " + n + " ids ordenados -> ABB: " + abb.altura() + ", AVL: " + avl.altura());
        verificar(abb.altura() == n, "carga: ABB degenerado tiene altura n");
        verificar(avl.altura() <= 14 && avl.esAVLValido(), "carga: AVL tiene altura <= 14 (~log2 n) y es valido");
        verificar(avl.getRotaciones() > 0, "carga: el AVL hizo rotaciones");
    }

    static void pruebaAleatoria() {
        Random rnd = new Random(2026);
        AVLResultados avl = new AVLResultados();
        TreeMap<String, Boolean> ref = new TreeMap<>(); // solo como referencia comparativa
        boolean consistente = true;
        boolean valido = true;
        for (int i = 0; i < 3000; i++) {
            String id = String.valueOf(rnd.nextInt(500));
            if (rnd.nextBoolean()) {
                if (!ref.containsKey(id)) {
                    avl.insertar(p(id));
                    ref.put(id, true);
                }
            } else {
                boolean e1 = avl.eliminar(id);
                boolean e2 = ref.remove(id) != null;
                if (e1 != e2) consistente = false;
            }
            if (avl.cantidad() != ref.size()) consistente = false;
            if (i % 100 == 0 && !avl.esAVLValido()) valido = false;
        }
        String[] ids = avl.ids();
        int k = 0;
        for (String s : ref.keySet()) {
            if (k >= ids.length || !ids[k++].equals(s)) consistente = false;
        }
        verificar(consistente, "aleatorio: AVL coincide con TreeMap (3000 operaciones)");
        verificar(valido && avl.esAVLValido(), "aleatorio: AVL valido durante y al final");
    }

    // ================= Integracion con el pipeline =================
    static void pruebaIntegracion() {
        ColaInferencia cola = new ColaInferencia();
        PilaHistorial historial = new PilaHistorial();
        AVLResultados indice = new AVLResultados();

        String[] ids = {"q7", "q2", "q9", "q4"};
        for (String id : ids) cola.encolar(new Solicitud(id, "texto " + id));

        while (!cola.estaVacia()) {
            Solicitud s = cola.desencolar();
            Prediccion pr = new Prediccion(s, "intent_" + s.getId(), 0.9);
            historial.apilar(pr);
            indice.insertar(pr);
        }
        verificar(indice.cantidad() == 4 && historial.cantidad() == 4, "integracion: cada solicitud queda en historial e indice");
        verificar(indice.buscar("q9") != null && indice.buscar("q9").getIntent().equals("intent_q9"),
                "integracion: se consulta el resultado de una solicitud por su id");
        verificar(historial.tope().getSolicitud().getId().equals("q4"), "integracion: el historial conserva el orden de atencion");
    }

    public static void main(String[] args) {
        pruebasABB();
        pruebasRotaciones();
        pruebasAVL();
        pruebaCarga();
        pruebaAleatoria();
        pruebaIntegracion();
        System.out.println("\nResultado: " + ok + " correctas, " + fallos + " fallidas");
        if (fallos > 0) System.exit(1);
    }
}
