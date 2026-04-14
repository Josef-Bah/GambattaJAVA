package gambatta.tn.services.tournoi;

import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.entites.tournois.inscriptiontournoi;
import gambatta.tn.entites.tournois.playerjoinrequest;
import gambatta.tn.entites.tournois.tournoi;
import gambatta.tn.tools.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EquipeService {

    private Connection cnx;

    public EquipeService() {
        cnx = MyDataBase.getInstance();
    }

    // --- CRUD COMPLET ---

    public List<equipe> findAll() {
        List<equipe> list = new ArrayList<>();
        String sql = "SELECT id, nom, team_leader, titres, objectifs, coach, logo, join_approval_mode, status FROM equipe";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                equipe e = mapEquipe(rs);
                list.add(e);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public equipe findById(Long id) {
        String sql = "SELECT * FROM equipe WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setLong(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return mapEquipe(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ✅ Nouvelle méthode pour findByName (appelée depuis InscriptionController)
    public equipe findByName(String nom) {
        String sql = "SELECT * FROM equipe WHERE nom=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, nom);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return mapEquipe(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean save(equipe e) {
        if (e.getId() == null || e.getId() == 0) {
            return add(e);
        } else {
            return update(e);
        }
    }

    private boolean add(equipe e) {
        if (findByName(e.getNom()) != null) return false;
        String sql = "INSERT INTO equipe (nom, team_leader, titres, objectifs, coach, logo, join_approval_mode, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, e.getNom());
            pst.setString(2, e.getTeamLeader());
            pst.setString(3, e.getTitres());
            pst.setString(4, e.getObjectifs());
            pst.setString(5, e.getCoach());
            pst.setString(6, e.getLogo());
            pst.setString(7, e.getJoinApprovalMode());
            pst.setString(8, e.getStatus());
            pst.executeUpdate();
            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) e.setId(rs.getLong(1));
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private boolean update(equipe e) {
        String sql = "UPDATE equipe SET nom=?, team_leader=?, titres=?, objectifs=?, coach=?, logo=?, join_approval_mode=?, status=? WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, e.getNom());
            pst.setString(2, e.getTeamLeader());
            pst.setString(3, e.getTitres());
            pst.setString(4, e.getObjectifs());
            pst.setString(5, e.getCoach());
            pst.setString(6, e.getLogo());
            pst.setString(7, e.getJoinApprovalMode());
            pst.setString(8, e.getStatus());
            pst.setLong(9, e.getId());
            return pst.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM equipe WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setLong(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- Méthode utilitaire pour mapper ResultSet → Equipe ---
    private equipe mapEquipe(ResultSet rs) throws SQLException {
        equipe e = new equipe();
        e.setId(rs.getLong("id"));
        e.setNom(rs.getString("nom"));
        e.setTeamLeader(rs.getString("team_leader"));
        e.setTitres(rs.getString("titres"));
        e.setObjectifs(rs.getString("objectifs"));
        e.setCoach(rs.getString("coach"));
        e.setLogo(rs.getString("logo"));
        e.setJoinApprovalMode(rs.getString("join_approval_mode"));
        e.setStatus(rs.getString("status"));
        return e;
    }

    // --- PDF simplifié ---
    public String generatePdf() {
        StringBuilder sb = new StringBuilder();
        sb.append("Liste des équipes\nID\tNom\tLeader\tTitres\n");
        for (equipe e : findAll()) {
            sb.append(e.getId()).append("\t")
                    .append(e.getNom()).append("\t")
                    .append(e.getTeamLeader()).append("\t")
                    .append(e.getTitres()).append("\n");
        }
        return sb.toString();
    }
}