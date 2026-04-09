package gambatta.tn.services.tournoi;

import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.entites.tournois.inscriptiontournoi;
import gambatta.tn.entites.tournois.playerjoinrequest;
import gambatta.tn.entites.tournois.tournoi;
import gambatta.tn.tools.MyDataBase;

import java.sql.Connection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EquipeService {

    private Connection cnx;

    public EquipeService() {
        cnx = MyDataBase.getInstance();
    }

    // Index method: list equipes with search, sort, pagination
    public List<equipe> index(String searchTerm, String sortField, String sortOrder, int page, int limit) {
        List<equipe> equipes = new ArrayList<>();
        String sql = "SELECT id, nom, teamLeader, titres, objectifs, coach, logo, joinApprovalMode, status FROM equipe WHERE 1=1";
        if (searchTerm != null && !searchTerm.isEmpty()) {
            sql += " AND nom LIKE ?";
        }
        if ("nom".equals(sortField) || "teamLeader".equals(sortField)) {
            sql += " ORDER BY " + sortField + " " + ("desc".equals(sortOrder) ? "DESC" : "ASC");
        }
        sql += " LIMIT ? OFFSET ?";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            int paramIndex = 1;
            if (searchTerm != null && !searchTerm.isEmpty()) {
                pst.setString(paramIndex++, "%" + searchTerm + "%");
            }
            pst.setInt(paramIndex++, limit);
            pst.setInt(paramIndex++, (page - 1) * limit);

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                equipe e = new equipe();
                e.setId(rs.getLong("id"));
                e.setNom(rs.getString("nom"));
                e.setTeamLeader(rs.getString("teamLeader"));
                e.setTitres(rs.getString("titres"));
                e.setObjectifs(rs.getString("objectifs"));
                e.setCoach(rs.getString("coach"));
                e.setLogo(rs.getString("logo"));
                e.setJoinApprovalMode(rs.getString("joinApprovalMode"));
                e.setStatus(rs.getString("status"));
                equipes.add(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return equipes;
    }

    // Status counts
    public Map<String, Integer> getStatusCounts() {
        Map<String, Integer> statusCounts = new HashMap<>();
        statusCounts.put("EN_ATTENTE", 0);
        statusCounts.put("ACCEPTEE", 0);
        statusCounts.put("REFUSEE", 0);

        String sql = "SELECT status, COUNT(*) as count FROM equipe GROUP BY status";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String status = rs.getString("status").toUpperCase();
                if (statusCounts.containsKey(status)) {
                    statusCounts.put(status, rs.getInt("count"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statusCounts;
    }

    // Active tournois
    public List<tournoi> getActiveTournois() {
        List<tournoi> tournois = new ArrayList<>();
        String sql = "SELECT id, nomt, datedebutt, datefint, descrit, statutt FROM tournoi WHERE statutt IN ('ouvert', 'en_cours') AND datefint >= ?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setDate(1, Date.valueOf(java.time.LocalDate.now().plusDays(1)));
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                tournoi t = new tournoi();
                t.setId(rs.getLong("id"));
                t.setNomt(rs.getString("nomt"));
                t.setDatedebutt(rs.getTimestamp("datedebutt").toLocalDateTime());
                t.setDatefint(rs.getTimestamp("datefint").toLocalDateTime());
                t.setDescrit(rs.getString("descrit"));
                t.setStatutt(rs.getString("statutt"));
                tournois.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tournois;
    }

    // Generate PDF (simplified, return string representation)
    public String generatePdf() {
        List<equipe> equipes = findAll();
        StringBuilder pdfContent = new StringBuilder();
        pdfContent.append("Registre des Équipes\n");
        pdfContent.append("ID\tNOM\tTEAM LEADER\tTITRES\n");
        for (int i = 0; i < equipes.size(); i++) {
            equipe e = equipes.get(i);
            pdfContent.append((i + 1)).append("\t")
                      .append(e.getNom() != null ? e.getNom() : "-").append("\t")
                      .append(e.getTeamLeader() != null ? e.getTeamLeader() : "Non défini").append("\t")
                      .append(e.getTitres() != null ? e.getTitres() : "Aucun titre").append("\n");
        }
        return pdfContent.toString();
    }

    // Stats
    public Map<String, Object> stats() {
        Map<String, Object> stats = new HashMap<>();
        String sqlTotal = "SELECT COUNT(*) as total FROM equipe";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sqlTotal)) {
            if (rs.next()) {
                stats.put("totalEquipes", rs.getInt("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<Map<String, Object>> equipesByLeader = new ArrayList<>();
        String sqlLeader = "SELECT COALESCE(teamLeader, 'Non défini') as leader, COUNT(*) as count FROM equipe GROUP BY teamLeader";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sqlLeader)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("leader", rs.getString("leader"));
                row.put("count", rs.getInt("count"));
                equipesByLeader.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        stats.put("equipesByLeader", equipesByLeader);
        return stats;
    }

    // New equipe
    public boolean save(equipe equipe) {
        if (equipe.getId() == null) {
            // Check existing
            if (findByNom(equipe.getNom()) != null) {
                return false; // Exists
            }
            String sql = "INSERT INTO equipe (nom, teamLeader, titres, objectifs, coach, logo, joinApprovalMode, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pst = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pst.setString(1, equipe.getNom());
                pst.setString(2, equipe.getTeamLeader());
                pst.setString(3, equipe.getTitres());
                pst.setString(4, equipe.getObjectifs());
                pst.setString(5, equipe.getCoach());
                pst.setString(6, equipe.getLogo());
                pst.setString(7, equipe.getJoinApprovalMode());
                pst.setString(8, equipe.getStatus());
                pst.executeUpdate();
                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    equipe.setId(rs.getLong(1));
                }
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "UPDATE equipe SET nom=?, teamLeader=?, titres=?, objectifs=?, coach=?, logo=?, joinApprovalMode=?, status=? WHERE id=?";
            try (PreparedStatement pst = cnx.prepareStatement(sql)) {
                pst.setString(1, equipe.getNom());
                pst.setString(2, equipe.getTeamLeader());
                pst.setString(3, equipe.getTitres());
                pst.setString(4, equipe.getObjectifs());
                pst.setString(5, equipe.getCoach());
                pst.setString(6, equipe.getLogo());
                pst.setString(7, equipe.getJoinApprovalMode());
                pst.setString(8, equipe.getStatus());
                pst.setLong(9, equipe.getId());
                pst.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // Find all
    public List<equipe> findAll() {
        return index(null, "nom", "asc", 1, Integer.MAX_VALUE);
    }

    // Find by id
    public equipe findById(Long id) {
        String sql = "SELECT id, nom, teamLeader, titres, objectifs, coach, logo, joinApprovalMode, status FROM equipe WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setLong(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                equipe e = new equipe();
                e.setId(rs.getLong("id"));
                e.setNom(rs.getString("nom"));
                e.setTeamLeader(rs.getString("teamLeader"));
                e.setTitres(rs.getString("titres"));
                e.setObjectifs(rs.getString("objectifs"));
                e.setCoach(rs.getString("coach"));
                e.setLogo(rs.getString("logo"));
                e.setJoinApprovalMode(rs.getString("joinApprovalMode"));
                e.setStatus(rs.getString("status"));
                return e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Find by nom
    public equipe findByNom(String nom) {
        if (cnx == null) {
            throw new RuntimeException("Connexion DB null dans EquipeService");
        }

        String sql = "SELECT id, nom, teamLeader, titres, objectifs, coach, logo, joinApprovalMode, status FROM equipe WHERE nom = ?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, nom);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                equipe e = new equipe();
                e.setId(rs.getLong("id"));
                e.setNom(rs.getString("nom"));
                e.setTeamLeader(rs.getString("teamLeader"));
                e.setTitres(rs.getString("titres"));
                e.setObjectifs(rs.getString("objectifs"));
                e.setCoach(rs.getString("coach"));
                e.setLogo(rs.getString("logo"));
                e.setJoinApprovalMode(rs.getString("joinApprovalMode"));
                e.setStatus(rs.getString("status"));
                return e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Delete
    public boolean delete(Long id) {
        String sql = "DELETE FROM equipe WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setLong(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Join tournoi
    public boolean joinTournoi(Long equipeId, Long tournoiId) {
        // Check if already joined
        String checkSql = "SELECT id FROM inscritournoi WHERE equipe_id = ? AND tournoi_id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(checkSql)) {
            pst.setLong(1, equipeId);
            pst.setLong(2, tournoiId);
            if (pst.executeQuery().next()) {
                return false; // Already joined
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        equipe equipeObj = findById(equipeId);
        if (equipeObj == null) return false;

        String status = equipe.JOIN_APPROVAL_BY_LEADER.equals(equipeObj.getJoinApprovalMode()) ? inscriptiontournoi.STATUS_ACCEPTED : inscriptiontournoi.STATUS_PENDING;

        String sql = "INSERT INTO inscritournoi (equipe_id, tournoi_id, status) VALUES (?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setLong(1, equipeId);
            pst.setLong(2, tournoiId);
            pst.setString(3, status);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Request join
    public boolean requestJoin(String playerName, Long equipeId) {
        String sql = "INSERT INTO playerjoinrequest (player_name, equipe_id, status, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, playerName);
            pst.setLong(2, equipeId);
            pst.setString(3, playerjoinrequest.STATUS_PENDING);
            pst.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
