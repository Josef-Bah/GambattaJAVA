package gambatta.tn.ui.reclamation;

import gambatta.tn.entites.reclamation.reclamation;
import gambatta.tn.entites.reclamation.response;
import gambatta.tn.services.reclamation.ServiceReclamation;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class ClientChatController {

    @FXML private Label lblTicketId;
    @FXML private VBox vboxChat;
    @FXML private TextArea txtMessage;

    private reclamation currentRec;
    private ServiceReclamation service = new ServiceReclamation();

    // GESTION INCRUSTATION (SINGLE STAGE)
    private ReclamationController parentController;

    public void setParentController(ReclamationController parentController) {
        this.parentController = parentController;
    }

    public void initData(reclamation r) {
        this.currentRec = r;
        lblTicketId.setText("REF: 0x" + String.format("%06X", r.getIdrec()));
        chargerDiscussion();
    }

    private void chargerDiscussion() {
        vboxChat.getChildren().clear();
        if (currentRec.getResponses() == null || currentRec.getResponses().isEmpty()) {
            Label lblEmpty = new Label("SYS_INFO : Aucun échange enregistré.");
            lblEmpty.setStyle("-fx-text-fill: #64748b; -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px;");
            vboxChat.getChildren().add(lblEmpty);
            return;
        }

        for (response rep : currentRec.getResponses()) {
            VBox bulle = new VBox(5);
            bulle.setStyle("-fx-background-color: rgba(2,6,23,0.6); -fx-padding: 12; -fx-border-color: rgba(56,189,248,0.3); -fx-border-radius: 8; -fx-border-width: 0 0 0 2;");

            Label auteur = new Label("SUPPORT_LINK_ESTABLISHED :");
            auteur.setStyle("-fx-text-fill: #38bdf8; -fx-font-family: 'Consolas', monospace; -fx-font-size: 10px; -fx-font-weight: bold;");

            Label texte = new Label(rep.getContenurep());
            texte.setStyle("-fx-text-fill: white; -fx-font-family: 'Consolas', monospace; -fx-font-size: 12px; -fx-line-spacing: 4;");
            texte.setWrapText(true);

            bulle.getChildren().addAll(auteur, texte);
            vboxChat.getChildren().add(bulle);
        }
    }

    @FXML
    private void handleEnvoyer() {
        String msg = txtMessage.getText().trim();
        if (msg.isEmpty()) return;

        // 1. Création de la réponse client
        response nouvelleRep = new response();
        nouvelleRep.setContenurep("[CLIENT] : " + msg);

        // 2. Liaison et sauvegarde
        currentRec.addResponse(nouvelleRep);
        service.ajouterReponse(nouvelleRep);

        // 3. Mise à jour visuelle
        txtMessage.clear();
        chargerDiscussion();
    }

    @FXML
    private void handleFermer() {
        // Appelle la méthode du parent pour fermer le panneau latéral avec animation
        if (parentController != null) {
            parentController.masquerFormulaireAjout();
        } else {
            txtMessage.getParent().setVisible(false);
        }
    }
}