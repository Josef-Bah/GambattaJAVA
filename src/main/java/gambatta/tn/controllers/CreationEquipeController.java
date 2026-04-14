package gambatta.tn.controllers;

import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.services.tournoi.EquipeService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CreationEquipeController {

    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtLeader; // Synced with FXML
    @FXML
    private TextField txtCoach;
    @FXML
    private TextField txtLogo;
    @FXML
    private TextField txtTitres;
    @FXML
    private TextArea txtObjectifs;

    private EquipeService equipeService = new EquipeService();

    @FXML
    private void handleSave() { // Matches FXML onAction="#handleSave"
        String nom = txtNom.getText().trim();
        String leader = txtLeader.getText().trim();
        String coach = txtCoach.getText().trim();
        String logo = txtLogo.getText().trim();
        String titres = txtTitres.getText().trim();
        String objectifs = txtObjectifs.getText().trim();

        if (nom.isEmpty() || leader.isEmpty()) {
            showWarning("Veuillez remplir au moins le nom de l'équipe et le capitaine.");
            return;
        }

        equipe newEquipe = new equipe();
        newEquipe.setNom(nom);
        newEquipe.setTeamLeader(leader);
        newEquipe.setCoach(coach);
        newEquipe.setLogo(logo);
        newEquipe.setTitres(titres);
        newEquipe.setObjectifs(objectifs);
        newEquipe.setStatus("EN_ATTENTE");

        boolean saved = equipeService.save(newEquipe);

        if (saved) {
            showAlert("L'équipe " + nom + " a été créée avec succès !");
            navigateBack();
        } else {
            showError("Une équipe avec ce nom existe déjà ou une erreur est survenue.");
        }
    }

    @FXML
    private void handleAnnuler() {
        navigateBack();
    }

    private void navigateBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/InscriptionEquipeInterface.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 700); // Updated size to match new layout
            scene.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());

            Stage stage = (Stage) txtNom.getScene().getWindow();
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
