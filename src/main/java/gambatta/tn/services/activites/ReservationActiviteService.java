package gambatta.tn.services.activites;

import gambatta.tn.entites.activites.ReservationActivite;
import gambatta.tn.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationActiviteService {

    Connection cnx;

    public ReservationActiviteService() {
        cnx = MyDataBase.getInstance();
    }

    // ✅ CREATE
    public void add(ReservationActivite r) {
        String sql = "INSERT INTO reservationactivite " +
                "(datedebut, heurer, statutr, ida_id, idu_id, creneau_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            // Désactiver temporairement la contrainte de clé étrangère (si l'utilisateur 1 n'existe pas encore en DB)
            Statement st = cnx.createStatement();
            st.execute("SET FOREIGN_KEY_CHECKS=0");

            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setDate(1, new java.sql.Date(r.getDatedebut().getTime()));
            ps.setString(2, r.getHeurer());
            ps.setString(3, r.getStatutr());
            ps.setInt(4, r.getActiviteId());
            ps.setInt(5, r.getUserId());

            if (r.getCreneauId() != null)
                ps.setInt(6, r.getCreneauId());
            else
                ps.setNull(6, Types.INTEGER);

            ps.executeUpdate();
            
            // Réactiver la contrainte
            st.execute("SET FOREIGN_KEY_CHECKS=1");

            System.out.println("✅ Reservation added");

        } catch (SQLException e) {
            System.out.println("Reservation Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ✅ READ
    public List<ReservationActivite> getAll() {

        List<ReservationActivite> list = new ArrayList<>();

        String sql = "SELECT * FROM reservationactivite";

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {

                ReservationActivite r = new ReservationActivite();

                r.setId(rs.getInt("id"));
                r.setDatedebut(rs.getDate("datedebut"));
                r.setHeurer(rs.getString("heurer"));
                r.setStatutr(rs.getString("statutr"));
                r.setActiviteId(rs.getInt("ida_id"));
                r.setUserId(rs.getInt("idu_id"));
                r.setCreneauId(rs.getInt("creneau_id"));

                list.add(r);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }

    // ✅ UPDATE
    public void update(ReservationActivite r) {

        String sql = "UPDATE reservationactivite SET " +
                "datedebut=?, heurer=?, statutr=? WHERE id=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);

            ps.setDate(1, new java.sql.Date(r.getDatedebut().getTime()));
            ps.setString(2, r.getHeurer());
            ps.setString(3, r.getStatutr());
            ps.setInt(4, r.getId());

            ps.executeUpdate();

            System.out.println("✏️ Reservation updated");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ✅ DELETE
    public void delete(int id) {

        String sql = "DELETE FROM reservationactivite WHERE id=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();

            System.out.println("❌ Reservation deleted");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ✅ READ BY USER ID
    public List<ReservationActivite> getByUserId(int userId) {
        List<ReservationActivite> list = new ArrayList<>();
        String sql = "SELECT * FROM reservationactivite WHERE idu_id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ReservationActivite r = new ReservationActivite();
                r.setId(rs.getInt("id"));
                r.setDatedebut(rs.getDate("datedebut"));
                r.setHeurer(rs.getString("heurer"));
                r.setStatutr(rs.getString("statutr"));
                r.setActiviteId(rs.getInt("ida_id"));
                r.setUserId(rs.getInt("idu_id"));
                r.setCreneauId(rs.getInt("creneau_id"));
                list.add(r);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }
}

