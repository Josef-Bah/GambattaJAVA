package gambatta.tn.services.activites;

import gambatta.tn.entites.activites.activite;
import gambatta.tn.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActiviteService {

    Connection cnx;

    public ActiviteService() {
        cnx = MyDataBase.getInstance();
    }

    // ✅ CREATE
    public void add(activite a) {

        String sql = "INSERT INTO activite (noma, typea, dispoa, descria, imagea, adresse, afav) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);

            ps.setString(1, a.getNoma());
            ps.setString(2, a.getTypea());
            ps.setString(3, a.getDispoa());
            ps.setString(4, a.getDescria());
            ps.setString(5, a.getImagea());
            ps.setString(6, a.getAdresse());
            ps.setBoolean(7, a.isAfav());

            ps.executeUpdate();

            System.out.println("✅ Activite added");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ✅ READ
    public List<activite> getAll() {

        List<activite> list = new ArrayList<>();

        String sql = "SELECT * FROM activite";

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {

                activite a = new activite();

                a.setId(rs.getInt("id"));
                a.setNoma(rs.getString("noma"));
                a.setTypea(rs.getString("typea"));
                a.setDispoa(rs.getString("dispoa"));
                a.setDescria(rs.getString("descria"));
                a.setImagea(rs.getString("imagea"));
                a.setAdresse(rs.getString("adresse"));
                a.setAfav(rs.getBoolean("afav"));

                list.add(a);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }

    // ✅ DELETE
    public void delete(int id) {

        String sql = "DELETE FROM activite WHERE id=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();

            System.out.println("🗑 Activite deleted");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ✅ UPDATE
    public void update(activite a) {

        String sql = "UPDATE activite SET noma=?, typea=?, dispoa=?, descria=?, imagea=?, adresse=?, afav=? WHERE id=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);

            ps.setString(1, a.getNoma());
            ps.setString(2, a.getTypea());
            ps.setString(3, a.getDispoa());
            ps.setString(4, a.getDescria());
            ps.setString(5, a.getImagea());
            ps.setString(6, a.getAdresse());
            ps.setBoolean(7, a.isAfav());
            ps.setInt(8, a.getId());

            ps.executeUpdate();

            System.out.println("✏️ Activite updated");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ✅ GET ID BY NAME
    public int getIdByName(String name) {
        String sql = "SELECT id FROM activite WHERE noma = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1; // Return -1 if not found
    }
}