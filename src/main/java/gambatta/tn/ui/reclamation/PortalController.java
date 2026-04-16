package gambatta.tn.ui.reclamation;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class PortalController {

    @FXML
    private void handleUserAccess() throws IOException {
        // Charge la page que tu as déjà faite (Dashboard User)
        loadScene("/gambatta.tn.ui/reclamation/reclamation.fxml", "GAMBATTA - ESPACE CLIENT");
    }

    @FXML
    private void handleAdminAccess() throws IOException {
        // Charge la nouvelle page Admin (Dashboard Admin)
        loadScene("/gambatta.tn.ui/reclamation/admin_dashboard.fxml", "GAMBATTA - ADMINISTRATION");
    }

    private void loadScene(String fxmlPath, String title) throws IOException {
        Stage stage = (Stage) javafx.stage.Window.getWindows().get(0); // Récupère la fenêtre actuelle
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.centerOnScreen();
    }
}