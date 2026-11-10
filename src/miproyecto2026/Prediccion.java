package miproyecto2026;

/** Resultado de clasificar una solicitud: intencion predicha, su score y (opcional) la intencion real. */
public class Prediccion {
    private final Solicitud solicitud;
    private final String intent;
    private final double score;
    private final String intentReal; // null si no se conoce

    public Prediccion(Solicitud solicitud, String intent, double score) {
        this(solicitud, intent, score, null);
    }

    public Prediccion(Solicitud solicitud, String intent, double score, String intentReal) {
        if (solicitud == null || intent == null) {
            throw new IllegalArgumentException("solicitud e intent no pueden ser null");
        }
        this.solicitud = solicitud;
        this.intent = intent;
        this.score = score;
        this.intentReal = intentReal;
    }

    public Solicitud getSolicitud() { return solicitud; }
    public String getIntent() { return intent; }
    public double getScore() { return score; }
    public String getIntentReal() { return intentReal; }

    @Override
    public String toString() {
        return "Prediccion{solicitud=" + solicitud.getId() + ", intent=" + intent + ", score=" + score + '}';
    }
}
