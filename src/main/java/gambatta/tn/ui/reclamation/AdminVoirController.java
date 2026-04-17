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
import javafx.stage.Stage;
import java.io.File;

public class AdminVoirController {

    @FXML private Label lblTitre, lblDescription, lblDate, lblCategorie, lblAuteur, lblStatut, lblSentiment, lblNoImage;
    @FXML private ImageView imgPreuve;
    @FXML private VBox vboxHistorique;
    @FXML private Button btnClose;

    public void initData(reclamation r) {
        lblTitre.setText(r.getTitre() != null ? r.getTitre().toUpperCase() : "SANS TITRE");
        lblDescription.setText(r.getDescrirec() != null ? r.getDescrirec() : "Aucune description fournie.");
        lblCategorie.setText(r.getCategorierec() != null ? r.getCategorierec().toUpperCase() : "NON SPÉCIFIÉ");

        lblAuteur.setText("JOUEUR_01"); // Si tu as l'info de l'utilisateur, remplace ici

        if (r.getDaterec() != null) {
            lblDate.setText(r.getDaterec().toString());
        }

        // Style dynamique du Statut
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

        // Gestion de l'image
        boolean hasImage = false;
        if (r.getPreuve() != null && r.getPreuve().getImageName() != null) {
            try {
                File file = new File(r.getPreuve().getImageName());
                if (file.exists()) {
                    imgPreuve.setImage(new Image(file.toURI().toString()));
                    hasImage = true;
                }
            } catch (Exception e) {
                System.out.println("Erreur de chargement de l'image.");
            }
        }

        if (!hasImage) {
            lblNoImage.setVisible(true);
            imgPreuve.setVisible(false);
        }

        chargerHistorique(r);

        // --- Animation de Hover gérée en Java pour éviter l'erreur FXML ---
        if (btnClose != null) {
            String baseStyle = "-fx-background-color: transparent; -fx-border-color: #ef4444; -fx-text-fill: #ef4444; -fx-font-weight: 900; -fx-font-family: 'Consolas', monospace; -fx-padding: 12 30; -fx-background-radius: 25; -fx-border-radius: 25; -fx-border-width: 2; -fx-cursor: hand;";
            String hoverStyle = "-fx-background-color: #ef4444; -fx-border-color: #ef4444; -fx-text-fill: white; -fx-font-weight: 900; -fx-font-family: 'Consolas', monospace; -fx-padding: 12 30; -fx-background-radius: 25; -fx-border-radius: 25; -fx-border-width: 2; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(239,68,68,0.6), 15, 0, 0, 0);";

            btnClose.setOnMouseEntered(e -> btnClose.setStyle(hoverStyle));
            btnClose.setOnMouseExited(e -> btnClose.setStyle(baseStyle));
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
        ((Stage) lblTitre.getScene().getWindow()).close();
    }
}