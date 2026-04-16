package gambatta.tn.services.buvette;

import gambatta.tn.entites.buvette.produit;
import gambatta.tn.tools.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class VenteService {

    Connection cnx = MyDataBase.getInstance();

    public void createVente(List<gambatta.tn.entites.buvette.CartItem> cart) throws Exception {
        // 1. insert vente
        String sqlVente = "INSERT INTO vente (quantv, datev, montantv, user_id) VALUES (?, ?, ?, ?)";
        PreparedStatement psVente = cnx.prepareStatement(sqlVente, Statement.RETURN_GENERATED_KEYS);

        int quant = cart.stream().mapToInt(gambatta.tn.entites.buvette.CartItem::getQuantity).sum();
        double total = cart.stream().mapToDouble(item -> item.getProduct().getPrixp() * item.getQuantity()).sum();

        psVente.setInt(1, quant);
        psVente.setObject(2, LocalDateTime.now());
        psVente.setDouble(3, total);
        psVente.setInt(4, 1); // Valid user_id in DB

        psVente.executeUpdate();

        ResultSet rs = psVente.getGeneratedKeys();
        rs.next();
        int venteId = rs.getInt(1);

        // 2. insert vente_produit
        String sqlVP = "INSERT INTO vente_produit (vente_id, produit_id, quantite, prix_unitaire) VALUES (?, ?, ?, ?)";
        PreparedStatement psVP = cnx.prepareStatement(sqlVP);

        for (gambatta.tn.entites.buvette.CartItem item : cart) {
            psVP.setInt(1, venteId);
            psVP.setInt(2, item.getProduct().getId());
            psVP.setInt(3, item.getQuantity());
            psVP.setDouble(4, item.getProduct().getPrixp());
            psVP.executeUpdate();
        }

        System.out.println("Vente enregistrée !");
    }

    public List<gambatta.tn.entites.buvette.vente> getAll() {
        List<gambatta.tn.entites.buvette.vente> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM vente";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                gambatta.tn.entites.buvette.vente v = new gambatta.tn.entites.buvette.vente();
                v.setId(rs.getInt("id"));
                v.setQuantv(rs.getInt("quantv"));
                v.setDatev(rs.getObject("datev", LocalDateTime.class));
                v.setMontantv(rs.getDouble("montantv"));
                v.setUserId(rs.getInt("user_id"));
                list.add(v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void delete(int id) {
        try {
            // First delete associated vente_produit due to foreign key
            String delVP = "DELETE FROM vente_produit WHERE vente_id=?";
            PreparedStatement ps1 = cnx.prepareStatement(delVP);
            ps1.setInt(1, id);
            ps1.executeUpdate();

            String sql = "DELETE FROM vente WHERE id=?";
            PreparedStatement ps2 = cnx.prepareStatement(sql);
            ps2.setInt(1, id);
            ps2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}