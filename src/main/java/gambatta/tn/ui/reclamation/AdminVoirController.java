package gambatta.tn.ui.reclamation;

import gambatta.tn.entites.reclamation.reclamation;
import gambatta.tn.entites.reclamation.response;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.layout.StackPane;
import java.awt.Desktop;
import java.net.URI;

public class AdminVoirController {

    @FXML private Label lblTitre, lblDescription, lblDate, lblCategorie, lblAuteur, lblStatut, lblSentiment, lblNoImage;

    // NOUVEAUX ÉLÉMENTS DE L'INTERFACE (Assure-toi que ces fx:id existent dans ton FXML)
    @FXML private ImageView imgPreuve;
    @FXML private StackPane imageContainer;
    @FXML private Label lblCliquez;

    @FXML private VBox vboxHistorique;
    @FXML private Button btnClose;

    // GESTION INCRUSTATION
    private AdminDashboardController parentController;

    public void setParentController(AdminDashboardController parentController) {
        this.parentController = parentController;
    }

    public void initData(reclamation r) {
        lblTitre.setText(r.getTitre() != null ? r.getTitre().toUpperCase() : "SANS TITRE");
        lblDescription.setText(r.getDescrirec() != null ? r.getDescrirec() : "Aucune description fournie.");
        lblCategorie.setText(r.getCategorierec() != null ? r.getCategorierec().toUpperCase() : "NON SPÉCIFIÉ");

        lblAuteur.setText("JOUEUR_01");

        if (r.getDaterec() != null) {
            lblDate.setText(r.getDaterec().toString());
        }

        String st = r.getStatutrec() != null ? r.getStatutrec().toUpperCase() : "INCONNU";
        lblStatut.setText(st);
        if (st.equals("EN COURS")) {
            lblStatut.setStyle("-fx-text-fill: #0ea5e9; -fx-font-size: 14px; -fx-font-weight: 900; -fx-effect: dropshadow(gaussian, rgba(14,165,233,0.5), 10, 0, 0, 0);");
        } else if (st.equals("RÉSOLU")) {
            lblStatut.setStyle("-fx-text-fill: #10b981; -fx-font-size: 14px; -fx-font-weight: 900; -fx-effect: dropshadow(gaussian, rgba(16,185,129,0.5), 10, 0, 0, 0);");
        } else {
            lblStatut.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 14px; -fx-font-weight: 900; -fx-effect: dropshadow(gaussian, rgba(245,158,11,0.5), 10, 0, 0, 0);");
        }

        analyserSentiment(r.getDescrirec());

        // --- NOUVELLE GESTION DES IMAGES VIA LE CLOUD ---
        chargerPreuveVisuelle(r);

        chargerHistorique(r);

        if (btnClose != null) {
            String baseStyle = "-fx-background-color: transparent; -fx-border-color: #ef4444; -fx-text-fill: #ef4444; -fx-font-weight: 900; -fx-font-family: 'Consolas', monospace; -fx-padding: 12; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-width: 2; -fx-cursor: hand;";
            String hoverStyle = "-fx-background-color: #ef4444; -fx-border-color: #ef4444; -fx-text-fill: white; -fx-font-weight: 900; -fx-font-family: 'Consolas', monospace; -fx-padding: 12; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-width: 2; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(239,68,68,0.6), 15, 0, 0, 0);";

            btnClose.setOnMouseEntered(e -> btnClose.setStyle(hoverStyle));
            btnClose.setOnMouseExited(e -> btnClose.setStyle(baseStyle));
        }
    }

    // Extraction de la logique de chargement de l'image
    private void chargerPreuveVisuelle(reclamation r) {
        // Vérification de la présence de l'URL Cloudinary (HTTPS)
        if (r.getPreuve() != null && r.getPreuve().getImageName() != null && r.getPreuve().getImageName().startsWith("http")) {

            String urlCloud = r.getPreuve().getImageName();

            // true = background loading (indispensable pour les URLs Web pour ne pas freezer JavaFX)
            Image image = new Image(urlCloud, true);

            imgPreuve.setImage(image);
            imgPreuve.setVisible(true);

            if(lblNoImage != null) lblNoImage.setVisible(false);
            if(lblCliquez != null) {
                lblCliquez.setVisible(true);
                lblCliquez.setManaged(true);
            }

            // Styles dynamiques interactifs si imageContainer existe
            if(imageContainer != null) {
                String baseStyle = "-fx-background-color: rgba(15, 23, 42, 0.6); -fx-padding: 10; -fx-border-color: #0ea5e9; -fx-border-radius: 10; -fx-background-radius: 10; -fx-border-width: 2; -fx-cursor: hand;";
                String hoverStyle = "-fx-background-color: rgba(15, 23, 42, 0.9); -fx-padding: 10; -fx-border-color: #fcc033; -fx-border-radius: 10; -fx-background-radius: 10; -fx-border-width: 2; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(252, 192, 51, 0.5), 15, 0, 0, 0);";

                imageContainer.setStyle(baseStyle);
                imageContainer.setOnMouseEntered(e -> imageContainer.setStyle(hoverStyle));
                imageContainer.setOnMouseExited(e -> imageContainer.setStyle(baseStyle));

                // Événement Clic : Ouverture du Navigateur
                imageContainer.setOnMouseClicked(e -> ouvrirDansNavigateur(urlCloud));
            }

        } else {
            // Aucun fichier détecté ou ce n'est pas une URL
            imgPreuve.setVisible(false);
            if(lblNoImage != null) lblNoImage.setVisible(true);
            if(lblCliquez != null) {
                lblCliquez.setVisible(false);
                lblCliquez.setManaged(false);
            }
            if(imageContainer != null) {
                imageContainer.setStyle("-fx-background-color: rgba(15, 23, 42, 0.6); -fx-padding: 10; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 10; -fx-background-radius: 10; -fx-border-style: dashed;");
                imageContainer.setOnMouseClicked(null);
                imageContainer.setOnMouseEntered(null);
                imageContainer.setOnMouseExited(null);
            }
        }
    }

    private void ouvrirDansNavigateur(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception ex) {
            System.err.println("Impossible d'ouvrir le lien Web : " + url);
            ex.printStackTrace();
        }
    }

    private void chargerHistorique(reclamation r) {
        vboxHistorique.getChildren().clear();

        if (r.getResponses() == null || r.getResponses().isEmpty()) {
            Label lblEmpty = new Label("[ AUCUN ÉCHANGE DÉTECTÉ DANS LA BASE DE DONNÉES ]");
            lblEmpty.setStyle("-fx-text-fill: #64748b; -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px;");
            vboxHistorique.getChildren().add(lblEmpty);
            return;
        }

        for (response rep : r.getResponses()) {
            HBox chatRow = new HBox(15);
            chatRow.setAlignment(Pos.TOP_LEFT);

            StackPane avatarBox = new StackPane();
            Circle avatarBg = new Circle(20, javafx.scene.paint.Color.web("rgba(56, 189, 248, 0.2)"));
            avatarBg.setStroke(javafx.scene.paint.Color.web("#0ea5e9"));
            avatarBg.setStrokeWidth(2);
            Label avatarLetter = new Label("G");
            avatarLetter.setStyle("-fx-text-fill: #0ea5e9; -fx-font-weight: 900; -fx-font-size: 18px;");
            avatarBox.getChildren().addAll(avatarBg, avatarLetter);

            VBox bulle = new VBox(8);
            bulle.setStyle("-fx-background-color: rgba(30, 41, 59, 0.8); -fx-padding: 15 20; -fx-background-radius: 0 20 20 20; -fx-border-color: rgba(255,255,255,0.05); -fx-border-radius: 0 20 20 20;");
            HBox.setHgrow(bulle, Priority.ALWAYS);

            HBox infoRow = new HBox(10);
            infoRow.setAlignment(Pos.BASELINE_LEFT);
            Label lblNom = new Label("ADMIN GAMBATTA");
            lblNom.setStyle("-fx-text-fill: #fcc033; -fx-font-weight: 900; -fx-font-size: 12px;");

            String dateStr = (rep.getDaterep() != null) ? rep.getDaterep().toString() : "--";
            Label lblDateMsg = new Label(dateStr);
            lblDateMsg.setStyle("-fx-text-fill: #64748b; -fx-font-family: 'Consolas', monospace; -fx-font-size: 10px;");

            infoRow.getChildren().addAll(lblNom, lblDateMsg);

            Label lblContenu = new Label(rep.getContenurep());
            lblContenu.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-line-spacing: 4;");
            lblContenu.setWrapText(true);

            bulle.getChildren().addAll(infoRow, lblContenu);
            chatRow.getChildren().addAll(avatarBox, bulle);
            vboxHistorique.getChildren().add(chatRow);
        }
    }

    private void analyserSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            setSentimentUI("NEUTRE", "rgba(100, 116, 139, 0.2)", "#94a3b8");
            return;
        }

        String texteLower = text.toLowerCase();
        String[] motsNegatifs = {"nul", "problème", "colère", "déçu", "déception", "honteux", "arnaque", "inacceptable", "bug", "marche pas", "remboursement", "urgent", "pire"};
        String[] motsPositifs = {"merci", "bravo", "super", "génial", "satisfait", "excellent", "parfait", "recommande", "j'adore"};

        int score = 0;
        for (String mot : motsNegatifs) { if (texteLower.contains(mot)) score--; }
        for (String mot : motsPositifs) { if (texteLower.contains(mot)) score++; }

        if (score < 0) {
            setSentimentUI("CRITIQUE ⚠️", "rgba(239, 68, 68, 0.2)", "#ef4444");
        } else if (score > 0) {
            setSentimentUI("POSITIF ✓", "rgba(16, 185, 129, 0.2)", "#10b981");
        } else {
            setSentimentUI("NEUTRE 😐", "rgba(100, 116, 139, 0.2)", "#94a3b8");
        }
    }

    private void setSentimentUI(String texte, String bgColor, String textColor) {
        lblSentiment.setText(texte);
        lblSentiment.setStyle("-fx-background-color: " + bgColor + "; -fx-border-color: " + textColor + "; -fx-border-width: 1.5; -fx-text-fill: " + textColor + "; -fx-font-weight: 900; -fx-padding: 8 20; -fx-background-radius: 20; -fx-border-radius: 20; -fx-effect: dropshadow(gaussian, " + textColor + ", 15, 0, 0, 0);");
    }

    @FXML
    private void handleFermer() {
        if (parentController != null) {
            parentController.masquerFormulaireAjout();
        } else {
            lblTitre.getParent().setVisible(false);
        }
    }
}