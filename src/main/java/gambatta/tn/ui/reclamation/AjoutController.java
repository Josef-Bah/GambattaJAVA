package gambatta.tn.ui.reclamation;

import gambatta.tn.entites.reclamation.preuve;
import gambatta.tn.entites.reclamation.reclamation;
import gambatta.tn.services.reclamation.ServiceReclamation;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class AjoutController implements Initializable {

    @FXML private TextField txtTitre;
    @FXML private ComboBox<String> comboCategorie;
    @FXML private TextArea txtDescription;
    @FXML private Label lblCheminFichier;

    // Nouveaux labels d'erreur pour la validation sans pop-up
    @FXML private Label lblErrorTitre;
    @FXML private Label lblErrorCategorie;
    @FXML private Label lblErrorDescription;
    @FXML private Label lblSystemError;

    private File fichierPreuveSelectionne;
    private ServiceReclamation service = new ServiceReclamation();
    private ReclamationController parentController;

    private final String STYLE_NORMAL = "-fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 8;";
    private final String STYLE_ERROR = "-fx-border-color: #ef4444; -fx-border-width: 1.5; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(239, 68, 68, 0.4), 10, 0, 0, 0);";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboCategorie.setItems(FXCollections.observableArrayList(
                "Service Technique / Bug en jeu",
                "Facturation & Paiement",
                "Gestion de Compte",
                "Comportement Joueur / Signalement",
                "Autre Demande"
        ));

        // Enlever les erreurs au clic
        txtTitre.setOnMouseClicked(e -> resetError(txtTitre, lblErrorTitre));
        comboCategorie.setOnMouseClicked(e -> resetError(comboCategorie, lblErrorCategorie));
        txtDescription.setOnMouseClicked(e -> resetError(txtDescription, lblErrorDescription));
    }

    public void setParentController(ReclamationController parentController) {
        this.parentController = parentController;
    }

    // --- LOGIQUE DE VALIDATION INTEGREE (Sans Alert) ---
    private void resetError(Control control, Label errorLabel) {
        control.setStyle(STYLE_NORMAL);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        lblSystemError.setVisible(false);
        lblSystemError.setManaged(false);
    }

    private void showError(Control control, Label errorLabel, String message) {
        control.setStyle(STYLE_ERROR);
        errorLabel.setText("[!] " + message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private boolean validateInputs() {
        boolean isValid = true;
        resetError(txtTitre, lblErrorTitre);
        resetError(comboCategorie, lblErrorCategorie);
        resetError(txtDescription, lblErrorDescription);

        if (txtTitre.getText().trim().isEmpty() || txtTitre.getText().trim().length() < 5) {
            showError(txtTitre, lblErrorTitre, "Sujet requis (Min 5 caractères).");
            isValid = false;
        }

        if (comboCategorie.getValue() == null) {
            showError(comboCategorie, lblErrorCategorie, "Sélectionnez un service.");
            isValid = false;
        }

        if (txtDescription.getText().trim().isEmpty() || txtDescription.getText().trim().length() < 15) {
            showError(txtDescription, lblErrorDescription, "Détails requis (Min 15 caractères).");
            isValid = false;
        }

        return isValid;
    }

    @FXML
    private void handleValider() {
        if (!validateInputs()) {
            return;
        }

        reclamation r = new reclamation();
        r.setTitre(txtTitre.getText().trim());
        r.setCategorierec(comboCategorie.getValue());
        r.setDescrirec(txtDescription.getText().trim());
        r.setStatutrec("En attente");

        // GESTION CLOUD (Le fichier est stocké temporairement ici)
        if (fichierPreuveSelectionne != null) {
            preuve p = new preuve();
            p.setImageName(fichierPreuveSelectionne.getAbsolutePath());
            p.setOriginalName(fichierPreuveSelectionne.getName());
            p.setTaille((int) fichierPreuveSelectionne.length());
            r.setPreuve(p);
        }

        try {
            service.ajouter(r);
            if (parentController != null) {
                parentController.chargerTableau();
            }
            fermer(); // Ferme le panneau latéral
        } catch (Exception e) {
            lblSystemError.setText("[!] ERREUR : Connexion BDD perdue.");
            lblSystemError.setVisible(true);
            lblSystemError.setManaged(true);
        }
    }

    @FXML
    private void handleAmeliorerIA() {
        String texteActuel = txtDescription.getText();
        if (texteActuel == null || texteActuel.trim().isEmpty()) {
            showError(txtDescription, lblErrorDescription, "Entrez du texte à optimiser.");
            return;
        }
        String texteAmeliore = "TRANSFERT DE LOG OPTIMISÉ :\n\n" + texteActuel.trim() + "\n\nRequête transmise via le protocole GAMBATTA.";
        txtDescription.setText(texteAmeliore);
        resetError(txtDescription, lblErrorDescription);
    }

    @FXML
    private void handleChoisirFichier() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("SÉLECTIONNER UNE PREUVE VISUELLE");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images (Prêt pour Cloud)", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(txtTitre.getScene().getWindow());

        if (selectedFile != null) {
            fichierPreuveSelectionne = selectedFile;
            lblCheminFichier.setText("[ ATTACHÉ : " + selectedFile.getName() + " ]");
            lblCheminFichier.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void handleAnnuler() { fermer(); }

    private void fermer() {
        if (parentController != null) {
            parentController.masquerFormulaireAjout();
        } else {
            txtTitre.getParent().setVisible(false);
        }
    }
}