package gambatta.tn.mains;

import gambatta.tn.entites.tournois.tournoi;
import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.entites.tournois.inscriptiontournoi;

import gambatta.tn.services.tournoi.TournoiService;
import gambatta.tn.services.tournoi.EquipeService;
import gambatta.tn.services.tournoi.InscritournoiService;

import java.time.LocalDateTime;
import java.util.List;

public class main {
    public static void main(String[] args) {

        // === TOURNOI CRUD ===
        TournoiService tournoiService = new TournoiService();
        tournoi t = new tournoi();
        t.setNomt("Tournoi Test");
        t.setDatedebutt(LocalDateTime.now());
        t.setDatefint(LocalDateTime.now().plusDays(3));
        t.setDescrit("Test CRUD Tournoi");
        t.setStatutt("EN_ATTENTE");
        tournoiService.add(t); // ID généré


        // CREATE
        boolean addedTournoi = tournoiService.add(t);
        System.out.println("Tournoi ajouté = " + addedTournoi);

        // READ ALL
        System.out.println("\n--- Liste des Tournois ---");
        List<tournoi> listeTournoi = tournoiService.findAll();
        for (tournoi tr : listeTournoi) {
            System.out.println(tr.getId() + " - " + tr.getNomt() + " - " + tr.getStatutt());
        }

        // UPDATE
        t.setNomt("Tournoi Modifié");
        t.setStatutt("TERMINE");
        boolean updatedTournoi = tournoiService.update(t);
        System.out.println("Tournoi modifié = " + updatedTournoi);

        // DELETE
        boolean deletedTournoi = tournoiService.delete(t.getId());
        System.out.println("Tournoi supprimé = " + deletedTournoi);


        // === EQUIPE CRUD ===
        EquipeService equipeService = new EquipeService();
        equipe e = new equipe();
        e.setNom("Equipe Test");
        e.setTeamLeader("Leader Test");
        e.setStatus("EN_ATTENTE");
        equipeService.save(e); // ID généré


        // CREATE
        boolean addedEquipe = equipeService.save(e); // ID généré ici
        System.out.println("\nEquipe ajoutée = " + addedEquipe);

        // READ ALL
        System.out.println("\n--- Liste des Equipes ---");
        List<equipe> listeEquipe = equipeService.findAll();
        for (equipe eq : listeEquipe) {
            System.out.println(eq.getId() + " - " + eq.getNom() + " - " + eq.getStatus());
        }

        // UPDATE
        e.setNom("Equipe Modifiée");
        e.setStatus("VALIDE");
        boolean updatedEquipe = equipeService.save(e); // save modifie si ID existe
        System.out.println("Equipe modifiée = " + updatedEquipe);

        // DELETE
        boolean deletedEquipe = equipeService.delete(e.getId());
        System.out.println("Equipe supprimée = " + deletedEquipe);


        // === INSCRIPTION CRUD ===
        InscritournoiService inscritService = new InscritournoiService();


        // CREATE inscription
        inscriptiontournoi i = new inscriptiontournoi();
        i.setEquipe(e);
        i.setTournoi(t);
        i.setStatus(inscriptiontournoi.STATUS_PENDING);

        boolean addedInscription = inscritService.save(i);
        System.out.println("\nInscription ajoutée = " + addedInscription);

        // READ ALL
        System.out.println("\n--- Liste des Inscriptions ---");
        List<inscriptiontournoi> listeInscription = inscritService.findAll();
        for (inscriptiontournoi ins : listeInscription) {
            System.out.println(ins.getId() + " - Equipe: " + ins.getEquipe().getNom() +
                    " - Tournoi: " + ins.getTournoi().getNomt() +
                    " - Status: " + ins.getStatus());
        }

        // UPDATE
        i.setStatus(inscriptiontournoi.STATUS_ACCEPTED);
        boolean updatedInscription = inscritService.save(i);
        System.out.println("Inscription modifiée = " + updatedInscription);

        // DELETE
        boolean deletedInscription = inscritService.delete(i.getId());
        System.out.println("Inscription supprimée = " + deletedInscription);
    }
}