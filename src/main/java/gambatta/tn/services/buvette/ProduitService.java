package gambatta.tn.services.buvette;

import gambatta.tn.entites.buvette.produit;
import gambatta.tn.tools.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProduitService {

    Connection cnx = MyDataBase.getInstance();

    // CREATE
    public void add(produit p) {

        String sql = "INSERT INTO produit (nomp, descrip, prixp, stockp, dateajoutp, imagep, referencep) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);

            ps.setString(1, p.getNomp());
            ps.setString(2, p.getDescrip());
            ps.setDouble(3, p.getPrixp());
            ps.setInt(4, p.getStockp());
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(6, p.getImagep());
            ps.setInt(7, p.getReferencep());

            ps.executeUpdate();
            System.out.println("Produit ajouté !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // READ
    public List<produit> getAll() {
        List<produit> list = new ArrayList<>();

        String sql = "SELECT * FROM produit";

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {

                produit p = new produit(
                        rs.getInt("id"),
                        rs.getString("nomp"),
                        rs.getString("descrip"),
                        rs.getDouble("prixp"),
                        rs.getInt("stockp"),
                        rs.getTimestamp("dateajoutp").toLocalDateTime(),
                        rs.getString("imagep"),
                        rs.getInt("referencep")
                );

                list.add(p);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }

    // UPDATE
    public void update(produit p) {

        String sql = "UPDATE produit SET nomp=?, descrip=?, prixp=?, stockp=?, imagep=?, referencep=? WHERE id=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);

            ps.setString(1, p.getNomp());
            ps.setString(2, p.getDescrip());
            ps.setDouble(3, p.getPrixp());
            ps.setInt(4, p.getStockp());
            ps.setString(5, p.getImagep());
            ps.setInt(6, p.getReferencep());
            ps.setInt(7, p.getId());

            ps.executeUpdate();
            System.out.println("Produit modifié !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // DELETE
    public void delete(int id) {

        String sql = "DELETE FROM produit WHERE id=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);

            ps.executeUpdate();
            System.out.println("Produit supprimé !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}