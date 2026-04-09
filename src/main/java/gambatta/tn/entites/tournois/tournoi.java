package gambatta.tn.entites.tournois;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class tournoi {

    private Long id;

    // ----------- Nom du tournoi -----------
    private String nomt;

    // ----------- Date de début -----------
    private LocalDateTime datedebutt;

    // ----------- Date de fin -----------
    private LocalDateTime datefint;

    // ----------- Description -----------
    private String descrit;

    // ----------- Statut -----------
    private String statutt;

    // ----------- Relation Inscritournoi -----------
    private List<inscriptiontournoi> inscritournois = new ArrayList<>();

    public tournoi() {
        this.inscritournois = new ArrayList<>();
    }

    public boolean isActive() {
        return this.datefint != null && this.datefint.isAfter(LocalDateTime.now());
    }

    public boolean isExpired() {
        if (this.datefint == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        LocalDate endDate = this.datefint.toLocalDate();
        return !endDate.isAfter(today);
    }

    public String getComputedStatus() {
        if (this.isExpired()) {
            return "expire";
        }
        return this.statutt != null ? this.statutt : "ouvert";
    }

    // ================= GETTERS & SETTERS =================

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomt() {
        return this.nomt;
    }

    public void setNomt(String nomt) {
        this.nomt = nomt;
    }

    public LocalDateTime getDatedebutt() {
        return this.datedebutt;
    }

    public void setDatedebutt(LocalDateTime datedebutt) {
        this.datedebutt = datedebutt;
    }

    public LocalDateTime getDatefint() {
        return this.datefint;
    }

    public void setDatefint(LocalDateTime datefint) {
        this.datefint = datefint;
    }

    public String getDescrit() {
        return this.descrit;
    }

    public void setDescrit(String descrit) {
        this.descrit = descrit;
    }

    public String getStatutt() {
        return this.statutt;
    }

    public void setStatutt(String statutt) {
        this.statutt = statutt;
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
            inscritournoi.setTournoi(this);
        }
    }

    public void removeInscritournoi(inscriptiontournoi inscritournoi) {
        if (this.inscritournois.remove(inscritournoi)) {
            if (inscritournoi.getTournoi() == this) {
                inscritournoi.setTournoi(null);
            }
        }
    }
}
