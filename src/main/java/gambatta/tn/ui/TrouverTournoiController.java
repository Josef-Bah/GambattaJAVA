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
    @FXML
    private Label globalMsg;

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
                showInlineMsg("⚠ Veuillez sélectionner une équipe et un tournoi.", true);
                return;
            }

            // Actual enrollment logic
            inscriptiontournoi ins = new inscriptiontournoi();
            ins.setEquipe(selectedEquipe);
            ins.setTournoi(selectedTournoi);
            ins.setStatus(inscriptiontournoi.STATUS_PENDING);

            if (inscritService.save(ins)) {
                showInlineMsg("✅ Succès: Demande d'inscription envoyée pour " + selectedEquipe.getNom(), false);
                new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2)).setOnFinished(ev -> goBack());
            } else {
                showInlineMsg("⚠ Une erreur est survenue lors de l'envoi de la demande.", true);
            }
        });
    }

    private void showInlineMsg(String msg, boolean isError) {
        if (globalMsg != null) {
            globalMsg.setText(msg);
            globalMsg.getStyleClass().removeAll("msg-success", "msg-error");
            globalMsg.getStyleClass().add(isError ? "msg-error" : "msg-success");
            if (!isError) {
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
                pause.setOnFinished(e -> globalMsg.setText(""));
                pause.play();
            }
        }
    }

    private void showError(String message) {
        // Obsolete pop-up - removed for inline messages.
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
        // Obsolete pop-up - removed for inline messages.
    }

    private void showWarning(String message) {
        // Obsolete pop-up - removed for inline messages.
    }

}
