package gambatta.tn.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;

public class PortalController {

    @FXML private VBox rootVBox;

    @FXML
    void handleUserLogin() {
        navigate("/activites/ActiviteFront.fxml");
    }

    @FXML
    void handleAdminLogin() {
        navigate("/activites/ActiviteBack.fxml");
    }

    private void navigate(String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            if (rootVBox != null && rootVBox.getScene() != null) {
                rootVBox.getScene().setRoot(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Erreur lors de la navigation: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
