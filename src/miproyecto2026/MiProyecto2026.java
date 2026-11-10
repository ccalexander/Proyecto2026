package miproyecto2026;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
/**
 *
 * @author ALEX
 */
public class MiProyecto2026 {

    public static void main(String[] args) {
        Path train = Paths.get(args.length > 0 ? args[0] : "data/massive_es_train.csv");
        Path pred = Paths.get(args.length > 1 ? args[1] : "results/predictions.csv");
        Path salida = Paths.get(args.length > 2 ? args[2] : "results");
        try {
            PipelineEvaluacion.ejecutar(train, pred, salida, System.out);
        } catch (IOException e) {
            System.err.println("Error de lectura/escritura: " + e.getMessage());
        }
    }
}
