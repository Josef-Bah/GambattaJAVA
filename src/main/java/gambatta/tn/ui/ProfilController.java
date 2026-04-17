package gambatta.tn.ui;

import gambatta.tn.entites.user.user;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ProfilController {

    @FXML private Label firstNameLabel;
    @FXML private Label lastNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label numTelLabel;
    @FXML private Label roleLabel;

    private user currentUser;

    public void setUser(user u) {
        this.currentUser = u;
        firstNameLabel.setText(u.getFirstName());
        lastNameLabel.setText(u.getLastName());
        emailLabel.setText(u.getEmail());
        numTelLabel.setText(u.getNumTel() != null ? u.getNumTel() : "Non renseigné");
        roleLabel.setText(u.getRoles().replace("[\"", "").replace("\"]", ""));
    }


    @FXML
    public void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gambatta.tn.ui/Login.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) firstNameLabel.getScene().getWindow();
            Scene scene = new Scene(root, 1280, 720);
            scene.getStylesheets().add(
                    getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm()
            );
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}