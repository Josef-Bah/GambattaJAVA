package gambatta.tn.services.buvette;

import gambatta.tn.entites.buvette.produit;
import gambatta.tn.tools.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class VenteService {

    Connection cnx = MyDataBase.getInstance();

    public void createVente(List<produit> cart) {

        try {
            // 1. insert vente
            String sqlVente = "INSERT INTO vente (quantv, datev, montantv, user_id) VALUES (?, ?, ?, ?)";
            PreparedStatement psVente = cnx.prepareStatement(sqlVente, Statement.RETURN_GENERATED_KEYS);

            int quant = cart.size();
            double total = cart.stream().mapToDouble(produit::getPrixp).sum();

            psVente.setInt(1, quant);
            psVente.setObject(2, LocalDateTime.now());
            psVente.setDouble(3, total);
            psVente.setInt(4, 4); // TEMP user_id

            psVente.executeUpdate();

            ResultSet rs = psVente.getGeneratedKeys();
            rs.next();
            int venteId = rs.getInt(1);

            // 2. insert vente_produit
            String sqlVP = "INSERT INTO vente_produit (vente_id, produit_id, quantite, prix_unitaire) VALUES (?, ?, ?, ?)";
            PreparedStatement psVP = cnx.prepareStatement(sqlVP);

            for (produit p : cart) {
                psVP.setInt(1, venteId);
                psVP.setInt(2, p.getId());
                psVP.setInt(3, 1);
                psVP.setDouble(4, p.getPrixp());
                psVP.executeUpdate();
            }

            System.out.println("Vente enregistrée !");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}