package gambatta.tn.services.tournoi;

import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.entites.tournois.rencontre;
import gambatta.tn.entites.tournois.tournoi;
import gambatta.tn.tools.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RencontreService {

    private Connection cnx;
    private EquipeService equipeService;
    private TournoiService tournoiService;

    public RencontreService() {
        cnx = MyDataBase.getInstance();
        equipeService = new EquipeService();
        tournoiService = new TournoiService();
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS rencontre (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "tournoi_id BIGINT, " +
                "equipeA_id BIGINT, " +
                "equipeB_id BIGINT, " +
                "scoreA INT NULL, " +
                "scoreB INT NULL, " +
                "playedAt DATETIME NULL, " +
                "FOREIGN KEY (tournoi_id) REFERENCES tournoi(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (equipeA_id) REFERENCES equipe(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (equipeB_id) REFERENCES equipe(id) ON DELETE CASCADE" +
                ")";
        try (Statement st = cnx.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            System.err.println("Table rencontre may exist. " + e.getMessage());
        }
    }

    public List<rencontre> findAll() {
        List<rencontre> list = new ArrayList<>();
        String sql = "SELECT * FROM rencontre";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRencontre(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public List<rencontre> findByTournoi(Long tournoiId) {
        List<rencontre> list = new ArrayList<>();
        String sql = "SELECT * FROM rencontre WHERE tournoi_id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setLong(1, tournoiId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(mapRencontre(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public boolean save(rencontre r) {
        if (r.getId() == null || r.getId() == 0) {
            return add(r);
        } else {
            return update(r);
        }
    }

    private boolean add(rencontre r) {
        String sql = "INSERT INTO rencontre (tournoi_id, equipeA_id, equipeB_id, scoreA, scoreB, playedAt) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setLong(1, r.getTournoi().getId());
            pst.setLong(2, r.getEquipeA().getId());
            pst.setLong(3, r.getEquipeB().getId());
            if (r.getScoreA() != null) pst.setInt(4, r.getScoreA()); else pst.setNull(4, Types.INTEGER);
            if (r.getScoreB() != null) pst.setInt(5, r.getScoreB()); else pst.setNull(5, Types.INTEGER);
            if (r.getPlayedAt() != null) pst.setTimestamp(6, Timestamp.valueOf(r.getPlayedAt())); else pst.setNull(6, Types.TIMESTAMP);
            
            pst.executeUpdate();
            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) r.setId(rs.getLong(1));
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private boolean update(rencontre r) {
        String sql = "UPDATE rencontre SET tournoi_id=?, equipeA_id=?, equipeB_id=?, scoreA=?, scoreB=?, playedAt=? WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setLong(1, r.getTournoi().getId());
            pst.setLong(2, r.getEquipeA().getId());
            pst.setLong(3, r.getEquipeB().getId());
            if (r.getScoreA() != null) pst.setInt(4, r.getScoreA()); else pst.setNull(4, Types.INTEGER);
            if (r.getScoreB() != null) pst.setInt(5, r.getScoreB()); else pst.setNull(5, Types.INTEGER);
            if (r.getPlayedAt() != null) pst.setTimestamp(6, Timestamp.valueOf(r.getPlayedAt())); else pst.setNull(6, Types.TIMESTAMP);
            pst.setLong(7, r.getId());
            
            return pst.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM rencontre WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setLong(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private rencontre mapRencontre(ResultSet rs) throws SQLException {
        rencontre r = new rencontre();
        r.setId(rs.getLong("id"));
        
        long tournoiId = rs.getLong("tournoi_id");
        r.setTournoi(tournoiService.findById(tournoiId));
        
        long equipeAId = rs.getLong("equipeA_id");
        r.setEquipeA(equipeService.findById(equipeAId));
        
        long equipeBId = rs.getLong("equipeB_id");
        r.setEquipeB(equipeService.findById(equipeBId));
        
        int scoreA = rs.getInt("scoreA");
        if (!rs.wasNull()) r.setScoreA(scoreA);
        
        int scoreB = rs.getInt("scoreB");
        if (!rs.wasNull()) r.setScoreB(scoreB);
        
        Timestamp ts = rs.getTimestamp("playedAt");
        if (ts != null) r.setPlayedAt(ts.toLocalDateTime());
        
        return r;
    }
}
