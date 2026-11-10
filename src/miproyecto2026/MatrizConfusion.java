package miproyecto2026;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Matriz de confusion k x k (arreglo 2D). Filas = clase real, columnas = clase predicha.
 *
 * Invariantes:
 *  - k >= 1 y la matriz es cuadrada k x k
 *  - todas las celdas >= 0
 *  - total() == suma de todas las celdas == numero de registros
 *  - aciertos() == suma de la diagonal <= total()
 */
public class MatrizConfusion {
    private final String[] etiquetas;
    private final int[][] m;
    private int total;

    public MatrizConfusion(String[] etiquetas) {
        if (etiquetas == null || etiquetas.length == 0) {
            throw new IllegalArgumentException("Se necesita al menos una etiqueta");
        }
        this.etiquetas = etiquetas.clone();
        this.m = new int[etiquetas.length][etiquetas.length];
        this.total = 0;
    }

    public int clases() { return etiquetas.length; }

    public String etiqueta(int i) { return etiquetas[i]; }

    public int total() { return total; }

    /** Indice de una etiqueta (busqueda lineal en el arreglo de k etiquetas) o -1 si no existe. O(k). */
    public int indiceDe(String etiqueta) {
        for (int i = 0; i < etiquetas.length; i++) {
            if (etiquetas[i].equals(etiqueta)) return i;
        }
        return -1;
    }

    /** Pre: 0 <= real, pred < k. Post: m[real][pred]++ y total++. O(1). */
    public void registrar(int real, int pred) {
        if (real < 0 || real >= etiquetas.length || pred < 0 || pred >= etiquetas.length) {
            throw new IllegalArgumentException("Indice de clase fuera de rango");
        }
        m[real][pred]++;
        total++;
    }

    /** Version por nombre. Lanza IllegalArgumentException si alguna etiqueta no existe. */
    public void registrar(String real, String pred) {
        int r = indiceDe(real);
        int p = indiceDe(pred);
        if (r < 0 || p < 0) {
            throw new IllegalArgumentException("Etiqueta desconocida: " + (r < 0 ? real : pred));
        }
        registrar(r, p);
    }

    public int get(int real, int pred) { return m[real][pred]; }

    /** Suma de la diagonal. O(k). */
    public int aciertos() {
        int s = 0;
        for (int i = 0; i < m.length; i++) s += m[i][i];
        return s;
    }

    public double accuracy() {
        return total == 0 ? 0.0 : (double) aciertos() / total;
    }

    /** Cantidad de ejemplos reales de la clase i (suma de la fila). O(k). */
    public int soporte(int i) {
        int s = 0;
        for (int j = 0; j < m.length; j++) s += m[i][j];
        return s;
    }

    private int predichos(int j) {
        int s = 0;
        for (int i = 0; i < m.length; i++) s += m[i][j];
        return s;
    }

    /** TP / (TP + FP); 0 si la clase nunca fue predicha. O(k). */
    public double precision(int i) {
        int pred = predichos(i);
        return pred == 0 ? 0.0 : (double) m[i][i] / pred;
    }

    /** TP / (TP + FN); 0 si la clase no tiene ejemplos reales. O(k). */
    public double recall(int i) {
        int sop = soporte(i);
        return sop == 0 ? 0.0 : (double) m[i][i] / sop;
    }

    public double f1(int i) {
        double p = precision(i);
        double r = recall(i);
        return (p + r) == 0 ? 0.0 : 2 * p * r / (p + r);
    }

    /** Promedio simple del F1 de las clases con ejemplos reales. O(k^2). */
    public double f1Macro() {
        double suma = 0;
        int conSoporte = 0;
        for (int i = 0; i < m.length; i++) {
            if (soporte(i) > 0) {
                suma += f1(i);
                conSoporte++;
            }
        }
        return conSoporte == 0 ? 0.0 : suma / conSoporte;
    }

    /** Las k confusiones (real -> predicha, fuera de la diagonal) mas frecuentes. O(k * K^2). */
    public String[] masConfundidos(int top) {
        int kk = etiquetas.length;
        boolean[][] usado = new boolean[kk][kk];
        String[] res = new String[top];
        int n = 0;
        while (n < top) {
            int mejor = 0, bi = -1, bj = -1;
            for (int i = 0; i < kk; i++) {
                for (int j = 0; j < kk; j++) {
                    if (i != j && !usado[i][j] && m[i][j] > mejor) {
                        mejor = m[i][j];
                        bi = i;
                        bj = j;
                    }
                }
            }
            if (bi < 0) break;
            usado[bi][bj] = true;
            res[n++] = etiquetas[bi] + " -> " + etiquetas[bj] + " (" + mejor + ")";
        }
        return java.util.Arrays.copyOf(res, n);
    }

    /** Guarda la matriz en CSV: primera columna = clase real, cabecera = clases predichas. */
    public void guardarCSV(Path destino) throws IOException {
        StringBuilder sb = new StringBuilder("real\\pred");
        for (String e : etiquetas) sb.append(',').append(CsvUtil.escapar(e));
        sb.append('\n');
        for (int i = 0; i < m.length; i++) {
            sb.append(CsvUtil.escapar(etiquetas[i]));
            for (int j = 0; j < m.length; j++) sb.append(',').append(m[i][j]);
            sb.append('\n');
        }
        Files.write(destino, sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    public void imprimirResumen(PrintStream out) {
        out.printf("Registros: %d | Clases: %d | Accuracy: %.4f | F1 macro: %.4f%n",
                total, etiquetas.length, accuracy(), f1Macro());
        String[] conf = masConfundidos(5);
        if (conf.length > 0) {
            out.println("Confusiones mas frecuentes (real -> predicha):");
            for (String c : conf) out.println("  " + c);
        }
    }
}
