package miproyecto2026;

/**
 * Pruebas del TAD RepositorioMuestras (lista simplemente enlazada).
 * No requiere JUnit: ejecutar con Shift+F6 en NetBeans.
 */
public class PruebasRepositorioMuestras {

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

    private static Muestra m(String id) {
        return new Muestra(id, "texto " + id, "intent_" + id);
    }

    // ---------- lista vacia ----------
    static void pruebaListaVacia() {
        RepositorioMuestras r = new RepositorioMuestras();
        verificar(r.estaVacio(), "vacia: estaVacio() es true");
        verificar(r.cantidad() == 0, "vacia: cantidad() es 0");
        verificar(r.buscar("1") == null, "vacia: buscar devuelve null");
        verificar(!r.eliminar("1"), "vacia: eliminar devuelve false");
        verificar(!r.existe("1"), "vacia: existe devuelve false");
        r.recorrer(); // no debe lanzar excepcion
        verificar(true, "vacia: recorrer no falla");
    }

    // ---------- insertar ----------
    static void pruebaInsertar() {
        RepositorioMuestras r = new RepositorioMuestras();
        r.insertar(m("1"));
        verificar(r.cantidad() == 1, "insertar: cantidad pasa a 1");
        verificar(!r.estaVacio(), "insertar: ya no esta vacia");
        verificar(r.existe("1"), "insertar: existe(id) es true (postcondicion)");
        r.insertar(m("2"));
        r.insertar(m("3"));
        verificar(r.cantidad() == 3, "insertar: cantidad pasa a 3");
    }

    static void pruebaInsertarNull() {
        RepositorioMuestras r = new RepositorioMuestras();
        boolean lanzo = false;
        try {
            r.insertar(null);
        } catch (IllegalArgumentException e) {
            lanzo = true;
        }
        verificar(lanzo, "insertar(null) lanza IllegalArgumentException");
        verificar(r.cantidad() == 0, "insertar(null) no cambia la cantidad");
    }

    static void pruebaInsertarDuplicado() {
        RepositorioMuestras r = new RepositorioMuestras();
        r.insertar(m("1"));
        boolean lanzo = false;
        try {
            r.insertar(m("1"));
        } catch (IllegalArgumentException e) {
            lanzo = true;
        }
        verificar(lanzo, "insertar id repetido lanza IllegalArgumentException");
        verificar(r.cantidad() == 1, "insertar id repetido no cambia la cantidad");
    }

    // ---------- buscar ----------
    static void pruebaBuscar() {
        RepositorioMuestras r = new RepositorioMuestras();
        Muestra a = m("A");
        r.insertar(a);
        r.insertar(m("B"));
        r.insertar(m("C"));
        verificar(r.buscar("A") == a, "buscar: encuentra el ultimo nodo (A)");
        verificar(r.buscar("C") != null, "buscar: encuentra el primer nodo (C)");
        verificar(r.buscar("Z") == null, "buscar: id inexistente devuelve null");
        verificar(r.cantidad() == 3, "buscar: no modifica la cantidad");
    }

    // ---------- eliminar ----------
    static void pruebaEliminar() {
        RepositorioMuestras r = new RepositorioMuestras();
        r.insertar(m("1"));
        r.insertar(m("2"));
        r.insertar(m("3")); // orden en la lista: 3 -> 2 -> 1

        verificar(r.eliminar("3"), "eliminar: primer nodo devuelve true");
        verificar(!r.existe("3") && r.cantidad() == 2, "eliminar primero: ya no existe, cantidad 2");

        r.insertar(m("4")); // 4 -> 2 -> 1
        verificar(r.eliminar("2"), "eliminar: nodo del medio devuelve true");
        verificar(!r.existe("2") && r.cantidad() == 2, "eliminar medio: ya no existe, cantidad 2");

        verificar(r.eliminar("1"), "eliminar: ultimo nodo devuelve true");
        verificar(!r.existe("1") && r.cantidad() == 1, "eliminar ultimo: ya no existe, cantidad 1");

        verificar(!r.eliminar("99"), "eliminar: id inexistente devuelve false");
        verificar(r.cantidad() == 1, "eliminar inexistente: cantidad no cambia");

        verificar(r.eliminar("4"), "eliminar: unico nodo devuelve true");
        verificar(r.estaVacio() && r.cantidad() == 0, "eliminar todo: vuelve a quedar vacia (invariante)");
    }

    static void pruebaReinsertarTrasEliminar() {
        RepositorioMuestras r = new RepositorioMuestras();
        r.insertar(m("1"));
        r.eliminar("1");
        r.insertar(m("1"));
        verificar(r.existe("1") && r.cantidad() == 1, "reinsertar un id eliminado es valido");
    }

    // ---------- vaciar ----------
    static void pruebaVaciar() {
        RepositorioMuestras r = new RepositorioMuestras();
        r.insertar(m("1"));
        r.insertar(m("2"));
        r.vaciar();
        verificar(r.estaVacio() && r.cantidad() == 0, "vaciar: queda vacia con cantidad 0");
        verificar(r.buscar("1") == null, "vaciar: buscar devuelve null");
    }

    // ---------- carga grande ----------
    static void pruebaCargaGrande() {
        RepositorioMuestras r = new RepositorioMuestras();
        int n = 1000;
        for (int i = 0; i < n; i++) {
            r.insertar(m(String.valueOf(i)));
        }
        verificar(r.cantidad() == n, "carga: 1000 muestras insertadas");
        verificar(r.existe("0") && r.existe("999"), "carga: existen el primero y el ultimo");
        for (int i = 0; i < n; i += 2) {
            r.eliminar(String.valueOf(i));
        }
        verificar(r.cantidad() == 500, "carga: tras eliminar los pares quedan 500");
        verificar(!r.existe("10") && r.existe("11"), "carga: pares eliminados, impares presentes");
    }

    public static void main(String[] args) {
        pruebaListaVacia();
        pruebaInsertar();
        pruebaInsertarNull();
        pruebaInsertarDuplicado();
        pruebaBuscar();
        pruebaEliminar();
        pruebaReinsertarTrasEliminar();
        pruebaVaciar();
        pruebaCargaGrande();

        System.out.println("\nResultado: " + ok + " correctas, " + fallos + " fallidas");
        if (fallos > 0) {
            System.exit(1);
        }
    }
}
