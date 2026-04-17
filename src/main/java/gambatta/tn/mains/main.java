package gambatta.tn.mains;

import gambatta.tn.entites.user.user;
import gambatta.tn.services.user.UserService;

public class main {
    public static void main(String[] args) throws Exception {

        UserService service = new UserService();

        // CREATE
        user u = new user("test@gambatta.com", "[\"ROLE_USER\"]", "pass123", "Test", "User", "55123456");
        service.ajouter(u);

        // READ ALL
        service.afficher().forEach(System.out::println);

        // UPDATE
        user u2 = service.getById(2);
        u2.setFirstName("Modifié");
        service.modifier(u2);

        // DELETE
        service.supprimer(2);

        // READ ALL après
        service.afficher().forEach(System.out::println);
    }
}