package gambatta.tn.ui.reclamation;

import gambatta.tn.entites.reclamation.reclamation;
import gambatta.tn.entites.reclamation.response;
import gambatta.tn.services.reclamation.ServiceReclamation;
import gambatta.tn.services.reclamation.AIService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import org.json.JSONObject; // NOUVEAU : Nécessaire pour lire la réponse du Copilot

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdminTraiterController {

    @FXML private Label lblRef;
    @FXML private ComboBox<String> comboStatut;
    @FXML private ComboBox<String> comboAssignation;
    @FXML private ComboBox<String> comboSaisieRapide;
    @FXML private TextArea txtReponse;
    @FXML private VBox vboxHistorique;

    @FXML private Button btnScanFraude;
    @FXML private Label lblResultatFraude;

    @FXML private Button btnGenererReponse;
    @FXML private Label lblStatutIA;

    @FXML private Button btnDuplicateHunter;
    @FXML private Label lblResultatDoublon;
    @FXML private HBox boxLiensDoublons;

    // --- NOUVEAUX ÉLÉMENTS : COPILOT ---
    @FXML private TextField txtCopilot;
    @FXML private Button btnCopilot;
    @FXML private Label lblCopilotStatus;

    @FXML private Button btnUpdateStatut, btnAssigner, btnFermer, btnAmeliorer, btnReponseInteractive, btnReponseUnique;

    private reclamation currentRec;
    private AdminDashboardController parent;
    private ServiceReclamation service = new ServiceReclamation();

    private List<Integer> idsDoublons = new ArrayList<>();

    @FXML
    public void initialize() {
        if (comboStatut != null) comboStatut.getItems().addAll("En attente", "En cours", "Résolu", "Fermé");
        if (comboAssignation != null) comboAssignation.getItems().addAll("Non assigné", "Support Technique", "Service Financier", "Modération");

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
                    Platform.runLater(() -> comboSaisieRapide.getSelectionModel().clearSelection());
                }
            });
        }

        setupNeonHover(btnUpdateStatut, "#0ea5e9", "transparent");
        setupNeonHover(btnAssigner, "#f59e0b", "transparent");
        setupNeonHover(btnAmeliorer, "#10b981", "transparent");
        setupNeonHover(btnReponseInteractive, "#f59e0b", "transparent");
        setupSolidHover(btnReponseUnique, "#38bdf8", "#020617");

        setupNeonHover(btnScanFraude, "#ef4444", "transparent");
        setupNeonHover(btnDuplicateHunter, "#0ea5e9", "transparent");
        setupNeonHover(btnGenererReponse, "#a855f7", "transparent");

        // Hover vert pour le Copilot
        setupNeonHover(btnCopilot, "#10b981", "transparent");

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

        this.idsDoublons.clear();
        if (boxLiensDoublons != null) boxLiensDoublons.getChildren().clear();
        if (txtReponse != null) txtReponse.clear();

        if (lblRef != null) lblRef.setText("TICKET #" + r.getIdrec());
        if (comboStatut != null) comboStatut.setValue(r.getStatutrec() != null ? r.getStatutrec() : "En attente");
        if (comboAssignation != null) comboAssignation.setValue("Non assigné");

        if (vboxHistorique != null) chargerHistorique();

        if (lblResultatFraude != null) {
            lblResultatFraude.setText("Cliquez pour analyser la crédibilité...");
            lblResultatFraude.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-font-style: italic;");
        }
        if (lblResultatDoublon != null) {
            lblResultatDoublon.setText("Vérifier si ce problème a déjà été signalé récemment...");
            lblResultatDoublon.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-font-style: italic;");
        }
        if (lblStatutIA != null) {
            lblStatutIA.setText("Laissez l'IA rédiger le message pour vous...");
            lblStatutIA.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-font-style: italic;");
        }
        if (lblCopilotStatus != null) {
            lblCopilotStatus.setText("En attente d'instruction...");
            lblCopilotStatus.setStyle("-fx-text-fill: #065f46; -fx-font-size: 10px; -fx-font-style: italic;");
        }
        if (txtCopilot != null) txtCopilot.clear();
    }

    // ==========================================
    // LE MOTEUR DU COPILOT (TEXT-TO-ACTION)
    // ==========================================
    @FXML
    private void handleCopilot() {
        if (txtCopilot == null || txtCopilot.getText().trim().isEmpty()) return;
        String commande = txtCopilot.getText().trim();

        if (lblCopilotStatus != null) {
            lblCopilotStatus.setText("Le Copilot analyse la commande... 🧠");
            lblCopilotStatus.setStyle("-fx-text-fill: #10b981; -fx-font-style: italic; -fx-font-weight: bold; -fx-font-size: 11px;");
        }

        new Thread(() -> {
            AIService ai = new AIService();
            String jsonResult = ai.executerCopilot(commande);

            Platform.runLater(() -> {
                if (jsonResult.contains("QUOTA") || jsonResult.contains("ERREUR")) {
                    if(lblCopilotStatus != null) {
                        lblCopilotStatus.setText("❌ Erreur Copilot : Surcharge API.");
                        lblCopilotStatus.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px;");
                    }
                    return;
                }

                try {
                    JSONObject json = new JSONObject(jsonResult);

                    // 1. Mise à jour automatique du Statut
                    if (json.has("statut") && !json.isNull("statut") && comboStatut != null) {
                        comboStatut.setValue(json.getString("statut"));
                    }

                    // 2. Mise à jour automatique de l'Assignation
                    if (json.has("assignation") && !json.isNull("assignation") && comboAssignation != null) {
                        comboAssignation.setValue(json.getString("assignation"));
                    }

                    // 3. Rédaction automatique du message
                    if (json.has("message") && !json.isNull("message") && txtReponse != null) {
                        txtReponse.setText(json.getString("message"));
                    }

                    if(lblCopilotStatus != null) {
                        lblCopilotStatus.setText("✅ Ordre exécuté avec succès !");
                        lblCopilotStatus.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 11px;");
                    }
                    if(txtCopilot != null) txtCopilot.clear();

                    // 4. Déclenchement automatique de la sauvegarde si demandé
                    if (json.has("auto_envoyer") && json.getBoolean("auto_envoyer")) {
                        String statutPrevu = comboStatut != null ? comboStatut.getValue() : "En cours";
                        verifierEtExecuterSauvegarde(statutPrevu);
                        if (parent != null) parent.chargerTableau();
                        handleAnnuler();
                    }

                } catch (Exception e) {
                    if(lblCopilotStatus != null) {
                        lblCopilotStatus.setText("❌ Impossible de comprendre l'ordre.");
                        lblCopilotStatus.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px;");
                    }
                    System.err.println("JSON copilot malformé : " + jsonResult);
                }
            });
        }).start();
    }

    @FXML
    private void handleScanFraude() {
        if (currentRec == null || currentRec.getDescrirec() == null || currentRec.getDescrirec().isEmpty()) return;

        if (lblResultatFraude != null) {
            lblResultatFraude.setText("Analyse comportementale en cours... 🧠");
            lblResultatFraude.setStyle("-fx-text-fill: #38bdf8; -fx-font-style: italic; -fx-font-size: 11px;");
        }

        new Thread(() -> {
            AIService ai = new AIService();
            String resultat = ai.detecterMensonge(currentRec.getDescrirec());
            Platform.runLater(() -> {
                if (lblResultatFraude != null) {
                    lblResultatFraude.setText(resultat);
                    if (resultat.contains("7") || resultat.contains("8") || resultat.contains("9") || resultat.contains("100")) {
                        lblResultatFraude.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 11px;");
                    } else if (resultat.contains("⚠️") || resultat.contains("❌")) {
                        lblResultatFraude.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-font-size: 11px;"); // Orange si erreur quota
                    } else {
                        lblResultatFraude.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 11px;");
                    }
                }
            });
        }).start();
    }

    @FXML
    private void handleDuplicateHunter() {
        if (currentRec == null || currentRec.getDescrirec() == null || currentRec.getDescrirec().isEmpty()) return;

        if (lblResultatDoublon != null) {
            lblResultatDoublon.setText("Scan de la base de données en cours... 🕵️‍♂️");
            lblResultatDoublon.setStyle("-fx-text-fill: #0ea5e9; -fx-font-style: italic; -fx-font-weight: bold; -fx-font-size: 11px;");
        }
        if (boxLiensDoublons != null) boxLiensDoublons.getChildren().clear();

        new Thread(() -> {
            List<reclamation> tousLesTickets = service.afficher();
            AIService ai = new AIService();
            String resultatBrut = ai.detecterDoublon(currentRec.getDescrirec(), tousLesTickets, currentRec.getIdrec());

            Platform.runLater(() -> {
                if (resultatBrut.contains("IDs:")) {
                    try {
                        String[] parties = resultatBrut.split("\\|\\|\\|");
                        String idsExtrait = parties[0].replaceAll("[^0-9,]", "").trim();

                        idsDoublons.clear();
                        if (!idsExtrait.isEmpty()) {
                            for (String idStr : idsExtrait.split(",")) {
                                if (!idStr.trim().isEmpty()) {
                                    int idTrouve = Integer.parseInt(idStr.trim());
                                    idsDoublons.add(idTrouve);

                                    Button btnNav = new Button("👁️ Voir #" + idTrouve);
                                    btnNav.setStyle("-fx-background-color: rgba(14, 165, 233, 0.2); -fx-text-fill: #0ea5e9; -fx-border-color: #0ea5e9; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 10px; -fx-font-weight: bold;");
                                    btnNav.setOnAction(e -> afficherPopupApercu(idTrouve, tousLesTickets));
                                    if (boxLiensDoublons != null) boxLiensDoublons.getChildren().add(btnNav);
                                }
                            }
                        }

                        String messageFinal = parties.length > 1 ? parties[1].trim() : "Doublons détectés.";
                        lblResultatDoublon.setText(messageFinal + " \n[⚡ " + idsDoublons.size() + " tickets liés trouvés]");
                        lblResultatDoublon.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-font-size: 11px;");

                    } catch (Exception e) {
                        lblResultatDoublon.setText(resultatBrut);
                    }
                } else {
                    idsDoublons.clear();
                    lblResultatDoublon.setText(resultatBrut);
                    if (resultatBrut.contains("⚠️") || resultatBrut.contains("❌")) {
                        lblResultatDoublon.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-font-size: 11px;");
                    } else {
                        lblResultatDoublon.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 11px;");
                    }
                }
            });
        }).start();
    }

    private void afficherPopupApercu(int idDoublon, List<reclamation> tousLesTickets) {
        reclamation cible = tousLesTickets.stream().filter(r -> r.getIdrec() == idDoublon).findFirst().orElse(null);
        if (cible != null) {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.UNDECORATED);

            VBox root = new VBox(15);
            root.setStyle("-fx-background-color: #0f172a; -fx-border-color: #0ea5e9; -fx-border-width: 2; -fx-padding: 20; -fx-background-radius: 10; -fx-border-radius: 10;");
            root.setEffect(new DropShadow(20, Color.BLACK));
            root.setPrefWidth(400);

            Label title = new Label("🔍 APERÇU DU TICKET #" + cible.getIdrec());
            title.setStyle("-fx-text-fill: #0ea5e9; -fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;");

            Label lblCat = new Label("MODULE : " + (cible.getCategorierec() != null ? cible.getCategorierec() : "N/A"));
            lblCat.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 11px;");

            Label lblStat = new Label("STATUT ACTUEL : " + (cible.getStatutrec() != null ? cible.getStatutrec() : "N/A"));
            lblStat.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-font-size: 11px;");

            TextArea areaDesc = new TextArea(cible.getDescrirec() != null ? cible.getDescrirec() : "Aucune description.");
            areaDesc.setWrapText(true);
            areaDesc.setEditable(false);
            areaDesc.setPrefRowCount(5);
            areaDesc.setStyle("-fx-control-inner-background: #1e293b; -fx-text-fill: white; -fx-background-radius: 8;");

            Button btnFermerApercu = new Button("FERMER L'APERÇU");
            btnFermerApercu.setStyle("-fx-background-color: transparent; -fx-border-color: #ef4444; -fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5; -fx-border-radius: 5;");
            btnFermerApercu.setMaxWidth(Double.MAX_VALUE);
            btnFermerApercu.setOnAction(e -> dialog.close());

            root.getChildren().addAll(title, lblCat, lblStat, areaDesc, btnFermerApercu);

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            dialog.setScene(scene);
            dialog.showAndWait();
        }
    }

    @FXML
    private void handleGenererReponse() {
        if (currentRec == null || currentRec.getDescrirec() == null || currentRec.getDescrirec().isEmpty()) return;

        if (lblStatutIA != null) {
            lblStatutIA.setText("Rédaction du message en cours... ✍️");
            lblStatutIA.setStyle("-fx-text-fill: #a855f7; -fx-font-style: italic; -fx-font-weight: bold; -fx-font-size: 11px;");
        }

        new Thread(() -> {
            AIService ai = new AIService();
            String resultatBrut = ai.suggererReponseIntelligente(currentRec.getDescrirec());

            Platform.runLater(() -> {
                if (resultatBrut.contains("|||")) {
                    String[] decoupage = resultatBrut.split("\\|\\|\\|");
                    String reponseGeneree = decoupage.length > 1 ? decoupage[1].trim() : "Erreur de génération.";

                    if (lblStatutIA != null) {
                        if (decoupage[0].contains("⚠️") || decoupage[0].contains("❌")) {
                            lblStatutIA.setText(decoupage[0].trim()); // Affiche l'alerte Quota
                            lblStatutIA.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-font-size: 11px;");
                        } else {
                            lblStatutIA.setText("✨ Réponse générée avec succès ! (" + decoupage[0].trim() + ")");
                            lblStatutIA.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 11px;");
                        }
                    }
                    if (txtReponse != null) txtReponse.setText(reponseGeneree);
                } else {
                    if (lblStatutIA != null) lblStatutIA.setText("Format IA inattendu.");
                    if (txtReponse != null) txtReponse.setText(resultatBrut);
                }
            });
        }).start();
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
        if (!verifierEtExecuterSauvegarde("En cours")) return;
        if (txtReponse != null) txtReponse.clear();
        chargerHistorique();
        if (parent != null) parent.chargerTableau();
    }

    @FXML
    private void handleReponseUnique() {
        if (!verifierEtExecuterSauvegarde("Résolu")) return;
        if (parent != null) parent.chargerTableau();
        handleAnnuler();
    }

    @FXML
    private void handleSauvegarder() {
        if (!verifierEtExecuterSauvegarde(null)) return;
        if (parent != null) {
            parent.chargerTableau();
            parent.masquerFormulaireAjout();
        }
    }

    private Boolean[] afficherPopupConfirmation(int nbDoublons) {
        Boolean[] choix = new Boolean[]{false, false};

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: #0f172a; -fx-border-color: #f59e0b; -fx-border-width: 2; -fx-padding: 25; -fx-background-radius: 10; -fx-border-radius: 10;");
        root.setEffect(new DropShadow(20, Color.BLACK));
        root.setAlignment(Pos.CENTER);

        Label title = new Label("⚠️ DÉCISION REQUISE");
        title.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'Consolas', monospace;");

        Label message = new Label("L'IA a lié ce ticket à " + nbDoublons + " autres réclamations similaires.\nVoulez-vous envoyer cette réponse à TOUT LE MONDE ?");
        message.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-text-alignment: center;");
        message.setWrapText(true);

        Button btnTous = new Button("🚀 OUI, RÉPONDRE À TOUS (" + (nbDoublons + 1) + " Tickets)");
        btnTous.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 10 20; -fx-background-radius: 8;");
        btnTous.setMaxWidth(Double.MAX_VALUE);
        btnTous.setOnAction(e -> { choix[0] = true; choix[1] = true; dialog.close(); });

        Button btnUn = new Button("👤 NON, JUSTE CE TICKET");
        btnUn.setStyle("-fx-background-color: transparent; -fx-border-color: #0ea5e9; -fx-text-fill: #0ea5e9; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 10 20; -fx-background-radius: 8; -fx-border-radius: 8;");
        btnUn.setMaxWidth(Double.MAX_VALUE);
        btnUn.setOnAction(e -> { choix[0] = true; choix[1] = false; dialog.close(); });

        Button btnAnnuler = new Button("ANNULER L'ACTION");
        btnAnnuler.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 10 20;");
        btnAnnuler.setOnAction(e -> { choix[0] = false; dialog.close(); });

        VBox buttonsBox = new VBox(10, btnTous, btnUn, btnAnnuler);
        buttonsBox.setAlignment(Pos.CENTER);

        root.getChildren().addAll(title, message, buttonsBox);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.showAndWait();

        return choix;
    }

    private boolean verifierEtExecuterSauvegarde(String statutForce) {
        if (txtReponse == null || txtReponse.getText().trim().isEmpty() && statutForce == null) {
            return executerSauvegarde(statutForce, false);
        }

        boolean applyToAll = false;

        if (!idsDoublons.isEmpty()) {
            Boolean[] choix = afficherPopupConfirmation(idsDoublons.size());
            if (!choix[0]) return false;
            applyToAll = choix[1];
        }

        return executerSauvegarde(statutForce, applyToAll);
    }

    private boolean executerSauvegarde(String statutForce, boolean applyToAll) {
        String statutFinal = (statutForce != null) ? statutForce :
                (comboStatut != null && comboStatut.getValue() != null ? comboStatut.getValue() : currentRec.getStatutrec());

        String texteSaisi = (txtReponse != null) ? txtReponse.getText() : "";
        boolean hasTexte = texteSaisi != null && !texteSaisi.trim().isEmpty();

        try {
            if (hasTexte) {
                response nouvelleReponse = new response();
                nouvelleReponse.setContenurep(texteSaisi.trim());
                currentRec.addResponse(nouvelleReponse);
                service.ajouterReponse(nouvelleReponse);
            }
            currentRec.setStatutrec(statutFinal);
            service.modifier(currentRec);
        } catch (Exception e) {
            System.err.println("Erreur sauvegarde ticket principal : " + e.getMessage());
        }

        if (applyToAll && !idsDoublons.isEmpty()) {
            try {
                List<reclamation> tousLesTickets = service.afficher();
                for (Integer idDoublon : idsDoublons) {
                    reclamation doublonRec = tousLesTickets.stream()
                            .filter(r -> r.getIdrec() == idDoublon)
                            .findFirst()
                            .orElse(null);

                    if (doublonRec != null) {
                        try {
                            if (hasTexte) {
                                response repDoublon = new response();
                                repDoublon.setContenurep(texteSaisi.trim());
                                doublonRec.addResponse(repDoublon);
                                service.ajouterReponse(repDoublon);
                            }
                            doublonRec.setStatutrec(statutFinal);
                            service.modifier(doublonRec);
                        } catch (Exception errDoublon) {
                            System.err.println("Le ticket #" + idDoublon + " n'a pas pu être mis à jour.");
                        }
                    }
                }
            } catch (Exception ex) {
                System.err.println("Erreur globale lors de la boucle des doublons.");
            }
        }

        idsDoublons.clear();
        if (boxLiensDoublons != null) boxLiensDoublons.getChildren().clear();

        return true;
    }

    @FXML private void handleChangerStatut() {
        if (comboStatut != null) {
            currentRec.setStatutrec(comboStatut.getValue());
            service.modifier(currentRec);
            if (parent != null) parent.chargerTableau();
        }
    }

    @FXML private void handleAssigner() {}

    @FXML private void handleAnnuler() {
        if (parent != null) parent.masquerFormulaireAjout();
        else if (lblRef != null) lblRef.getScene().getWindow().hide();
    }
}