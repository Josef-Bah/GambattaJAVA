package gambatta.tn.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class DetailsActiviteController {

    @FXML
    private Label activityName;

    @FXML
    private Label activityDescription;

    @FXML
    private Label activityStatus;

    @FXML
    private Button btnRetour;

    @FXML
    public void initialize() {
        // Load activity details logic here
    }

    @FXML
    private void handleRetour() {
        // Logic to return to the previous screen
    }

    public void setActivityDetails(gambatta.tn.entites.activites.activite activity) {
        activityName.setText(activity.getNoma());
        activityDescription.setText(activity.getAdresse());
        activityStatus.setText("Statut: " + activity.getDispoa());
    }
}
