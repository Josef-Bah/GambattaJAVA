package gambatta.tn.mains;

import gambatta.tn.entites.buvette.produit;
import gambatta.tn.services.buvette.ProduitService;

import java.util.List;
import java.util.Scanner;

public class main {

    public static void main(String[] args) {

        ProduitService service = new ProduitService();
        Scanner sc = new Scanner(System.in);

        while (true) {

            System.out.println("\n===== BUVETTE MENU =====");
            System.out.println("1 - Ajouter produit (CREATE)");
            System.out.println("2 - Afficher produits (READ)");
            System.out.println("3 - Modifier produit (UPDATE)");
            System.out.println("4 - Supprimer produit (DELETE)");
            System.out.println("0 - Quitter");
            System.out.print("Choix : ");

            int choice = sc.nextInt();
            sc.nextLine(); // clear buffer

            switch (choice) {

                // CREATE
                case 1:
                    System.out.print("Nom produit: ");
                    String nom = sc.nextLine();

                    System.out.print("Description: ");
                    String desc = sc.nextLine();

                    System.out.print("Prix: ");
                    double prix = sc.nextDouble();

                    System.out.print("Stock: ");
                    int stock = sc.nextInt();

                    sc.nextLine();

                    System.out.print("Image: ");
                    String image = sc.nextLine();

                    System.out.print("Reference: ");
                    int ref = sc.nextInt();

                    produit p = new produit(
                            nom,
                            desc,
                            prix,
                            stock,
                            null,
                            image,
                            ref
                    );

                    service.add(p);
                    break;

                // READ
                case 2:
                    List<produit> list = service.getAll();

                    System.out.println("\n--- LISTE PRODUITS ---");
                    for (produit pr : list) {
                        System.out.println(pr);
                    }
                    break;

                // UPDATE
                case 3:
                    System.out.print("ID produit à modifier: ");
                    int idUp = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Nouveau nom: ");
                    String newNom = sc.nextLine();

                    System.out.print("Nouvelle description: ");
                    String newDesc = sc.nextLine();

                    System.out.print("Nouveau prix: ");
                    double newPrix = sc.nextDouble();

                    System.out.print("Nouveau stock: ");
                    int newStock = sc.nextInt();

                    sc.nextLine();

                    System.out.print("Nouvelle image: ");
                    String newImage = sc.nextLine();

                    System.out.print("Nouvelle reference: ");
                    int newRef = sc.nextInt();

                    produit pUpdate = new produit(
                            idUp,
                            newNom,
                            newDesc,
                            newPrix,
                            newStock,
                            null,
                            newImage,
                            newRef
                    );

                    service.update(pUpdate);
                    break;

                // DELETE
                case 4:
                    System.out.print("ID produit à supprimer: ");
                    int idDel = sc.nextInt();

                    service.delete(idDel);
                    break;

                // EXIT
                case 0:
                    System.out.println("Bye !");
                    return;

                default:
                    System.out.println("Choix invalide !");
            }
        }
    }
}