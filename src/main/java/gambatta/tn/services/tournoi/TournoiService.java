package gambatta.tn.services.tournoi;

import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.entites.tournois.tournoi;
import gambatta.tn.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TournoiService {

    private Connection cnx;

    public TournoiService() {
        this.cnx = MyDataBase.getInstance();
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        if (cnx == null) {
            System.err.println("ERREUR : Connexion à la base de données impossible.");
            return;
        }
        try (Statement st = cnx.createStatement()) {
            st.execute("ALTER TABLE tournoi ADD COLUMN IF NOT EXISTS logo VARCHAR(255) NULL");
        } catch (SQLException e) {
            // Ignorer si la colonne existe déjà (certaines versions de MySQL ne supportent pas IF NOT EXISTS sur ADD COLUMN)
            if (e.getErrorCode() != 1060) {
                System.err.println("Erreur migration tournoi (logo) : " + e.getMessage());
            }
        }
    }

    // CREATE
    public boolean add(tournoi tournoi) {
        if (cnx == null) return false;
        String sql = "INSERT INTO tournoi (nomt, datedebutt, datefint, descrit, statutt, logo) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, tournoi.getNomt());
            pst.setTimestamp(2, Timestamp.valueOf(tournoi.getDatedebutt()));
            pst.setTimestamp(3, Timestamp.valueOf(tournoi.getDatefint()));
            pst.setString(4, tournoi.getDescrit());
            pst.setString(5, tournoi.getStatutt());
            pst.setString(6, tournoi.getLogo());

            int rows = pst.executeUpdate();

            if (rows > 0) {
                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    tournoi.setId(rs.getLong(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Erreur add : " + e.getMessage());
        }

        return false;
    }

    // READ ALL
    public List<tournoi> findAll() {
        List<tournoi> tournois = new ArrayList<>();
        if (cnx == null) return tournois;
        String sql = "SELECT id, nomt, datedebutt, datefint, descrit, statutt, logo FROM tournoi";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                tournoi t = new tournoi();
                t.setId(rs.getLong("id"));
                t.setNomt(rs.getString("nomt"));
                t.setDatedebutt(rs.getTimestamp("datedebutt").toLocalDateTime());
                t.setDatefint(rs.getTimestamp("datefint").toLocalDateTime());
                t.setDescrit(rs.getString("descrit"));
                t.setStatutt(rs.getString("statutt"));
                t.setLogo(rs.getString("logo"));
                tournois.add(t);
            }

        } catch (SQLException e) {
            System.out.println("Erreur findAll : " + e.getMessage());
        }

        return tournois;
    }

    // READ BY ID
    public tournoi findById(Long id) {
        if (cnx == null) return null;
        String sql = "SELECT id, nomt, datedebutt, datefint, descrit, statutt, logo FROM tournoi WHERE id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setLong(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                tournoi t = new tournoi();
                t.setId(rs.getLong("id"));
                t.setNomt(rs.getString("nomt"));
                t.setDatedebutt(rs.getTimestamp("datedebutt").toLocalDateTime());
                t.setDatefint(rs.getTimestamp("datefint").toLocalDateTime());
                t.setDescrit(rs.getString("descrit"));
                t.setStatutt(rs.getString("statutt"));
                t.setLogo(rs.getString("logo"));
                return t;
            }

        } catch (SQLException e) {
            System.out.println("Erreur findById : " + e.getMessage());
        }

        return null;
    }

    // UPDATE
    public boolean update(tournoi tournoi) {
        if (cnx == null) return false;
        String sql = "UPDATE tournoi SET nomt=?, datedebutt=?, datefint=?, descrit=?, statutt=?, logo=? WHERE id=?";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, tournoi.getNomt());
            pst.setTimestamp(2, Timestamp.valueOf(tournoi.getDatedebutt()));
            pst.setTimestamp(3, Timestamp.valueOf(tournoi.getDatefint()));
            pst.setString(4, tournoi.getDescrit());
            pst.setString(5, tournoi.getStatutt());
            pst.setString(6, tournoi.getLogo());
            pst.setLong(7, tournoi.getId());

            return pst.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Erreur update : " + e.getMessage());
        }

        return false;
    }

    // DELETE
    public boolean delete(Long id) {
        if (cnx == null) return false;
        String sql = "DELETE FROM tournoi WHERE id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setLong(1, id);
            return pst.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Erreur delete : " + e.getMessage());
        }

        return false;
    }

    // --- PDF simplifié pour Tournoi ---
    public String generatePdf() {
        StringBuilder sb = new StringBuilder();
        sb.append("Liste des Tournois\n");
        sb.append("ID\tNom\tDescription\tStatut\tDate Début\tDate Fin\n");

        for (tournoi t : findAll()) {
            sb.append(t.getId()).append("\t")
                    .append(t.getNomt()).append("\t")
                    .append(t.getDescrit()).append("\t")
                    .append(t.getStatutt()).append("\t")
                    .append(t.getDatedebutt()).append("\t")
                    .append(t.getDatefint()).append("\n");
        }

        return sb.toString();
    }
}