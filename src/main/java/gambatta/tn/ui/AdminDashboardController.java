package gambatta.tn.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class AdminDashboardController {

    @FXML private StackPane contentArea;

    @FXML
    public void initialize() {
        loadTournois(null);
    }

    @FXML
    public void loadTournois(ActionEvent event) {
        loadView("/gambatta.tn.ui/tournoi.fxml");
    }

    @FXML
    public void loadValidation(ActionEvent event) {
        loadView("/gambatta.tn.ui/AdminValidationEquipe.fxml");
    }

    @FXML
    public void loadInscriptions(ActionEvent event) {
        loadView("/gambatta.tn.ui/AdminValidationTournoi.fxml");
    }

    @FXML
    public void loadFlashscore(ActionEvent event) {
        loadView("/gambatta.tn.ui/AdminFlashscore.fxml");
    }

    @FXML
    public void loadTeamLeader(ActionEvent event) {
        loadView("/gambatta.tn.ui/TeamLeaderInterface.fxml");
    }

    @FXML
    public void goHome(ActionEvent event) {
        Stage stage = (Stage) contentArea.getScene().getWindow();
        stage.close();
    }

    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not load view: " + fxml);
        }
    }
}
