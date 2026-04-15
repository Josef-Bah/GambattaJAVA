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
        btnTrouverTournoi.setOnAction(e -> opentrouverTournoiWindow());
    }

    @FXML
    private void handleLancerCreation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/CreationEquipeForm.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 700);
            scene.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());

            Stage stage = (Stage) btnLancerCreation.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Créer mon équipe");
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Erreur lors de l'ouverture du formulaire : " + ex.getMessage());
        }
    }

    @FXML
    private void handleRejoindreEquipe() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/RejoindreEquipeForm.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 700);
            scene.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());

            Stage stage = (Stage) btnOuvrirRejoindre.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Rejoindre une équipe");
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Erreur lors de l'ouverture du formulaire : " + ex.getMessage());
        }
    }

    private void opentrouverTournoiWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/trouverTournoiInterface.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 700);
            scene.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            Stage stage = (Stage) btnTrouverTournoi.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Trouver un tournoi");
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Impossible d'ouvrir l'interface de recherche de tournoi.");
        }
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
