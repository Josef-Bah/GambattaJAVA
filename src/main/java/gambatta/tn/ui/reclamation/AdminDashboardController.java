package gambatta.tn.ui.reclamation;

import gambatta.tn.entites.reclamation.reclamation;
import gambatta.tn.services.reclamation.ServiceReclamation;
import gambatta.tn.services.reclamation.PdfService;
import gambatta.tn.services.reclamation.AIService;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONObject;

import java.awt.Desktop; // AJOUTÉ
import java.net.URI;     // AJOUTÉ
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminDashboardController implements Initializable {

    @FXML private FlowPane cardsContainer;
    @FXML private ScrollPane cyberScroll;
    @FXML private StackPane overlayContainer;

    @FXML private PieChart pieChartStatut;
    @FXML private PieChart pieChartModule;
    @FXML private Label lblTotalInfo;

    @FXML private TextField searchField;
    @FXML private ToggleGroup filterGroup;
    @FXML private ToggleButton btnTous, btnAttente, btnResolu;
    @FXML private ComboBox<String> comboModule, comboTri;

    @FXML private Button btnToggleCopilot;
    @FXML private HBox boxCopilot;
    @FXML private TextField txtGlobalCopilot;

    private ServiceReclamation service = new ServiceReclamation();

    private ObservableList<reclamation> masterData = FXCollections.observableArrayList();
    private FilteredList<reclamation> filteredData;
    private SortedList<reclamation> sortedData;

    private String departementAdminConnecte = "SUPER_ADMIN";

    public void setAdminRole(String departement) {
        this.departementAdminConnecte = departement;
        if (!departement.equals("SUPER_ADMIN")) {
            if (btnToggleCopilot != null) btnToggleCopilot.setVisible(false);
            if (comboModule != null) {
                comboModule.setValue(departement);
                comboModule.setDisable(true);
            }
        }
        chargerTableau();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (cyberScroll != null) {
            cyberScroll.setStyle("-fx-background: #020617; -fx-background-color: transparent; -fx-control-inner-background: #020617;");
            cyberScroll.setFitToWidth(true);
        }

        if (comboModule != null) {
            comboModule.getItems().addAll("TOUS LES MODULES",
                    "Service Technique",
                    "Facturation & Paiement",
                    "Gestion de Compte",
                    "Comportement Joueur",
                    "Gestion des Activités",
                    "Tournois & Compétitions",
                    "Restauration & Nourriture",
                    "Gestion d'Équipe");
            comboModule.setValue("TOUS LES MODULES");
        }

        if (comboTri != null) {
            comboTri.getItems().addAll("Plus récent", "Plus ancien", "Ordre Alphabétique (A-Z)");
            comboTri.setValue("Plus récent");
        }

        filteredData = new FilteredList<>(masterData, p -> true);
        sortedData = new SortedList<>(filteredData);

        if (searchField != null) searchField.textProperty().addListener((obs, oldVal, newVal) -> updateFiltresEtTri());
        if (filterGroup != null) filterGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> updateFiltresEtTri());
        if (comboModule != null) comboModule.valueProperty().addListener((obs, oldVal, newVal) -> updateFiltresEtTri());
        if (comboTri != null) comboTri.valueProperty().addListener((obs, oldVal, newVal) -> updateFiltresEtTri());

        chargerTableau();
    }

    @FXML
    public void chargerTableau() {
        List<reclamation> dataDB = service.afficher();
        masterData.setAll(dataDB);
        mettreAJourStatistiques();
        updateFiltresEtTri();
    }

    // =========================================================================
    // TEMPLATE D'ALERTE UNIVERSEL (GAMBATTA STYLE) - VERSION AVEC BOUTON WEB
    // =========================================================================
    private void afficherAlerteGambatta(String type, String titre, String message, String urlOptionnelle) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.setTitle(titre);

            DialogPane dialogPane = alert.getDialogPane();
            String color = type.equals("ERREUR") ? "#ef4444" : type.equals("SUCCES") ? "#10b981" : "#fcc033";
            dialogPane.setStyle("-fx-background-color: #0f172a; -fx-border-color: " + color + "; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");

            VBox content = new VBox(15);
            content.setPadding(new javafx.geometry.Insets(20));

            Label lblTitre = new Label(titre.toUpperCase());
            lblTitre.setStyle("-fx-text-fill: " + color + "; -fx-font-family: 'Consolas'; -fx-font-size: 16px; -fx-font-weight: bold;");

            Label lblMsg = new Label(message);
            lblMsg.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
            lblMsg.setWrapText(true);

            content.getChildren().addAll(lblTitre, lblMsg);

            // GESTION DU LIEN CLOUDINARY AVEC BOUTON D'OUVERTURE DIRECTE
            if (urlOptionnelle != null && !urlOptionnelle.isEmpty()) {
                HBox urlBox = new HBox(10);
                urlBox.setAlignment(Pos.CENTER_LEFT);

                TextField txtUrl = new TextField(urlOptionnelle);
                txtUrl.setEditable(false);
                txtUrl.setStyle("-fx-background-color: #020617; -fx-text-fill: #38bdf8; -fx-border-color: #38bdf8; -fx-font-family: 'Consolas'; -fx-padding: 8;");
                HBox.setHgrow(txtUrl, Priority.ALWAYS);

                Button btnOpen = new Button("🌐 OUVRIR");
                btnOpen.setStyle("-fx-background-color: #38bdf8; -fx-text-fill: #020617; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 15;");
                btnOpen.setOnAction(e -> {
                    try {
                        Desktop.getDesktop().browse(new URI(urlOptionnelle));
                    } catch (Exception ex) {
                        System.err.println("Erreur navigateur : " + ex.getMessage());
                    }
                });

                urlBox.getChildren().addAll(txtUrl, btnOpen);
                content.getChildren().add(urlBox);
            }

            dialogPane.setContent(content);
            dialogPane.getButtonTypes().add(ButtonType.OK);

            Node okButton = dialogPane.lookupButton(ButtonType.OK);
            if (okButton != null) {
                okButton.setStyle("-fx-background-color: transparent; -fx-border-color: " + color + "; -fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-radius: 5;");
            }

            alert.showAndWait();
        });
    }

    private Alert afficherAlerteChargement(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #0f172a; -fx-border-color: #0ea5e9; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");

        VBox content = new VBox(10);
        content.setPadding(new javafx.geometry.Insets(20));

        Label lblTitre = new Label(titre.toUpperCase());
        lblTitre.setStyle("-fx-text-fill: #0ea5e9; -fx-font-family: 'Consolas'; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label lblMsg = new Label(message);
        lblMsg.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        content.getChildren().addAll(lblTitre, lblMsg);
        dialogPane.setContent(content);
        alert.show();
        return alert;
    }

    @FXML
    private void handleExporterRapportGlobal() {
        if (sortedData == null || sortedData.isEmpty()) {
            afficherAlerteGambatta("INFO", "Base vide", "Aucune réclamation à exporter.", null);
            return;
        }

        Alert loadingAlert = afficherAlerteChargement("Génération Cloud", "Création et upload du PDF sécurisé en cours...\nVeuillez patienter ☁️");

        new Thread(() -> {
            // Utilisation de la méthode mise à jour dans PdfService
            String urlCloud = PdfService.genererRapportComplet(sortedData);
            Platform.runLater(() -> {
                loadingAlert.close();

                if (urlCloud != null && urlCloud.startsWith("http")) {
                    afficherAlerteGambatta("SUCCES", "Archive Sécurisée", "Le rapport PDF a été archivé en ligne avec succès.", urlCloud);
                } else {
                    afficherAlerteGambatta("ERREUR", "Échec Upload", "Erreur technique lors de l'envoi vers Cloudinary.", null);
                }
            });
        }).start();
    }

    @FXML
    private void toggleCopilot() {
        if (boxCopilot != null) {
            boolean isVisible = boxCopilot.isVisible();
            boxCopilot.setVisible(!isVisible);
            boxCopilot.setManaged(!isVisible);
            if (!isVisible && txtGlobalCopilot != null) {
                txtGlobalCopilot.requestFocus();
            }
        }
    }

    @FXML
    private void handleExecuterCopilotGlobal() {
        if (txtGlobalCopilot == null || txtGlobalCopilot.getText().trim().isEmpty()) return;
        String commande = txtGlobalCopilot.getText().trim();
        txtGlobalCopilot.setText("Exécution en cours... 🤖");
        txtGlobalCopilot.setDisable(true);

        new Thread(() -> {
            AIService ai = new AIService();
            String jsonResult = ai.executerCopilotGlobal(commande);

            Platform.runLater(() -> {
                txtGlobalCopilot.clear();
                txtGlobalCopilot.setDisable(false);

                if (jsonResult.contains("QUOTA") || jsonResult.contains("ERREUR") || jsonResult.contains("SATURE")) {
                    afficherAlerteGambatta("ERREUR", "Surcharge Système", "Les serveurs IA sont surchargés. Réessayez plus tard.", null);
                    return;
                }

                try {
                    JSONObject json = new JSONObject(jsonResult);
                    String action = json.has("action") ? json.getString("action") : "UNKNOWN";

                    switch (action) {
                        case "AUTO_TRIAGE":
                            handleAutoTriage();
                            break;
                        case "EXPORT_PDF":
                            handleExporterRapportGlobal();
                            break;
                        case "FILTER":
                            if (json.has("filtre_statut") && !json.isNull("filtre_statut")) {
                                String stat = json.getString("filtre_statut");
                                if (stat.equals("EN ATTENTE") && btnAttente != null) btnAttente.setSelected(true);
                                else if (stat.equals("RÉSOLU") && btnResolu != null) btnResolu.setSelected(true);
                                else if (btnTous != null) btnTous.setSelected(true);
                            }
                            if (json.has("filtre_module") && !json.isNull("filtre_module") && comboModule != null) {
                                String module = json.getString("filtre_module");
                                if (comboModule.getItems().contains(module)) {
                                    comboModule.setValue(module);
                                }
                            }
                            break;
                        default:
                            afficherAlerteGambatta("INFO", "Non Reconnu", "Je peux uniquement faire du triage, générer un PDF, ou filtrer l'affichage.", null);
                            break;
                    }
                    toggleCopilot();
                } catch (Exception e) {
                    System.err.println("JSON copilot malformé : " + jsonResult);
                }
            });
        }).start();
    }

    @FXML
    private void handleAutoTriage() {
        Alert loadingAlert = afficherAlerteChargement("Smart Routing", "Analyse neuronale en cours... 🧠\nL'IA redirige les tickets.");

        new Thread(() -> {
            AIService ai = new AIService();
            int count = 0;

            for (reclamation r : masterData) {
                String cat = r.getCategorierec();
                if (cat == null || cat.equalsIgnoreCase("GÉNÉRAL") || cat.equalsIgnoreCase("Autre Demande") || cat.equalsIgnoreCase("NON SPÉCIFIÉ")) {
                    String vraieCat = ai.determinerCategorie(r.getDescrirec());

                    if (vraieCat != null && !vraieCat.equalsIgnoreCase("Autre Demande") && !vraieCat.equals(cat) && !vraieCat.contains("QUOTA") && !vraieCat.contains("SATURE")) {
                        r.setCategorierec(vraieCat);
                        service.modifier(r);
                        count++;
                    }
                }
            }
            final int finalCount = count;

            Platform.runLater(() -> {
                loadingAlert.close();
                chargerTableau();
                afficherAlerteGambatta("SUCCES", "Routing Terminé", finalCount + " ticket(s) ont été réassignés au bon service par l'IA.", null);
            });
        }).start();
    }

    private void updateFiltresEtTri() {
        filteredData.setPredicate(r -> {
            boolean matchRole = true;
            if (!departementAdminConnecte.equals("SUPER_ADMIN")) {
                matchRole = r.getCategorierec() != null && r.getCategorierec().equalsIgnoreCase(departementAdminConnecte);
            }

            boolean matchSearch = true;
            if (searchField != null) {
                String search = searchField.getText().toLowerCase();
                matchSearch = search.isEmpty() ||
                        (r.getTitre() != null && r.getTitre().toLowerCase().contains(search)) ||
                        String.valueOf(r.getIdrec()).contains(search);
            }

            boolean matchStatus = true;
            if (filterGroup != null) {
                ToggleButton selectedToggle = (ToggleButton) filterGroup.getSelectedToggle();
                if (selectedToggle == btnAttente) matchStatus = "EN ATTENTE".equalsIgnoreCase(r.getStatutrec());
                if (selectedToggle == btnResolu) matchStatus = "RÉSOLU".equalsIgnoreCase(r.getStatutrec());
            }

            boolean matchModule = true;
            if (comboModule != null && !comboModule.isDisabled()) {
                String selectedModule = comboModule.getValue();
                matchModule = "TOUS LES MODULES".equals(selectedModule) || (r.getCategorierec() != null && r.getCategorierec().equalsIgnoreCase(selectedModule));
            }

            return matchRole && matchSearch && matchStatus && matchModule;
        });

        if (comboTri != null) {
            String critereTri = comboTri.getValue();
            sortedData.setComparator((r1, r2) -> {
                if ("Plus récent".equals(critereTri)) {
                    if (r1.getDaterec() == null || r2.getDaterec() == null) return 0;
                    return r2.getDaterec().compareTo(r1.getDaterec());
                } else if ("Plus ancien".equals(critereTri)) {
                    if (r1.getDaterec() == null || r2.getDaterec() == null) return 0;
                    return r1.getDaterec().compareTo(r2.getDaterec());
                } else if ("Ordre Alphabétique (A-Z)".equals(critereTri)) {
                    String t1 = r1.getTitre() != null ? r1.getTitre() : "";
                    String t2 = r2.getTitre() != null ? r2.getTitre() : "";
                    return t1.compareToIgnoreCase(t2);
                }
                return 0;
            });
        }
        dessinerCartesUI();
    }

    private void mettreAJourStatistiques() {
        if (pieChartStatut == null || pieChartModule == null) return;

        int total = filteredData.size();
        if (lblTotalInfo != null) lblTotalInfo.setText("TOTAL TICKETS : " + total);

        if (total == 0) {
            pieChartStatut.setData(FXCollections.observableArrayList());
            pieChartModule.setData(FXCollections.observableArrayList());
            return;
        }

        long attente = filteredData.stream().filter(r -> "EN ATTENTE".equalsIgnoreCase(r.getStatutrec())).count();
        long cours = filteredData.stream().filter(r -> "EN COURS".equalsIgnoreCase(r.getStatutrec())).count();
        long resolu = filteredData.stream().filter(r -> "RÉSOLU".equalsIgnoreCase(r.getStatutrec())).count();

        ObservableList<PieChart.Data> dataStatut = FXCollections.observableArrayList();
        if (attente > 0) dataStatut.add(new PieChart.Data("ATTENTE", attente));
        if (cours > 0) dataStatut.add(new PieChart.Data("EN COURS", cours));
        if (resolu > 0) dataStatut.add(new PieChart.Data("RÉSOLUS", resolu));
        pieChartStatut.setData(dataStatut);

        Map<String, Long> parModule = filteredData.stream()
                .collect(Collectors.groupingBy(r -> r.getCategorierec() != null ? r.getCategorierec().toUpperCase() : "GÉNÉRAL", Collectors.counting()));

        ObservableList<PieChart.Data> dataModule = FXCollections.observableArrayList();
        parModule.forEach((key, val) -> dataModule.add(new PieChart.Data(key, val)));
        pieChartModule.setData(dataModule);

        Platform.runLater(() -> appliquerStyleEtHover(total));
    }

    private void appliquerStyleEtHover(int totalGlobal) {
        String[] palette = {"#f59e0b", "#0ea5e9", "#10b981", "#a855f7", "#ec4899", "#f43f5e"};
        int i = 0;
        for (PieChart.Data data : pieChartStatut.getData()) {
            Node slice = data.getNode();
            if (slice != null) {
                slice.setStyle("-fx-pie-color: " + palette[i % palette.length] + "; -fx-border-color: #020617; -fx-border-width: 2px;");
                double percentage = (data.getPieValue() / totalGlobal) * 100;
                String info = String.format("%s : %.1f%% (%d)", data.getName(), percentage, (int)data.getPieValue());
                Tooltip tt = new Tooltip(info);
                tt.setStyle("-fx-background-color: #0f172a; -fx-text-fill: " + palette[i % palette.length] + "; -fx-font-weight: bold; -fx-border-color: " + palette[i % palette.length] + "; -fx-border-radius: 5;");
                tt.setShowDelay(Duration.ZERO);
                Tooltip.install(slice, tt);
            }
            i++;
        }
        int j = 2;
        for (PieChart.Data data : pieChartModule.getData()) {
            Node slice = data.getNode();
            if (slice != null) {
                slice.setStyle("-fx-pie-color: " + palette[j % palette.length] + "; -fx-border-color: #020617; -fx-border-width: 2px;");
                double percentage = (data.getPieValue() / totalGlobal) * 100;
                String info = String.format("%s : %.1f%% (%d)", data.getName(), percentage, (int)data.getPieValue());
                Tooltip tt = new Tooltip(info);
                tt.setStyle("-fx-background-color: #0f172a; -fx-text-fill: " + palette[j % palette.length] + "; -fx-font-weight: bold; -fx-border-color: " + palette[j % palette.length] + "; -fx-border-radius: 5;");
                tt.setShowDelay(Duration.ZERO);
                Tooltip.install(slice, tt);
            }
            j++;
        }
        pieChartStatut.lookupAll(".chart-legend-item").forEach(n -> { if (n instanceof Label) ((Label) n).setStyle("-fx-text-fill: white; -fx-font-size: 10px;"); });
        pieChartModule.lookupAll(".chart-legend-item").forEach(n -> { if (n instanceof Label) ((Label) n).setStyle("-fx-text-fill: white; -fx-font-size: 10px;"); });
    }

    private void dessinerCartesUI() {
        if (cardsContainer != null) {
            cardsContainer.getChildren().clear();
            for (reclamation r : sortedData) {
                cardsContainer.getChildren().add(createAdminCard(r));
            }
        }
    }

    private VBox createAdminCard(reclamation r) {
        VBox card = new VBox(15);
        card.setPrefWidth(380);
        card.setPrefHeight(240);
        String baseStyle = "-fx-background-color: rgba(15, 23, 42, 0.8); -fx-background-radius: 12; -fx-border-color: rgba(14, 165, 233, 0.4); -fx-border-width: 1.5; -fx-border-radius: 12; -fx-padding: 20;";
        card.setStyle(baseStyle);
        card.setOnMouseEntered(e -> card.setStyle(baseStyle + "-fx-border-color: #fcc033; -fx-effect: dropshadow(three-pass-box, rgba(252, 192, 51, 0.4), 15, 0, 0, 0);"));
        card.setOnMouseExited(e -> card.setStyle(baseStyle));

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label idLabel = new Label("ID: #" + r.getIdrec());
        idLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-family: 'Consolas', monospace; -fx-font-size: 10px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        String st = (r.getStatutrec() != null) ? r.getStatutrec().toUpperCase() : "EN ATTENTE";
        Label statusBadge = new Label(st);
        String badgeColor = st.equals("EN COURS") ? "#0ea5e9" : st.equals("RÉSOLU") ? "#10b981" : "#f59e0b";
        statusBadge.setStyle("-fx-background-color: " + badgeColor + "; -fx-text-fill: white; -fx-padding: 3 10; -fx-background-radius: 5; -fx-font-size: 10px; -fx-font-weight: bold;");
        header.getChildren().addAll(idLabel, spacer, statusBadge);

        Label title = new Label(r.getTitre() != null ? r.getTitre().toUpperCase() : "SANS TITRE");
        title.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 16px;");
        title.setWrapText(true);
        Label moduleLabel = new Label("MODULE: " + (r.getCategorierec() != null ? r.getCategorierec().toUpperCase() : "GÉNÉRAL"));
        moduleLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 10px; -fx-font-weight: bold;");

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button btnVoir = new Button("[VOIR]"); styleNeonButton(btnVoir, "#38bdf8", "#020617"); btnVoir.setOnAction(e -> handleVoir(r));
        Button btnTraiter = new Button("[TRAITER]"); styleNeonButton(btnTraiter, "#fcc033", "#020617"); btnTraiter.setOnAction(e -> handleTraiter(r));
        Button btnArchiver = new Button("[ARCHIVER]"); styleNeonButton(btnArchiver, "#ef4444", "white"); btnArchiver.setOnAction(e -> handleArchiver(r));
        actions.getChildren().addAll(btnVoir, btnTraiter, btnArchiver);

        card.getChildren().addAll(header, title, moduleLabel, new Separator(), actions);
        return card;
    }

    private void styleNeonButton(Button btn, String color, String hoverTextColor) {
        String base = "-fx-background-color: transparent; -fx-border-color: " + color + "; -fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 10px; -fx-border-radius: 5; -fx-cursor: hand; -fx-padding: 6 12;";
        String hover = "-fx-background-color: " + color + "; -fx-text-fill: " + hoverTextColor + "; -fx-effect: dropshadow(three-pass-box, " + color + ", 10, 0, 0, 0);";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(base + hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
    }

    public void afficherSidePanel(Parent node) {
        if (node instanceof Region) ((Region) node).setMaxWidth(450);
        StackPane.setAlignment(node, Pos.CENTER_RIGHT);
        overlayContainer.getChildren().setAll(node);
        overlayContainer.setVisible(true);
        node.setTranslateX(450);
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), node);
        slideIn.setToX(0); slideIn.play();
    }

    public void masquerFormulaireAjout() {
        if (overlayContainer.getChildren().isEmpty()) {
            overlayContainer.setVisible(false); chargerTableau(); return;
        }
        Parent node = (Parent) overlayContainer.getChildren().get(0);
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(250), node);
        slideOut.setToX(450);
        slideOut.setOnFinished(e -> { overlayContainer.getChildren().clear(); overlayContainer.setVisible(false); chargerTableau(); });
        slideOut.play();
    }

    private void handleVoir(reclamation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/reclamation/admin_voir.fxml"));
            Parent root = loader.load();
            AdminVoirController c = loader.getController(); c.setParentController(this); c.initData(r);
            afficherSidePanel(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleTraiter(reclamation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/reclamation/admin_traiter.fxml"));
            Parent root = loader.load();
            AdminTraiterController c = loader.getController(); c.initData(r, this);
            afficherSidePanel(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleArchiver(reclamation r) {
        service.archiver(r.getIdrec());
        chargerTableau();
    }

    @FXML
    private void handleLogout() throws Exception {
        Stage stage = (Stage) cardsContainer.getScene().getWindow();
        stage.getScene().setRoot(FXMLLoader.load(getClass().getResource("/gambatta.tn.ui/reclamation/portal.fxml")));
    }
}