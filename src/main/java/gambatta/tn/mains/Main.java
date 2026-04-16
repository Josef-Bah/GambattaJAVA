package gambatta.tn.mains;

import gambatta.tn.entites.reclamation.reclamation;
import gambatta.tn.services.reclamation.ServiceReclamation;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        ServiceReclamation service = new ServiceReclamation();

        System.out.println("========== DÉBUT DU TEST CRUD ==========\n");

        // ---------------------------------------------------------
        System.out.println("--- 1. TEST DU CREATE (Ajout) ---");
        reclamation rec = new reclamation();
        rec.setTitre("Bug de connexion");
        rec.setCategorierec("Technique");
        rec.setDescrirec("Je n'arrive pas à me connecter au dashboard.");
        rec.setUrgent(true);
        rec.setSentimentLabel("Frustré");

        service.ajouter(rec); // Insertion dans la BDD

        // ---------------------------------------------------------
        System.out.println("\n--- 2. TEST DU READ (Affichage) ---");
        List<reclamation> liste = service.afficher();
        for (reclamation r : liste) {
            System.out.println("ID: " + r.getIdrec() + " | Titre: " + r.getTitre() + " | Statut: " + r.getStatutrec());
        }

        // ---------------------------------------------------------
        // On récupère dynamiquement le DERNIER ticket ajouté pour tester la suite
        if (!liste.isEmpty()) {
            reclamation dernierTicket = liste.get(liste.size() - 1);
            int idTest = dernierTicket.getIdrec();

            System.out.println("\n--- 3. TEST DU UPDATE (Modification du ticket #" + idTest + ") ---");
            dernierTicket.setTitre("Bug de connexion (RÉSOLU)");
            dernierTicket.setStatutrec("TRAITÉ");

            service.modifier(dernierTicket); // Mise à jour dans la BDD

            // Petite vérification
            System.out.println("Vérification après modif :");
            for (reclamation r : service.afficher()) {
                if (r.getIdrec() == idTest) {
                    System.out.println("-> ID: " + r.getIdrec() + " | Nouveau Titre: " + r.getTitre() + " | Nouveau Statut: " + r.getStatutrec());
                }
            }

            // ---------------------------------------------------------
            System.out.println("\n--- 4. TEST DU DELETE (Suppression du ticket #" + idTest + ") ---");
            service.supprimer(idTest); // Suppression dans la BDD

            // Ultime vérification
            System.out.println("Vérification de la base finale :");
            List<reclamation> listeFinale = service.afficher();
            if (listeFinale.isEmpty()) {
                System.out.println("La base de données est vide.");
            } else {
                for (reclamation r : listeFinale) {
                    System.out.println("ID: " + r.getIdrec() + " | Titre: " + r.getTitre());
                }
            }
        }

        System.out.println("\n========== FIN DU TEST CRUD ==========");
    }
}