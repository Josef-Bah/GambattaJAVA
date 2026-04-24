package gambatta.tn.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.net.URI;

public class MapPopupController {

    @FXML private Label feedbackLabel;

    // Coordonnées Gambatta — remplace par la vraie adresse
    private static final String GOOGLE_MAPS_URL =
            "https://www.google.com/maps/search/Gambatta+ESports+Tunisie";

    @FXML
    public void ouvrirGoogleMaps() {
        try {
            Desktop.getDesktop().browse(new URI(GOOGLE_MAPS_URL));
        } catch (Exception e) {
            feedbackLabel.setText("❌ Impossible d'ouvrir le navigateur.");
            feedbackLabel.setStyle("-fx-text-fill: #ff4757;");
        }
    }

    @FXML
    public void ouvrirItineraire() {
        try {
            // Ouvre Google Maps avec itinéraire vers Gambatta
            String url = "https://www.google.com/maps/dir/?api=1&destination=Gambatta+ESports+Tunisie";
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            feedbackLabel.setText("❌ Impossible d'ouvrir le navigateur.");
            feedbackLabel.setStyle("-fx-text-fill: #ff4757;");
        }
    }

    @FXML
    public void fermer() {
        Stage stage = (Stage) feedbackLabel.getScene().getWindow();
        stage.close();
    }
}