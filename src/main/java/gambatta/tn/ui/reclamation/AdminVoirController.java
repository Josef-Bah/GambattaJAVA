package gambatta.tn.ui.reclamation;

import gambatta.tn.entites.reclamation.reclamation;
import gambatta.tn.entites.reclamation.response; // Import de ton entité réponse
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.File;

public class AdminVoirController {

    @FXML private Label lblTitre, lblDescription, lblDate, lblCategorie, lblAuteur, lblStatut, lblSentiment, lblNoImage;
    @FXML private ImageView imgPreuve;
    @FXML private VBox vboxHistorique; // Référence vers le conteneur ajouté dans le FXML

    public void initData(reclamation r) {
        // 1. Peuplement des données de base
        lblTitre.setText(r.getTitre() != null ? r.getTitre().toUpperCase() : "SANS TITRE");
        lblDescription.setText(r.getDescrirec() != null ? r.getDescrirec() : "Aucune description fournie.");
        lblCategorie.setText(r.getCategorierec() != null ? r.getCategorierec().toUpperCase() : "NON SPÉCIFIÉ");
        lblStatut.setText(r.getStatutrec() != null ? r.getStatutrec().toUpperCase() : "INCONNU");

        lblAuteur.setText("Client/Utilisateur");

        if (r.getDaterec() != null) {
            lblDate.setText(r.getDaterec().toString());
        }

        // 2. Analyse Sémantique (Détection de sentiment)
        analyserSentiment(r.getDescrirec());

        // 3. Gestion de l'image (Preuve)
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

        // 4. Chargement de l'historique des échanges (Réponses)
        chargerHistorique(r);
    }

    /**
     * Parcourt la liste des réponses et les affiche dans le VBox
     */
    private void chargerHistorique(reclamation r) {
        vboxHistorique.getChildren().clear(); // On vide d'abord le conteneur

        if (r.getResponses() == null || r.getResponses().isEmpty()) {
            Label lblEmpty = new Label("Aucun message enregistré pour ce ticket.");
            lblEmpty.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic;");
            vboxHistorique.getChildren().add(lblEmpty);
            return;
        }

        // On crée une bulle pour chaque réponse trouvée dans l'objet réclamation
        for (response rep : r.getResponses()) {
            VBox bulle = new VBox(5);
            bulle.setStyle("-fx-background-color: #1e293b; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #334155; -fx-border-width: 1;");

            // En-tête du message (Date)
            String dateStr = (rep.getDaterep() != null) ? rep.getDaterep().toString() : "Date inconnue";
            Label lblHeader = new Label("MESSAGE OFFICIEL - " + dateStr);
            lblHeader.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 10px; -fx-font-weight: bold;");

            // Contenu du message
            Label lblContenu = new Label(rep.getContenurep());
            lblContenu.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 13px;");
            lblContenu.setWrapText(true);

            bulle.getChildren().addAll(lblHeader, lblContenu);
            vboxHistorique.getChildren().add(bulle);
        }
    }

    private void analyserSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            setSentimentUI("NEUTRE 😐", "#64748b");
            return;
        }

        String texteLower = text.toLowerCase();
        String[] motsNegatifs = {"nul", "problème", "colère", "déçu", "déception", "honteux", "arnaque", "inacceptable", "bug", "marche pas", "remboursement", "urgent", "pire"};
        String[] motsPositifs = {"merci", "bravo", "super", "génial", "satisfait", "excellent", "parfait", "recommande", "j'adore"};

        int score = 0;
        for (String mot : motsNegatifs) { if (texteLower.contains(mot)) score--; }
        for (String mot : motsPositifs) { if (texteLower.contains(mot)) score++; }

        if (score < 0) {
            setSentimentUI("MÉCONTENTEMENT DÉTECTÉ 😡", "#ef4444");
        } else if (score > 0) {
            setSentimentUI("CLIENT SATISFAIT 💚", "#10b981");
        } else {
            setSentimentUI("TON NEUTRE 😐", "#64748b");
        }
    }

    private void setSentimentUI(String texte, String couleurHex) {
        lblSentiment.setText(texte);
        lblSentiment.setStyle("-fx-background-color: " + couleurHex + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
    }

    @FXML
    private void handleFermer() {
        ((Stage) lblTitre.getScene().getWindow()).close();
    }
}