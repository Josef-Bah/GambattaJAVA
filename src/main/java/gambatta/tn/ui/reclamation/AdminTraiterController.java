package gambatta.tn.ui.reclamation;

import gambatta.tn.entites.reclamation.reclamation;
import gambatta.tn.entites.reclamation.response;
import gambatta.tn.services.reclamation.ServiceReclamation;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

public class AdminTraiterController {

    @FXML private Label lblRef;
    @FXML private ComboBox<String> comboStatut;
    @FXML private ComboBox<String> comboAssignation;
    @FXML private ComboBox<String> comboSaisieRapide;
    @FXML private TextArea txtReponse;
    @FXML private VBox vboxHistorique;

    @FXML private Button btnUpdateStatut, btnAssigner, btnFermer, btnAmeliorer, btnReponseInteractive, btnReponseUnique;

    private reclamation currentRec;
    private AdminDashboardController parent;
    private ServiceReclamation service = new ServiceReclamation();

    @FXML
    public void initialize() {
        // SÉCURITÉ : On vérifie que les éléments existent dans le FXML avant de les utiliser
        if (comboStatut != null) {
            comboStatut.getItems().addAll("En attente", "En cours", "Résolu", "Fermé");
        }

        if (comboAssignation != null) {
            comboAssignation.getItems().addAll("Non assigné", "Support Technique", "Service Financier", "Modération");
        }

        if (comboSaisieRapide != null) {
            comboSaisieRapide.getItems().addAll(
                    "Bonjour, nous avons bien reçu votre demande.",
                    "Merci de nous fournir une capture d'écran du problème.",
                    "Votre demande a été transmise au service technique.",
                    "Le problème est maintenant résolu. Merci de votre patience."
            );

            comboSaisieRapide.setOnAction(e -> {
                String template = comboSaisieRapide.getValue();
                if (template != null && !template.isEmpty() && txtReponse != null) {
                    String currentText = txtReponse.getText();
                    txtReponse.setText(currentText + (currentText.isEmpty() ? "" : "\n") + template);
                    javafx.application.Platform.runLater(() -> {
                        comboSaisieRapide.getSelectionModel().clearSelection();
                    });
                }
            });
        }

        // EFFETS HOVER (La méthode setupNeonHover gère déjà les null)
        setupNeonHover(btnUpdateStatut, "#0ea5e9", "transparent");
        setupNeonHover(btnAssigner, "#f59e0b", "transparent");
        setupNeonHover(btnAmeliorer, "#10b981", "transparent");
        setupNeonHover(btnReponseInteractive, "#f59e0b", "transparent");
        setupSolidHover(btnReponseUnique, "#38bdf8", "#020617");

        if(btnFermer != null) {
            String baseFermer = "-fx-background-color: transparent; -fx-border-color: #ef4444; -fx-text-fill: #ef4444; -fx-font-weight: 900; -fx-font-family: 'Consolas', monospace; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-width: 2; -fx-padding: 10; -fx-cursor: hand;";
            String hoverFermer = "-fx-background-color: #ef4444; -fx-border-color: #ef4444; -fx-text-fill: white; -fx-font-weight: 900; -fx-font-family: 'Consolas', monospace; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-width: 2; -fx-padding: 10; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(239,68,68,0.6), 15, 0, 0, 0);";
            btnFermer.setOnMouseEntered(e -> btnFermer.setStyle(hoverFermer));
            btnFermer.setOnMouseExited(e -> btnFermer.setStyle(baseFermer));
        }
    }

    public void initData(reclamation r, AdminDashboardController parent) {
        this.currentRec = r;
        this.parent = parent;

        if (lblRef != null) lblRef.setText("TICKET #" + r.getIdrec());
        if (comboStatut != null) comboStatut.setValue(r.getStatutrec() != null ? r.getStatutrec() : "En attente");
        if (comboAssignation != null) comboAssignation.setValue("Non assigné");

        if (vboxHistorique != null) chargerHistorique();
    }

    private void chargerHistorique() {
        if (vboxHistorique == null) return;

        vboxHistorique.getChildren().clear();

        if (currentRec.getResponses() == null || currentRec.getResponses().isEmpty()) {
            Label noMessage = new Label("[ AUCUN MESSAGE. INITIEZ LA COMMUNICATION. ]");
            noMessage.setStyle("-fx-text-fill: #64748b; -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px;");
            vboxHistorique.getChildren().add(noMessage);
            return;
        }

        for (response rep : currentRec.getResponses()) {
            HBox chatRow = new HBox(10);
            chatRow.setAlignment(Pos.TOP_LEFT);

            StackPane avatarBox = new StackPane();
            Circle avatarBg = new Circle(15, javafx.scene.paint.Color.web("rgba(56, 189, 248, 0.2)"));
            avatarBg.setStroke(javafx.scene.paint.Color.web("#0ea5e9"));
            avatarBg.setStrokeWidth(1.5);
            Label avatarLetter = new Label("A");
            avatarLetter.setStyle("-fx-text-fill: #0ea5e9; -fx-font-weight: 900; -fx-font-size: 14px;");
            avatarBox.getChildren().addAll(avatarBg, avatarLetter);

            VBox bulle = new VBox(5);
            bulle.setStyle("-fx-background-color: rgba(30, 41, 59, 0.8); -fx-padding: 10 15; -fx-background-radius: 0 15 15 15; -fx-border-color: rgba(255,255,255,0.05); -fx-border-radius: 0 15 15 15;");
            HBox.setHgrow(bulle, Priority.ALWAYS);

            HBox infoRow = new HBox(10);
            infoRow.setAlignment(Pos.BASELINE_LEFT);
            Label lblNom = new Label("ADMIN");
            lblNom.setStyle("-fx-text-fill: #fcc033; -fx-font-weight: 900; -fx-font-size: 10px; -fx-font-family: 'Consolas', monospace;");

            String dateStr = (rep.getDaterep() != null) ? rep.getDaterep().toString() : "--";
            Label lblDateMsg = new Label(dateStr);
            lblDateMsg.setStyle("-fx-text-fill: #64748b; -fx-font-family: 'Consolas', monospace; -fx-font-size: 8px;");

            infoRow.getChildren().addAll(lblNom, lblDateMsg);

            Label lblContenu = new Label(rep.getContenurep());
            lblContenu.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-line-spacing: 3;");
            lblContenu.setWrapText(true);

            bulle.getChildren().addAll(infoRow, lblContenu);
            chatRow.getChildren().addAll(avatarBox, bulle);
            vboxHistorique.getChildren().add(chatRow);
        }
    }

    private void setupNeonHover(Button btn, String colorHex, String baseBg) {
        if (btn == null) return;
        String base = "-fx-background-color: " + baseBg + "; -fx-border-color: " + colorHex + "; -fx-text-fill: " + colorHex + "; -fx-font-weight: bold; -fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 8; -fx-cursor: hand;";
        String hover = "-fx-background-color: " + colorHex + "; -fx-border-color: " + colorHex + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 8; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, " + colorHex + ", 15, 0, 0, 0);";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
    }

    private void setupSolidHover(Button btn, String bgColorHex, String textColor) {
        if (btn == null) return;
        String base = "-fx-background-color: " + bgColorHex + "; -fx-border-color: " + bgColorHex + "; -fx-text-fill: " + textColor + "; -fx-font-weight: 900; -fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 8; -fx-cursor: hand;";
        String hover = "-fx-background-color: white; -fx-border-color: white; -fx-text-fill: " + bgColorHex + "; -fx-font-weight: 900; -fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 8; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, " + bgColorHex + ", 20, 0, 0, 0);";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
    }

    @FXML
    private void handleAmeliorerTexte() {
        if (txtReponse == null) return;
        String text = txtReponse.getText().trim();
        if (text.isEmpty()) return;
        String texteAmeliore = "Bonjour,\n\n" + text.substring(0, 1).toUpperCase() + text.substring(1) + "\n\nCordialement,\nL'équipe Support Gambatta";
        txtReponse.setText(texteAmeliore);
    }

    @FXML
    private void handleReponseInteractive() {
        if (!creerEtSauvegarderReponse()) return;
        currentRec.setStatutrec("En cours");
        service.modifier(currentRec);
        if (txtReponse != null) txtReponse.clear();
        chargerHistorique();
        if (parent != null) parent.chargerTableau();
    }

    @FXML
    private void handleReponseUnique() {
        if (!creerEtSauvegarderReponse()) return;
        currentRec.setStatutrec("Résolu");
        service.modifier(currentRec);
        if (parent != null) parent.chargerTableau();
        handleAnnuler(); // Ferme le panneau après
    }

    private boolean creerEtSauvegarderReponse() {
        if (txtReponse == null) return false;
        String texteSaisi = txtReponse.getText();
        if (texteSaisi == null || texteSaisi.trim().isEmpty()) return false;
        response nouvelleReponse = new response();
        nouvelleReponse.setContenurep(texteSaisi.trim());
        currentRec.addResponse(nouvelleReponse);
        service.ajouterReponse(nouvelleReponse);
        return true;
    }

    @FXML private void handleChangerStatut() {
        if (comboStatut != null) {
            currentRec.setStatutrec(comboStatut.getValue());
            service.modifier(currentRec);
            if (parent != null) parent.chargerTableau();
        }
    }

    @FXML private void handleAssigner() {
        // Logique d'assignation
    }

    @FXML
    private void handleSauvegarder() {
        if (comboStatut != null) {
            currentRec.setStatutrec(comboStatut.getValue());
        }

        if (txtReponse != null) {
            String texteSaisi = txtReponse.getText();
            if (texteSaisi != null && !texteSaisi.trim().isEmpty()) {
                response nouvelleReponse = new response();
                nouvelleReponse.setContenurep(texteSaisi.trim());
                currentRec.addResponse(nouvelleReponse);
                service.ajouterReponse(nouvelleReponse);
            }
        }

        service.modifier(currentRec);

        if (parent != null) {
            parent.chargerTableau();
            parent.masquerFormulaireAjout();
        }
    }

    @FXML private void handleAnnuler() {
        if (parent != null) {
            parent.masquerFormulaireAjout();
        } else if (lblRef != null) {
            lblRef.getScene().getWindow().hide();
        }
    }
}