package miproyecto2026;

import java.util.Arrays;

/** Utilidades minimas para leer y escribir CSV (campos entre comillas, comas y "" escapadas). */
public class CsvUtil {

    private CsvUtil() { }

    /** Separa una linea CSV en sus campos. Soporta comas dentro de comillas y "" como comilla literal. */
    public static String[] parsearLinea(String linea) {
        String[] tmp = new String[linea.length() + 1];
        int n = 0;
        StringBuilder sb = new StringBuilder();
        boolean enComillas = false;
        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (enComillas) {
                if (c == '"') {
                    if (i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                        sb.append('"');
                        i++;
                    } else {
                        enComillas = false;
                    }
                } else {
                    sb.append(c);
                }
            } else if (c == '"') {
                enComillas = true;
            } else if (c == ',') {
                tmp[n++] = sb.toString();
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tmp[n++] = sb.toString();
        return Arrays.copyOf(tmp, n);
    }

    /** Escapa un campo para escribirlo en CSV. */
    public static String escapar(String campo) {
        if (campo.indexOf(',') >= 0 || campo.indexOf('"') >= 0 || campo.indexOf('\n') >= 0) {
            return "\"" + campo.replace("\"", "\"\"") + "\"";
        }
        return campo;
    }
}
