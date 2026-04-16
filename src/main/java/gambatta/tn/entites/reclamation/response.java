package gambatta.tn.entites.reclamation;
import java.time.LocalDateTime;

public class response {
    private int idrep;
    private String contenurep;
    private LocalDateTime daterep;

    // La liaison avec ta classe Reclamation
    private reclamation reclamation;

    // Constructeur par défaut
    public response() {
        this.daterep = LocalDateTime.now(); // Initialisation automatique (optionnel mais pratique)
    }

    // --- GETTERS ET SETTERS ---

    public int getIdrep() {
        return idrep;
    }

    public void setIdrep(int idrep) {
        this.idrep = idrep;
    }

    public String getContenurep() {
        return contenurep;
    }

    public void setContenurep(String contenurep) {
        this.contenurep = contenurep;
    }

    public LocalDateTime getDaterep() {
        return daterep;
    }

    public void setDaterep(LocalDateTime daterep) {
        this.daterep = daterep;
    }

    public reclamation getReclamation() {
        return reclamation;
    }

    public void setReclamation(reclamation reclamation) {
        this.reclamation = reclamation;
    }
}