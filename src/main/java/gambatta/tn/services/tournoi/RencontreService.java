package gambatta.tn.services.tournoi;

import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.entites.tournois.rencontre;
import gambatta.tn.entites.tournois.tournoi;
import gambatta.tn.tools.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RencontreService {

    private Connection cnx;
    private EquipeService equipeService;
    private TournoiService tournoiService;
    private String lastErrorMessage;

    public RencontreService() {
        cnx = MyDataBase.getInstance();
        equipeService = new EquipeService();
        tournoiService = new TournoiService();
        createTableIfNotExists();
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    private void createTableIfNotExists() {
        String createTableSql = "CREATE TABLE IF NOT EXISTS rencontre (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "tournoi_id BIGINT, " +
                "equipe_a_id BIGINT, " +
                "equipe_b_id BIGINT, " +
                "score_a INT NULL, " +
                "score_b INT NULL, " +
                "played_at DATETIME NULL, " +
                "stage VARCHAR(50) NULL, " +
                "FOREIGN KEY (tournoi_id) REFERENCES tournoi(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (equipe_a_id) REFERENCES equipe(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (equipe_b_id) REFERENCES equipe(id) ON DELETE CASCADE" +
                ")";
        try (Statement st = cnx.createStatement()) {
            st.execute(createTableSql);
            
            // Migration : Ajouter les nouvelles colonnes si elles n'existent pas
            migrateColumn(st, "equipe_a_id", "BIGINT NULL");
            migrateColumn(st, "equipe_b_id", "BIGINT NULL");
            migrateColumn(st, "score_a", "INT NULL");
            migrateColumn(st, "score_b", "INT NULL");
            migrateColumn(st, "played_at", "DATETIME NULL");
            migrateColumn(st, "stage", "VARCHAR(50) NULL");
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création/vérification de la table rencontre : " + e.getMessage());
        }
    }

    private void migrateColumn(Statement st, String columnName, String definition) {
        try {
            st.execute("ALTER TABLE rencontre ADD COLUMN " + columnName + " " + definition);
            System.out.println("Colonne '" + columnName + "' ajoutée à la table rencontre.");
        } catch (SQLException e) {
            // Ignorer si la colonne existe déjà (Erreur 1060 en MySQL)
            if (e.getErrorCode() != 1060 && !e.getSQLState().equals("42S21")) {
                System.err.println("Erreur migration (" + columnName + ") : " + e.getMessage());
            }
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
        lastErrorMessage = null;
        String sql = "INSERT INTO rencontre (tournoi_id, equipe_a_id, equipe_b_id, score_a, score_b, played_at, stage) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setLong(1, r.getTournoi().getId());
            pst.setLong(2, r.getEquipeA().getId());
            pst.setLong(3, r.getEquipeB().getId());
            if (r.getScoreA() != null) pst.setInt(4, r.getScoreA()); else pst.setNull(4, Types.INTEGER);
            if (r.getScoreB() != null) pst.setInt(5, r.getScoreB()); else pst.setNull(5, Types.INTEGER);
            if (r.getPlayedAt() != null) pst.setTimestamp(6, Timestamp.valueOf(r.getPlayedAt())); else pst.setNull(6, Types.TIMESTAMP);
            pst.setString(7, r.getStage());
            
            pst.executeUpdate();
            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) r.setId(rs.getLong(1));
            return true;
        } catch (SQLException ex) {
            lastErrorMessage = ex.getMessage();
            System.err.println("ERREUR RencontreService.add : " + ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }

    private boolean update(rencontre r) {
        lastErrorMessage = null;
        String sql = "UPDATE rencontre SET tournoi_id=?, equipe_a_id=?, equipe_b_id=?, score_a=?, score_b=?, played_at=?, stage=? WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setLong(1, r.getTournoi().getId());
            pst.setLong(2, r.getEquipeA().getId());
            pst.setLong(3, r.getEquipeB().getId());
            if (r.getScoreA() != null) pst.setInt(4, r.getScoreA()); else pst.setNull(4, Types.INTEGER);
            if (r.getScoreB() != null) pst.setInt(5, r.getScoreB()); else pst.setNull(5, Types.INTEGER);
            if (r.getPlayedAt() != null) pst.setTimestamp(6, Timestamp.valueOf(r.getPlayedAt())); else pst.setNull(6, Types.TIMESTAMP);
            pst.setString(7, r.getStage());
            pst.setLong(8, r.getId());
            
            return pst.executeUpdate() > 0;
        } catch (SQLException ex) {
            lastErrorMessage = ex.getMessage();
            System.err.println("ERREUR RencontreService.update : " + ex.getMessage());
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

    /**
     * Propager le vainqueur d'un match vers l'étape suivante.
     * Si le match de l'étape suivante n'existe pas, il est créé automatiquement.
     */
    public void advanceWinner(rencontre r) {
        if (r.getScoreA() == null || r.getScoreB() == null || r.getScoreA().equals(r.getScoreB())) {
            return; // Pas encore de vainqueur clair (ou match nul)
        }

        equipe winner = (r.getScoreA() > r.getScoreB()) ? r.getEquipeA() : r.getEquipeB();
        String nextStage = getNextStage(r.getStage());
        if (nextStage == null) return; // Déjà à la finale (ou stage inconnu)

        // 1. Trouver l'index de ce match dans son étape actuelle pour savoir où placer le vainqueur
        List<rencontre> currentStageMatches = findByTournoi(r.getTournoi().getId()).stream()
                .filter(m -> r.getStage().equalsIgnoreCase(m.getStage()))
                .sorted(Comparator.comparing(rencontre::getId))
                .collect(Collectors.toList());

        int matchIndex = -1;
        for (int i = 0; i < currentStageMatches.size(); i++) {
            if (currentStageMatches.get(i).getId().equals(r.getId())) {
                matchIndex = i;
                break;
            }
        }

        if (matchIndex == -1) return;

        // 2. Déterminer la position dans le match suivant
        // Match 0 et 1 -> Match suivant 0 (A et B)
        // Match 2 et 3 -> Match suivant 1 (A et B)
        int targetMatchIndex = matchIndex / 2;
        boolean isEquipeA = (matchIndex % 2 == 0);

        // 3. Chercher ou Créer le match de l'étape suivante
        List<rencontre> nextStageMatches = findByTournoi(r.getTournoi().getId()).stream()
                .filter(m -> nextStage.equalsIgnoreCase(m.getStage()))
                .sorted(Comparator.comparing(rencontre::getId))
                .collect(Collectors.toList());

        rencontre nextMatch;
        if (targetMatchIndex < nextStageMatches.size()) {
            // Le match existe déjà, on le met à jour
            nextMatch = nextStageMatches.get(targetMatchIndex);
        } else {
            // Le match n'existe pas, on le crée en base !
            nextMatch = new rencontre();
            nextMatch.setTournoi(r.getTournoi());
            nextMatch.setStage(nextStage);
            nextMatch.setPlayedAt(r.getPlayedAt().plusDays(2)); // Suggestion de date (J+2)
            // On le sauvegarde pour avoir un ID
            add(nextMatch); 
        }

        // 4. Assigner le vainqueur
        if (isEquipeA) {
            nextMatch.setEquipeA(winner);
        } else {
            nextMatch.setEquipeB(winner);
        }
        
        // 5. Sauvegarder les changements
        update(nextMatch);
    }

    private String getNextStage(String currentStage) {
        if (currentStage == null) return null;
        String cur = currentStage.toUpperCase();
        if (cur.contains("HUITIEME")) return "QUART";
        if (cur.contains("QUART")) return "DEMI";
        if (cur.contains("DEMI")) return "FINALE";
        return null;
    }

    private rencontre mapRencontre(ResultSet rs) throws SQLException {
        rencontre r = new rencontre();
        r.setId(rs.getLong("id"));
        
        long tournoiId = rs.getLong("tournoi_id");
        r.setTournoi(tournoiService.findById(tournoiId));
        
        long equipeAId = rs.getLong("equipe_a_id");
        r.setEquipeA(equipeService.findById(equipeAId));
        
        long equipeBId = rs.getLong("equipe_b_id");
        r.setEquipeB(equipeService.findById(equipeBId));
        
        int scoreA = rs.getInt("score_a");
        if (!rs.wasNull()) r.setScoreA(scoreA);
        
        int scoreB = rs.getInt("score_b");
        if (!rs.wasNull()) r.setScoreB(scoreB);
        
        Timestamp ts = rs.getTimestamp("played_at");
        if (ts != null) r.setPlayedAt(ts.toLocalDateTime());
        
        r.setStage(rs.getString("stage"));
        
        return r;
    }
}
