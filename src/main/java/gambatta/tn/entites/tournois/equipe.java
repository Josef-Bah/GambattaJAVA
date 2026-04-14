package gambatta.tn.entites.tournois;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.List;

public class equipe {

    public static final String JOIN_APPROVAL_BY_LEADER = "leader";
    public static final String JOIN_APPROVAL_BY_ADMIN = "admin";

    private LongProperty id;
    private StringProperty nom;
    private StringProperty teamLeader;
    private StringProperty titres;
    private StringProperty objectifs;
    private StringProperty coach;
    private StringProperty logo;
    private String joinApprovalMode = JOIN_APPROVAL_BY_LEADER;
    private List<inscriptiontournoi> inscritournois;
    private StringProperty status;

    public equipe() {
        this.id = new SimpleLongProperty();
        this.nom = new SimpleStringProperty();
        this.teamLeader = new SimpleStringProperty();
        this.titres = new SimpleStringProperty();
        this.objectifs = new SimpleStringProperty();
        this.coach = new SimpleStringProperty();
        this.logo = new SimpleStringProperty();
        this.status = new SimpleStringProperty("EN_ATTENTE");
        this.inscritournois = new ArrayList<>();
    }

    // --- id ---
    public Long getId() {
        if (id.get() == 0) return null; // considère 0 comme "pas encore inséré"
        return id.get();
    }
    public void setId(long id) { this.id.set(id); }
    public LongProperty idProperty() { return id; }

    // --- nom ---
    public String getNom() { return nom.get(); }
    public void setNom(String nom) { this.nom.set(nom); }
    public StringProperty nomProperty() { return nom; }

    // --- teamLeader ---
    public String getTeamLeader() { return teamLeader.get(); }
    public void setTeamLeader(String leader) { this.teamLeader.set(leader); }
    public StringProperty teamLeaderProperty() { return teamLeader; }

    // --- status ---
    public String getStatus() { return status.get(); }
    public void setStatus(String s) { this.status.set(s); }
    public StringProperty statusProperty() { return status; }

    // --- titres, objectifs, coach, logo ---
    public String getTitres() { return titres.get(); }
    public void setTitres(String t) { this.titres.set(t); }
    public String getObjectifs() { return objectifs.get(); }
    public void setObjectifs(String o) { this.objectifs.set(o); }
    public String getCoach() { return coach.get(); }
    public void setCoach(String c) { this.coach.set(c); }
    public String getLogo() { return logo.get(); }
    public void setLogo(String l) { this.logo.set(l); }

    public String getJoinApprovalMode() { return joinApprovalMode; }
    public void setJoinApprovalMode(String joinApprovalMode) { this.joinApprovalMode = joinApprovalMode; }

    public List<inscriptiontournoi> getInscritournois() { return inscritournois; }
    public void addInscritournoi(inscriptiontournoi i) {
        if (!inscritournois.contains(i)) {
            inscritournois.add(i);
            i.setEquipe(this);
        }
    }
    public void removeInscritournoi(inscriptiontournoi i) {
        if (inscritournois.remove(i)) {
            if (i.getEquipe() == this) i.setEquipe(null);
        }
    }
    @Override
    public String toString() {
        return getNom() != null ? getNom() : "Sans nom";
    }
}