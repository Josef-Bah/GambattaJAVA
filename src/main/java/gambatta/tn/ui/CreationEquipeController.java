package gambatta.tn.ui;

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
    @FXML
    private javafx.scene.layout.VBox vboxGenIA;
    @FXML
    private TextField txtPromptLogo;
    @FXML
    private javafx.scene.control.Button btnGenLogo;

    private EquipeService equipeService = new EquipeService();

    @FXML
    private void handleGenerateLogoIA() {
        boolean isVisible = vboxGenIA.isVisible();
        vboxGenIA.setVisible(!isVisible);
        vboxGenIA.setManaged(!isVisible);
    }

    @FXML
    private void handleDoGenerateLogo() {
        String prompt = txtPromptLogo.getText().trim();
        if (prompt.isEmpty()) {
            showWarning("Veuillez entrer une description pour le logo.");
            return;
        }

        btnGenLogo.setDisable(true);
        btnGenLogo.setText("GÉNÉRATION...");

        // DiceBear est gratuit et sans clé, parfait pour générer des logos créatifs instantanément
        String logoUrl = "https://api.dicebear.com/7.x/identicon/png?seed=" + prompt.replaceAll("\\s+", "");
        
        // Simuler un léger délai pour l'effet "génération"
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
        pause.setOnFinished(e -> {
            txtLogo.setText(logoUrl);
            showAlert("Logo généré avec succès ! (Utilisation de l'API créative DiceBear)");
            btnGenLogo.setDisable(false);
            btnGenLogo.setText("GÉNÉRER MON LOGO");
        });
        pause.play();
    }

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
