package gambatta.tn.ui.reclamation;

import gambatta.tn.entites.reclamation.reclamation;
import gambatta.tn.entites.reclamation.response;
import gambatta.tn.services.reclamation.ServiceReclamation;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ClientChatController {

    @FXML private Label lblTicketId;
    @FXML private VBox vboxChat;
    @FXML private TextArea txtMessage;

    private reclamation currentRec;
    private ServiceReclamation service = new ServiceReclamation();

    public void initData(reclamation r) {
        this.currentRec = r;
        lblTicketId.setText("TICKET_REF: 0x" + String.format("%06X", r.getIdrec()));
        chargerDiscussion();
    }

    private void chargerDiscussion() {
        vboxChat.getChildren().clear();
        if (currentRec.getResponses() == null || currentRec.getResponses().isEmpty()) return;

        for (response rep : currentRec.getResponses()) {
            VBox bulle = new VBox(5);
            // Style différent selon si c'est un message système/admin ou autre
            bulle.setStyle("-fx-background-color: #0f172a; -fx-padding: 10; -fx-border-color: #1e293b; -fx-border-radius: 5;");

            Label auteur = new Label("SUPPORT_LINK_ESTABLISHED :");
            auteur.setStyle("-fx-text-fill: #38bdf8; -fx-font-family: 'Consolas'; -fx-font-size: 10px;");

            Label texte = new Label(rep.getContenurep());
            texte.setStyle("-fx-text-fill: #cbd5e1; -fx-font-family: 'Consolas'; -fx-font-size: 11px;");
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
        ((Stage) txtMessage.getScene().getWindow()).close();
    }
}