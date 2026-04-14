package gambatta.tn.controllers;

import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.entites.tournois.inscriptiontournoi;
import gambatta.tn.services.tournoi.EquipeService;
import gambatta.tn.services.tournoi.InscritournoiService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RejoindreEquipeController {

    @FXML
    private TextField txtPlayerName;
    @FXML
    private ComboBox<equipe> comboEquipeSelection;

    private EquipeService equipeService = new EquipeService();
    private InscritournoiService inscritService = new InscritournoiService();

    @FXML
    public void initialize() {
        // Charger les équipes dans la ComboBox
        comboEquipeSelection.setItems(FXCollections.observableArrayList(equipeService.findAll()));
    }

    @FXML
    private void handleEnvoyerDemande() {
        String playerName = txtPlayerName.getText().trim();
        equipe selectedEquipe = comboEquipeSelection.getSelectionModel().getSelectedItem();

        if (playerName.isEmpty() || selectedEquipe == null) {
            showWarning("Veuillez entrer votre nom et sélectionner une équipe.");
            return;
        }

        inscriptiontournoi inscription = new inscriptiontournoi();
        inscription.setEquipe(selectedEquipe);
        inscription.setTournoi(null); 
        inscription.setStatus(inscriptiontournoi.STATUS_PENDING);

        boolean saved = inscritService.save(inscription);

        if (saved) {
            showAlert("Votre demande pour rejoindre l'équipe " + selectedEquipe.getNom() + " a été envoyée !");
            navigateBack();
        } else {
            showError("Une erreur est survenue lors de l'envoi de votre demande.");
        }
    }

    @FXML
    private void handleRetour() {
        navigateBack();
    }

    private void navigateBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/InscriptionEquipeInterface.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 700);
            scene.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());

            Stage stage = (Stage) txtPlayerName.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Inscription Équipe");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attention");
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
