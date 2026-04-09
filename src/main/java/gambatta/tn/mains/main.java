package gambatta.tn.mains;

import gambatta.tn.entites.tournois.tournoi;
import gambatta.tn.services.tournoi.TournoiService;

import java.time.LocalDateTime;
import java.util.List;

public class main {
    public static void main(String[] args) {

        TournoiService service = new TournoiService();

        // CREATE
        tournoi t = new tournoi();
        t.setNomt("Tournoi Test");
        t.setDatedebutt(LocalDateTime.now());
        t.setDatefint(LocalDateTime.now().plusDays(3));
        t.setDescrit("Test CRUD");
        t.setStatutt("EN_ATTENTE");

        boolean added = service.add(t);
        System.out.println("Ajout = " + added);

        // READ ALL
        List<tournoi> liste = service.findAll();
        for (tournoi tr : liste) {
            System.out.println(tr.getId() + " - " + tr.getNomt() + " - " + tr.getStatutt());
        }

        // READ BY ID
        tournoi found = service.findById(t.getId());
        if (found != null) {
            System.out.println("Trouvé : " + found.getNomt());
        }

        // UPDATE
        t.setNomt("Tournoi Modifié");
        t.setStatutt("TERMINE");
        boolean updated = service.update(t);
        System.out.println("Update = " + updated);

        // DELETE
        boolean deleted = service.delete(t.getId());
        System.out.println("Delete = " + deleted);
    }
}