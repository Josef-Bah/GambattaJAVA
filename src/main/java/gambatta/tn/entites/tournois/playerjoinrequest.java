package gambatta.tn.entites.tournois;

import java.time.LocalDateTime;

public class playerjoinrequest {

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_ACCEPTED = "accepted";
    public static final String STATUS_REFUSED = "refused";

    private Long id;

    private String playerName;

    private equipe equipe;

    private String status = STATUS_PENDING;

    private LocalDateTime createdAt;

    public playerjoinrequest() {
        this.createdAt = LocalDateTime.now();
    }

    // ===== Getters & Setters =====

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public equipe getEquipe() {
        return this.equipe;
    }

    public void setEquipe(equipe equipe) {
        this.equipe = equipe;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
