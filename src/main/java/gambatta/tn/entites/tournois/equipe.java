package gambatta.tn.entites.tournois;

import java.util.ArrayList;
import java.util.List;

public class equipe {

    public static final String JOIN_APPROVAL_BY_LEADER = "leader";
    public static final String JOIN_APPROVAL_BY_ADMIN = "admin";

    private Long id;

    private String nom;

    private String teamLeader;

    private String titres;

    private String objectifs;

    private String coach;

    private String logo;

    private String joinApprovalMode = JOIN_APPROVAL_BY_LEADER;

    private List<inscriptiontournoi> inscritournois;

    private String status = "EN_ATTENTE";

    public equipe() {
        this.inscritournois = new ArrayList<>();
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return this.nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getTeamLeader() {
        return this.teamLeader;
    }

    public void setTeamLeader(String teamLeader) {
        this.teamLeader = teamLeader;
    }

    public String getTitres() {
        return this.titres;
    }

    public void setTitres(String titres) {
        this.titres = titres;
    }

    public String getObjectifs() {
        return this.objectifs;
    }

    public void setObjectifs(String objectifs) {
        this.objectifs = objectifs;
    }

    public String getCoach() {
        return this.coach;
    }

    public void setCoach(String coach) {
        this.coach = coach;
    }

    public String getLogo() {
        return this.logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getJoinApprovalMode() {
        return this.joinApprovalMode;
    }

    public void setJoinApprovalMode(String joinApprovalMode) {
        this.joinApprovalMode = joinApprovalMode;
    }

    /**
     * @return List<inscriptiontournoi>
     */
    public List<inscriptiontournoi> getInscritournois() {
        return this.inscritournois;
    }

    public void addInscritournoi(inscriptiontournoi inscritournoi) {
        if (!this.inscritournois.contains(inscritournoi)) {
            this.inscritournois.add(inscritournoi);
            inscritournoi.setEquipe(this);
        }
    }

    public void removeInscritournoi(inscriptiontournoi inscritournoi) {
        if (this.inscritournois.remove(inscritournoi)) {
            if (inscritournoi.getEquipe() == this) {
                inscritournoi.setEquipe(null);
            }
        }
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
