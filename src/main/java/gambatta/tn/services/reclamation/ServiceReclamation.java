package gambatta.tn.services.reclamation;

import gambatta.tn.entites.reclamation.preuve;
import gambatta.tn.entites.reclamation.reclamation;
import gambatta.tn.entites.reclamation.response; // IMPORT CRUCIAL POUR EVITER "cannot find symbol"
import gambatta.tn.tools.MyDataBase;

import java.time.LocalDateTime;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceReclamation {

    private Connection cnx;

    public ServiceReclamation() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    // ==========================================
    // 1. CREATE (Ajouter)
    // ==========================================
    public void ajouter(reclamation r) {
        String req = "INSERT INTO reclamation (titre, categorierec, descrirec, statutrec, daterec, name, lastname, is_archived, image_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement stm = cnx.prepareStatement(req);
            stm.setString(1, r.getTitre());
            stm.setString(2, r.getCategorierec());
            stm.setString(3, r.getDescrirec());
            stm.setString(4, r.getStatutrec());
            stm.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            stm.setString(6, "Utilisateur");
            stm.setString(7, "Anonyme");
            stm.setInt(8, 0); // is_archived à 0 par défaut

            if (r.getPreuve() != null) {
                stm.setString(9, r.getPreuve().getImageName());
            } else {
                stm.setNull(9, Types.VARCHAR);
            }

            stm.executeUpdate();
            System.out.println("✅ Réclamation ajoutée avec preuve !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // 2. READ (Afficher) - AVEC HISTORIQUE DES RÉPONSES
    // ==========================================
    public List<reclamation> afficher() {
        List<reclamation> liste = new ArrayList<>();
        // ATTENTION : On n'affiche que les tickets NON ARCHIVÉS
        String req = "SELECT * FROM reclamation WHERE is_archived = 0";

        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);

            while (rs.next()) {
                reclamation r = new reclamation();
                r.setIdrec(rs.getInt("idrec"));
                r.setTitre(rs.getString("titre"));
                r.setCategorierec(rs.getString("categorierec"));
                r.setDescrirec(rs.getString("descrirec"));
                r.setStatutrec(rs.getString("statutrec"));

                Timestamp timestamp = rs.getTimestamp("daterec");
                if (timestamp != null) {
                    r.setDaterec(timestamp.toLocalDateTime());
                }

                String path = rs.getString("image_path");
                if (path != null && !path.isEmpty()) {
                    preuve p = new preuve();
                    p.setImageName(path);
                    r.setPreuve(p);
                }

                // =========================================================
                // NOUVEAU : RÉCUPÉRATION DE L'HISTORIQUE DES RÉPONSES
                // =========================================================
                String reqRep = "SELECT * FROM response WHERE idrec = ?";
                try (PreparedStatement stmRep = cnx.prepareStatement(reqRep)) {
                    stmRep.setInt(1, r.getIdrec());
                    ResultSet rsRep = stmRep.executeQuery();

                    while (rsRep.next()) {
                        response rep = new response();
                        rep.setIdrep(rsRep.getInt("idrep"));
                        rep.setContenurep(rsRep.getString("contenurep"));

                        Timestamp tsRep = rsRep.getTimestamp("daterep");
                        if (tsRep != null) {
                            rep.setDaterep(tsRep.toLocalDateTime());
                        }
                        // On attache la réponse à ce ticket précis
                        r.addResponse(rep);
                    }
                }
                // =========================================================

                // Récupération optionnelle (sans crash si la colonne n'existe pas encore)
                try {
                    r.setUrgent(rs.getBoolean("isUrgent"));
                    r.setSentimentLabel(rs.getString("sentimentLabel"));
                } catch (SQLException e) { }

                liste.add(r);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'affichage : " + e.getMessage());
        }
        return liste;
    }

    // ==========================================
    // 3. UPDATE (Modifier)
    // ==========================================
    public void modifier(reclamation r) {
        String req = "UPDATE reclamation SET titre=?, categorierec=?, descrirec=?, statutrec=?, image_path=? WHERE idrec=?";
        try {
            PreparedStatement stm = cnx.prepareStatement(req);
            stm.setString(1, r.getTitre());
            stm.setString(2, r.getCategorierec());
            stm.setString(3, r.getDescrirec());
            stm.setString(4, r.getStatutrec());

            if (r.getPreuve() != null) {
                stm.setString(5, r.getPreuve().getImageName());
            } else {
                stm.setNull(5, Types.VARCHAR);
            }

            stm.setInt(6, r.getIdrec());

            stm.executeUpdate();
            System.out.println("✅ Réclamation et preuve modifiées !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la modification : " + e.getMessage());
        }
    }

    // ==========================================
    // 4. DELETE (Supprimer)
    // ==========================================
    public void supprimer(int idrec) {
        String req = "DELETE FROM reclamation WHERE idrec=?";
        try {
            PreparedStatement stm = cnx.prepareStatement(req);
            stm.setInt(1, idrec);
            stm.executeUpdate();
            System.out.println("✅ Réclamation supprimée !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression : " + e.getMessage());
        }
    }

    // ==========================================
    // 5. ARCHIVER (Soft Delete)
    // ==========================================
    public void archiver(int idrec) {
        String req = "UPDATE reclamation SET is_archived = 1 WHERE idrec = ?";
        try {
            PreparedStatement stm = cnx.prepareStatement(req);
            stm.setInt(1, idrec);
            stm.executeUpdate();
            System.out.println("✅ Ticket #" + idrec + " déplacé vers les archives.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // 6. GESTION DES RÉPONSES
    // ==========================================
    public void ajouterReponse(response rep) {
        // Remplacement de 'reclamation_id' par 'idrec'
        String req = "INSERT INTO response (contenurep, daterep, idrec) VALUES (?, ?, ?)";

        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setString(1, rep.getContenurep());
            stm.setTimestamp(2, java.sql.Timestamp.valueOf(rep.getDaterep()));

            if (rep.getReclamation() != null) {
                stm.setInt(3, rep.getReclamation().getIdrec());
            } else {
                throw new SQLException("Erreur : La réponse n'est liée à aucune réclamation.");
            }

            int rows = stm.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ DEBUG: Réponse enregistrée en BDD pour le ticket #" + rep.getReclamation().getIdrec());
            }
        } catch (SQLException e) {
            System.err.println("❌ ERREUR LORS DE L'AJOUT DE LA RÉPONSE : " + e.getMessage());
            e.printStackTrace();
        }
    }
}