package gambatta.tn.ui.reclamation;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class PortalController {

    @FXML private Button btnUser;
    @FXML private Button btnAdmin;
    @FXML private VBox cardClient;
    @FXML private VBox cardAdmin;

    @FXML
    public void initialize() {
        // --- EFFETS HOVER POUR LE BOUTON USER (Plein) ---
        String baseUser = "-fx-background-color: linear-gradient(to bottom right, #facc15, #f59e0b); -fx-text-fill: #020617; -fx-font-weight: 900; -fx-font-size: 15px; -fx-padding: 12 0; -fx-background-radius: 10; -fx-cursor: hand;";
        String hoverUser = "-fx-background-color: #fef08a; -fx-text-fill: #020617; -fx-font-weight: 900; -fx-font-size: 15px; -fx-padding: 12 0; -fx-background-radius: 10; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(250,204,21,0.6), 15, 0, 0, 0);";

        btnUser.setOnMouseEntered(e -> btnUser.setStyle(hoverUser));
        btnUser.setOnMouseExited(e -> btnUser.setStyle(baseUser));

        // --- EFFETS HOVER POUR LE BOUTON ADMIN (Contour) ---
        String baseAdmin = "-fx-background-color: transparent; -fx-border-color: #facc15; -fx-border-width: 2; -fx-border-radius: 10; -fx-text-fill: #facc15; -fx-font-weight: 900; -fx-font-size: 15px; -fx-padding: 10 0; -fx-cursor: hand;";
        String hoverAdmin = "-fx-background-color: rgba(250, 204, 21, 0.1); -fx-border-color: #facc15; -fx-border-width: 2; -fx-border-radius: 10; -fx-text-fill: #facc15; -fx-font-weight: 900; -fx-font-size: 15px; -fx-padding: 10 0; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(250,204,21,0.4), 10, 0, 0, 0);";

        btnAdmin.setOnMouseEntered(e -> btnAdmin.setStyle(hoverAdmin));
        btnAdmin.setOnMouseExited(e -> btnAdmin.setStyle(baseAdmin));
    }

    @FXML
    private void handleUserAccess() throws IOException {
        loadScene("/gambatta.tn.ui/reclamation/reclamation.fxml", "GAMBATTA - ESPACE CLIENT");
    }

    @FXML
    private void handleAdminAccess() throws IOException {
        loadScene("/gambatta.tn.ui/reclamation/admin_dashboard.fxml", "GAMBATTA - ADMINISTRATION");
    }

    private void loadScene(String fxmlPath, String title) throws IOException {
        Stage stage = (Stage) javafx.stage.Window.getWindows().get(0);
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.centerOnScreen();
    }
}