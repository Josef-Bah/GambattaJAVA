package gambatta.tn.services.tournoi;

import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.entites.tournois.inscriptiontournoi;
import gambatta.tn.entites.tournois.tournoi;
import gambatta.tn.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InscritournoiService {

    private Connection cnx;

    public InscritournoiService() {
        this.cnx = MyDataBase.getInstance();
    }

    // Index: list all inscriptions
    public List<inscriptiontournoi> index() {
        List<inscriptiontournoi> inscriptions = new ArrayList<>();
        String sql = "SELECT i.id, i.status, e.id as equipe_id, e.nom as equipe_nom, t.id as tournoi_id, t.nomt as tournoi_nomt " +
                     "FROM inscritournoi i " +
                     "LEFT JOIN equipe e ON i.equipe_id = e.id " +
                     "LEFT JOIN tournoi t ON i.tournoi_id = t.id";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                inscriptiontournoi i = new inscriptiontournoi();
                i.setId(rs.getLong("id"));
                i.setStatus(rs.getString("status"));

                equipe eq = new equipe();
                eq.setId(rs.getLong("equipe_id"));
                eq.setNom(rs.getString("equipe_nom"));
                i.setEquipe(eq);

                tournoi t = new tournoi();
                t.setId(rs.getLong("tournoi_id"));
                t.setNomt(rs.getString("tournoi_nomt"));
                i.setTournoi(t);

                inscriptions.add(i);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inscriptions;
    }

    // Find by id
    public inscriptiontournoi findById(Long id) {
        String sql = "SELECT i.id, i.status, e.id as equipe_id, e.nom as equipe_nom, t.id as tournoi_id, t.nomt as tournoi_nomt " +
                     "FROM inscritournoi i " +
                     "LEFT JOIN equipe e ON i.equipe_id = e.id " +
                     "LEFT JOIN tournoi t ON i.tournoi_id = t.id " +
                     "WHERE i.id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setLong(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                inscriptiontournoi i = new inscriptiontournoi();
                i.setId(rs.getLong("id"));
                i.setStatus(rs.getString("status"));

                equipe eq = new equipe();
                eq.setId(rs.getLong("equipe_id"));
                eq.setNom(rs.getString("equipe_nom"));
                i.setEquipe(eq);

                tournoi t = new tournoi();
                t.setId(rs.getLong("tournoi_id"));
                t.setNomt(rs.getString("tournoi_nomt"));
                i.setTournoi(t);

                return i;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Save
    public boolean save(inscriptiontournoi inscription) {
        if (inscription.getId() == null) {
            String sql = "INSERT INTO inscritournoi (equipe_id, tournoi_id, status) VALUES (?, ?, ?)";
            try (PreparedStatement pst = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pst.setLong(1, inscription.getEquipe().getId());
                pst.setLong(2, inscription.getTournoi().getId());
                pst.setString(3, inscription.getStatus());
                pst.executeUpdate();
                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    inscription.setId(rs.getLong(1));
                }
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "UPDATE inscritournoi SET equipe_id=?, tournoi_id=?, status=? WHERE id=?";
            try (PreparedStatement pst = cnx.prepareStatement(sql)) {
                pst.setLong(1, inscription.getEquipe().getId());
                pst.setLong(2, inscription.getTournoi().getId());
                pst.setString(3, inscription.getStatus());
                pst.setLong(4, inscription.getId());
                pst.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // Delete
    public boolean delete(Long id) {
        String sql = "DELETE FROM inscritournoi WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setLong(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Generate PDF (simplified)
    public String generatePdf() {
        List<inscriptiontournoi> inscriptions = index();
        StringBuilder pdfContent = new StringBuilder();
        pdfContent.append("Registre des Inscriptions\n");
        pdfContent.append("ID\tÉQUIPE\tTOURNOI\tSTATUT\n");
        for (int i = 0; i < inscriptions.size(); i++) {
            inscriptiontournoi ins = inscriptions.get(i);
            String equipe = ins.getEquipe() != null ? ins.getEquipe().getNom() : "-";
            String tournoi = ins.getTournoi() != null ? ins.getTournoi().getNomt() : "-";
            pdfContent.append((i + 1)).append("\t")
                      .append(equipe).append("\t")
                      .append(tournoi).append("\t")
                      .append(ins.getStatus() != null ? ins.getStatus() : "-").append("\n");
        }
        return pdfContent.toString();
    }

    // Stats
    public Map<String, Object> stats() {
        Map<String, Object> stats = new HashMap<>();
        String sqlTotal = "SELECT COUNT(*) as total FROM inscritournoi";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sqlTotal)) {
            if (rs.next()) {
                stats.put("totalInscriptions", rs.getInt("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<Map<String, Object>> inscriptionsByStatus = new ArrayList<>();
        String sqlStatus = "SELECT status, COUNT(*) as count FROM inscritournoi GROUP BY status";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sqlStatus)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("status", rs.getString("status"));
                row.put("count", rs.getInt("count"));
                inscriptionsByStatus.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        stats.put("inscriptionsByStatus", inscriptionsByStatus);
        return stats;
    }

    // Stats JSON (return map, can be converted to JSON)
    public Map<String, Object> statsJson() {
        return stats();
    }
}
