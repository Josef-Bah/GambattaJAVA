package gambatta.tn.mains;

import gambatta.tn.entites.activites.activite;
import gambatta.tn.entites.activites.ReservationActivite;
import gambatta.tn.services.activites.ActiviteService;
import gambatta.tn.services.activites.ReservationActiviteService;

public class main {

    public static void main(String[] args) {

        // ✅ ACTIVITE SERVICE TEST
        ActiviteService service = new ActiviteService();

        // CREATE
        activite a = new activite(
                "Tennis",
                "Sport",
                "Disponible",
                "Activite sportive",
                "image.png",
                "Tunis",
                true
        );

        service.add(a);

        // READ
        System.out.println("📋 Liste des activités:");
        service.getAll().forEach(System.out::println);

        // DELETE (test later)
        // service.delete(1);

        // UPDATE (test later)

        // ✅ RESERVATION ACTIVITE SERVICE TEST
        ReservationActiviteService reservationService = new ReservationActiviteService();

        // CREATE
        ReservationActivite r = new ReservationActivite(
                new java.util.Date(),
                "10:00",
                "EN_COURS",
                1, // activite_id
                1, // user_id
                null
        );

        reservationService.add(r);

        // READ
        System.out.println("📋 Liste des réservations:");
        reservationService.getAll().forEach(System.out::println);
    }
}