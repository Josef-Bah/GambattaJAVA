package gambatta.tn.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MapController {

    @FXML
    private Label locationLabel;
    
    @FXML
    private Button closeBtn;
    
    @FXML
    public void initialize() {
        locationLabel.setText("📍 Emplacement sur la carte de Gambatta");
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }
}
