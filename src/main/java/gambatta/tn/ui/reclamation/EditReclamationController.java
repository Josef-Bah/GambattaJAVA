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

public class EditReclamationController implements Initializable {

    @FXML private TextField txtTitre;
    @FXML private ComboBox<String> comboCategorie;
    @FXML private TextArea txtDescription;
    @FXML private Label lblCheminFichier;

    private File fichierPreuveSelectionne;
    private ServiceReclamation service = new ServiceReclamation();
    private reclamation reclamationSelectionnee;
    private ReclamationController parent;

    // Styles de contrôle de saisie (Bleu Gambatta vs Rouge Erreur)
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

        // Reset du style au clic
        txtTitre.setOnMouseClicked(e -> txtTitre.setStyle(STYLE_NORMAL));
        comboCategorie.setOnMouseClicked(e -> comboCategorie.setStyle(STYLE_NORMAL));
        txtDescription.setOnMouseClicked(e -> txtDescription.setStyle(STYLE_NORMAL));
    }

    public void initData(reclamation r, ReclamationController parent) {
        this.reclamationSelectionnee = r;
        this.parent = parent;

        txtTitre.setText(r.getTitre());
        txtDescription.setText(r.getDescrirec());
        comboCategorie.setValue(r.getCategorierec());

        preuve preuveExistante = r.getPreuve();
        if (preuveExistante != null && preuveExistante.getImageName() != null && !preuveExistante.getImageName().isEmpty()) {
            lblCheminFichier.setText("[ FICHIER ACTUEL CONSERVÉ ]");
            lblCheminFichier.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
        }
    }

    /**
     * MÉTHODE DE VALIDATION (Contrôle de Saisie)
     */
    private boolean validateInputs() {
        boolean isValid = true;
        StringBuilder errorMsg = new StringBuilder("ERREUR DE MODIFICATION :\n");

        if (txtTitre.getText().trim().isEmpty() || txtTitre.getText().trim().length() < 5) {
            txtTitre.setStyle(STYLE_ERROR);
            errorMsg.append("- Le titre doit contenir au moins 5 caractères.\n");
            isValid = false;
        }

        if (comboCategorie.getValue() == null) {
            comboCategorie.setStyle(STYLE_ERROR);
            errorMsg.append("- Veuillez sélectionner une catégorie valide.\n");
            isValid = false;
        }

        if (txtDescription.getText().trim().length() < 15) {
            txtDescription.setStyle(STYLE_ERROR);
            errorMsg.append("- La description est trop courte (15 car. min).\n");
            isValid = false;
        }

        if (!isValid) {
            afficherAlerte(errorMsg.toString(), Alert.AlertType.ERROR);
        }

        return isValid;
    }

    @FXML
    private void handleSauvegarder() {
        if (!validateInputs()) return;

        reclamationSelectionnee.setTitre(txtTitre.getText().trim());
        reclamationSelectionnee.setCategorierec(comboCategorie.getValue());
        reclamationSelectionnee.setDescrirec(txtDescription.getText().trim());

        if (fichierPreuveSelectionne != null) {
            preuve p = reclamationSelectionnee.getPreuve();
            if (p == null) p = new preuve();
            p.setImageName(fichierPreuveSelectionne.getAbsolutePath());
            p.setOriginalName(fichierPreuveSelectionne.getName());
            p.setTaille((int) fichierPreuveSelectionne.length());
            reclamationSelectionnee.setPreuve(p);
        }

        try {
            service.modifier(reclamationSelectionnee);
            if (parent != null) parent.chargerTableau();
            handleAnnuler();
        } catch (Exception e) {
            afficherAlerte("ÉCHEC SYSTÈME : Impossible de mettre à jour la base.", Alert.AlertType.ERROR);
        }
    }

    @FXML private void handleAmeliorerIA() {
        if (txtDescription.getText().trim().isEmpty()) {
            txtDescription.setStyle(STYLE_ERROR);
            return;
        }
        txtDescription.setText("MODIFICATION ANALYSÉE :\n\n" + txtDescription.getText().trim() + "\n\n[Mise à jour via GAMBATTA_CORE]");
        txtDescription.setStyle(STYLE_NORMAL);
    }

    @FXML private void handleChoisirFichier() {
        FileChooser fc = new FileChooser();
        File f = fc.showOpenDialog(txtTitre.getScene().getWindow());
        if (f != null) {
            fichierPreuveSelectionne = f;
            lblCheminFichier.setText("[ NOUVEL ATTACHEMENT : " + f.getName() + " ]");
            lblCheminFichier.setStyle("-fx-text-fill: #fbbf24; -fx-font-weight: bold;");
        }
    }

    @FXML private void handleAnnuler() { ((Stage) txtTitre.getScene().getWindow()).close(); }

    private void afficherAlerte(String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message);
        alert.setHeaderText(null);
        alert.setTitle("SYSTEM_OVERRIDE_REPORT");
        DialogPane dp = alert.getDialogPane();
        try {
            dp.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            dp.getStyleClass().add("gaming-alert");
        } catch (Exception e) {}
        alert.showAndWait();
    }
}