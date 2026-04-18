package gambatta.tn.services.tournoi;

import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.entites.tournois.playerjoinrequest;
import gambatta.tn.tools.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PlayerJoinRequestService {

    private Connection cnx;
    private EquipeService equipeService;

    public PlayerJoinRequestService() {
        cnx = MyDataBase.getInstance();
        equipeService = new EquipeService();
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS playerjoinrequest (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "playerName VARCHAR(255), " +
                "equipe_id BIGINT, " +
                "status VARCHAR(50), " +
                "createdAt DATETIME, " +
                "FOREIGN KEY (equipe_id) REFERENCES equipe(id) ON DELETE CASCADE" +
                ")";
        try (Statement st = cnx.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            System.err.println("Table playerjoinrequest may exist. " + e.getMessage());
        }
    }

    public List<playerjoinrequest> findAll() {
        List<playerjoinrequest> list = new ArrayList<>();
        String sql = "SELECT * FROM playerjoinrequest";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRequest(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public List<playerjoinrequest> findByEquipe(Long equipeId) {
        List<playerjoinrequest> list = new ArrayList<>();
        String sql = "SELECT * FROM playerjoinrequest WHERE equipe_id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setLong(1, equipeId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(mapRequest(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public boolean save(playerjoinrequest p) {
        if (p.getId() == null || p.getId() == 0) {
            return add(p);
        } else {
            return update(p);
        }
    }

    private boolean add(playerjoinrequest p) {
        String sql = "INSERT INTO playerjoinrequest (playerName, equipe_id, status, createdAt) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, p.getPlayerName());
            // Utiliser idProperty().get() pour éviter le null quand id=0
            long equipeId = p.getEquipe().idProperty().get();
            if (equipeId == 0) {
                System.err.println("[PlayerJoinRequestService] ERREUR: equipe_id est 0, l'équipe n'a pas d'id valide.");
                return false;
            }
            pst.setLong(2, equipeId);
            pst.setString(3, p.getStatus() != null ? p.getStatus() : playerjoinrequest.STATUS_PENDING);
            pst.setTimestamp(4, Timestamp.valueOf(p.getCreatedAt() != null ? p.getCreatedAt() : LocalDateTime.now()));
            
            pst.executeUpdate();
            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) p.setId(rs.getLong(1));
            return true;
        } catch (SQLException ex) {
            System.err.println("[PlayerJoinRequestService] SQL Error add: " + ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }

    private boolean update(playerjoinrequest p) {
        String sql = "UPDATE playerjoinrequest SET playerName=?, equipe_id=?, status=?, createdAt=? WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, p.getPlayerName());
            long equipeId = p.getEquipe().idProperty().get();
            pst.setLong(2, equipeId);
            pst.setString(3, p.getStatus());
            pst.setTimestamp(4, Timestamp.valueOf(p.getCreatedAt() != null ? p.getCreatedAt() : LocalDateTime.now()));
            pst.setLong(5, p.getId());
            return pst.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[PlayerJoinRequestService] SQL Error update: " + ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM playerjoinrequest WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setLong(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private playerjoinrequest mapRequest(ResultSet rs) throws SQLException {
        playerjoinrequest p = new playerjoinrequest();
        p.setId(rs.getLong("id"));
        p.setPlayerName(rs.getString("playerName"));
        
        long equipeId = rs.getLong("equipe_id");
        p.setEquipe(equipeService.findById(equipeId));
        
        p.setStatus(rs.getString("status"));
        
        Timestamp ts = rs.getTimestamp("createdAt");
        if (ts != null) {
            p.setCreatedAt(ts.toLocalDateTime());
        }
        
        return p;
    }
}
