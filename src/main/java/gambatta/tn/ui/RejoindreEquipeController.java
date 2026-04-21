package gambatta.tn.ui;

import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.entites.tournois.playerjoinrequest;
import gambatta.tn.services.tournoi.EquipeService;
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

    private EquipeService           equipeService = new EquipeService();
    private PlayerJoinRequestService requestService = new PlayerJoinRequestService();

    @FXML
    public void initialize() {
        comboEquipeSelection.setItems(FXCollections.observableArrayList(equipeService.findAll()));
        
        // Vérifier si l'initialisation du service a échoué (ex: table non créée)
        if (requestService.getLastErrorMessage() != null) {
            showAlert(Alert.AlertType.WARNING, "Problème Initialisation", 
                "Le service de demandes a rencontré un problème au démarrage :\n" + requestService.getLastErrorMessage());
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
            showAlert(Alert.AlertType.INFORMATION, "✅ Demande envoyée",
                    "Votre demande pour rejoindre \"" + selectedEquipe.getNom() + "\" a bien été envoyée !\nLe capitaine va examiner votre profil.");
            clearForm();
        } else {
            String error = requestService.getLastErrorMessage();
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Une erreur est survenue lors de l'envoi de votre demande.\n" + (error != null ? error : "Cause inconnue."));
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

    private void clearErrors() { errPlayerName.setText(""); errEquipe.setText(""); }

    private void clearForm() {
        txtPlayerName.clear();
        comboEquipeSelection.getSelectionModel().clearSelection();
        clearErrors();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
