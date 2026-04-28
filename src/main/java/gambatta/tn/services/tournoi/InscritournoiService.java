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
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        if (cnx == null) return;
        String sql = "CREATE TABLE IF NOT EXISTS inscritournoi (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "equipe_id BIGINT, " +
                "tournoi_id BIGINT, " +
                "status VARCHAR(50) DEFAULT 'PENDING', " +
                "date_inscrit DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (equipe_id) REFERENCES equipe(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (tournoi_id) REFERENCES tournoi(id) ON DELETE CASCADE" +
                ")";
        try (Statement st = cnx.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            System.err.println("Table inscritournoi may exist. " + e.getMessage());
        }
    }

    // READ ALL
    public List<inscriptiontournoi> findAll() {
        List<inscriptiontournoi> inscriptions = new ArrayList<>();
        if (cnx == null) return inscriptions;
        String sql = "SELECT i.id, i.status, i.date_inscrit, e.id AS equipe_id, e.nom AS equipe_nom, t.id AS tournoi_id, t.nomt AS tournoi_nomt " +
                "FROM inscritournoi i " +
                "LEFT JOIN equipe e ON i.equipe_id = e.id " +
                "LEFT JOIN tournoi t ON i.tournoi_id = t.id";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                inscriptiontournoi i = new inscriptiontournoi();
                i.setId(rs.getLong("id"));
                if (rs.getTimestamp("date_inscrit") != null) {
                    i.setDateInscrit(rs.getTimestamp("date_inscrit").toLocalDateTime());
                }

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

    // FIND BY ID
    public inscriptiontournoi findById(Long id) {
        if (cnx == null) return null;
        String sql = "SELECT i.id, i.status, i.date_inscrit, e.id AS equipe_id, e.nom AS equipe_nom, t.id AS tournoi_id, t.nomt AS tournoi_nomt " +
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
                if (rs.getTimestamp("date_inscrit") != null) {
                    i.setDateInscrit(rs.getTimestamp("date_inscrit").toLocalDateTime());
                }

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

    // SAVE / UPDATE
    public boolean save(inscriptiontournoi inscription) {
        if (cnx == null) return false;
        if (inscription.getId() == null || inscription.getId() == 0) {
            // INSERT
            String sql = "INSERT INTO inscritournoi (equipe_id, tournoi_id, status, date_inscrit) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pst = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pst.setLong(1, inscription.getEquipe().getId());
                pst.setLong(2, inscription.getTournoi().getId());
                pst.setString(3, inscription.getStatus());
                pst.setTimestamp(4, Timestamp.valueOf(inscription.getDateInscrit()));
                pst.executeUpdate();
                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    inscription.setId(rs.getLong(1));
                }
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            // UPDATE
            String sql = "UPDATE inscritournoi SET equipe_id=?, tournoi_id=?, status=?, date_inscrit=? WHERE id=?";
            try (PreparedStatement pst = cnx.prepareStatement(sql)) {
                pst.setLong(1, inscription.getEquipe().getId());
                pst.setLong(2, inscription.getTournoi().getId());
                pst.setString(3, inscription.getStatus());
                pst.setTimestamp(4, Timestamp.valueOf(inscription.getDateInscrit()));
                pst.setLong(5, inscription.getId());
                pst.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    // DELETE
    public boolean delete(Long id) {
        if (cnx == null) return false;
        String sql = "DELETE FROM inscritournoi WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setLong(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // GENERATE PDF (simplified)
    public String generatePdf() {
        List<inscriptiontournoi> list = findAll();
        StringBuilder pdfContent = new StringBuilder();
        pdfContent.append("Registre des Inscriptions\n");
        pdfContent.append("ID\tÉQUIPE\tTOURNOI\tSTATUT\n");
        for (int i = 0; i < list.size(); i++) {
            inscriptiontournoi ins = list.get(i);
            String equipeName = ins.getEquipe() != null ? ins.getEquipe().getNom() : "-";
            String tournoiName = ins.getTournoi() != null ? ins.getTournoi().getNomt() : "-";
            pdfContent.append((i + 1))
                    .append("\t").append(equipeName)
                    .append("\t").append(tournoiName)
                    .append("\t").append(ins.getStatus() != null ? ins.getStatus() : "-")
                    .append("\n");
        }
        return pdfContent.toString();
    }

    // STATISTICS
    public Map<String, Object> stats() {
        Map<String, Object> stats = new HashMap<>();
        if (cnx == null) return stats;
        String sqlTotal = "SELECT COUNT(*) as total FROM inscritournoi";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sqlTotal)) {
            if (rs.next()) {
                stats.put("totalInscriptions", rs.getInt("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<Map<String, Object>> byStatus = new ArrayList<>();
        String sqlStatus = "SELECT status, COUNT(*) as count FROM inscritournoi GROUP BY status";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sqlStatus)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("status", rs.getString("status"));
                row.put("count", rs.getInt("count"));
                byStatus.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        stats.put("inscriptionsByStatus", byStatus);
        return stats;
    }
}