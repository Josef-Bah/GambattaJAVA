package gambatta.tn.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MapController {

    @FXML private Label lblActivity;
    @FXML private Label lblLocation;
    @FXML private Label errorLabel;
    @FXML private Button closeBtn;

    // Rooms
    @FXML private StackPane room_terrain1;
    @FXML private StackPane room_terrain2;
    @FXML private StackPane room_salle1;
    @FXML private StackPane room_salle2;
    @FXML private StackPane room_buvette;
    @FXML private StackPane room_salle_vip;
    @FXML private StackPane room_accueil;

    @FXML private String styleNormal;
    @FXML private String styleHighlight;

    private String activityAddress = "";
    private String activityName = "";

    @FXML
    public void initialize() {
        // Nothing to do here
    }

    public void setActivityLocation(String address) {
        this.activityAddress = (address == null || address.trim().isEmpty()) ? "" : address;
        this.activityName = "Activité";
        highlightRoom();
    }

    public void setActivityData(String name, String address) {
        this.activityName = (name == null) ? "Activité" : name;
        this.activityAddress = (address == null || address.trim().isEmpty()) ? "" : address;
        
        if (lblActivity != null) lblActivity.setText("Activité : " + this.activityName);
        if (lblLocation != null) lblLocation.setText("Emplacement : " + this.activityAddress);

        highlightRoom();
    }

    private void highlightRoom() {
        // Reset all to normal
        StackPane[] allRooms = {room_terrain1, room_terrain2, room_salle1, room_salle2, room_buvette, room_salle_vip, room_accueil};
        for (StackPane room : allRooms) {
            if (room != null) room.setStyle(styleNormal);
        }

        if (activityAddress == null || activityAddress.trim().isEmpty()) {
            if (errorLabel != null) errorLabel.setText("Aucun emplacement spécifique n'a été défini pour cette activité.");
            return;
        }

        String addr = activityAddress.toLowerCase();
        StackPane target = null;

        if (addr.contains("terrain 1")) target = room_terrain1;
        else if (addr.contains("terrain 2")) target = room_terrain2;
        else if (addr.contains("salle 1")) target = room_salle1;
        else if (addr.contains("salle 2")) target = room_salle2;
        else if (addr.contains("buvette") || addr.contains("resto") || addr.contains("café")) target = room_buvette;
        else if (addr.contains("vip") || addr.contains("etage") || addr.contains("étage")) target = room_salle_vip;
        else if (addr.contains("accueil")) target = room_accueil;
        else if (addr.contains("terrain")) target = room_terrain1; // default to terrain 1
        else if (addr.contains("salle")) target = room_salle1; // default to salle 1

        if (target != null) {
            target.setStyle(styleHighlight);
            if (errorLabel != null) errorLabel.setText("");
        } else {
            if (errorLabel != null) errorLabel.setText("L'emplacement '" + activityAddress + "' n'est pas répertorié sur le plan principal.");
        }
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }
}
