package gambatta.tn.ui.reclamation;

import gambatta.tn.entites.reclamation.preuve;
import gambatta.tn.entites.reclamation.reclamation;
import gambatta.tn.services.reclamation.ServiceReclamation;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class AjoutController implements Initializable {

    @FXML private TextField txtTitre;
    @FXML private ComboBox<String> comboCategorie;
    @FXML private TextArea txtDescription;
    @FXML private Label lblCheminFichier;

    private File fichierPreuveSelectionne;
    private ServiceReclamation service = new ServiceReclamation();
    private ReclamationController parentController;

    // Constantes de style pour le contrôle de saisie
    private final String STYLE_NORMAL = "-fx-border-color: rgba(14, 165, 233, 0.3); -fx-border-width: 1.5; -fx-border-radius: 8;";
    private final String STYLE_ERROR = "-fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(239, 68, 68, 0.4), 10, 0, 0, 0);";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboCategorie.setItems(FXCollections.observableArrayList(
                "Service Technique / Bug en jeu",
                "Facturation & Paiement",
                "Gestion de Compte",
                "Comportement Joueur / Signalement",
                "Autre Demande"
        ));

        // Réinitialisation des styles au clic pour une meilleure UX
        txtTitre.setOnMouseClicked(e -> txtTitre.setStyle(STYLE_NORMAL));
        comboCategorie.setOnMouseClicked(e -> comboCategorie.setStyle(STYLE_NORMAL));
        txtDescription.setOnMouseClicked(e -> txtDescription.setStyle(STYLE_NORMAL));
    }

    public void setParentController(ReclamationController parentController) {
        this.parentController = parentController;
    }

    /**
     * MÉTHODE DE CONTRÔLE DE SAISIE AVANCÉE
     */
    private boolean validateInputs() {
        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder("ERREUR DE TRANSMISSION :\n");

        // 1. Validation du Titre (Non vide + Taille min)
        if (txtTitre.getText().trim().isEmpty()) {
            txtTitre.setStyle(STYLE_ERROR);
            errorMessage.append("- Le sujet ne peut pas être vide.\n");
            isValid = false;
        } else if (txtTitre.getText().trim().length() < 5) {
            txtTitre.setStyle(STYLE_ERROR);
            errorMessage.append("- Le sujet doit contenir au moins 5 caractères.\n");
            isValid = false;
        }

        // 2. Validation de la Catégorie
        if (comboCategorie.getValue() == null) {
            comboCategorie.setStyle(STYLE_ERROR);
            errorMessage.append("- Veuillez sélectionner un service cible.\n");
            isValid = false;
        }

        // 3. Validation de la Description (Taille min pour éviter les spams)
        if (txtDescription.getText().trim().isEmpty()) {
            txtDescription.setStyle(STYLE_ERROR);
            errorMessage.append("- La description du log est obligatoire.\n");
            isValid = false;
        } else if (txtDescription.getText().trim().length() < 15) {
            txtDescription.setStyle(STYLE_ERROR);
            errorMessage.append("- Veuillez fournir plus de détails (15 car. min).\n");
            isValid = false;
        }

        if (!isValid) {
            afficherAlerte(errorMessage.toString(), Alert.AlertType.ERROR);
        }

        return isValid;
    }

    @FXML
    private void handleValider() {
        // Exécution du contrôle de saisie
        if (!validateInputs()) {
            return;
        }

        // Si tout est OK, on procède à l'ajout
        reclamation r = new reclamation();
        r.setTitre(txtTitre.getText().trim());
        r.setCategorierec(comboCategorie.getValue());
        r.setDescrirec(txtDescription.getText().trim());
        r.setStatutrec("En attente");

        if (fichierPreuveSelectionne != null) {
            preuve p = new preuve();
            p.setImageName(fichierPreuveSelectionne.getAbsolutePath());
            p.setOriginalName(fichierPreuveSelectionne.getName());
            p.setTaille((int) fichierPreuveSelectionne.length());
            r.setPreuve(p);
        }

        try {
            service.ajouter(r);
            if (parentController != null) parentController.chargerTableau();
            fermer();
        } catch (Exception e) {
            afficherAlerte("ÉCHEC DE L'INJECTION : Connexion base de données perdue.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAmeliorerIA() {
        String texteActuel = txtDescription.getText();
        if (texteActuel == null || texteActuel.trim().isEmpty()) {
            txtDescription.setStyle(STYLE_ERROR);
            afficherAlerte("ANALYSE IMPOSSIBLE : Le champ de données est vide.", Alert.AlertType.WARNING);
            return;
        }
        String texteAmeliore = "TRANSFERT DE LOG OPTIMISÉ :\n\n" + texteActuel.trim() + "\n\nRequête transmise via le protocole GAMBATTA.";
        txtDescription.setText(texteAmeliore);
        txtDescription.setStyle(STYLE_NORMAL);
    }

    @FXML
    private void handleChoisirFichier() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une preuve (Image ou PDF)");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("Documents", "*.pdf")
        );

        File selectedFile = fileChooser.showOpenDialog(txtTitre.getScene().getWindow());

        if (selectedFile != null) {
            fichierPreuveSelectionne = selectedFile;
            lblCheminFichier.setText("[ ATTACHÉ : " + selectedFile.getName() + " ]");
            lblCheminFichier.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-family: 'Consolas';");
        }
    }

    @FXML
    private void handleAnnuler() { fermer(); }

    private void fermer() { ((Stage) txtTitre.getScene().getWindow()).close(); }

    private void afficherAlerte(String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message);
        alert.setHeaderText(null);
        alert.setTitle("SYSTEM_MESSAGE");

        DialogPane dialogPane = alert.getDialogPane();
        try {
            // On applique ton CSS pour que l'alerte soit aussi "Gaming"
            dialogPane.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            dialogPane.getStyleClass().add("gaming-alert");
        } catch(Exception e){}

        alert.showAndWait();
    }
}