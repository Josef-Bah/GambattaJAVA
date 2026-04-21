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

public class EditReclamationController implements Initializable {

    @FXML private TextField txtTitre;
    @FXML private ComboBox<String> comboCategorie;
    @FXML private TextArea txtDescription;
    @FXML private Label lblCheminFichier;

    // Labels d'erreur incrustés
    @FXML private Label lblErrorTitre;
    @FXML private Label lblErrorCategorie;
    @FXML private Label lblErrorDescription;
    @FXML private Label lblSystemError;

    private File fichierPreuveSelectionne;
    private ServiceReclamation service = new ServiceReclamation();
    private reclamation reclamationSelectionnee;
    private ReclamationController parent;

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

        // Reset du style au clic
        txtTitre.setOnMouseClicked(e -> resetError(txtTitre, lblErrorTitre));
        comboCategorie.setOnMouseClicked(e -> resetError(comboCategorie, lblErrorCategorie));
        txtDescription.setOnMouseClicked(e -> resetError(txtDescription, lblErrorDescription));
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
            lblCheminFichier.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;");
        }
    }

    // --- LOGIQUE DE VALIDATION SANS POP-UP ---
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
    private void handleSauvegarder() {
        if (!validateInputs()) return;

        reclamationSelectionnee.setTitre(txtTitre.getText().trim());
        reclamationSelectionnee.setCategorierec(comboCategorie.getValue());
        reclamationSelectionnee.setDescrirec(txtDescription.getText().trim());

        // Préparation Cloud
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
            fermer(); // Utilisation de l'incrustation
        } catch (Exception e) {
            lblSystemError.setText("[!] ÉCHEC SYSTÈME : Base de données injoignable.");
            lblSystemError.setVisible(true);
            lblSystemError.setManaged(true);
        }
    }

    @FXML private void handleAmeliorerIA() {
        if (txtDescription.getText().trim().isEmpty()) {
            showError(txtDescription, lblErrorDescription, "Entrez du texte à optimiser.");
            return;
        }
        txtDescription.setText("MODIFICATION ANALYSÉE :\n\n" + txtDescription.getText().trim() + "\n\n[Mise à jour via GAMBATTA_CORE]");
        resetError(txtDescription, lblErrorDescription);
    }

    @FXML private void handleChoisirFichier() {
        FileChooser fc = new FileChooser();
        fc.setTitle("SÉLECTIONNER UN NOUVEAU FICHIER");
        File f = fc.showOpenDialog(txtTitre.getScene().getWindow());
        if (f != null) {
            fichierPreuveSelectionne = f;
            lblCheminFichier.setText("[ NOUVEL ATTACHEMENT : " + f.getName() + " ]");
            lblCheminFichier.setStyle("-fx-text-fill: #fbbf24; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;");
        }
    }

    @FXML private void handleAnnuler() { fermer(); }

    private void fermer() {
        if (parent != null) {
            parent.masquerFormulaireAjout();
        } else {
            txtTitre.getParent().setVisible(false);
        }
    }
}