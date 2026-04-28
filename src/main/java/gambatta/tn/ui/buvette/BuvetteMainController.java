package gambatta.tn.ui.buvette;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;

public class BuvetteMainController {

    @FXML private StackPane contentArea;

    @FXML
    public void initialize() {
        showCrud(); // default page for admin is now Products CRUD
    }

    @FXML
    public void showCrud() {
        loadPage("/buvette/CrudProduitManagement.xml");
    }

    @FXML
    public void showVentesCrud() {
        loadPage("/buvette/CrudVenteManagement.fxml");
    }

    @FXML
    public void logout() {
        loadPageToRoot("/buvette/SelectionView.fxml", "Bienvenue sur Gambatta");
    }

    private void loadPageToRoot(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root, 1280, 720));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node page = loader.load();
            contentArea.getChildren().setAll(page);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
