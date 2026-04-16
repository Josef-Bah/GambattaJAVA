package gambatta.tn.ui.reclamation;

import gambatta.tn.entites.reclamation.reclamation;
import gambatta.tn.entites.reclamation.response;
import gambatta.tn.services.reclamation.ServiceReclamation;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AdminTraiterController {

    @FXML private Label lblRef;
    @FXML private ComboBox<String> comboStatut;
    @FXML private ComboBox<String> comboAssignation;
    @FXML private ComboBox<String> comboSaisieRapide;
    @FXML private TextArea txtReponse;
    @FXML private VBox vboxHistorique;

    private reclamation currentRec;
    private AdminDashboardController parent;
    private ServiceReclamation service = new ServiceReclamation();

    @FXML
    public void initialize() {
        comboStatut.getItems().addAll("En attente", "En cours", "Résolu", "Fermé");
        comboAssignation.getItems().addAll("Non assigné", "Support Technique", "Service Financier", "Modération");

        // --- 1. CONFIGURATION DE LA SAISIE RAPIDE ---
        comboSaisieRapide.getItems().addAll(
                "Bonjour, nous avons bien reçu votre demande.",
                "Merci de nous fournir une capture d'écran du problème.",
                "Votre demande a été transmise au service technique.",
                "Le problème est maintenant résolu. Merci de votre patience."
        );

        // Ajoute le texte sélectionné dans la TextArea automatiquement
        comboSaisieRapide.setOnAction(e -> {
            String template = comboSaisieRapide.getValue();
            if (template != null && !template.isEmpty()) {
                String currentText = txtReponse.getText();
                txtReponse.setText(currentText + (currentText.isEmpty() ? "" : "\n") + template);

                // CORRECTION DU CRASH : On utilise Platform.runLater pour laisser
                // JavaFX terminer son animation avant de vider la sélection
                javafx.application.Platform.runLater(() -> {
                    comboSaisieRapide.getSelectionModel().clearSelection();
                });
            }
        });
    }

    public void initData(reclamation r, AdminDashboardController parent) {
        this.currentRec = r;
        this.parent = parent;
        lblRef.setText("TICKET #" + r.getIdrec());
        comboStatut.setValue(r.getStatutrec() != null ? r.getStatutrec() : "En attente");
        comboAssignation.setValue("Non assigné");
        chargerHistorique();
    }

    private void chargerHistorique() {
        vboxHistorique.getChildren().clear();
        if (currentRec.getResponses() == null || currentRec.getResponses().isEmpty()) {
            Label noMessage = new Label("Aucun message. Initiez la conversation.");
            noMessage.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic;");
            vboxHistorique.getChildren().add(noMessage);
            return;
        }

        for (response rep : currentRec.getResponses()) {
            VBox bulle = new VBox(5);
            bulle.setStyle("-fx-background-color: #1e293b; -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #334155;");

            String dateStr = (rep.getDaterep() != null) ? rep.getDaterep().toString() : "";
            Label lblHeader = new Label("Admin - " + dateStr);
            lblHeader.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 10px; -fx-font-weight: bold;");

            Label lblContenu = new Label(rep.getContenurep());
            lblContenu.setStyle("-fx-text-fill: white;");
            lblContenu.setWrapText(true);

            bulle.getChildren().addAll(lblHeader, lblContenu);
            vboxHistorique.getChildren().add(bulle);
        }
    }

    // --- 2. AMÉLIORATION DE TEXTE (Simulation IA) ---
    @FXML
    private void handleAmeliorerTexte() {
        String text = txtReponse.getText().trim();
        if (text.isEmpty()) return;

        // Met la première lettre en majuscule, ajoute des formules de politesse
        String texteAmeliore = "Bonjour,\n\n" +
                text.substring(0, 1).toUpperCase() + text.substring(1) +
                "\n\nCordialement,\nL'équipe Support";

        txtReponse.setText(texteAmeliore);
    }

    // --- 3. RÉPONSE INTERACTIVE (Garder le ticket ouvert) ---
    @FXML
    private void handleReponseInteractive() {
        if (!creerEtSauvegarderReponse()) return;

        // On passe automatiquement le ticket "En cours" (attente de réponse du client)
        currentRec.setStatutrec("En cours");
        service.modifier(currentRec);

        txtReponse.clear();
        chargerHistorique();
        if (parent != null) parent.chargerTableau();
    }

    // --- 4. RÉPONSE UNIQUE (Fermeture du ticket) ---
    @FXML
    private void handleReponseUnique() {
        if (!creerEtSauvegarderReponse()) return;

        // On passe le ticket en "Résolu" car c'est une réponse unique et finale
        currentRec.setStatutrec("Résolu");
        service.modifier(currentRec);

        if (parent != null) parent.chargerTableau();
        handleAnnuler(); // Ferme la fenêtre
    }

    /**
     * Méthode utilitaire pour éviter de dupliquer le code de création de réponse
     */
    private boolean creerEtSauvegarderReponse() {
        String texteSaisi = txtReponse.getText();
        if (texteSaisi == null || texteSaisi.trim().isEmpty()) {
            return false;
        }

        // 1. Création de l'entité réponse
        response nouvelleReponse = new response();
        nouvelleReponse.setContenurep(texteSaisi.trim());

        // 2. LIAISON CRUCIALE : On attache la réponse à l'objet réclamation actuel
        // Cette méthode addResponse fait automatiquement le nouvelleReponse.setReclamation(this)
        currentRec.addResponse(nouvelleReponse);

        // 3. APPEL AU SERVICE : On passe l'objet réponse qui contient maintenant l'ID du ticket
        service.ajouterReponse(nouvelleReponse);

        return true;
    }

    @FXML private void handleChangerStatut() {
        currentRec.setStatutrec(comboStatut.getValue());
        service.modifier(currentRec);
        if (parent != null) parent.chargerTableau();
    }

    @FXML private void handleAssigner() {
        // String departement = comboAssignation.getValue();
        // currentRec.setDepartement(departement);
        // service.modifier(currentRec);
    }

    @FXML private void handleAnnuler() {
        ((Stage) comboStatut.getScene().getWindow()).close();
    }

}