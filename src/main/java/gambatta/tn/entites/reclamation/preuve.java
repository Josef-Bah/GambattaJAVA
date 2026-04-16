package gambatta.tn.entites.reclamation;
import java.time.LocalDateTime;

public class preuve {
    private int idp;
    private String imageName;
    private String originalName;
    private String typeMime;
    private int taille;
    private LocalDateTime dateUpload;

    // La liaison avec ta classe Reclamation
    private reclamation reclamation;

    /**
     * Initialise automatiquement la date lors de la création
     */
    public preuve() {
        this.dateUpload = LocalDateTime.now();
    }

    /**
     * Retourne la taille en format lisible (ex: 1.2 Mo)
     */
    public String getTailleLisible() {
        if (this.taille <= 0) {
            return "0 Octets";
        }
        String[] unites = {"Octets", "Ko", "Mo", "Go"};
        int i = (int) Math.floor(Math.log(this.taille) / Math.log(1024));
        double calcul = this.taille / Math.pow(1024, i);

        // Arrondi à 2 décimales
        double resultatArrondi = Math.round(calcul * 100.0) / 100.0;

        return resultatArrondi + " " + unites[i];
    }

    // --- GETTERS ET SETTERS ---

    public int getIdp() {
        return idp;
    }

    public void setIdp(int idp) {
        this.idp = idp;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getTypeMime() {
        return typeMime;
    }

    public void setTypeMime(String typeMime) {
        this.typeMime = typeMime;
    }

    public int getTaille() {
        return taille;
    }

    public void setTaille(int taille) {
        this.taille = taille;
    }

    public LocalDateTime getDateUpload() {
        return dateUpload;
    }

    public void setDateUpload(LocalDateTime dateUpload) {
        this.dateUpload = dateUpload;
    }

    public reclamation getReclamation() {
        return reclamation;
    }

    public void setReclamation(reclamation reclamation) {
        this.reclamation = reclamation;
    }
}