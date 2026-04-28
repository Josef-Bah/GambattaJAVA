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
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ReclamationController implements Initializable {

    @FXML private FlowPane cardsContainer;
    @FXML private ScrollPane cyberScroll;
    @FXML private StackPane overlayContainer;

    @FXML private TextField searchField;
    @FXML private ToggleGroup filterGroup;
    @FXML private ToggleButton btnTous, btnAttente, btnResolu;
    @FXML private ComboBox<String> comboModule, comboTri;
    @FXML private Label lblTotalTickets;

    private ServiceReclamation service = new ServiceReclamation();

    private ObservableList<reclamation> masterData = FXCollections.observableArrayList();
    private FilteredList<reclamation> filteredData;
    private SortedList<reclamation> sortedData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (cyberScroll != null) {
            cyberScroll.setStyle("-fx-background: #020617; -fx-background-color: transparent; -fx-control-inner-background: #020617;");
        }

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

        comboTri.getItems().addAll("Plus récent", "Plus ancien", "Ordre Alphabétique (A-Z)");
        comboTri.setValue("Plus récent");

        filteredData = new FilteredList<>(masterData, p -> true);
        sortedData = new SortedList<>(filteredData);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateFiltresEtTri());
        filterGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> updateFiltresEtTri());
        comboModule.valueProperty().addListener((obs, oldVal, newVal) -> updateFiltresEtTri());
        comboTri.valueProperty().addListener((obs, oldVal, newVal) -> updateFiltresEtTri());

        chargerTableau();
    }

    @FXML
    public void chargerTableau() {
        List<reclamation> dataDB = service.afficher();
        masterData.setAll(dataDB);
        calculerStatistiques();
        updateFiltresEtTri();
    }

    private void calculerStatistiques() {
        int total = masterData.size();
        long attente = masterData.stream().filter(r -> "EN ATTENTE".equalsIgnoreCase(r.getStatutrec())).count();
        long resolu = masterData.stream().filter(r -> "RÉSOLU".equalsIgnoreCase(r.getStatutrec())).count();

        lblTotalTickets.setText("TOTAL : " + total + " TICKETS");
        if(btnTous != null) btnTous.setText("TOUS (" + total + ")");
        if(btnAttente != null) btnAttente.setText("EN ATTENTE (" + attente + ")");
        if(btnResolu != null) btnResolu.setText("RÉSOLUS (" + resolu + ")");
    }

    // =========================================================================
    // SYSTEME D'ALERTES CYBER (UNIFIÉ AVEC CLOUDINARY SUPPORT)
    // =========================================================================

    private void showCyberAlert(String type, String titre, String message, String urlCloud) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.NONE);
            DialogPane pane = alert.getDialogPane();
            String color = type.equals("ERREUR") ? "#ef4444" : type.equals("SUCCES") ? "#10b981" : "#38bdf8";

            pane.setStyle("-fx-background-color: #0f172a; -fx-border-color: " + color + "; -fx-border-width: 2px; -fx-border-radius: 10;");

            VBox root = new VBox(15);
            root.setAlignment(Pos.CENTER);
            root.setPadding(new javafx.geometry.Insets(25));

            Label t = new Label(titre.toUpperCase());
            t.setStyle("-fx-text-fill: " + color + "; -fx-font-family: 'Consolas'; -fx-font-size: 18px; -fx-font-weight: bold;");

            Label m = new Label(message);
            m.setStyle("-fx-text-fill: white; -fx-font-family: 'Consolas'; -fx-font-size: 12px; -fx-text-alignment: center;");
            m.setWrapText(true);

            root.getChildren().addAll(t, m);

            // Bouton spécial si lien Cloudinary présent
            if (urlCloud != null && !urlCloud.isEmpty()) {
                HBox urlBox = new HBox(10);
                urlBox.setAlignment(Pos.CENTER);
                TextField txtUrl = new TextField(urlCloud);
                txtUrl.setEditable(false);
                txtUrl.setStyle("-fx-background-color: #020617; -fx-text-fill: #38bdf8; -fx-border-color: #38bdf8; -fx-font-family: 'Consolas';");

                Button btnOpen = new Button("🌐 OUVRIR");
                btnOpen.setStyle("-fx-background-color: #38bdf8; -fx-text-fill: #020617; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 15;");
                btnOpen.setOnAction(e -> {
                    try { Desktop.getDesktop().browse(new URI(urlCloud)); } catch (Exception ex) { ex.printStackTrace(); }
                });
                urlBox.getChildren().addAll(txtUrl, btnOpen);
                root.getChildren().add(urlBox);
            }

            pane.setContent(root);
            pane.getButtonTypes().add(ButtonType.OK);
            Node okBtn = pane.lookupButton(ButtonType.OK);
            if (okBtn != null) {
                okBtn.setStyle("-fx-background-color: transparent; -fx-border-color: " + color + "; -fx-text-fill: white; -fx-cursor: hand;");
            }
            alert.showAndWait();
        });
    }

    private Alert showCyberLoading(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        DialogPane pane = alert.getDialogPane();
        pane.setStyle("-fx-background-color: #0f172a; -fx-border-color: #0ea5e9; -fx-border-width: 2px; -fx-border-radius: 10;");
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new javafx.geometry.Insets(25));
        Label t = new Label(titre.toUpperCase());
        t.setStyle("-fx-text-fill: #0ea5e9; -fx-font-family: 'Consolas'; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label m = new Label(message);
        m.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-family: 'Consolas';");
        root.getChildren().addAll(t, m);
        pane.setContent(root);
        alert.show();
        return alert;
    }

    // =========================================================================
    // EXPORT PDF (INTÉGRATION CLOUDINARY)
    // =========================================================================

    private void handleGenererPdf(reclamation r) {
        Alert loading = showCyberLoading("Protocole PDF", "Génération du dossier et transfert sécurisé vers Cloudinary...");

        new Thread(() -> {
            String url = PdfService.genererPdfLocal(r);
            Platform.runLater(() -> {
                loading.close();
                if (url != null && url.startsWith("http")) {
                    showCyberAlert("SUCCES", "Dossier Sécurisé", "Le dossier individuel du ticket #" + r.getIdrec() + " est prêt.", url);
                } else {
                    showCyberAlert("ERREUR", "Échec Système", "Le protocole de transfert Cloudinary a échoué.", null);
                }
            });
        }).start();
    }

    @FXML
    private void handleExporterRapportGlobal() {
        if (sortedData == null || sortedData.isEmpty()) {
            showCyberAlert("INFO", "Base Vide", "Aucune donnée détectée pour l'export.", null);
            return;
        }

        Alert loading = showCyberLoading("Archive Totale", "Compilation des logs système et synchronisation Cloud...");

        new Thread(() -> {
            String url = PdfService.genererRapportComplet(sortedData);
            Platform.runLater(() -> {
                loading.close();
                if (url != null && url.startsWith("http")) {
                    showCyberAlert("SUCCES", "Archive Complète", "L'archive globale a été sauvegardée dans le Cloud.", url);
                } else {
                    showCyberAlert("ERREUR", "Échec Archive", "Erreur lors de la compilation ou de l'upload.", null);
                }
            });
        }).start();
    }

    // =========================================================================
    // MÉTIER : ANALYSE STATISTIQUE & AUTO-TRIAGE IA
    // =========================================================================

    @FXML
    private void handleAfficherStats() {
        VBox statsRoot = new VBox(25);
        statsRoot.setPrefWidth(450);
        statsRoot.setStyle("-fx-background-color: #0f172a; -fx-border-color: #a855f7; -fx-border-width: 0 0 0 3; -fx-padding: 30;");

        Label title = new Label("📊 ANALYSE PERSONNELLE");
        title.setStyle("-fx-text-fill: #a855f7; -fx-font-size: 20px; -fx-font-weight: 900; -fx-letter-spacing: 1px;");

        Button btnClose = new Button("✕ FERMER");
        styleNeonButton(btnClose, "#64748b", "white");
        btnClose.setOnAction(e -> masquerFormulaireAjout());

        HBox header = new HBox(title, new Region(), btnClose);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        statsRoot.getChildren().add(header);

        if (masterData.isEmpty()) {
            Label lblEmpty = new Label("> AUCUNE DONNÉE À ANALYSER");
            lblEmpty.setStyle("-fx-text-fill: #ef4444; -fx-font-family: 'Consolas'; -fx-font-size: 13px;");
            statsRoot.getChildren().add(lblEmpty);
            afficherSidePanel(statsRoot);
            return;
        }

        Button btnAutoTriage = new Button("✨ IA : AUTO-TRIER LES TICKETS NON CLASSÉS");
        styleNeonButton(btnAutoTriage, "#fcc033", "#020617");
        btnAutoTriage.setMaxWidth(Double.MAX_VALUE);
        btnAutoTriage.setOnAction(e -> handleAutoTriage());

        PieChart pieStatut = new PieChart();
        pieStatut.setTitle("RÉPARTITION PAR STATUT");
        pieStatut.setLegendSide(Side.BOTTOM);
        pieStatut.setPrefHeight(300);

        PieChart pieModule = new PieChart();
        pieModule.setTitle("RÉPARTITION PAR MODULE");
        pieModule.setLegendSide(Side.BOTTOM);
        pieModule.setPrefHeight(300);

        long total = masterData.size();

        Map<String, Long> mapStatut = masterData.stream().collect(Collectors.groupingBy(r -> r.getStatutrec().toUpperCase(), Collectors.counting()));
        mapStatut.forEach((k, v) -> pieStatut.getData().add(new PieChart.Data(k, v)));

        Map<String, Long> mapModule = masterData.stream().collect(Collectors.groupingBy(r -> r.getCategorierec() != null ? r.getCategorierec().toUpperCase() : "GÉNÉRAL", Collectors.counting()));
        mapModule.forEach((k, v) -> pieModule.getData().add(new PieChart.Data(k, v)));

        statsRoot.getChildren().addAll(btnAutoTriage, pieStatut, new Separator(), pieModule);

        Platform.runLater(() -> {
            appliquerCyberStyle(pieStatut, total);
            appliquerCyberStyle(pieModule, total);
        });

        afficherSidePanel(statsRoot);
    }

    private void handleAutoTriage() {
        Alert info = showCyberLoading("IA Co-Pilot", "Analyse neuronale des descriptions en cours...");

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
                info.close();
                chargerTableau();
                showCyberAlert("SUCCES", "Smart Routing Terminé", finalCount + " ticket(s) ont été analysés et redirigés par l'IA.", null);
            });
        }).start();
    }

    private void appliquerCyberStyle(PieChart chart, long totalGlobal) {
        String[] palette = {"#f59e0b", "#0ea5e9", "#10b981", "#a855f7", "#ec4899"};
        int i = 0;

        for (PieChart.Data data : chart.getData()) {
            Node slice = data.getNode();
            if (slice != null) {
                String color = palette[i % palette.length];
                slice.setStyle("-fx-pie-color: " + color + "; -fx-border-color: #020617; -fx-border-width: 2px;");

                double percentage = (data.getPieValue() / totalGlobal) * 100;
                String text = String.format("%s : %.1f%%", data.getName(), percentage);

                Tooltip tt = new Tooltip(text);
                tt.setStyle("-fx-background-color: #020617; -fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-border-color: " + color + "; -fx-border-radius: 5;");
                tt.setShowDelay(Duration.ZERO);
                Tooltip.install(slice, tt);
            }
            i++;
        }

        chart.lookupAll(".chart-legend-item").forEach(node -> {
            if (node instanceof Label) ((Label) node).setStyle("-fx-text-fill: white; -fx-font-size: 10px;");
        });
        chart.lookupAll(".chart-title").forEach(node -> {
            if (node instanceof Label) ((Label) node).setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-font-weight: bold;");
        });
    }

    // =========================================================================

    private void updateFiltresEtTri() {
        filteredData.setPredicate(r -> {
            String search = searchField.getText().toLowerCase();
            boolean matchSearch = search.isEmpty() ||
                    (r.getTitre() != null && r.getTitre().toLowerCase().contains(search)) ||
                    String.valueOf(r.getIdrec()).contains(search);

            ToggleButton selectedToggle = null;
            if(filterGroup != null) selectedToggle = (ToggleButton) filterGroup.getSelectedToggle();

            boolean matchStatus = true;
            if (selectedToggle == btnAttente) matchStatus = "EN ATTENTE".equalsIgnoreCase(r.getStatutrec());
            if (selectedToggle == btnResolu) matchStatus = "RÉSOLU".equalsIgnoreCase(r.getStatutrec());

            String selectedModule = comboModule.getValue();
            boolean matchModule = "TOUS LES MODULES".equals(selectedModule) || (r.getCategorierec() != null && r.getCategorierec().equalsIgnoreCase(selectedModule));

            return matchSearch && matchStatus && matchModule;
        });

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

        dessinerCartesUI();
    }

    private void dessinerCartesUI() {
        cardsContainer.getChildren().clear();

        for (reclamation r : sortedData) {
            VBox card = new VBox(15);
            card.setPrefWidth(360);
            card.setPrefHeight(240);

            String cardBaseStyle = "-fx-background-color: rgba(30, 41, 59, 0.7); -fx-background-radius: 20; -fx-border-color: rgba(56, 189, 248, 0.2); -fx-border-radius: 20; -fx-border-width: 1.5; -fx-padding: 25;";
            String cardHoverStyle = "-fx-background-color: rgba(30, 41, 59, 0.9); -fx-background-radius: 20; -fx-border-color: #fcc033; -fx-border-radius: 20; -fx-border-width: 1.5; -fx-padding: 25; -fx-effect: dropshadow(gaussian, rgba(252, 192, 51, 0.3), 15, 0, 0, 0);";

            card.setStyle(cardBaseStyle);
            card.setOnMouseEntered(e -> card.setStyle(cardHoverStyle));
            card.setOnMouseExited(e -> card.setStyle(cardBaseStyle));

            HBox topRow = new HBox();
            topRow.setAlignment(Pos.CENTER_LEFT);

            Label idLabel = new Label("#" + r.getIdrec());
            idLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 11px; -fx-font-weight: bold;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            String statutText = (r.getStatutrec() != null) ? r.getStatutrec().toUpperCase() : "EN ATTENTE";
            Label status = new Label(statutText);
            String badgeColor = statutText.equals("EN COURS") ? "#0ea5e9" : statutText.equals("RÉSOLU") ? "#10b981" : "#f59e0b";
            status.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: " + badgeColor + "; -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 10px; -fx-font-weight: bold; -fx-border-color: " + badgeColor + "; -fx-border-radius: 12; -fx-border-width: 1;");

            topRow.getChildren().addAll(idLabel, spacer, status);

            Label title = new Label(r.getTitre() != null ? r.getTitre().toUpperCase() : "SANS TITRE");
            title.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: white;");
            title.setWrapText(true);

            Label serviceLabel = new Label(r.getCategorierec() != null ? r.getCategorierec().toUpperCase() : "GÉNÉRAL");
            serviceLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 11px; -fx-font-family: 'Consolas', monospace;");

            Label desc = new Label(r.getDescrirec());
            desc.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px; -fx-line-spacing: 3;");
            desc.setWrapText(true);
            desc.setMaxHeight(45);

            Region pushBottom = new Region();
            VBox.setVgrow(pushBottom, Priority.ALWAYS);

            HBox actions = new HBox(8);
            actions.setAlignment(Pos.CENTER_RIGHT);

            Button btnPdf = new Button("📄 PDF");
            styleNeonButton(btnPdf, "#f43f5e", "#020617");
            btnPdf.setOnAction(e -> handleGenererPdf(r));

            Button btnVoir = new Button("👁 VOIR");
            styleNeonButton(btnVoir, "#0ea5e9", "#020617");
            btnVoir.setOnAction(e -> handleVoirDetails(r));

            Button btnModifier = new Button("✎ ÉDITER");
            styleNeonButton(btnModifier, "#fcc033", "#020617");
            btnModifier.setOnAction(e -> handleModifier(r));

            Button btnAnnuler = new Button("🗑 SUPP.");
            styleNeonButton(btnAnnuler, "#ef4444", "white");
            btnAnnuler.setOnAction(e -> afficherConfirmationCustom(r));

            actions.getChildren().addAll(btnPdf, btnVoir, btnModifier, btnAnnuler);

            if ("EN COURS".equals(statutText)) {
                Button btnChat = new Button("💬 CHAT");
                styleNeonButton(btnChat, "#10b981", "#020617");
                btnChat.setOnAction(e -> handleOuvrirChat(r));
                actions.getChildren().add(0, btnChat);
            }

            card.getChildren().addAll(topRow, title, serviceLabel, desc, pushBottom, new Separator(), actions);
            cardsContainer.getChildren().add(card);
        }
    }

    private void styleNeonButton(Button btn, String color, String hoverTextColor) {
        String base = "-fx-background-color: transparent; -fx-border-color: " + color + "; -fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 10px; -fx-border-radius: 15; -fx-background-radius: 15; -fx-cursor: hand; -fx-padding: 6 12;";
        String hover = "-fx-background-color: " + color + "; -fx-text-fill: " + hoverTextColor + "; -fx-effect: dropshadow(gaussian, " + color + ", 10, 0, 0, 0);";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(base + hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
    }

    public void afficherSidePanel(Parent node) {
        if (node instanceof Region) {
            ((Region) node).setMaxWidth(400);
        }
        StackPane.setAlignment(node, Pos.CENTER_RIGHT);
        overlayContainer.getChildren().setAll(node);
        overlayContainer.setVisible(true);

        node.setTranslateX(450);
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), node);
        slideIn.setToX(0);
        slideIn.play();
    }

    public void masquerFormulaireAjout() {
        if (overlayContainer.getChildren().isEmpty()) {
            overlayContainer.setVisible(false);
            chargerTableau();
            return;
        }

        Parent node = (Parent) overlayContainer.getChildren().get(0);

        if (StackPane.getAlignment(node) == Pos.CENTER_RIGHT) {
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(250), node);
            slideOut.setToX(450);
            slideOut.setOnFinished(e -> {
                overlayContainer.getChildren().clear();
                overlayContainer.setVisible(false);
                chargerTableau();
            });
            slideOut.play();
        } else {
            overlayContainer.getChildren().clear();
            overlayContainer.setVisible(false);
            chargerTableau();
        }
    }

    @FXML
    private void handleAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/reclamation/ajout_reclamation.fxml"));
            Parent root = loader.load();
            AjoutController ajoutController = loader.getController();
            ajoutController.setParentController(this);
            afficherSidePanel(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleModifier(reclamation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/reclamation/edit_reclamation.fxml"));
            Parent root = loader.load();
            EditReclamationController controller = loader.getController();
            controller.initData(r, this);
            afficherSidePanel(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleVoirDetails(reclamation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/reclamation/show_reclamation.fxml"));
            Parent root = loader.load();
            ShowReclamationController controller = loader.getController();
            controller.setParentController(this);
            controller.initData(r);
            afficherSidePanel(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleOuvrirChat(reclamation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/reclamation/client_chat.fxml"));
            Parent root = loader.load();
            ClientChatController controller = loader.getController();
            controller.setParentController(this);
            controller.initData(r);
            afficherSidePanel(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void afficherConfirmationCustom(reclamation r) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setMaxWidth(400);
        root.setMaxHeight(250);
        root.setStyle("-fx-background-color: rgba(15, 23, 42, 0.95); -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 40; -fx-effect: dropshadow(gaussian, rgba(239, 68, 68, 0.5), 25, 0, 0, 0);");

        Label title = new Label("PURGE SYSTÈME");
        title.setStyle("-fx-font-family: 'Segoe UI Black', 'Consolas'; -fx-font-size: 22px; -fx-text-fill: #ef4444;");

        Label message = new Label("Supprimer définitivement le ticket :\n\"" + r.getTitre() + "\" ?");
        message.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 14px; -fx-text-alignment: center;");
        message.setWrapText(true);

        HBox btnBox = new HBox(20);
        btnBox.setAlignment(Pos.CENTER);

        Button btnCancel = new Button("ABORT");
        styleNeonButton(btnCancel, "#64748b", "white");
        btnCancel.setOnAction(e -> masquerFormulaireAjout());

        Button btnConfirm = new Button("TERMINATE");
        styleNeonButton(btnConfirm, "#ef4444", "white");
        btnConfirm.setOnAction(e -> {
            service.supprimer(r.getIdrec());
            masquerFormulaireAjout();
        });

        btnBox.getChildren().addAll(btnCancel, btnConfirm);
        root.getChildren().addAll(title, message, btnBox);

        StackPane.setAlignment(root, Pos.CENTER);
        overlayContainer.getChildren().setAll(root);
        overlayContainer.setVisible(true);
    }

    @FXML
    private void handleLogout() throws java.io.IOException {
        Stage stage = (Stage) cardsContainer.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/gambatta.tn.ui/reclamation/portal.fxml"));
        stage.getScene().setRoot(root);
    }
}