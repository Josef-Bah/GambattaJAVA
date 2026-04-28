package gambatta.tn.ui;

import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.services.tournoi.EquipeService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class CreationEquipeController {

    @FXML private TextField txtNom;
    @FXML private TextField txtLeader;
    @FXML private TextField txtCoach;
    @FXML private TextField txtLogo;
    @FXML private TextField txtTitres;
    @FXML private TextArea  txtObjectifs;
    @FXML private VBox      vboxGenIA;
    @FXML private TextField txtPromptLogo;
    @FXML private Button    btnGenLogo;

    // Validation error labels
    @FXML private Label errNom;
    @FXML private Label errLeader;
    @FXML private Label errLogo;
    @FXML private Label globalMsg;

    private EquipeService equipeService = new EquipeService();

    @FXML
    private void handleGenerateLogoIA() {
        boolean visible = vboxGenIA.isVisible();
        vboxGenIA.setVisible(!visible);
        vboxGenIA.setManaged(!visible);
    }

    @FXML
    private void handleDoGenerateLogo() {
        String prompt = txtPromptLogo.getText().trim();
        if (prompt.isEmpty()) {
            showInlineMsg("⚠ Veuillez entrer une description pour le logo.", true);
            return;
        }
        btnGenLogo.setDisable(true);
        btnGenLogo.setText("⏳ Génération...");

        String logoUrl = "https://api.dicebear.com/7.x/identicon/png?seed=" + prompt.replaceAll("\\s+", "");
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> Platform.runLater(() -> {
            txtLogo.setText(logoUrl);
            showInlineMsg("✅ Logo généré avec succès via DiceBear IA !", false);
            btnGenLogo.setDisable(false);
            btnGenLogo.setText("🤖 GÉNÉRER");
        }));
        pause.play();
    }

    @FXML
    private void handleSave() {
        if (!validate()) return;

        equipe e = new equipe();
        e.setNom(txtNom.getText().trim());
        e.setTeamLeader(txtLeader.getText().trim());
        e.setCoach(txtCoach.getText().trim());
        e.setLogo(txtLogo.getText().trim());
        e.setTitres(txtTitres.getText().trim());
        e.setObjectifs(txtObjectifs.getText().trim());
        e.setStatus("EN_ATTENTE");

        if (equipeService.save(e)) {
            showInlineMsg("✅ Succès: L'équipe a été créée et est en attente de validation !", false);
            new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2)).setOnFinished(ev -> goBack());
        } else {
            showInlineMsg("⚠ Ce nom d'équipe existe déjà.", true);
        }
    }

    @FXML
    private void handleAnnuler() {
        goBack();
    }

    // ── VALIDATION ──────────────────────────────────────────

    private boolean validate() {
        clearErrors();
        boolean ok = true;
        String nom    = txtNom.getText().trim();
        String leader = txtLeader.getText().trim();

        if (nom.isEmpty()) {
            errNom.setText("⚠ Le nom de l'équipe est obligatoire."); ok = false;
        } else if (nom.length() < 3) {
            errNom.setText("⚠ Le nom doit contenir au moins 3 caractères."); ok = false;
        } else if (nom.length() > 50) {
            errNom.setText("⚠ Le nom ne peut pas dépasser 50 caractères."); ok = false;
        }

        if (leader.isEmpty()) {
            errLeader.setText("⚠ Le nom du capitaine est obligatoire."); ok = false;
        } else if (leader.length() < 2) {
            errLeader.setText("⚠ Le nom du capitaine doit avoir au moins 2 caractères."); ok = false;
        }

        if (txtLogo.getText().trim().isEmpty()) {
            errLogo.setText("⚠ Le logo de l'équipe est obligatoire."); ok = false;
        }
        return ok;
    }

    private void clearErrors() {
        errNom.setText(""); errLeader.setText(""); errLogo.setText("");
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

    // ── NAVIGATION ──────────────────────────────────────────

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/InscriptionEquipeInterface.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            Stage stage = (Stage) txtNom.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Inscription Équipe");
            stage.setMaximized(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            // Fallback: simply close
            ((Stage) txtNom.getScene().getWindow()).close();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        // Obsolete pop-up - removed for inline validation
    }
}
