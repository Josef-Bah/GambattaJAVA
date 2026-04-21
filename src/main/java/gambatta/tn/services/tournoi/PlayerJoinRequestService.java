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
    private String lastErrorMessage;

    public PlayerJoinRequestService() {
        cnx = MyDataBase.getInstance();
        equipeService = new EquipeService();
        createTableIfNotExists();
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    private void createTableIfNotExists() {
        try (Statement st = cnx.createStatement()) {
            // On retire la contrainte FOREIGN KEY explicite pour éviter l'erreur 150 (mismatch de type/moteur)
            // L'intégrité sera gérée par le code, mais on garde un INDEX pour la performance.
            String createTableSql = "CREATE TABLE IF NOT EXISTS playerjoinrequest (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "player_name VARCHAR(255), " +
                    "equipe_id BIGINT, " +
                    "status VARCHAR(50), " +
                    "created_at DATETIME, " +
                    "INDEX idx_equipe_request (equipe_id)" +
                    ") ENGINE=InnoDB";
            
            st.execute(createTableSql);
            System.out.println("✅ Table playerjoinrequest vérifiée/créée.");

            // Migration : Ajouter / Renommer les colonnes si nécessaire
            migrateColumn(st, "player_name", "VARCHAR(255) NULL");
            migrateColumn(st, "created_at", "DATETIME NULL");
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur CRITIQUE création table playerjoinrequest : " + e.getMessage());
            this.lastErrorMessage = "Erreur Initialisation Table : " + e.getMessage();
            e.printStackTrace();
        }
    }

    private void migrateColumn(Statement st, String columnName, String definition) {
        try {
            st.execute("ALTER TABLE playerjoinrequest ADD COLUMN " + columnName + " " + definition);
            System.out.println("Colonne '" + columnName + "' ajoutée à playerjoinrequest.");
        } catch (SQLException e) {
            // Ignorer l'erreur si la colonne existe déjà
            if (e.getErrorCode() != 1060 && !e.getSQLState().equals("42S21")) {
                System.err.println("Erreur migration playerjoinrequest (" + columnName + ") : " + e.getMessage());
            }
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
        lastErrorMessage = null;
        String sql = "INSERT INTO playerjoinrequest (player_name, equipe_id, status, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, p.getPlayerName());
            
            if (p.getEquipe() == null) {
                lastErrorMessage = "L'équipe sélectionnée est invalide (null).";
                return false;
            }
            if (p.getEquipe().getId() == null) {
                lastErrorMessage = "L'équipe sélectionnée n'a pas d'identifiant en base (ID=null).";
                return false;
            }

            pst.setLong(2, p.getEquipe().getId());
            pst.setString(3, p.getStatus() != null ? p.getStatus() : playerjoinrequest.STATUS_PENDING);
            pst.setTimestamp(4, Timestamp.valueOf(p.getCreatedAt() != null ? p.getCreatedAt() : LocalDateTime.now()));
            
            pst.executeUpdate();
            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) p.setId(rs.getLong(1));
            return true;
        } catch (SQLException ex) {
            lastErrorMessage = "Erreur SQL : " + ex.getMessage();
            ex.printStackTrace();
        }
        return false;
    }

    private boolean update(playerjoinrequest p) {
        String sql = "UPDATE playerjoinrequest SET player_name=?, equipe_id=?, status=?, created_at=? WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, p.getPlayerName());
            pst.setLong(2, p.getEquipe().getId());
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
        p.setPlayerName(rs.getString("player_name"));
        
        long equipeId = rs.getLong("equipe_id");
        p.setEquipe(equipeService.findById(equipeId));
        
        p.setStatus(rs.getString("status"));
        
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            p.setCreatedAt(ts.toLocalDateTime());
        }
        
        return p;
    }
}
