package gambatta.tn.entites.buvette;

import java.time.LocalDateTime;

public class produit {

    private int id;
    private String nomp;
    private String descrip;
    private double prixp;
    private int stockp;
    private LocalDateTime dateajoutp;
    private String imagep;
    private int referencep;

    public produit() {}

    public produit(String nomp, String descrip, double prixp, int stockp,
                   LocalDateTime dateajoutp, String imagep, int referencep) {
        this.nomp = nomp;
        this.descrip = descrip;
        this.prixp = prixp;
        this.stockp = stockp;
        this.dateajoutp = dateajoutp;
        this.imagep = imagep;
        this.referencep = referencep;
    }

    public produit(int id, String nomp, String descrip, double prixp, int stockp,
                   LocalDateTime dateajoutp, String imagep, int referencep) {
        this.id = id;
        this.nomp = nomp;
        this.descrip = descrip;
        this.prixp = prixp;
        this.stockp = stockp;
        this.dateajoutp = dateajoutp;
        this.imagep = imagep;
        this.referencep = referencep;
    }

    // GETTERS
    public int getId() { return id; }
    public String getNomp() { return nomp; }
    public String getDescrip() { return descrip; }
    public double getPrixp() { return prixp; }
    public int getStockp() { return stockp; }
    public LocalDateTime getDateajoutp() { return dateajoutp; }
    public String getImagep() { return imagep; }
    public int getReferencep() { return referencep; }

    // SETTERS
    public void setId(int id) { this.id = id; }
    public void setNomp(String nomp) { this.nomp = nomp; }
    public void setDescrip(String descrip) { this.descrip = descrip; }
    public void setPrixp(double prixp) { this.prixp = prixp; }
    public void setStockp(int stockp) { this.stockp = stockp; }
    public void setDateajoutp(LocalDateTime dateajoutp) { this.dateajoutp = dateajoutp; }
    public void setImagep(String imagep) { this.imagep = imagep; }
    public void setReferencep(int referencep) { this.referencep = referencep; }

    @Override
    public String toString() {
        return "Produit{" +
                "id=" + id +
                ", nomp='" + nomp + '\'' +
                ", prixp=" + prixp +
                ", stockp=" + stockp +
                ", referencep=" + referencep +
                '}';
    }
}