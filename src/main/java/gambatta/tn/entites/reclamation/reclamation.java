package gambatta.tn.entites.reclamation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class reclamation {
    private int idrec;
    private String titre;
    private String categorierec;
    private String descrirec;
    private String statutrec;
    private LocalDateTime daterec;

    // Attributs gérés par l'IA
    private boolean isUrgent;
    private String sentimentLabel;

    // Relations (Liaisons avec les autres classes)
    private List<preuve> preuves;
    private List<response> respons;

    // private User client; // À décommenter quand tu auras créé ta classe User/Client

    /**
     * Constructeur par défaut : initialise les listes et les valeurs par défaut
     */
    public reclamation() {
        this.daterec = LocalDateTime.now();
        this.statutrec = "EN ATTENTE"; // Statut par défaut
        this.isUrgent = false;
        this.preuves = new ArrayList<>();
        this.respons = new ArrayList<>();
    }

    // ================================================================
    // === MÉTHODES DE COMPATIBILITÉ POUR L'INTERFACE (SINGULIER) =====
    // ================================================================

    /**
     * Retourne la première preuve de la liste (utilisé par l'interface UI)
     */
    public preuve getPreuve() {
        if (this.preuves != null && !this.preuves.isEmpty()) {
            return this.preuves.get(0);
        }
        return null;
    }

    /**
     * Remplace la preuve actuelle par une nouvelle (utilisé par l'interface UI)
     */
    public void setPreuve(preuve preuve) {
        if (this.preuves == null) {
            this.preuves = new ArrayList<>();
        }
        this.preuves.clear(); // On vide la liste pour ne garder qu'une seule preuve
        if (preuve != null) {
            this.addPreuve(preuve);
        }
    }

    // ================================================================
    // === MÉTHODES UTILITAIRES POUR LES RELATIONS (PLURIEL) ==========
    // ================================================================

    public void addPreuve(preuve preuve) {
        if (this.preuves == null) {
            this.preuves = new ArrayList<>();
        }
        this.preuves.add(preuve);
        preuve.setReclamation(this); // Maintient la relation bidirectionnelle
    }

    public void addResponse(response response) {
        if (this.respons == null) {
            this.respons = new ArrayList<>();
        }
        this.respons.add(response);
        response.setReclamation(this);
    }

    // ================================================================
    // === GETTERS ET SETTERS CLASSIQUES ==============================
    // ================================================================

    public int getIdrec() {
        return idrec;
    }

    public void setIdrec(int idrec) {
        this.idrec = idrec;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getCategorierec() {
        return categorierec;
    }

    public void setCategorierec(String categorierec) {
        this.categorierec = categorierec;
    }

    public String getDescrirec() {
        return descrirec;
    }

    public void setDescrirec(String descrirec) {
        this.descrirec = descrirec;
    }

    public String getStatutrec() {
        return statutrec;
    }

    public void setStatutrec(String statutrec) {
        this.statutrec = statutrec;
    }

    public LocalDateTime getDaterec() {
        return daterec;
    }

    public void setDaterec(LocalDateTime daterec) {
        this.daterec = daterec;
    }

    public boolean isUrgent() {
        return isUrgent;
    }

    public void setUrgent(boolean isUrgent) {
        this.isUrgent = isUrgent;
    }

    public String getSentimentLabel() {
        return sentimentLabel;
    }

    public void setSentimentLabel(String sentimentLabel) {
        this.sentimentLabel = sentimentLabel;
    }

    public List<preuve> getPreuves() {
        return preuves;
    }

    public void setPreuves(List<preuve> preuves) {
        this.preuves = preuves;
    }

    public List<response> getResponses() {
        return respons;
    }

    public void setResponses(List<response> respons) {
        this.respons = respons;
    }
}