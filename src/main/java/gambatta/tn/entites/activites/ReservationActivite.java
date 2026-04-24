package gambatta.tn.entites.activites;

import java.util.Date;

public class ReservationActivite {

    private int id;
    private Date datedebut;
    private String heurer;
    private String statutr;

    private int activiteId;
    private int userId;
    private Integer creneauId;

    private String email;
    private String telephone;

    // CONSTRUCTORS
    public ReservationActivite() {}

    public ReservationActivite(Date datedebut, String heurer, String statutr,
                               int activiteId, int userId, Integer creneauId) {
        this.datedebut = datedebut;
        this.heurer = heurer;
        this.statutr = statutr;
        this.activiteId = activiteId;
        this.userId = userId;
        this.creneauId = creneauId;
    }

    public ReservationActivite(Date datedebut, String heurer, String statutr,
                               int activiteId, int userId, Integer creneauId, String email, String telephone) {
        this(datedebut, heurer, statutr, activiteId, userId, creneauId);
        this.email = email;
        this.telephone = telephone;
    }

    // GETTERS / SETTERS

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Date getDatedebut() { return datedebut; }
    public void setDatedebut(Date datedebut) { this.datedebut = datedebut; }

    public String getHeurer() { return heurer; }
    public void setHeurer(String heurer) { this.heurer = heurer; }

    public String getStatutr() { return statutr; }
    public void setStatutr(String statutr) { this.statutr = statutr; }

    public int getActiviteId() { return activiteId; }
    public void setActiviteId(int activiteId) { this.activiteId = activiteId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Integer getCreneauId() { return creneauId; }
    public void setCreneauId(Integer creneauId) { this.creneauId = creneauId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    @Override
    public String toString() {
        return "Reservation: " + datedebut + " | " + heurer + " | " + statutr + " | " + email;
    }
}

