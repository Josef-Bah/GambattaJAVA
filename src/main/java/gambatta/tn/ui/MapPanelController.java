package gambatta.tn.ui;

import gambatta.tn.services.user.MapService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

public class MapPanelController implements Initializable {

    @FXML private ImageView      mapView;
    @FXML private ProgressIndicator loadingSpinner;
    @FXML private VBox           errorCard;
    @FXML private Label          feedbackLabel;

    private Runnable onBack;

    /** Called by the opener to configure the back-navigation action. */
    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadMap();
    }

    // ─── Map loading ──────────────────────────────────────────────────────────

    private void loadMap() {
        loadingSpinner.setVisible(true);
        loadingSpinner.setProgress(-1);
        mapView.setVisible(false);
        errorCard.setVisible(false);
        feedbackLabel.setText("");

        Thread fetchThread = new Thread(() -> {
            Image img = MapService.fetchStaticMap();
            Platform.runLater(() -> {
                loadingSpinner.setVisible(false);
                if (img != null && !img.isError()) {
                    mapView.setImage(img);
                    Rectangle clip = new Rectangle(600, 380);
                    clip.setArcWidth(24);
                    clip.setArcHeight(24);
                    mapView.setClip(clip);
                    mapView.setVisible(true);
                } else {
                    errorCard.setVisible(true);
                }
            });
        }, "gambatta-map-fetch");
        fetchThread.setDaemon(true);
        fetchThread.start();
    }

    @FXML
    public void refreshMap() {
        loadMap();
    }

    // ─── External links ───────────────────────────────────────────────────────

    @FXML
    public void openGoogleMaps() {
        openUrl(MapService.getGoogleMapsUrl());
    }

    @FXML
    public void openItineraire() {
        openUrl(MapService.getDirectionsUrl());
    }

    private void openUrl(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            feedbackLabel.setText("❌ Impossible d'ouvrir le navigateur : " + e.getMessage());
            feedbackLabel.setStyle("-fx-text-fill: #ff4757;");
        }
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    @FXML
    public void goBack() {
        if (onBack != null) {
            onBack.run();
            return;
        }
        // Fallback: navigate to HomeUser
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/gambatta.tn.ui/HomeUser.fxml"));
            mapView.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
