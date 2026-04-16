package gambatta.tn.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MapController {

    @FXML private Label locationLabel;
    @FXML private Button closeBtn;
    
    // Grids for glowing effect
    @FXML private javafx.scene.layout.StackPane paneTerrain1;
    @FXML private javafx.scene.layout.StackPane paneMainStage;
    @FXML private javafx.scene.layout.StackPane paneSallePC;
    @FXML private javafx.scene.layout.StackPane paneConsole;

    // Pins to show
    @FXML private Label pin1;
    @FXML private Label pin2;
    @FXML private Label pin3;
    @FXML private Label pin4;

    @FXML
    public void initialize() {
        locationLabel.setText("📍 Chargement...");
    }

    public void setActivityLocation(String address) {
        if (address == null) address = "";
        locationLabel.setText("📍 Recherche de : " + address);
        
        String lower = address.toLowerCase();
        
        // Reset all pins
        pin1.setVisible(false); pin2.setVisible(false); pin3.setVisible(false); pin4.setVisible(false);
        // Reset borders
        paneTerrain1.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 10;");
        paneMainStage.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 10;");
        paneSallePC.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 10;");
        paneConsole.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 10;");

        // Simple Keyword logic mapping
        String glowStyle = "-fx-background-color: #1e293b; -fx-background-radius: 10; -fx-border-color: #FFD700; -fx-border-radius: 10; -fx-border-width: 3;";

        if (lower.contains("terrain") || lower.contains("tennis") || lower.contains("foot") || lower.contains("padel")) {
            pin1.setVisible(true);
            paneTerrain1.setStyle(glowStyle);
            locationLabel.setText("📍 Emplacement : Terrain 1 (" + address + ")");
        } 
        else if (lower.contains("pc") || lower.contains("salle") || lower.contains("lan")) {
            pin3.setVisible(true);
            paneSallePC.setStyle(glowStyle);
            locationLabel.setText("📍 Emplacement : PC Room (" + address + ")");
        }
        else if (lower.contains("console") || lower.contains("fifa")) {
            pin4.setVisible(true);
            paneConsole.setStyle(glowStyle);
            locationLabel.setText("📍 Emplacement : Console Area (" + address + ")");
        }
        else {
            // Default to main stage
            pin2.setVisible(true);
            paneMainStage.setStyle(glowStyle);
            locationLabel.setText("📍 Emplacement : Main Stage (" + address + ")");
        }
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }
}
