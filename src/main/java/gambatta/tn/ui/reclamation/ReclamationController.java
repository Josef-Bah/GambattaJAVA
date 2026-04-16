package gambatta.tn.ui.reclamation;

import gambatta.tn.entites.reclamation.reclamation;
import gambatta.tn.services.reclamation.ServiceReclamation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ReclamationController implements Initializable {

    @FXML private FlowPane cardsContainer;

    private ServiceReclamation service = new ServiceReclamation();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chargerTableau();
    }

    @FXML
    public void chargerTableau() {
        cardsContainer.getChildren().clear();
        List<reclamation> liste = service.afficher();

        for (reclamation r : liste) {
            // Création de la carte (Module Technologique)
            VBox card = new VBox(10);
            card.setPrefWidth(380);
            card.setPrefHeight(230); // Ajusté pour le bouton supplémentaire

            String cardBaseStyle = "-fx-background-color: rgba(15, 23, 42, 0.6); -fx-background-radius: 12; -fx-border-color: rgba(14, 165, 233, 0.3); -fx-border-radius: 12; -fx-border-width: 1.5; -fx-padding: 20;";
            String cardHoverStyle = "-fx-background-color: rgba(30, 41, 59, 0.9); -fx-background-radius: 12; -fx-border-color: #FFD700; -fx-border-radius: 12; -fx-border-width: 1.5; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(255, 215, 0, 0.3), 15, 0, 0, 0);";

            card.setStyle(cardBaseStyle);
            card.setOnMouseEntered(e -> card.setStyle(cardHoverStyle));
            card.setOnMouseExited(e -> card.setStyle(cardBaseStyle));

            // Ligne 1 : ID et Statut
            HBox topRow = new HBox();
            topRow.setAlignment(Pos.CENTER_LEFT);

            Label idLabel = new Label("SYS_ID: #" + r.getIdrec());
            idLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 10px; -fx-font-weight: bold; -fx-letter-spacing: 1px;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            String statutText = (r.getStatutrec() != null) ? r.getStatutrec().toUpperCase() : "EN ATTENTE";
            Label status = new Label(statutText);
            String badgeStyle = "-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: 900; -fx-background-radius: 5; -fx-padding: 4 10;";

            if (statutText.equals("RÉSOLU") || statutText.equals("TRAITÉ")) {
                status.setStyle(badgeStyle + "-fx-background-color: #10b981;");
            } else if (statutText.equals("EN COURS")) {
                status.setStyle(badgeStyle + "-fx-background-color: #0ea5e9;"); // Bleu cyber pour l'actif
            } else {
                status.setStyle(badgeStyle + "-fx-background-color: #f59e0b;");
            }
            topRow.getChildren().addAll(idLabel, spacer, status);

            // Titre, Catégorie et Description
            Label title = new Label(r.getTitre() != null ? r.getTitre().toUpperCase() : "LOG CORROMPU");
            title.setStyle("-fx-font-size: 16px; -fx-font-weight: 900; -fx-text-fill: white; -fx-font-family: 'Segoe UI Black';");

            Label serviceLabel = new Label("MODULE : " + (r.getCategorierec() != null ? r.getCategorierec() : "GÉNÉRAL").toUpperCase());
            serviceLabel.setStyle("-fx-text-fill: #0ea5e9; -fx-font-weight: 900; -fx-font-size: 10px;");

            Label desc = new Label(r.getDescrirec());
            desc.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");
            desc.setWrapText(true);
            desc.setMaxHeight(40);

            Region pushBottom = new Region();
            VBox.setVgrow(pushBottom, Priority.ALWAYS);

            // --- SECTION ACTIONS ---
            HBox actions = new HBox(8);
            actions.setAlignment(Pos.CENTER_RIGHT);

            // RÈGLE : Si l'admin attend le client (EN COURS), on ajoute le bouton CHAT
            if ("EN COURS".equals(statutText)) {
                Button btnChat = new Button("💬 RESPOND");
                styleNeonButton(btnChat, "#10b981", "#020617"); // Style vert néon
                btnChat.setOnAction(e -> handleOuvrirChat(r));
                actions.getChildren().add(btnChat);
            }

            Button btnVoir = new Button("👁 VIEW");
            styleNeonButton(btnVoir, "#0ea5e9", "#020617");
            btnVoir.setOnAction(e -> handleVoirDetails(r));

            Button btnModifier = new Button("✏️ EDIT");
            styleNeonButton(btnModifier, "#FFD700", "#020617");
            btnModifier.setOnAction(e -> handleModifier(r));

            Button btnAnnuler = new Button("🚫 DEL");
            styleNeonButton(btnAnnuler, "#ef4444", "white");
            btnAnnuler.setOnAction(e -> afficherConfirmationCustom(r));

            actions.getChildren().addAll(btnVoir, btnModifier, btnAnnuler);

            card.getChildren().addAll(topRow, title, serviceLabel, desc, pushBottom, new Separator(), actions);
            cardsContainer.getChildren().add(card);
        }
    }

    private void handleOuvrirChat(reclamation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/reclamation/client_chat.fxml"));
            Parent root = loader.load();

            ClientChatController controller = loader.getController();
            controller.initData(r);

            Stage stage = new Stage();
            stage.setTitle("TERMINAL DE COMMUNICATION : #" + r.getIdrec());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            chargerTableau(); // Rafraîchir pour voir si le statut a changé
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleVoirDetails(reclamation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/reclamation/show_reclamation.fxml"));
            Parent root = loader.load();
            ShowReclamationController controller = loader.getController();
            controller.initData(r);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void styleNeonButton(Button btn, String neonColor, String textHoverColor) {
        String baseStyle = "-fx-background-color: transparent; -fx-border-color: " + neonColor + "; -fx-border-width: 1; -fx-text-fill: " + neonColor + "; -fx-font-weight: 900; -fx-border-radius: 5; -fx-padding: 5 10; -fx-cursor: hand; -fx-font-size: 10px;";
        String hoverStyle = "-fx-background-color: " + neonColor + "; -fx-border-color: " + neonColor + "; -fx-border-width: 1; -fx-text-fill: " + textHoverColor + "; -fx-font-weight: 900; -fx-border-radius: 5; -fx-padding: 5 10; -fx-cursor: hand; -fx-font-size: 10px; -fx-effect: dropshadow(three-pass-box, " + neonColor + ", 10, 0.4, 0, 0);";
        btn.setStyle(baseStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
    }

    // --- AUTRES MÉTHODES ---

    @FXML
    private void handleAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/reclamation/ajout_reclamation.fxml"));
            Parent root = loader.load();
            AjoutController ajoutController = loader.getController();
            ajoutController.setParentController(this);
            Stage stage = new Stage();
            stage.setTitle("INITIALISER TICKET");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleModifier(reclamation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/reclamation/edit_reclamation.fxml"));
            Parent root = loader.load();
            EditReclamationController controller = loader.getController();
            controller.initData(r, this);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void afficherConfirmationCustom(reclamation r) {
        Stage confirmStage = new Stage();
        confirmStage.initModality(Modality.APPLICATION_MODAL);
        confirmStage.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #020617; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 15; -fx-background-radius: 15; -fx-padding: 40;");

        Label title = new Label("PURGE SYSTÈME");
        title.setStyle("-fx-font-family: 'Segoe UI Black'; -fx-font-size: 22px; -fx-text-fill: #ef4444;");

        Label message = new Label("Supprimer définitivement le log :\n\"" + r.getTitre() + "\" ?");
        message.setStyle("-fx-text-fill: #cbd5e1; -fx-text-alignment: center;");
        message.setWrapText(true);

        HBox btnBox = new HBox(20);
        btnBox.setAlignment(Pos.CENTER);

        Button btnCancel = new Button("ABORT");
        styleNeonButton(btnCancel, "#64748b", "white");
        btnCancel.setOnAction(e -> confirmStage.close());

        Button btnConfirm = new Button("TERMINATE");
        styleNeonButton(btnConfirm, "#ef4444", "white");
        btnConfirm.setOnAction(e -> {
            service.supprimer(r.getIdrec());
            chargerTableau();
            confirmStage.close();
        });

        btnBox.getChildren().addAll(btnCancel, btnConfirm);
        root.getChildren().addAll(title, message, btnBox);
        confirmStage.setScene(new Scene(root, Color.TRANSPARENT));
        confirmStage.showAndWait();
    }

    @FXML
    private void handleLogout() throws java.io.IOException {
        Stage stage = (Stage) cardsContainer.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/gambatta.tn.ui/reclamation/portal.fxml"));
        stage.getScene().setRoot(root);
    }
}