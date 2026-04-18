package gambatta.tn.ui;

import gambatta.tn.services.tournoi.EquipeService;
import gambatta.tn.services.tournoi.InscritournoiService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class InscriptionController {
    
    @FXML
    private Button btnTrouverTournoi;
    @FXML
    private Button btnLancerCreation;
    @FXML
    private Button btnOuvrirRejoindre;

    private EquipeService equipeService = new EquipeService();
    private InscritournoiService inscritService = new InscritournoiService();

    @FXML
    public void initialize() {
        btnTrouverTournoi.setOnAction(e -> openWindow("/gambatta.tn.ui/trouverTournoiInterface.fxml", "Trouver un Tournoi"));
    }

    @FXML
    public void goBack() {
        ((Stage) btnTrouverTournoi.getScene().getWindow()).close();
    }

    @FXML
    private void handleLancerCreation() {
        openWindow("/gambatta.tn.ui/CreationEquipeForm.fxml", "Créer mon équipe");
    }

    @FXML
    private void handleRejoindreEquipe() {
        openWindow("/gambatta.tn.ui/RejoindreEquipeForm.fxml", "Rejoindre une équipe");
    }

    private void openWindow(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            Stage stage = new Stage();
            stage.setTitle(title); stage.setScene(scene); stage.setMaximized(true); stage.show();
        } catch (Exception ex) { ex.printStackTrace(); showError("Impossible d'ouvrir : " + title); }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
