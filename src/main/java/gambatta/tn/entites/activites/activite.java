package gambatta.tn.entites.activites;

import java.util.Date;

public class activite {

    private int id;
    private String noma;
    private String typea;
    private String dispoa;
    private String descria;
    private String imagea;
    private String adresse;
    private boolean afav;
    private Date created_at;
    private Date updated_at;

    public activite() {}

    public activite(String noma, String typea, String dispoa, String descria,
                    String imagea, String adresse, boolean afav) {
        this.noma = noma;
        this.typea = typea;
        this.dispoa = dispoa;
        this.descria = descria;
        this.imagea = imagea;
        this.adresse = adresse;
        this.afav = afav;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNoma() { return noma; }
    public void setNoma(String noma) { this.noma = noma; }

    public String getTypea() { return typea; }
    public void setTypea(String typea) { this.typea = typea; }

    public String getDispoa() { return dispoa; }
    public void setDispoa(String dispoa) { this.dispoa = dispoa; }

    public String getDescria() { return descria; }
    public void setDescria(String descria) { this.descria = descria; }

    public String getImagea() { return imagea; }
    public void setImagea(String imagea) { this.imagea = imagea; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public boolean isAfav() { return afav; }
    public void setAfav(boolean afav) { this.afav = afav; }

    public Date getCreated_at() { return created_at; }
    public void setCreated_at(Date created_at) { this.created_at = created_at; }

    public Date getUpdated_at() { return updated_at; }
    public void setUpdated_at(Date updated_at) { this.updated_at = updated_at; }

    @Override
    public String toString() {
        return noma + " | " + typea + " | " + adresse;
    }
}