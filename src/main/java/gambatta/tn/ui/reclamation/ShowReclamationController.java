package gambatta.tn.ui.reclamation;

import gambatta.tn.entites.reclamation.preuve;
import gambatta.tn.entites.reclamation.reclamation;
import gambatta.tn.entites.reclamation.response;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class ShowReclamationController {

    @FXML private Label lblTitre, lblStatut, lblCategorie, lblDescription;
    @FXML private Label lblId, lblDate, lblUrgence, lblTicketClos;
    @FXML private VBox vboxReponses, boxPreuve;
    @FXML private ImageView imgPreuve;
    @FXML private Button btnOuvrirPreuve, btnOuvrirDiscussion;

    private String cheminPreuveActuel;
    private reclamation currentRec;

    /**
     * Initialise la vue avec les données de la réclamation.
     * Gère l'affichage dynamique selon le type de réponse (Unique ou Interactive).
     */
    public void initData(reclamation r) {
        this.currentRec = r;

        // 1. Informations de base
        lblTitre.setText(r.getTitre() != null ? r.getTitre().toUpperCase() : "SANS TITRE");
        lblCategorie.setText(r.getCategorierec() != null ? r.getCategorierec().toUpperCase() : "GÉNÉRAL");
        lblId.setText(String.format("0x%06X", r.getIdrec()));
        lblDescription.setText(r.getDescrirec());

        if (r.getDaterec() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd // HH:mm:ss");
            lblDate.setText(r.getDaterec().format(formatter));
        }

        // 2. Logique Dynamique des boutons (Point 1 & 2)
        String statutText = r.getStatutrec() != null ? r.getStatutrec().toUpperCase() : "EN ATTENTE";
        lblStatut.setText("[" + statutText + "]");

        // Point 2 : Si l'admin attend le client (EN COURS) -> Bouton discussion
        if (btnOuvrirDiscussion != null) {
            if ("EN COURS".equals(statutText)) {
                btnOuvrirDiscussion.setVisible(true);
                btnOuvrirDiscussion.setManaged(true);
            } else {
                btnOuvrirDiscussion.setVisible(false);
                btnOuvrirDiscussion.setManaged(false);
            }
        }

        // Point 1 : Si réponse unique/clôture (RÉSOLU / FERMÉ) -> Message de clôture
        if ("RÉSOLU".equals(statutText) || "FERMÉ".equals(statutText)) {
            lblStatut.setStyle("-fx-border-color: #10b981; -fx-text-fill: #10b981;");
            if (lblTicketClos != null) {
                lblTicketClos.setVisible(true);
                lblTicketClos.setManaged(true);
            }
        } else if (lblTicketClos != null) {
            lblTicketClos.setVisible(false);
            lblTicketClos.setManaged(false);
        }

        // 3. Gestion de l'urgence
        if (r.isUrgent()) {
            lblUrgence.setText("CRITICAL");
            lblUrgence.setStyle("-fx-text-fill: #ef4444;");
        } else {
            lblUrgence.setText("NOMINAL");
            lblUrgence.setStyle("-fx-text-fill: #10b981;");
        }

        // 4. Gestion de la preuve
        preuve preuveObj = r.getPreuve();
        if (preuveObj != null && preuveObj.getImageName() != null && !preuveObj.getImageName().isEmpty()) {
            this.cheminPreuveActuel = preuveObj.getImageName();
            File fichier = new File(this.cheminPreuveActuel);
            if (fichier.exists()) {
                String nomFichier = fichier.getName().toLowerCase();
                if (nomFichier.endsWith(".png") || nomFichier.endsWith(".jpg") || nomFichier.endsWith(".jpeg")) {
                    imgPreuve.setImage(new Image(fichier.toURI().toString()));
                    imgPreuve.setVisible(true);
                    imgPreuve.setManaged(true);
                    btnOuvrirPreuve.setVisible(false);
                    btnOuvrirPreuve.setManaged(false);
                } else {
                    imgPreuve.setVisible(false);
                    imgPreuve.setManaged(false);
                    btnOuvrirPreuve.setText("📄 ACCESS FILE : " + nomFichier);
                    btnOuvrirPreuve.setVisible(true);
                    btnOuvrirPreuve.setManaged(true);
                }
                boxPreuve.setVisible(true);
                boxPreuve.setManaged(true);
            } else {
                boxPreuve.setVisible(false);
                boxPreuve.setManaged(false);
            }
        } else {
            boxPreuve.setVisible(false);
            boxPreuve.setManaged(false);
        }

        // 5. Chargement de l'historique (Point 1 : affiche même les réponses uniques)
        chargerReponsesReelles();
    }

    private void chargerReponsesReelles() {
        vboxReponses.getChildren().clear();
        if (currentRec.getResponses() == null || currentRec.getResponses().isEmpty()) {
            Label info = new Label("SYS_INFO : No messages found in history.");
            info.setStyle("-fx-text-fill: #64748b; -fx-font-family: 'Consolas'; -fx-font-size: 11px;");
            vboxReponses.getChildren().add(info);
            return;
        }

        for (response rep : currentRec.getResponses()) {
            VBox bulle = new VBox(5);
            bulle.setStyle("-fx-background-color: #0f172a; -fx-padding: 10; -fx-border-color: #1e293b; -fx-border-radius: 5;");

            Label auteur = new Label("ADMIN_REPONSE :");
            auteur.setStyle("-fx-text-fill: #38bdf8; -fx-font-family: 'Consolas'; -fx-font-size: 10px;");

            Label texte = new Label(rep.getContenurep());
            texte.setStyle("-fx-text-fill: #cbd5e1; -fx-font-family: 'Consolas'; -fx-font-size: 11px;");
            texte.setWrapText(true);

            bulle.getChildren().addAll(auteur, texte);
            vboxReponses.getChildren().add(bulle);
        }
    }

    @FXML
    private void handleOuvrirDiscussion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta/tn/ui/reclamation/client_chat.fxml"));
            Parent root = loader.load();

            // TRANSFERT DE DONNÉES AU CONTRÔLEUR DE CHAT
            ClientChatController chatController = loader.getController();
            chatController.initData(currentRec);

            Stage stage = new Stage();
            stage.setTitle("TERMINAL DISCUSSION : #" + currentRec.getIdrec());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

            handleFermer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleOuvrirPreuve() {
        if (cheminPreuveActuel != null) {
            try {
                File fichier = new File(cheminPreuveActuel);
                if (fichier.exists() && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(fichier);
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    @FXML
    private void handleFermer() {
        ((Stage) lblTitre.getScene().getWindow()).close();
    }
}