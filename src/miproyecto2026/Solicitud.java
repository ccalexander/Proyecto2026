package miproyecto2026;

/** Solicitud de un usuario pendiente de ser clasificada por el Transformer. */
public class Solicitud {
    private final String id;
    private final String texto;
    private final long timestamp;

    public Solicitud(String id, String texto) {
        if (id == null || texto == null) {
            throw new IllegalArgumentException("id y texto no pueden ser null");
        }
        this.id = id;
        this.texto = texto;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public String getTexto() { return texto; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "Solicitud{id=" + id + ", texto=" + texto + '}';
    }
}
