package gambatta.tn.ui;

import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.entites.tournois.tournoi;
import gambatta.tn.services.tournoi.EquipeService;
import gambatta.tn.services.tournoi.InscritournoiService;
import gambatta.tn.services.tournoi.TournoiService;
import gambatta.tn.entites.tournois.inscriptiontournoi;
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
    private final InscritournoiService inscritService = new InscritournoiService();

    @FXML
    public void initialize() {
        lblRetour.setOnMouseClicked(e -> goBack());
        
        List<equipe> equipes = equipeService.findAll();
        comboEquipe.setItems(FXCollections.observableArrayList(equipes));

        List<tournoi> tournois = tournoiService.findAll();
        comboTournoi.setItems(FXCollections.observableArrayList(tournois));

        btnEnvoyer.setOnAction(e -> {
            equipe selectedEquipe = comboEquipe.getSelectionModel().getSelectedItem();
            tournoi selectedTournoi = comboTournoi.getSelectionModel().getSelectedItem();

            if (selectedEquipe == null || selectedTournoi == null) {
                showWarning("Veuillez sélectionner une équipe et un tournoi.");
                return;
            }

            // Actual enrollment logic
            inscriptiontournoi ins = new inscriptiontournoi();
            ins.setEquipe(selectedEquipe);
            ins.setTournoi(selectedTournoi);
            ins.setStatus(inscriptiontournoi.STATUS_PENDING);

            if (inscritService.save(ins)) {
                showAlert("Demande d'inscription envoyée avec succès pour l'équipe " + selectedEquipe.getNom()
                        + " au tournoi " + selectedTournoi.getNomt());
                goBack();
            } else {
                showError("Une erreur est survenue lors de l'envoi de la demande.");
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/InscriptionEquipeInterface.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 700);
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
}
