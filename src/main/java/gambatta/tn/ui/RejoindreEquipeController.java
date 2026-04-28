package gambatta.tn.ui;

import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.entites.tournois.playerjoinrequest;
import gambatta.tn.services.tournoi.EquipeService;
import gambatta.tn.services.tournoi.GeminiService;
import gambatta.tn.services.tournoi.PlayerJoinRequestService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RejoindreEquipeController {

    @FXML private TextField      txtPlayerName;
    @FXML private ComboBox<equipe> comboEquipeSelection;
    @FXML private Label          errPlayerName;
    @FXML private Label          errEquipe;
    @FXML private Label          globalMsg;

    private EquipeService           equipeService = new EquipeService();
    private PlayerJoinRequestService requestService = new PlayerJoinRequestService();

    @FXML
    public void initialize() {
        comboEquipeSelection.setItems(FXCollections.observableArrayList(equipeService.findAll()));
        
        // Vérifier si l'initialisation du service a échoué
        if (requestService.getLastErrorMessage() != null) {
            showInlineMsg("⚠ Problème Service: " + requestService.getLastErrorMessage(), true);
        }
    }

    @FXML
    private void handleEnvoyerDemande() {
        if (!validate()) return;

        String playerName    = txtPlayerName.getText().trim();
        equipe selectedEquipe = comboEquipeSelection.getSelectionModel().getSelectedItem();

        // Utiliser PlayerJoinRequest (entité dédiée)
        playerjoinrequest request = new playerjoinrequest();
        request.setPlayerName(playerName);
        request.setEquipe(selectedEquipe);
        request.setStatus(playerjoinrequest.STATUS_PENDING);

        if (requestService.save(request)) {
            showInlineMsg("✅ Succès: Demande pour \"" + selectedEquipe.getNom() + "\" envoyée !", false);
            new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2)).setOnFinished(e -> handleRetour());
        } else {
            String error = requestService.getLastErrorMessage();
            showInlineMsg("⚠ Erreur: " + (error != null ? error : "Cause inconnue."), true);
        }
    }

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/InscriptionEquipeInterface.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            Stage stage = (Stage) txtPlayerName.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Inscription Équipe");
            stage.setMaximized(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            ((Stage) txtPlayerName.getScene().getWindow()).close();
        }
    }

    // ── VALIDATION ──────────────────────────────────────────

    private boolean validate() {
        clearErrors(); boolean ok = true;
        String name = txtPlayerName.getText().trim();
        if (name.isEmpty()) {
            errPlayerName.setText("⚠ Votre nom est obligatoire."); ok = false;
        } else if (name.length() < 2) {
            errPlayerName.setText("⚠ Le nom doit avoir au moins 2 caractères."); ok = false;
        }
        if (comboEquipeSelection.getSelectionModel().getSelectedItem() == null) {
            errEquipe.setText("⚠ Veuillez sélectionner une équipe."); ok = false;
        }
        return ok;
    }

    private void clearErrors() { 
        errPlayerName.setText(""); 
        errEquipe.setText(""); 
        if (globalMsg != null) {
            globalMsg.getStyleClass().removeAll("msg-success", "msg-error");
            globalMsg.setText("");
        }
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

    private void clearForm() {
        txtPlayerName.clear();
        comboEquipeSelection.getSelectionModel().clearSelection();
        clearErrors();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        // Obsolete pop-up - removed for inline messages.
    }
}
