package miproyecto2026;

import java.util.Arrays;

/**
 * Arreglos 1D paralelos con los resultados de un lote de inferencia:
 * real[i], pred[i] (indices de clase) y score[i] (confianza del modelo).
 *
 * Invariante: 0 <= cantidad <= capacidad; las posiciones [0, cantidad) estan ocupadas.
 */
public class ResultadosLote {
    private final int[] real;
    private final int[] pred;
    private final double[] score;
    private int cantidad;

    public ResultadosLote(int capacidad) {
        if (capacidad < 0) {
            throw new IllegalArgumentException("La capacidad no puede ser negativa");
        }
        this.real = new int[capacidad];
        this.pred = new int[capacidad];
        this.score = new double[capacidad];
        this.cantidad = 0;
    }

    public int cantidad() { return cantidad; }

    /** Pre: hay espacio. Post: cantidad' = cantidad + 1. O(1). */
    public void agregar(int r, int p, double s) {
        if (cantidad == real.length) {
            throw new IllegalStateException("El lote esta lleno");
        }
        real[cantidad] = r;
        pred[cantidad] = p;
        score[cantidad] = s;
        cantidad++;
    }

    private void validar(int i) {
        if (i < 0 || i >= cantidad) {
            throw new IndexOutOfBoundsException("Posicion " + i + " fuera de [0," + cantidad + ")");
        }
    }

    /** Acceso por indice, O(1). */
    public int getReal(int i) { validar(i); return real[i]; }
    public int getPred(int i) { validar(i); return pred[i]; }
    public double getScore(int i) { validar(i); return score[i]; }

    /** Recorrido lineal, O(n). */
    public double accuracy() {
        if (cantidad == 0) return 0.0;
        int ok = 0;
        for (int i = 0; i < cantidad; i++) {
            if (real[i] == pred[i]) ok++;
        }
        return (double) ok / cantidad;
    }

    public double scorePromedio() {
        if (cantidad == 0) return 0.0;
        double s = 0;
        for (int i = 0; i < cantidad; i++) s += score[i];
        return s / cantidad;
    }

    public double scoreMinimo() {
        if (cantidad == 0) return 0.0;
        double min = score[0];
        for (int i = 1; i < cantidad; i++) if (score[i] < min) min = score[i];
        return min;
    }

    public double scoreMaximo() {
        if (cantidad == 0) return 0.0;
        double max = score[0];
        for (int i = 1; i < cantidad; i++) if (score[i] > max) max = score[i];
        return max;
    }

    /** Score promedio solo de los aciertos (true) o solo de los errores (false); 0 si no hay. O(n). */
    public double scorePromedioSegunAcierto(boolean acierto) {
        double s = 0;
        int c = 0;
        for (int i = 0; i < cantidad; i++) {
            if ((real[i] == pred[i]) == acierto) {
                s += score[i];
                c++;
            }
        }
        return c == 0 ? 0.0 : s / c;
    }

    /** Histograma: cuantos registros hay por clase (real o predicha), con k clases. O(n + k). */
    public int[] conteoPorClase(int k, boolean usarReal) {
        int[] conteo = new int[k];
        for (int i = 0; i < cantidad; i++) {
            int c = usarReal ? real[i] : pred[i];
            conteo[c]++;
        }
        return conteo;
    }

    /** Posiciones cuyo score es menor al umbral (candidatas a revision). O(n). */
    public int[] indicesBajaConfianza(double umbral) {
        int[] tmp = new int[cantidad];
        int n = 0;
        for (int i = 0; i < cantidad; i++) {
            if (score[i] < umbral) tmp[n++] = i;
        }
        return Arrays.copyOf(tmp, n);
    }
}
