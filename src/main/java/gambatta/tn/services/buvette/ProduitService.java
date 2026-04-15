package gambatta.tn.services.buvette;

import gambatta.tn.entites.buvette.produit;
import gambatta.tn.tools.MyDataBase;

import java.sql.*;
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
            ps.setObject(5, p.getDateajoutp());
            ps.setString(6, p.getImagep());
            ps.setInt(7, p.getReferencep());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // READ ALL
    public List<produit> getAll() {
        List<produit> list = new ArrayList<>();

        String sql = "SELECT * FROM produit";

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                produit p = new produit();
                p.setId(rs.getInt("id"));
                p.setNomp(rs.getString("nomp"));
                p.setDescrip(rs.getString("descrip"));
                p.setPrixp(rs.getDouble("prixp"));
                p.setStockp(rs.getInt("stockp"));
                p.setDateajoutp(rs.getObject("dateajoutp", java.time.LocalDateTime.class));
                p.setImagep(rs.getString("imagep"));
                p.setReferencep(rs.getInt("referencep"));

                list.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // SEARCH BY DESCRIPTION
    public List<produit> search(String keyword) {
        List<produit> list = new ArrayList<>();

        String sql = "SELECT * FROM produit WHERE descrip LIKE ?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                produit p = new produit();
                p.setId(rs.getInt("id"));
                p.setNomp(rs.getString("nomp"));
                p.setDescrip(rs.getString("descrip"));
                p.setPrixp(rs.getDouble("prixp"));
                p.setStockp(rs.getInt("stockp"));

                list.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public double getTotalRevenue() {
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery("SELECT SUM(montantv) FROM vente");
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public String getTopProduct() {
        try {
            String sql = """
        SELECT p.nomp, COUNT(*) as total
        FROM vente_produit vp
        JOIN produit p ON vp.produit_id = p.id
        GROUP BY p.nomp
        ORDER BY total DESC LIMIT 1
        """;
            ResultSet rs = cnx.createStatement().executeQuery(sql);
            if (rs.next()) return rs.getString("nomp");
        } catch (Exception e) { e.printStackTrace(); }
        return "None";
    }

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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM produit WHERE id=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}