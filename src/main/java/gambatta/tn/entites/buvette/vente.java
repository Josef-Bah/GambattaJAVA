package gambatta.tn.entites.buvette;

import java.time.LocalDateTime;

public class vente {
    private int id;
    private int quantv;
    private LocalDateTime datev;
    private double montantv;
    private int userId;

    public vente() {}

    public vente(int quantv, LocalDateTime datev, double montantv, int userId) {
        this.quantv = quantv;
        this.datev = datev;
        this.montantv = montantv;
        this.userId = userId;
    }

    public vente(int id, int quantv, LocalDateTime datev, double montantv, int userId) {
        this.id = id;
        this.quantv = quantv;
        this.datev = datev;
        this.montantv = montantv;
        this.userId = userId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getQuantv() { return quantv; }
    public void setQuantv(int quantv) { this.quantv = quantv; }

    public LocalDateTime getDatev() { return datev; }
    public void setDatev(LocalDateTime datev) { this.datev = datev; }

    public double getMontantv() { return montantv; }
    public void setMontantv(double montantv) { this.montantv = montantv; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}
