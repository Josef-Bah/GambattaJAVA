package gambatta.tn.mains;

import gambatta.tn.entites.buvette.produit;
import gambatta.tn.services.buvette.ProduitService;

public class main {

    public static void main(String[] args) {

        ProduitService service = new ProduitService();

        // CREATE
        produit p = new produit(
                "Legmi",
                "Boisson",
                3.0,
                50,
                null,
                "Legmi.png",
                6969
        );

        service.add(p);

        // READ
        System.out.println("Liste produits:");
        for (produit pr : service.getAll()) {
            System.out.println(pr);
        }

        // UPDATE (example ID 1)
        produit p2 = new produit(
                1,
                "Monster Ultra Updated",
                "Updated desc",
                9.0,
                60,
                null,
                "monster2.png",
                2001
        );

        service.update(p2);

        // DELETE
        service.delete(26);
    }
}