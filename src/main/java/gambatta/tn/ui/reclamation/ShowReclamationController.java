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

    // GESTION INCRUSTATION
    private ReclamationController parentController;

    public void setParentController(ReclamationController parentController) {
        this.parentController = parentController;
    }

    public void initData(reclamation r) {
        this.currentRec = r;

        lblTitre.setText(r.getTitre() != null ? r.getTitre().toUpperCase() : "SANS TITRE");
        lblCategorie.setText(r.getCategorierec() != null ? r.getCategorierec().toUpperCase() : "GÉNÉRAL");
        lblId.setText(String.format("0x%06X", r.getIdrec()));
        lblDescription.setText(r.getDescrirec());

        if (r.getDaterec() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd // HH:mm:ss");
            lblDate.setText(r.getDaterec().format(formatter));
        }

        String statutText = r.getStatutrec() != null ? r.getStatutrec().toUpperCase() : "EN ATTENTE";
        lblStatut.setText("[" + statutText + "]");

        if (btnOuvrirDiscussion != null) {
            if ("EN COURS".equals(statutText)) {
                btnOuvrirDiscussion.setVisible(true);
                btnOuvrirDiscussion.setManaged(true);
            } else {
                btnOuvrirDiscussion.setVisible(false);
                btnOuvrirDiscussion.setManaged(false);
            }
        }

        if ("RÉSOLU".equals(statutText) || "FERMÉ".equals(statutText)) {
            lblStatut.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
            if (lblTicketClos != null) {
                lblTicketClos.setVisible(true);
                lblTicketClos.setManaged(true);
            }
        } else {
            lblStatut.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
            if (lblTicketClos != null) {
                lblTicketClos.setVisible(false);
                lblTicketClos.setManaged(false);
            }
        }

        if (r.isUrgent()) {
            lblUrgence.setText("CRITICAL");
            lblUrgence.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-font-weight: bold;");
        } else {
            lblUrgence.setText("NOMINAL");
            lblUrgence.setStyle("-fx-text-fill: #10b981; -fx-font-size: 12px; -fx-font-weight: bold;");
        }

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
            texte.setStyle("-fx-text-fill: #cbd5e1; -fx-font-family: 'Consolas'; -fx-font-size: 12px;");
            texte.setWrapText(true);

            bulle.getChildren().addAll(auteur, texte);
            vboxReponses.getChildren().add(bulle);
        }
    }

    @FXML
    private void handleOuvrirDiscussion() {
        // Au lieu d'ouvrir une nouvelle fenêtre, on dit au parent de charger le chat dans le panneau !
        if (parentController != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta/tn/ui/reclamation/client_chat.fxml"));
                Parent root = loader.load();

                ClientChatController chatController = loader.getController();
                chatController.initData(currentRec);
                // Si ClientChatController a besoin d'un parent pour se fermer :
                // chatController.setParentController(parentController);

                // On dit au parent d'afficher CE NOUVEAU panneau par-dessus (ou à la place)
                parentController.afficherSidePanel(root);

            } catch (IOException e) {
                e.printStackTrace();
            }
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
        if (parentController != null) {
            // Ferme le panneau latéral avec l'animation
            parentController.masquerFormulaireAjout();
        } else {
            lblTitre.getParent().setVisible(false);
        }
    }
}