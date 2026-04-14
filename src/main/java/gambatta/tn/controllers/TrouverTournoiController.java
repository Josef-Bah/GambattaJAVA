package gambatta.tn.controllers;

import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.entites.tournois.tournoi;
import gambatta.tn.services.tournoi.EquipeService;
import gambatta.tn.services.tournoi.TournoiService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;

public class TrouverTournoiController {

    @FXML
    private ComboBox<equipe> comboEquipe;

    @FXML
    private ComboBox<tournoi> comboTournoi;

    @FXML
    private Button btnEnvoyer;

    @FXML
    private Label lblRetour;

    private final EquipeService equipeService = new EquipeService();
    private final TournoiService tournoiService = new TournoiService();

    @FXML
    public void initialize() {
        // Retour à l'interface d'inscription
        lblRetour.setOnMouseClicked(e -> goBack());
        // Charger les équipes depuis la base
        List<equipe> equipes = equipeService.findAll();
        comboEquipe.setItems(FXCollections.observableArrayList(equipes));

        // Charger les tournois depuis la base
        List<tournoi> tournois = tournoiService.findAll();
        comboTournoi.setItems(FXCollections.observableArrayList(tournois));

        // Bouton envoyer
        btnEnvoyer.setOnAction(e -> {
            equipe selectedEquipe = comboEquipe.getSelectionModel().getSelectedItem();
            tournoi selectedTournoi = comboTournoi.getSelectionModel().getSelectedItem();

            if (selectedEquipe == null || selectedTournoi == null) {
                showAlert("Veuillez sélectionner une équipe et un tournoi.");
                return;
            }

            // Ici tu peux appeler ton service d'inscription
            System.out.println("Demande envoyée : Équipe = " + selectedEquipe.getNom()
                    + ", Tournoi = " + selectedTournoi.getNomt());

            showAlert("Inscription envoyée pour l'équipe " + selectedEquipe.getNom()
                    + " au tournoi " + selectedTournoi.getNomt());
        });
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/InscriptionEquipeInterface.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            Stage stage = (Stage) lblRetour.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Inscription Nouvelle Équipe");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}