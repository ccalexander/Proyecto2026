package miproyecto2026;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Pruebas de CsvUtil, MatrizConfusion, ResultadosLote y del pipeline completo. */
public class PruebasEvaluacion {

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

    private static boolean cerca(double a, double b) { return Math.abs(a - b) < 1e-9; }

    // ---------- CsvUtil ----------
    static void pruebasCsv() {
        String[] f = CsvUtil.parsearLinea("1,hola mundo,greeting");
        verificar(f.length == 3 && f[1].equals("hola mundo"), "csv: linea simple");
        f = CsvUtil.parsearLinea("2,\"hola, que tal\",greeting");
        verificar(f.length == 3 && f[1].equals("hola, que tal"), "csv: coma dentro de comillas");
        f = CsvUtil.parsearLinea("3,\"dijo \"\"hola\"\"\",x");
        verificar(f[1].equals("dijo \"hola\""), "csv: comillas escapadas");
        f = CsvUtil.parsearLinea("4,,x");
        verificar(f.length == 3 && f[1].isEmpty(), "csv: campo vacio");
        verificar(CsvUtil.escapar("a,b").equals("\"a,b\"") && CsvUtil.escapar("ab").equals("ab"), "csv: escapar");
    }

    // ---------- MatrizConfusion ----------
    static MatrizConfusion matrizEjemplo() {
        MatrizConfusion mc = new MatrizConfusion(new String[]{"A", "B"});
        for (int i = 0; i < 3; i++) mc.registrar("A", "A");
        mc.registrar("A", "B");
        for (int i = 0; i < 2; i++) mc.registrar("B", "B");
        for (int i = 0; i < 2; i++) mc.registrar("B", "A");
        return mc; // [[3,1],[2,2]]
    }

    static void pruebasMatriz() {
        MatrizConfusion mc = matrizEjemplo();
        verificar(mc.get(0, 0) == 3 && mc.get(0, 1) == 1 && mc.get(1, 0) == 2 && mc.get(1, 1) == 2,
                "matriz: celdas [[3,1],[2,2]]");
        verificar(mc.total() == 8 && mc.aciertos() == 5, "matriz: total 8, aciertos 5");
        verificar(cerca(mc.accuracy(), 5.0 / 8), "matriz: accuracy 0.625");
        verificar(mc.soporte(0) == 4 && mc.soporte(1) == 4, "matriz: soporte por clase");
        verificar(cerca(mc.precision(0), 0.6) && cerca(mc.recall(0), 0.75), "matriz: precision/recall de A");
        verificar(cerca(mc.precision(1), 2.0 / 3) && cerca(mc.recall(1), 0.5), "matriz: precision/recall de B");
        double f1a = 2 * 0.6 * 0.75 / 1.35;
        double f1b = 2 * (2.0 / 3) * 0.5 / (2.0 / 3 + 0.5);
        verificar(cerca(mc.f1(0), f1a) && cerca(mc.f1(1), f1b), "matriz: F1 por clase");
        verificar(cerca(mc.f1Macro(), (f1a + f1b) / 2), "matriz: F1 macro");
        String[] conf = mc.masConfundidos(5);
        verificar(conf.length == 2 && conf[0].equals("B -> A (2)") && conf[1].equals("A -> B (1)"),
                "matriz: confusiones ordenadas por frecuencia");

        boolean lanzo = false;
        try { mc.registrar("A", "Z"); } catch (IllegalArgumentException e) { lanzo = true; }
        verificar(lanzo && mc.total() == 8, "matriz: etiqueta desconocida lanza excepcion y no cambia nada");
        lanzo = false;
        try { mc.registrar(0, 5); } catch (IllegalArgumentException e) { lanzo = true; }
        verificar(lanzo, "matriz: indice fuera de rango lanza excepcion");
        lanzo = false;
        try { new MatrizConfusion(new String[0]); } catch (IllegalArgumentException e) { lanzo = true; }
        verificar(lanzo, "matriz: sin etiquetas lanza excepcion");

        MatrizConfusion vacia = new MatrizConfusion(new String[]{"A", "B", "C"});
        verificar(vacia.accuracy() == 0.0 && vacia.f1Macro() == 0.0 && vacia.precision(2) == 0.0,
                "matriz vacia: metricas en 0 sin NaN");
        vacia.registrar("A", "A");
        verificar(vacia.recall(2) == 0.0 && cerca(vacia.f1Macro(), 1.0),
                "matriz: clase sin ejemplos reales no afecta el F1 macro");
    }

    static void pruebaMatrizCsv() throws Exception {
        Path tmp = Files.createTempFile("matriz", ".csv");
        matrizEjemplo().guardarCSV(tmp);
        String[] lineas = Files.readAllLines(tmp, StandardCharsets.UTF_8).toArray(new String[0]);
        verificar(lineas.length == 3 && lineas[0].equals("real\\pred,A,B") && lineas[1].equals("A,3,1")
                && lineas[2].equals("B,2,2"), "matriz: guardarCSV escribe cabecera y filas");
        Files.delete(tmp);
    }

    // ---------- ResultadosLote ----------
    static void pruebasLote() {
        ResultadosLote l = new ResultadosLote(4);
        verificar(l.cantidad() == 0 && l.accuracy() == 0.0 && l.scorePromedio() == 0.0, "lote vacio: metricas en 0");
        l.agregar(0, 0, 0.9);
        l.agregar(1, 0, 0.4);
        l.agregar(1, 1, 0.8);
        l.agregar(2, 2, 0.3);
        verificar(l.cantidad() == 4 && l.getReal(1) == 1 && l.getPred(1) == 0 && cerca(l.getScore(1), 0.4),
                "lote: acceso por indice");
        verificar(cerca(l.accuracy(), 0.75), "lote: accuracy 3/4");
        verificar(cerca(l.scorePromedio(), 0.6) && cerca(l.scoreMinimo(), 0.3) && cerca(l.scoreMaximo(), 0.9),
                "lote: score promedio, minimo y maximo");
        verificar(cerca(l.scorePromedioSegunAcierto(true), (0.9 + 0.8 + 0.3) / 3)
                && cerca(l.scorePromedioSegunAcierto(false), 0.4), "lote: score segun acierto/error");
        int[] h = l.conteoPorClase(3, true);
        verificar(h[0] == 1 && h[1] == 2 && h[2] == 1, "lote: histograma de clases reales");
        int[] bajos = l.indicesBajaConfianza(0.5);
        verificar(bajos.length == 2 && bajos[0] == 1 && bajos[1] == 3, "lote: indices con confianza < 0.5");
        boolean lanzo = false;
        try { l.agregar(0, 0, 0.1); l.agregar(0, 0, 0.1); } catch (IllegalStateException e) { lanzo = true; }
        verificar(lanzo, "lote: agregar sobre capacidad llena lanza excepcion");
        lanzo = false;
        try { l.getReal(99); } catch (IndexOutOfBoundsException e) { lanzo = true; }
        verificar(lanzo, "lote: indice fuera de rango lanza excepcion");
    }

    // ---------- Pipeline completo ----------
    static void pruebaPipeline() throws Exception {
        Path dir = Files.createTempDirectory("transformerds");
        Path train = dir.resolve("train.csv");
        Path pred = dir.resolve("predictions.csv");
        Path salida = dir.resolve("results");

        Files.write(train, ("id,utt,intent\n"
                + "1,hola,saludo\n"
                + "2,\"pon una alarma, por favor\",alarma\n"
                + "2,repetida,alarma\n"
                + "3,linea rota\n").getBytes(StandardCharsets.UTF_8));

        // 6 filas validas (una con id repetido), 1 invalida
        Files.write(pred, ("id,utt,intent_real,intent_pred,score\n"
                + "10,hola,saludo,saludo,0.95\n"
                + "11,\"alarma, a las 7\",alarma,alarma,0.90\n"
                + "12,que clima hace,clima,alarma,0.40\n"
                + "13,hola buenas,saludo,saludo,0.85\n"
                + "14,otra alarma,alarma,clima,0.30\n"
                + "10,id repetido,saludo,saludo,0.99\n"
                + "15,fila rota,clima\n").getBytes(StandardCharsets.UTF_8));

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        MatrizConfusion mc = PipelineEvaluacion.ejecutar(train, pred, salida, new PrintStream(buf, true, "UTF-8"));
        String log = buf.toString("UTF-8");

        verificar(mc != null && mc.clases() == 3, "pipeline: 3 clases detectadas");
        verificar(mc.total() == 5, "pipeline: 5 predicciones procesadas (1 repetida y 1 invalida descartadas)");
        verificar(mc.aciertos() == 3 && cerca(mc.accuracy(), 0.6), "pipeline: accuracy 3/5");
        int a = mc.indiceDe("alarma"), c = mc.indiceDe("clima");
        verificar(mc.get(c, a) == 1 && mc.get(a, c) == 1, "pipeline: confusiones clima->alarma y alarma->clima");
        verificar(log.contains("Muestras cargadas: 2 | repetidas: 1 | invalidas: 1"), "pipeline: lista enlazada descarta repetidas e invalidas");
        verificar(log.contains("invalidas: 1") && log.contains("ids repetidos descartados: 1"), "pipeline: informa descartes");
        verificar(log.contains("valido: true"), "pipeline: el indice AVL queda valido");
        verificar(Files.exists(salida.resolve("confusion_matrix.csv")), "pipeline: escribe results/confusion_matrix.csv");

        // sin archivo de predicciones
        ByteArrayOutputStream buf2 = new ByteArrayOutputStream();
        MatrizConfusion nada = PipelineEvaluacion.ejecutar(train, dir.resolve("no_existe.csv"), salida, new PrintStream(buf2, true, "UTF-8"));
        verificar(nada == null && buf2.toString("UTF-8").contains("export_predictions.py"),
                "pipeline: sin predicciones avisa que ejecutes el script de Python");
    }

    public static void main(String[] args) throws Exception {
        pruebasCsv();
        pruebasMatriz();
        pruebaMatrizCsv();
        pruebasLote();
        pruebaPipeline();
        System.out.println("\nResultado: " + ok + " correctas, " + fallos + " fallidas");
        if (fallos > 0) System.exit(1);
    }
}
