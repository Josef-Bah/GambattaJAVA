package gambatta.tn.ui.reclamation;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import gambatta.tn.entites.reclamation.preuve;
import gambatta.tn.entites.reclamation.reclamation;
import gambatta.tn.services.reclamation.ServiceReclamation;
import gambatta.tn.services.reclamation.BadWordService;
import gambatta.tn.services.reclamation.AIService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class AjoutController implements Initializable {

    @FXML private TextField txtTitre;
    @FXML private ComboBox<String> comboCategorie;
    @FXML private TextArea txtDescription;
    @FXML private Label lblCheminFichier;

    @FXML private Label lblErrorTitre;
    @FXML private Label lblErrorCategorie;
    @FXML private Label lblErrorDescription;
    @FXML private Label lblSystemError;

    private File fichierPreuveSelectionne;

    // --- SERVICES ---
    private ServiceReclamation service = new ServiceReclamation();
    private BadWordService badWordService = new BadWordService();
    private AIService aiService = new AIService();
    private ReclamationController parentController;

    // --- CONFIGURATION CLOUDINARY ---
    private final Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "dh0jz5ruc",
            "api_key", "887475919413442",
            "api_secret", "VntQ0sqXcoPvTyiwStJ3KbTH5GA",
            "secure", true
    ));

    private final String STYLE_NORMAL = "-fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 8;";
    private final String STYLE_ERROR = "-fx-border-color: #ef4444; -fx-border-width: 1.5; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(239, 68, 68, 0.4), 10, 0, 0, 0);";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboCategorie.setItems(FXCollections.observableArrayList(
                "Service Technique",
                "Facturation & Paiement",
                "Gestion de Compte",
                "Comportement Joueur",
                "Gestion des Activités",
                "Tournois & Compétitions",
                "Restauration & Nourriture",
                "Gestion d'Équipe",
                "Autre Demande"
        ));

        txtTitre.setOnMouseClicked(e -> resetError(txtTitre, lblErrorTitre));
        comboCategorie.setOnMouseClicked(e -> resetError(comboCategorie, lblErrorCategorie));
        txtDescription.setOnMouseClicked(e -> resetError(txtDescription, lblErrorDescription));
    }

    public void setParentController(ReclamationController parentController) {
        this.parentController = parentController;
    }

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
        // 1. Validation basique
        if (!validateInputs()) return;

        // 2. Affichage du message de traitement
        lblSystemError.setText("[...] ANALYSE DE SÉCURITÉ EN COURS");
        lblSystemError.setStyle("-fx-text-fill: #38bdf8;");
        lblSystemError.setVisible(true);
        lblSystemError.setManaged(true);

        String titre = txtTitre.getText().trim();
        String description = txtDescription.getText().trim();

        // 3. VÉRIFICATION API BAD WORDS (IA)
        if (badWordService.contientBadWord(titre) || badWordService.contientBadWord(description)) {
            lblSystemError.setText("[!] VIOLATION PROTOCOLE : Langage inapproprié détecté.");
            lblSystemError.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            txtTitre.setStyle(STYLE_ERROR);
            txtDescription.setStyle(STYLE_ERROR);
            return;
        }

        lblSystemError.setText("[...] ROUTAGE INTELLIGENT IA EN COURS");

        // --- 4. SMART ROUTING IA (Le cœur de la fonctionnalité) ---
        // L'IA lit le texte et décide de la vraie catégorie
        String choixClient = comboCategorie.getValue();
        String categorieIA = aiService.determinerCategorie(description);

        System.out.println("--- SMART ROUTING GAMBATTA ---");
        System.out.println("Choix Client : " + choixClient);
        System.out.println("Correction IA : " + categorieIA);
        System.out.println("------------------------------");

        // Création de l'entité
        reclamation r = new reclamation();
        r.setTitre(titre);
        r.setCategorierec(categorieIA); // <-- L'IA ÉCRASE LE CHOIX DU CLIENT ICI
        r.setDescrirec(description);
        r.setStatutrec("En attente");

        // 5. GESTION DE L'UPLOAD CLOUD
        if (fichierPreuveSelectionne != null) {
            lblSystemError.setText("[...] UPLOAD VERS LE CLOUD EN COURS");
            try {
                Map uploadResult = cloudinary.uploader().upload(fichierPreuveSelectionne, ObjectUtils.asMap(
                        "folder", "gambatta/preuves",
                        "resource_type", "auto"
                ));

                String urlCloud = (String) uploadResult.get("secure_url");
                preuve p = new preuve();
                p.setImageName(urlCloud);
                p.setOriginalName(fichierPreuveSelectionne.getName());
                p.setTaille((int) fichierPreuveSelectionne.length());
                r.setPreuve(p);

            } catch (Exception e) {
                e.printStackTrace();
                lblSystemError.setText("[!] ERREUR CLOUD : Impossible d'envoyer l'image.");
                lblSystemError.setStyle("-fx-text-fill: #ef4444;");
                return;
            }
        }

        // 6. SAUVEGARDE BDD
        try {
            service.ajouter(r);
            if (parentController != null) {
                parentController.chargerTableau();
            }
            fermer();
        } catch (Exception e) {
            e.printStackTrace();
            lblSystemError.setText("[!] ERREUR : Connexion BDD perdue.");
            lblSystemError.setStyle("-fx-text-fill: #ef4444;");
        }
    }

    @FXML
    private void handleAmeliorerIA() {
        String texteActuel = txtDescription.getText();
        if (texteActuel == null || texteActuel.trim().isEmpty()) {
            showError(txtDescription, lblErrorDescription, "Entrez d'abord du texte à optimiser.");
            return;
        }

        lblSystemError.setText("[...] OPTIMISATION NEURONALE EN COURS");
        lblSystemError.setStyle("-fx-text-fill: #a855f7; -fx-font-weight: bold;");
        lblSystemError.setVisible(true);
        lblSystemError.setManaged(true);

        new Thread(() -> {
            String texteAmeliore = aiService.optimiserTexte(texteActuel);

            Platform.runLater(() -> {
                txtDescription.setText(texteAmeliore);
                lblSystemError.setText("[+] OPTIMISATION TERMINÉE");
                lblSystemError.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                resetError(txtDescription, lblErrorDescription);
            });
        }).start();
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