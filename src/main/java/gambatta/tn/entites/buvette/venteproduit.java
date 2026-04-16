package gambatta.tn.entites.buvette;

public class venteproduit {
    private int id;
    private int quantite;
    private double prix_unitaire;
    private int vente_id;
    private int produit_id;

    public venteproduit() {}

    public venteproduit(int quantite, double prix_unitaire, int vente_id, int produit_id) {
        this.quantite = quantite;
        this.prix_unitaire = prix_unitaire;
        this.vente_id = vente_id;
        this.produit_id = produit_id;
    }

    public venteproduit(int id, int quantite, double prix_unitaire, int vente_id, int produit_id) {
        this.id = id;
        this.quantite = quantite;
        this.prix_unitaire = prix_unitaire;
        this.vente_id = vente_id;
        this.produit_id = produit_id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public double getPrix_unitaire() { return prix_unitaire; }
    public void setPrix_unitaire(double prix_unitaire) { this.prix_unitaire = prix_unitaire; }

    public int getVente_id() { return vente_id; }
    public void setVente_id(int vente_id) { this.vente_id = vente_id; }

    public int getProduit_id() { return produit_id; }
    public void setProduit_id(int produit_id) { this.produit_id = produit_id; }
}
