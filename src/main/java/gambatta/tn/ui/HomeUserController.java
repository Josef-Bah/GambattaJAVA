package gambatta.tn.ui;

import gambatta.tn.entites.user.user;
import gambatta.tn.tools.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class HomeUserController {

    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        user currentUser = Session.getCurrentUser();

        if (currentUser != null) {
            String firstName = currentUser.getFirstName() == null ? "" : currentUser.getFirstName().trim();
            welcomeLabel.setText("Bienvenue " + firstName + " dans votre espace Gambatta.");
        } else {
            welcomeLabel.setText("Bienvenue dans votre espace Gambatta.");
        }
    }

    @FXML
    private void openProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/UserProfile.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            scene.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void logout() {
        try {
            Session.clear();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            scene.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void ouvrirMap() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gambatta.tn.ui/MapPopup.fxml")
            );
            Parent root = loader.load();

            Stage popup = new Stage();
            popup.setTitle("📍 Localisation — Gambatta E-Sports");
            popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            popup.setResizable(false);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm()
            );
            popup.setScene(scene);
            popup.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}