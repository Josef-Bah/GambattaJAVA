package gambatta.tn.entites.tournois;

public class inscriptiontournoi {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_ACCEPTED = "ACCEPTED";
    public static final String STATUS_REFUSED = "REFUSED";

    private Long id;

    // Relation ManyToOne avec Equipe
    private equipe equipe;

    // Relation ManyToOne avec Tournoi
    private tournoi tournoi;

    private String status = STATUS_PENDING;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public equipe getEquipe() {
        return equipe;
    }

    public void setEquipe(equipe equipe) {
        this.equipe = equipe;
    }

    public tournoi getTournoi() {
        return tournoi;
    }

    public void setTournoi(tournoi tournoi) {
        this.tournoi = tournoi;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
