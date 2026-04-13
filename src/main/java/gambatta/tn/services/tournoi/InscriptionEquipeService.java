package gambatta.tn.services.tournoi;

import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.entites.tournois.tournoi;
import gambatta.tn.entites.tournois.inscriptiontournoi;
import gambatta.tn.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InscriptionEquipeService {

    private Connection cnx;

    public InscriptionEquipeService() {
        this.cnx = MyDataBase.getInstance(); // Singleton JDBC
    }

    // CREATE ou UPDATE
    public boolean save(inscriptiontournoi insc) {
        if (insc.getId() == null) {
            // INSERT
            String sql = "INSERT INTO inscriptiontournoi (equipe_id, tournoi_id, status) VALUES (?, ?, ?)";
            try (PreparedStatement pst = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pst.setLong(1, insc.getEquipe().getId());
                pst.setLong(2, insc.getTournoi().getId());
                pst.setString(3, insc.getStatus());
                int rows = pst.executeUpdate();
                if (rows > 0) {
                    ResultSet rs = pst.getGeneratedKeys();
                    if (rs.next()) {
                        insc.setId(rs.getLong(1));
                    }
                    return true;
                }
            } catch (SQLException e) {
                System.out.println("Erreur save (INSERT) : " + e.getMessage());
            }
        } else {
            // UPDATE
            String sql = "UPDATE inscriptiontournoi SET equipe_id=?, tournoi_id=?, status=? WHERE id=?";
            try (PreparedStatement pst = cnx.prepareStatement(sql)) {
                pst.setLong(1, insc.getEquipe().getId());
                pst.setLong(2, insc.getTournoi().getId());
                pst.setString(3, insc.getStatus());
                pst.setLong(4, insc.getId());
                return pst.executeUpdate() > 0;
            } catch (SQLException e) {
                System.out.println("Erreur save (UPDATE) : " + e.getMessage());
            }
        }
        return false;
    }

    // READ ALL
    public List<inscriptiontournoi> findAll() {
        List<inscriptiontournoi> list = new ArrayList<>();
        String sql = "SELECT i.id, i.status, i.equipe_id, i.tournoi_id, " +
                "e.nom as equipe_nom, t.nomt as tournoi_nom, t.datedebutt, t.datefint, t.statutt " +
                "FROM inscriptiontournoi i " +
                "JOIN equipe e ON i.equipe_id = e.id " +
                "JOIN tournoi t ON i.tournoi_id = t.id";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                inscriptiontournoi insc = new inscriptiontournoi();

                // Equipe
                equipe e = new equipe();
                e.setId(rs.getLong("equipe_id"));
                e.setNom(rs.getString("equipe_nom"));

                // Tournoi
                tournoi t = new tournoi();
                t.setId(rs.getLong("tournoi_id"));
                t.setNomt(rs.getString("tournoi_nom"));
                t.setDatedebutt(rs.getTimestamp("datedebutt").toLocalDateTime());
                t.setDatefint(rs.getTimestamp("datefint").toLocalDateTime());
                t.setStatutt(rs.getString("statutt"));

                insc.setId(rs.getLong("id"));
                insc.setEquipe(e);
                insc.setTournoi(t);
                insc.setStatus(rs.getString("status"));

                list.add(insc);
            }

        } catch (SQLException e) {
            System.out.println("Erreur findAll : " + e.getMessage());
        }
        return list;
    }

    // DELETE
    public boolean delete(Long id) {
        String sql = "DELETE FROM inscriptiontournoi WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setLong(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erreur delete : " + e.getMessage());
        }
        return false;
    }

    // FIND BY ID
    public inscriptiontournoi findById(Long id) {
        String sql = "SELECT i.id, i.status, i.equipe_id, i.tournoi_id, " +
                "e.nom as equipe_nom, t.nomt as tournoi_nom, t.datedebutt, t.datefint, t.statutt " +
                "FROM inscriptiontournoi i " +
                "JOIN equipe e ON i.equipe_id = e.id " +
                "JOIN tournoi t ON i.tournoi_id = t.id " +
                "WHERE i.id=?";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setLong(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                inscriptiontournoi insc = new inscriptiontournoi();

                equipe e = new equipe();
                e.setId(rs.getLong("equipe_id"));
                e.setNom(rs.getString("equipe_nom"));

                tournoi t = new tournoi();
                t.setId(rs.getLong("tournoi_id"));
                t.setNomt(rs.getString("tournoi_nom"));
                t.setDatedebutt(rs.getTimestamp("datedebutt").toLocalDateTime());
                t.setDatefint(rs.getTimestamp("datefint").toLocalDateTime());
                t.setStatutt(rs.getString("statutt"));

                insc.setId(rs.getLong("id"));
                insc.setEquipe(e);
                insc.setTournoi(t);
                insc.setStatus(rs.getString("status"));

                return insc;
            }
        } catch (SQLException e) {
            System.out.println("Erreur findById : " + e.getMessage());
        }
        return null;
    }
}