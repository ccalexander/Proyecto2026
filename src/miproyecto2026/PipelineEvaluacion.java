package miproyecto2026;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Integra todas las estructuras del proyecto en un unico flujo:
 *
 *   CSV de entrenamiento -> RepositorioMuestras (lista enlazada)
 *   CSV de predicciones  -> ColaInferencia -> (motor precalculado) -> Prediccion
 *                              +-> PilaHistorial        (ultimas predicciones)
 *                              +-> AVLResultados        (indice por id)
 *                              +-> MatrizConfusion      (matriz 2D)
 *                              +-> ResultadosLote       (arreglos 1D)
 *
 * El "motor" es el Transformer de Python: python/export_predictions.py ya calculo
 * la prediccion de cada solicitud y la dejo en results/predictions.csv.
 */
public class PipelineEvaluacion {

    private PipelineEvaluacion() { }

    /**
     * @return la matriz de confusion, o null si no hay predicciones que procesar.
     */
    public static MatrizConfusion ejecutar(Path trainCsv, Path predCsv, Path dirSalida, PrintStream out)
            throws IOException {

        // ---------- 1. Repositorio de muestras (lista enlazada) ----------
        out.println("=== 1. Repositorio de muestras (lista enlazada) ===");
        if (Files.exists(trainCsv)) {
            cargarMuestras(trainCsv, out);
        } else {
            out.println("No se encontro " + trainCsv + " (se omite este paso).");
        }

        // ---------- 2. Lectura de predicciones ----------
        out.println("\n=== 2. Predicciones del modelo ===");
        if (!Files.exists(predCsv)) {
            out.println("No se encontro " + predCsv + ".");
            out.println("Ejecuta primero: python python/export_predictions.py");
            return null;
        }
        String[] lineas = leerLineasDeDatos(predCsv);
        int n = lineas.length;

        String[] ids = new String[n];
        String[] textos = new String[n];
        String[] reales = new String[n];
        String[] preds = new String[n];
        double[] scores = new double[n];
        boolean[] valida = new boolean[n];
        int invalidas = 0;

        for (int i = 0; i < n; i++) {
            String[] f = CsvUtil.parsearLinea(lineas[i]);
            if (f.length < 5) { invalidas++; continue; }
            try {
                scores[i] = Double.parseDouble(f[4].trim());
            } catch (NumberFormatException e) {
                invalidas++;
                continue;
            }
            ids[i] = f[0];
            textos[i] = f[1];
            reales[i] = f[2];
            preds[i] = f[3];
            valida[i] = true;
        }

        // Etiquetas unicas (arreglo 1D)
        String[] tmp = new String[2 * n + 1];
        int k = 0;
        for (int i = 0; i < n; i++) {
            if (!valida[i]) continue;
            k = agregarEtiqueta(tmp, k, reales[i]);
            k = agregarEtiqueta(tmp, k, preds[i]);
        }
        if (k == 0) {
            out.println("No hay filas validas en " + predCsv);
            return null;
        }
        String[] etiquetas = Arrays.copyOf(tmp, k);
        out.println("Filas leidas: " + n + " | invalidas: " + invalidas + " | clases: " + k);

        // ---------- 3. Cola de solicitudes pendientes ----------
        ColaInferencia cola = new ColaInferencia();
        int[] filaDe = new int[n];
        int encoladas = 0;
        for (int i = 0; i < n; i++) {
            if (!valida[i]) continue;
            cola.encolar(new Solicitud(ids[i], textos[i]));
            filaDe[encoladas++] = i;
        }
        out.println("Solicitudes en cola: " + cola.cantidad());

        // ---------- 4. Procesamiento ----------
        out.println("\n=== 3. Procesamiento (cola -> historial + indice AVL + matriz) ===");
        PilaHistorial historial = new PilaHistorial();
        AVLResultados indice = new AVLResultados();
        MatrizConfusion matriz = new MatrizConfusion(etiquetas);
        ResultadosLote lote = new ResultadosLote(encoladas);
        int duplicadas = 0;
        int pos = 0;
        String primerId = null;

        while (!cola.estaVacia()) {
            Solicitud s = cola.desencolar();      // FIFO: atiende en orden de llegada
            int i = filaDe[pos++];                // la prediccion precalculada de esa solicitud
            Prediccion pr = new Prediccion(s, preds[i], scores[i], reales[i]);
            try {
                indice.insertar(pr);
            } catch (IllegalArgumentException e) { // id repetido: se descarta
                duplicadas++;
                continue;
            }
            historial.apilar(pr);
            int r = matriz.indiceDe(reales[i]);
            int p = matriz.indiceDe(preds[i]);
            matriz.registrar(r, p);
            lote.agregar(r, p, scores[i]);
            if (primerId == null) primerId = s.getId();
        }
        out.println("Procesadas: " + lote.cantidad() + " | ids repetidos descartados: " + duplicadas
                + " | cola vacia: " + cola.estaVacia());

        // ---------- 5. Consultas ----------
        out.println("\n=== 4. Consultas ===");
        out.println("Historial (pila): " + historial.cantidad() + " predicciones. Ultima: "
                + (historial.estaVacia() ? "-" : historial.tope()));
        out.println("Indice AVL: " + indice.cantidad() + " nodos, altura " + indice.altura()
                + ", valido: " + indice.esAVLValido());
        if (primerId != null) {
            out.println("buscar(\"" + primerId + "\") -> " + indice.buscar(primerId));
        }
        out.println("buscar(\"id_inexistente\") -> " + indice.buscar("id_inexistente"));

        // ---------- 6. Evaluacion ----------
        out.println("\n=== 5. Evaluacion (matriz de confusion 2D + arreglos 1D) ===");
        matriz.imprimirResumen(out);
        out.printf("Score promedio: %.4f | minimo: %.4f | maximo: %.4f%n",
                lote.scorePromedio(), lote.scoreMinimo(), lote.scoreMaximo());
        out.printf("Score promedio en aciertos: %.4f | en errores: %.4f%n",
                lote.scorePromedioSegunAcierto(true), lote.scorePromedioSegunAcierto(false));
        out.println("Predicciones con confianza < 0.5: " + lote.indicesBajaConfianza(0.5).length);

        Files.createDirectories(dirSalida);
        Path destino = dirSalida.resolve("confusion_matrix.csv");
        matriz.guardarCSV(destino);
        out.println("Matriz guardada en " + destino);
        return matriz;
    }

    // ---------- auxiliares ----------

    private static void cargarMuestras(Path trainCsv, PrintStream out) throws IOException {
        String[] lineas = leerLineasDeDatos(trainCsv);
        RepositorioMuestras repo = new RepositorioMuestras();
        int repetidas = 0;
        int invalidas = 0;
        String primerId = null;
        for (String linea : lineas) {
            String[] f = CsvUtil.parsearLinea(linea);
            if (f.length < 3) { invalidas++; continue; }
            try {
                repo.insertar(new Muestra(f[0], f[1], f[2]));
                if (primerId == null) primerId = f[0];
            } catch (IllegalArgumentException e) {
                repetidas++;
            }
        }
        out.println("Muestras cargadas: " + repo.cantidad() + " | repetidas: " + repetidas
                + " | invalidas: " + invalidas);
        if (primerId != null) {
            out.println("buscar(\"" + primerId + "\") -> " + repo.buscar(primerId));
        }
    }

    /** Lee un CSV en UTF-8, descarta la cabecera y las lineas vacias. */
    private static String[] leerLineasDeDatos(Path csv) throws IOException {
        List<String> todas = Files.readAllLines(csv, StandardCharsets.UTF_8);
        String[] tmp = new String[todas.size()];
        int n = 0;
        for (int i = 1; i < todas.size(); i++) { // la linea 0 es la cabecera
            if (!todas.get(i).trim().isEmpty()) tmp[n++] = todas.get(i);
        }
        return Arrays.copyOf(tmp, n);
    }

    private static int agregarEtiqueta(String[] arr, int k, String etiqueta) {
        for (int i = 0; i < k; i++) {
            if (arr[i].equals(etiqueta)) return k;
        }
        arr[k] = etiqueta;
        return k + 1;
    }
}
