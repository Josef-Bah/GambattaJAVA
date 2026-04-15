package gambatta.tn.mains;

import gambatta.tn.entites.buvette.produit;
import gambatta.tn.services.buvette.ProduitService;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ProduitFX extends Application {

    @Override
    public void start(Stage stage) {

        ProduitService service = new ProduitService();

        TableView<produit> table = new TableView<>();

        // Columns
        TableColumn<produit, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<produit, String> colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomp"));

        TableColumn<produit, Double> colPrix = new TableColumn<>("Prix");
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prixp"));

        TableColumn<produit, Integer> colStock = new TableColumn<>("Stock");
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockp"));

        TableColumn<produit, Integer> colRef = new TableColumn<>("Reference");
        colRef.setCellValueFactory(new PropertyValueFactory<>("referencep"));

        table.getColumns().addAll(colId, colNom, colPrix, colStock, colRef);

        // DATA
        table.getItems().addAll(service.getAll());

        VBox root = new VBox(table);

        Scene scene = new Scene(root, 700, 400);

        stage.setTitle("Liste Produits Buvette");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}