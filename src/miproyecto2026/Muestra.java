
package miproyecto2026;

public class Muestra {
    private String id;
    private String utt;
    private String intent;

    public Muestra(String id, String utt, String intent) {
        this.id = id;
        this.utt = utt;
        this.intent = intent;
    }
    public String getId() {return id;}
    public String getUtt() {return utt;}
    public String getIntent() {return intent;}

    @Override
    public String toString() {
        return "Muestra{" + "id=" + id + ", utt=" + utt + ", intent=" + intent + '}';
    }
    
}
