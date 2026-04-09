package gambatta.tn.entites.tournois;

import java.time.LocalDateTime;

public class rencontre {

    private Long id;

    private tournoi tournoi;

    private equipe equipeA;

    private equipe equipeB;

    private Integer scoreA;

    private Integer scoreB;

    private LocalDateTime playedAt;

    public void validate() {
        if (this.equipeA != null && this.equipeB != null && this.equipeA.getId() != null && this.equipeA.getId().equals(this.equipeB.getId())) {
            throw new IllegalArgumentException("Les deux équipes doivent être différentes.");
        }

        boolean scoreASet = this.scoreA != null;
        boolean scoreBSet = this.scoreB != null;
        if (scoreASet ^ scoreBSet) {
            throw new IllegalArgumentException("Les deux scores doivent être renseignés.");
        }

        if ((scoreASet || scoreBSet) && this.playedAt == null) {
            throw new IllegalArgumentException("La date/heure du match est obligatoire si un score est saisi.");
        }
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public tournoi getTournoi() {
        return this.tournoi;
    }

    public void setTournoi(tournoi tournoi) {
        this.tournoi = tournoi;
    }

    public equipe getEquipeA() {
        return this.equipeA;
    }

    public void setEquipeA(equipe equipeA) {
        this.equipeA = equipeA;
    }

    public equipe getEquipeB() {
        return this.equipeB;
    }

    public void setEquipeB(equipe equipeB) {
        this.equipeB = equipeB;
    }

    public Integer getScoreA() {
        return this.scoreA;
    }

    public void setScoreA(Integer scoreA) {
        this.scoreA = scoreA;
    }

    public Integer getScoreB() {
        return this.scoreB;
    }

    public void setScoreB(Integer scoreB) {
        this.scoreB = scoreB;
    }

    public LocalDateTime getPlayedAt() {
        return this.playedAt;
    }

    public void setPlayedAt(LocalDateTime playedAt) {
        this.playedAt = playedAt;
    }
}
