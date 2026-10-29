package miproyecto2026;

/** Resultado de clasificar una solicitud: intencion predicha y su score. */
public class Prediccion {
    private final Solicitud solicitud;
    private final String intent;
    private final double score;

    public Prediccion(Solicitud solicitud, String intent, double score) {
        if (solicitud == null || intent == null) {
            throw new IllegalArgumentException("solicitud e intent no pueden ser null");
        }
        this.solicitud = solicitud;
        this.intent = intent;
        this.score = score;
    }

    public Solicitud getSolicitud() { return solicitud; }
    public String getIntent() { return intent; }
    public double getScore() { return score; }

    @Override
    public String toString() {
        return "Prediccion{solicitud=" + solicitud.getId() + ", intent=" + intent + ", score=" + score + '}';
    }
}
