package gambatta.tn.ui;

import gambatta.tn.entites.activites.ReservationActivite;
import gambatta.tn.entites.activites.activite;
import gambatta.tn.entites.activites.rules;
import gambatta.tn.services.activites.ActiviteService;
import gambatta.tn.services.activites.ReservationActiviteService;
import gambatta.tn.services.activites.RulesService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.image.*;
import javafx.stage.FileChooser;
import java.io.File;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import java.io.IOException;
import java.util.stream.Collectors;

public class ActiviteBackController {

    // --- TAB ACTIVITES ---
    @FXML private TableView<activite> tableActivites;
    @FXML private TableColumn<activite, String> colActImage;
    @FXML private TableColumn<activite, String> colActNom;
    @FXML private TableColumn<activite, String> colActType;
    @FXML private TableColumn<activite, String> colActDispo;
    @FXML private TextField tfSearchActivites;

    @FXML private VBox vboxForm;
    @FXML private TextField tfActNom;
    @FXML private TextField tfActType;
    @FXML private TextField tfActDispo;
    @FXML private TextField tfActAdresse;
    @FXML private TextArea taActDesc;
    @FXML private TextField tfActImage;
    @FXML private javafx.scene.image.ImageView ivImagePreview;
    
    @FXML private Label errActNom;
    @FXML private Label errActType;
    @FXML private Label errActDispo;
    @FXML private Label errActAdresse;
    @FXML private Label errActDesc;

    // --- TAB RULES ---
    @FXML private TableView<rules> tableRules;
    @FXML private TableColumn<rules, String> colRuleDesc;
    @FXML private TableColumn<rules, String> colRuleAct;
    @FXML private TextField tfSearchRules;

    @FXML private ComboBox<activite> cbRuleActivite;
    @FXML private TextArea taRuleDesc;
    @FXML private Label errRuleAct;
    @FXML private Label errRuleDesc;

    // --- TAB RESERVATIONS ---
    @FXML private TableView<ReservationActivite> tableReservations;
    @FXML private TableColumn<ReservationActivite, String> colResDate;
    @FXML private TableColumn<ReservationActivite, String> colResHeure;
    @FXML private TableColumn<ReservationActivite, String> colResStatut;
    @FXML private TableColumn<ReservationActivite, String> colResAct;
    @FXML private TableColumn<ReservationActivite, String> colResClient;
    @FXML private TextField tfSearchReservations;

    // --- DASHBOARD & KPIs ---
    @FXML private Label lblTotalActs;
    @FXML private Label lblTotalRes;
    @FXML private Label lblTopAct;
    @FXML private PieChart pieChartActivites;
    @FXML private BarChart<String, Number> barChartReservations;
    @FXML private CategoryAxis xAxisAct;
    @FXML private NumberAxis yAxisRes;

    private ActiviteService activiteService = new ActiviteService();
    private RulesService rulesService = new RulesService();
    private ReservationActiviteService reservationService = new ReservationActiviteService();

    // Observers pour la recherche dynamique
    private javafx.collections.ObservableList<activite> masterActivites = FXCollections.observableArrayList();
    private javafx.collections.ObservableList<rules> masterRules = FXCollections.observableArrayList();
    private javafx.collections.ObservableList<ReservationActivite> masterReservations = FXCollections.observableArrayList();

    @FXML
    void toggleForm() {
        boolean v = vboxForm.isVisible();
        vboxForm.setVisible(!v);
        vboxForm.setManaged(!v);
    }

    @FXML
    public void initialize() {
        // Init Activites table
        
        colActImage.setCellFactory(param -> new TableCell<activite, String>() {
            private final ImageView imgV = new ImageView();
            private final HBox wrapper = new HBox(imgV);
            {
                imgV.setFitWidth(80); imgV.setFitHeight(50); imgV.setPreserveRatio(false);
                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(80, 50);
                clip.setArcWidth(10); clip.setArcHeight(10);
                imgV.setClip(clip);
                wrapper.setAlignment(javafx.geometry.Pos.CENTER);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) { setGraphic(null); }
                else {
                    activite a = getTableRow().getItem();
                    Image finalImg = null;
                    if(a.getImagea() != null && !a.getImagea().isEmpty()){
                        try{
                            if (a.getImagea().startsWith("http") || a.getImagea().startsWith("file:")) { finalImg = new Image(a.getImagea(), true); } 
                            else { File f = new File(a.getImagea()); if (f.exists()) finalImg = new Image(f.toURI().toString(), true); }
                        }catch(Exception e){}
                    }
                    if(finalImg == null || finalImg.isError()) finalImg = new Image("https://picsum.photos/seed/" + Math.abs(a.getNoma().hashCode()) + "/300/200", true);
                    imgV.setImage(finalImg);
                    setGraphic(wrapper);
                }
            }
        });

        colActNom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNoma()));
        colActNom.setCellFactory(param -> new TableCell<activite, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); }
                else {
                    Label lbl = new Label(item.toUpperCase());
                    lbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
                    setGraphic(lbl);
                }
            }
        });
        
        colActType.setCellFactory(param -> new TableCell<activite, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) { setGraphic(null); }
                else {
                    activite a = getTableRow().getItem();
                    Label typeLbl = new Label(a.getTypea().toUpperCase());
                    typeLbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-font-weight: bold;");
                    Label badge = new Label(a.getTypea().substring(0, 1).toUpperCase() + a.getTypea().substring(1).toLowerCase());
                    badge.setStyle("-fx-background-color: rgba(46, 213, 115, 0.15); -fx-text-fill: #2ed573; -fx-padding: 3 12; -fx-background-radius: 12; -fx-font-size: 11px;");
                    HBox box = new HBox(15, typeLbl, badge);
                    box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    setGraphic(box);
                }
            }
        });

        colActDispo.setCellFactory(param -> new TableCell<activite, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) { setGraphic(null); }
                else {
                    activite a = getTableRow().getItem();
                    Label badge = new Label(a.getDispoa() != null && !a.getDispoa().isEmpty() ? a.getDispoa() : "Disponible");
                    badge.setStyle("-fx-background-color: rgba(46, 213, 115, 0.2); -fx-text-fill: #2ed573; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold;");
                    HBox box = new HBox(badge);
                    box.setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(box);
                }
            }
        });


        
        tableActivites.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                tfActNom.setText(newSel.getNoma());
                tfActType.setText(newSel.getTypea());
                tfActDispo.setText(newSel.getDispoa());
                tfActAdresse.setText(newSel.getAdresse());
                taActDesc.setText(newSel.getDescria());
                tfActImage.setText(newSel.getImagea());
                try {
                    if (newSel.getImagea() != null && !newSel.getImagea().isEmpty()) {
                        if (newSel.getImagea().startsWith("http") || newSel.getImagea().startsWith("file:")) {
                            ivImagePreview.setImage(new Image(newSel.getImagea(), true));
                        } else {
                            File f = new File(newSel.getImagea());
                            if (f.exists()) ivImagePreview.setImage(new Image(f.toURI().toString(), true));
                            else ivImagePreview.setImage(null);
                        }
                    } else {
                        ivImagePreview.setImage(null);
                    }
                } catch(Exception e) {
                    ivImagePreview.setImage(null);
                }
            }
        });

        // Init Rules table
        colRuleDesc.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRuleDescription()));
        colRuleAct.setCellValueFactory(data -> {
            activite a = getActiviteById(data.getValue().getActiviteId());
            return new SimpleStringProperty(a != null ? a.getNoma() : "Inconnu");
        });
        
        tableRules.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                taRuleDesc.setText(newSel.getRuleDescription());
                cbRuleActivite.setValue(getActiviteById(newSel.getActiviteId()));
            }
        });

        // Init Reservations table
        colResDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDatedebut().toString()));
        colResHeure.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getHeurer()));
        colResStatut.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatutr()));
        colResAct.setCellValueFactory(data -> {
            activite a = getActiviteById(data.getValue().getActiviteId());
            return new SimpleStringProperty(a != null ? a.getNoma() : "Inconnu");
        });
        colResClient.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail() != null ? data.getValue().getEmail() : "N/A"));

        // Setup FilteredLists mapped to Search fields
        FilteredList<activite> filteredActivites = new FilteredList<>(masterActivites, p -> true);
        tfSearchActivites.textProperty().addListener((obs, oldV, newV) -> {
            filteredActivites.setPredicate(act -> {
                if (newV == null || newV.isEmpty()) return true;
                String lower = newV.toLowerCase();
                return act.getNoma().toLowerCase().contains(lower) 
                    || act.getTypea().toLowerCase().contains(lower)
                    || act.getAdresse().toLowerCase().contains(lower);
            });
        });
        SortedList<activite> sortedActivites = new SortedList<>(filteredActivites);
        sortedActivites.comparatorProperty().bind(tableActivites.comparatorProperty());
        tableActivites.setItems(sortedActivites);

        FilteredList<rules> filteredRules = new FilteredList<>(masterRules, p -> true);
        tfSearchRules.textProperty().addListener((obs, oldV, newV) -> {
            filteredRules.setPredicate(r -> {
                if (newV == null || newV.isEmpty()) return true;
                return r.getRuleDescription().toLowerCase().contains(newV.toLowerCase());
            });
        });
        SortedList<rules> sortedRules = new SortedList<>(filteredRules);
        sortedRules.comparatorProperty().bind(tableRules.comparatorProperty());
        tableRules.setItems(sortedRules);

        FilteredList<ReservationActivite> filteredRes = new FilteredList<>(masterReservations, p -> true);
        tfSearchReservations.textProperty().addListener((obs, oldV, newV) -> {
            filteredRes.setPredicate(r -> {
                if (newV == null || newV.isEmpty()) return true;
                String lower = newV.toLowerCase();
                return r.getStatutr().toLowerCase().contains(lower) || r.getHeurer().contains(lower);
            });
        });
        SortedList<ReservationActivite> sortedRes = new SortedList<>(filteredRes);
        sortedRes.comparatorProperty().bind(tableReservations.comparatorProperty());
        tableReservations.setItems(sortedRes);

        refreshAll();
    }

    private void refreshAll() {
        masterActivites.setAll(activiteService.getAll());
        masterRules.setAll(rulesService.getAll());
        masterReservations.setAll(reservationService.getAll());
        cbRuleActivite.setItems(FXCollections.observableArrayList(masterActivites));
        refreshStats();
    }
    
    private activite getActiviteById(int id) {
        return activiteService.getAll().stream().filter(a -> a.getId() == id).findFirst().orElse(null);
    }

    // --- ACTIVITE HANDLERS ---
    @FXML void handleSelectImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir l'image de l'activité");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File f = fc.showOpenDialog(tfActNom.getScene().getWindow());
        
        if (f != null) {
            tfActImage.setText("☁️ Upload en cours...");
            
            new Thread(() -> {
                try {
                    // Upload vers Cloudinary
                    String secureUrl = gambatta.tn.utils.CloudinaryUtil.uploadFile(f);
                    
                    javafx.application.Platform.runLater(() -> {
                        tfActImage.setText(secureUrl);
                        // Chargement de l'image en arrière-plan pour ne pas geler l'UI
                        ivImagePreview.setImage(new Image(secureUrl, true));
                        System.out.println("✅ Image prête : " + secureUrl);
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        String localUri = f.toURI().toString();
                        tfActImage.setText(localUri);
                        ivImagePreview.setImage(new Image(localUri, true));
                        
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Cloudinary - Mode Dégradé");
                        alert.setHeaderText("Signature ou Clé invalide");
                        alert.setContentText("L'upload a échoué. L'image sera chargée localement pour l'instant.\nErreur : " + e.getMessage());
                        alert.show();
                    });
                }
            }).start();
        }
    }

    private void resetValidation() {
        Control[] fields = {tfActNom, tfActType, tfActDispo, tfActAdresse, taActDesc};
        Label[] labels = {errActNom, errActType, errActDispo, errActAdresse, errActDesc};
        for (Control c : fields) c.getStyleClass().remove("error-field");
        for (Label l : labels) { l.setVisible(false); l.setManaged(false); }
    }

    private void setError(Control field, Label label, String msg) {
        if (!field.getStyleClass().contains("error-field")) field.getStyleClass().add("error-field");
        label.setText(msg);
        label.setVisible(true);
        label.setManaged(true);
    }

    private boolean validateSaisie() {
        resetValidation();
        boolean valid = true;

        if (tfActNom.getText() == null || tfActNom.getText().trim().isEmpty()) {
            setError(tfActNom, errActNom, "[!] Nom requis.");
            valid = false;
        }
        if (tfActType.getText() == null || tfActType.getText().trim().isEmpty()) {
            setError(tfActType, errActType, "[!] Type requis.");
            valid = false;
        }
        if (tfActAdresse.getText() == null || tfActAdresse.getText().trim().isEmpty()) {
            setError(tfActAdresse, errActAdresse, "[!] Emplacement requis.");
            valid = false;
        }
        if (tfActDispo.getText() == null || tfActDispo.getText().trim().isEmpty()) {
            setError(tfActDispo, errActDispo, "[!] Disponibilité requise.");
            valid = false;
        }
        if (taActDesc.getText() == null || taActDesc.getText().length() < 10) {
            setError(taActDesc, errActDesc, "[!] Détails requis (Min 10 caractères).");
            valid = false;
        }
        return valid;
    }

    @FXML void addActivite() {
        if (!validateSaisie()) return;
        String imgPath = tfActImage.getText() != null ? tfActImage.getText() : "";
        activite a = new activite(tfActNom.getText(), tfActType.getText(), tfActDispo.getText(), taActDesc.getText(), imgPath, tfActAdresse.getText(), false);
        activiteService.add(a);
        refreshAll();
        alert("SUCCÈS", "Opération réussie", "Activité ajoutée avec succès !");
    }
    
    @FXML void updateActivite() {
        activite selected = tableActivites.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (!validateSaisie()) return;
            selected.setNoma(tfActNom.getText());
            selected.setTypea(tfActType.getText());
            selected.setDispoa(tfActDispo.getText());
            selected.setDescria(taActDesc.getText());
            selected.setAdresse(tfActAdresse.getText());
            selected.setImagea(tfActImage.getText());
            activiteService.update(selected);
            refreshAll();
            alert("SUCCÈS", "Modification terminée", "Activité modifiée avec succès !");
        }
    }
    @FXML void deleteActivite() {
        activite selected = tableActivites.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showPurgeConfirm("l'activité : \"" + selected.getNoma() + "\"", () -> {
                try {
                    // Supression en cascade : on supprime d'abord les enfants
                    masterRules.stream()
                        .filter(r -> r.getActiviteId() == selected.getId())
                        .forEach(r -> rulesService.delete(r.getId()));
                        
                    masterReservations.stream()
                        .filter(r -> r.getActiviteId() == selected.getId())
                        .forEach(r -> reservationService.delete(r.getId()));
                        
                    // Ensuite on supprime le parent
                    activiteService.delete(selected.getId());
                    refreshAll();
                    alert("SUCCÈS", "Suppression terminée", "L'activité et ses données liées ont été supprimées.");
                    
                    tfActNom.clear();
                    tfActType.clear();
                    tfActDispo.clear();
                    tfActAdresse.clear();
                    taActDesc.clear();
                    tfActImage.clear();
                    resetValidation();
                } catch (Exception ex) {
                    alert("ERREUR", "Échec de suppression", "Erreur lors de la suppression en cascade.");
                }
            });
        }
    }

    @FXML void suggestActivitiesAI() {
        javafx.stage.Stage loadingStage = new javafx.stage.Stage();
        loadingStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        loadingStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        
        VBox lRoot = new VBox(15);
        lRoot.setAlignment(Pos.CENTER);
        lRoot.setPrefSize(350, 180);
        lRoot.setStyle("-fx-background-color: #0f172a; -fx-background-radius: 18; -fx-border-color: #FFD700; -fx-border-width: 2;");
        
        ProgressIndicator pi = new ProgressIndicator();
        pi.setStyle("-fx-progress-color: #FFD700;");
        Label lTitle = new Label("🤖 ANALYSE IA EN COURS...");
        lTitle.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label lSub = new Label("Gemini parcourt vos " + masterActivites.size() + " activités...");
        lSub.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        
        lRoot.getChildren().addAll(pi, lTitle, lSub);
        loadingStage.setScene(new Scene(lRoot, Color.TRANSPARENT));
        loadingStage.show();

        java.util.List<activite> snapshot = new java.util.ArrayList<>(masterActivites);

        new Thread(() -> {
            try {
                // REAL AI CALL: Passing DB snapshot to Gemini 1.5 Flash
                String jsonResult = gambatta.tn.utils.GeminiUtil.generateActivitySuggestion(snapshot);
                org.json.JSONObject suggestion = new org.json.JSONObject(jsonResult);

                javafx.application.Platform.runLater(() -> {
                    loadingStage.close();
                    
                    // Fill fields
                    tfActNom.setText(suggestion.optString("nom", "Nouvelle Activité"));
                    tfActType.setText(suggestion.optString("type", "Esport"));
                    tfActDispo.setText(suggestion.optString("dispo", "OUI"));
                    tfActAdresse.setText(suggestion.optString("adresse", "Salle 1"));
                    taActDesc.setText(suggestion.optString("desc", "") + " [Généré par Gemini]");

                    // Show Modern Success Dialog
                    alert("SUCCÈS IA", 
                        "Analyse terminée avec succès !", 
                        "Gemini a analysé vos " + snapshot.size() + " activités et a généré une nouvelle proposition unique.");
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    loadingStage.close();
                    alert("ERREUR IA", "Échec de la génération", e.getMessage());
                });
            }
        }).start();
    }

    private void alert(String title, String header, String body) {
        javafx.stage.Stage st = new javafx.stage.Stage();
        st.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        st.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new javafx.geometry.Insets(25));
        root.setPrefWidth(380);
        root.setStyle("-fx-background-color: #0f172a; -fx-background-radius: 20; -fx-border-color: #FFD700; -fx-border-width: 2; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 20, 0, 0, 10);");

        Label tLbl = new Label(title);
        tLbl.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 11px; -fx-font-weight: bold; -fx-letter-spacing: 1px;");
        
        Label hLbl = new Label(header);
        hLbl.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label bLbl = new Label(body);
        bLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 13px;");
        bLbl.setWrapText(true);
        bLbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Button btn = new Button("D'ACCORD");
        btn.setStyle("-fx-background-color: linear-gradient(to right, #FFD700, #ff9f43); -fx-text-fill: #020617; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 30; -fx-cursor: hand;");
        btn.setOnAction(e -> st.close());

        root.getChildren().addAll(tLbl, hLbl, bLbl, btn);
        Scene sc = new Scene(root);
        sc.setFill(Color.TRANSPARENT);
        st.setScene(sc);
        st.show();
    }

    @FXML
    void handleVoiceSearch() {
        try {
            java.util.Set<String> keywords = new java.util.HashSet<>();
            if (masterActivites != null) {
                for (activite a : masterActivites) {
                    if (a.getNoma() != null) {
                        keywords.add(a.getNoma());
                        for (String word : a.getNoma().split("\\s+")) {
                            if (word.length() > 2) keywords.add(word);
                        }
                    }
                    if (a.getTypea() != null) keywords.add(a.getTypea());
                }
            }

            if (keywords.isEmpty()) {
                keywords.add("Activité");
                keywords.add("Admin");
            }

            String choicesStr = keywords.stream()
                .map(s -> s.replace("'", "''"))
                .collect(Collectors.joining("','", "'", "'"));

            tfSearchActivites.setPromptText("🎙 Écoute active (6s)...");
            tfSearchActivites.setText("");

            new Thread(() -> {
                try {
                    String psCommand = "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; " +
                        "$ErrorActionPreference = 'Stop'; " +
                        "try { " +
                        "  Add-Type -AssemblyName System.Speech; " +
                        "  $engine = $null; " +
                        "  try { " +
                        "    $culture = Get-Culture; " +
                        "    $engine = New-Object System.Speech.Recognition.SpeechRecognitionEngine($culture); " +
                        "  } catch { " +
                        "    $engine = New-Object System.Speech.Recognition.SpeechRecognitionEngine; " +
                        "  } " +
                        "  $engine.SetInputToDefaultAudioDevice(); " +
                        "  $keywords = @(" + (choicesStr.isEmpty() ? "''" : choicesStr) + "); " +
                        "  if ($keywords.Count -gt 0 -and $keywords[0] -ne '') { " +
                        "    $choices = New-Object System.Speech.Recognition.Choices; " +
                        "    $choices.Add($keywords); " +
                        "    $gb = New-Object System.Speech.Recognition.GrammarBuilder($choices); " +
                        "    $g = New-Object System.Speech.Recognition.Grammar($gb); " +
                        "    $g.Priority = 127; # Priorité maximale pour vos jeux " +
                        "    $engine.LoadGrammar($g); " +
                        "  } " +
                        "  # Dictée de secours avec priorité basse " +
                        "  $dict = New-Object System.Speech.Recognition.DictationGrammar; " +
                        "  $dict.Priority = 0; " +
                        "  $engine.LoadGrammar($dict); " +
                        "  $res = $engine.Recognize((New-TimeSpan -Seconds 8)); " +
                        "  if ($res) { Write-Output $res.Text } " +
                        "} catch { Write-Output ('ERROR: ' + $_.Exception.Message) }";
                    
                    ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", psCommand);
                    pb.redirectErrorStream(true);
                    Process process = pb.start();
                    
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream(), "UTF-8"));
                    String line = reader.readLine();
                    process.waitFor();
                    
                    final String result = (line != null) ? line.trim() : "";
                    
                    javafx.application.Platform.runLater(() -> {
                        tfSearchActivites.setPromptText("Rechercher par nom, type...");
                        if (result.startsWith("ERROR:")) {
                            tfSearchActivites.setPromptText("Erreur micro");
                            System.err.println("VOICE ERROR: " + result);
                        } else if (!result.isEmpty()) {
                            tfSearchActivites.setText(result);
                        } else {
                            tfSearchActivites.setPromptText("Aucun mot reconnu...");
                        }
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> tfSearchActivites.setPromptText("Erreur micro"));
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- RULES HANDLERS ---
    private void resetRuleValidation() {
        cbRuleActivite.getStyleClass().remove("error-field");
        taRuleDesc.getStyleClass().remove("error-field");
        errRuleAct.setVisible(false); errRuleAct.setManaged(false);
        errRuleDesc.setVisible(false); errRuleDesc.setManaged(false);
    }

    private boolean validateRuleSaisie() {
        resetRuleValidation();
        boolean valid = true;

        if (cbRuleActivite.getValue() == null) {
            setError(cbRuleActivite, errRuleAct, "[!] Sélectionnez une activité.");
            valid = false;
        }
        if (taRuleDesc.getText() == null || taRuleDesc.getText().trim().isEmpty() || taRuleDesc.getText().length() < 10) {
            setError(taRuleDesc, errRuleDesc, "[!] Détails requis (Min 10 caractères).");
            valid = false;
        }
        return valid;
    }

    @FXML void addRule() {
        if (!validateRuleSaisie()) return;
        rules r = new rules();
        r.setActiviteId(cbRuleActivite.getValue().getId());
        r.setRuleDescription(taRuleDesc.getText());
        rulesService.add(r);
        refreshAll();
        alert("SUCCÈS", "Règle enregistrée", "Règle ajoutée avec succès !");
    }
    
    @FXML void updateRule() {
        rules selected = tableRules.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (!validateRuleSaisie()) return;
            selected.setActiviteId(cbRuleActivite.getValue().getId());
            selected.setRuleDescription(taRuleDesc.getText());
            rulesService.update(selected);
            refreshAll();
            alert("SUCCÈS", "Règle mise à jour", "La règle a été modifiée avec succès.");
        }
    }
    @FXML void deleteRule() {
        rules selected = tableRules.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showPurgeConfirm("la règle n°" + selected.getId(), () -> {
                rulesService.delete(selected.getId());
                refreshAll();
                alert("SUCCÈS", "Règle supprimée", "La règle a été retirée de la base de données.");
            });
        }
    }

    @FXML void suggestRuleAI() {
        resetRuleValidation();
        if (cbRuleActivite.getValue() == null) {
            setError(cbRuleActivite, errRuleAct, "[!] Sélectionnez une activité pour générer une règle.");
            return;
        }

        activite selectedActivite = cbRuleActivite.getValue();
        String activityName = selectedActivite.getNoma();
        int activityId = selectedActivite.getId();
        taRuleDesc.setText("🤖 Gemini analyse les règles existantes...");

        // Collect existing rules for this specific activity from DB
        java.util.List<gambatta.tn.entites.activites.rules> existingRules = masterRules.stream()
                .filter(r -> r.getActiviteId() == activityId)
                .collect(java.util.stream.Collectors.toList());

        new Thread(() -> {
            try {
                // Pass existing rules so Gemini generates a complementary, non-duplicate rule
                String ruleText = gambatta.tn.utils.GeminiUtil.generateRuleSuggestion(activityName, existingRules);
                javafx.application.Platform.runLater(() -> {
                    taRuleDesc.setText(ruleText);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    String[] fallbackRules = {
                        "Le respect des autres joueurs et arbitres est obligatoire sous peine de disqualification.",
                        "Le matériel doit être manipulé avec soin. Toute dégradation sera facturée.",
                        "La présence est requise 15 minutes avant le début de la session.",
                        "Toute triche ou utilisation de logiciels tiers entraînera un bannissement définitif."
                    };
                    String fallback = fallbackRules[(int)(Math.random() * fallbackRules.length)];
                    taRuleDesc.setText(fallback + "\n[Généré localement]");
                });
            }
        }).start();
    }

    // --- RESERVATIONS HANDLERS ---
    @FXML void validerReservation() {
        ReservationActivite selected = tableReservations.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setStatutr("ACCEPTEE");
            reservationService.update(selected);
            refreshAll();

            activite act = getActiviteById(selected.getActiviteId());
            String actName = act != null ? act.getNoma() : "Activité";
            
            showNotificationChoice(selected, actName);
        }
    }

    private void showNotificationChoice(ReservationActivite res, String actName) {
        javafx.stage.Stage st = new javafx.stage.Stage();
        st.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        st.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new javafx.geometry.Insets(25));
        root.setPrefWidth(450);
        root.setStyle("-fx-background-color: #0f172a; -fx-background-radius: 20; -fx-border-color: #FFD700; -fx-border-width: 2; -fx-effect: dropshadow(gaussian, rgba(255,215,0,0.2), 20, 0, 0, 0);");

        Label title = new Label("CHOIX DE NOTIFICATION");
        title.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 20px; -fx-font-weight: bold; -fx-letter-spacing: 1px;");

        Label desc = new Label("Comment souhaitez-vous confirmer la réservation ?");
        desc.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        HBox options = new HBox(15);
        options.setAlignment(Pos.CENTER);

        Button btnEmail = new Button("📧 EMAIL");
        btnEmail.setStyle("-fx-background-color: #1e293b; -fx-text-fill: #3b82f6; -fx-border-color: #3b82f6; -fx-border-radius: 10; -fx-padding: 12 25; -fx-font-weight: bold; -fx-cursor: hand;");
        btnEmail.setOnAction(e -> { st.close(); notifyClient(res, actName, true, false); });

        Button btnWhatsApp = new Button("💬 WHATSAPP");
        btnWhatsApp.setStyle("-fx-background-color: #1e293b; -fx-text-fill: #2ed573; -fx-border-color: #2ed573; -fx-border-radius: 10; -fx-padding: 12 25; -fx-font-weight: bold; -fx-cursor: hand;");
        btnWhatsApp.setOnAction(e -> { st.close(); notifyClient(res, actName, false, true); });

        Button btnBoth = new Button("🔄 LES DEUX");
        btnBoth.setStyle("-fx-background-color: #FFD700; -fx-text-fill: #020617; -fx-background-radius: 10; -fx-padding: 12 25; -fx-font-weight: bold; -fx-cursor: hand;");
        btnBoth.setOnAction(e -> { st.close(); notifyClient(res, actName, true, true); });

        options.getChildren().addAll(btnEmail, btnWhatsApp, btnBoth);
        
        Button btnCancel = new Button("PLUS TARD");
        btnCancel.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> st.close());

        root.getChildren().addAll(title, desc, options, btnCancel);
        Scene sc = new Scene(root); sc.setFill(Color.TRANSPARENT); st.setScene(sc); st.showAndWait();
    }

    private void notifyClient(ReservationActivite res, String actName, boolean useEmail, boolean useWhatsApp) {
        new Thread(() -> {
            boolean emailOk = false;
            boolean waOk = false;
            String errorMsg = "";

            if (useEmail) {
                try {
                    gambatta.tn.utils.MailerUtil.sendConfirmationEmail(res.getEmail(), actName, res.getDatedebut().toString(), res.getHeurer(), "ACCEPTEE");
                    emailOk = true;
                } catch (Exception e) { errorMsg += "Email: " + e.getMessage() + "\n"; }
            }

            if (useWhatsApp) {
                try {
                    String dateStr = res.getDatedebut() != null ? res.getDatedebut().toString() : "";
                    String link = gambatta.tn.utils.WhatsAppUtil.buildWhatsAppLink(
                        res.getTelephone(), 
                        actName, 
                        res.getId(), 
                        dateStr, 
                        res.getHeurer()
                    );
                    
                    if (link != null) {
                        // On ouvre le navigateur (Platform.runLater car c'est une action UI)
                        javafx.application.Platform.runLater(() -> gambatta.tn.utils.WhatsAppUtil.openInBrowser(link));
                        waOk = true;
                    } else {
                        errorMsg += "WhatsApp: Numéro client manquant ou invalide\n";
                    }
                } catch (Exception e) { 
                    errorMsg += "WhatsApp: " + e.getMessage() + "\n"; 
                }
            }

            final boolean finalEmailOk = emailOk;
            final boolean finalWaOk = waOk;
            final String finalError = errorMsg;

            javafx.application.Platform.runLater(() -> {
                if ((useEmail == finalEmailOk) && (useWhatsApp == finalWaOk)) {
                    alert("SUCCÈS", "Notifications envoyées", "Le client a été notifié avec succès.");
                } else {
                    alert("ATTENTION", "Échec partiel/total", "Détails :\n" + finalError);
                }
            });
        }).start();
    }

    @FXML void refuserReservation() {
        ReservationActivite selected = tableReservations.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setStatutr("REFUSE");
            reservationService.update(selected);
            refreshAll();
        }
    }

    @FXML void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/activites/Portal.fxml"));
            tableActivites.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- PDF EXPORT METHODS ---
    @FXML void exportActivitesPDF() {
        exportTableToPDF("Rapport_Activites.pdf", "Liste des Activités - Gambatta", tableActivites);
    }
    @FXML void exportRulesPDF() {
        exportTableToPDF("Rapport_Regles.pdf", "Règles Administratrives - Gambatta", tableRules);
    }
    @FXML void exportReservationsPDF() {
        exportTableToPDF("Rapport_Reservations.pdf", "Etat des Réservations - Gambatta", tableReservations);
    }

    private void exportTableToPDF(String defaultName, String title, TableView<?> table) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fc.setInitialFileName(defaultName);
        File docFile = fc.showSaveDialog(table.getScene().getWindow());
        if (docFile == null) return;

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // BACKGROUND TOP ACCENT
                contentStream.setNonStrokingColor(15, 23, 42); // Dark Blue #0f172a
                contentStream.addRect(0, 780, 612, 50);
                contentStream.fill();

                // LOGO / BRANDING
                contentStream.setNonStrokingColor(255, 215, 0); // Gold
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 795);
                contentStream.showText("GAMBATTA");
                contentStream.endText();
                
                contentStream.setNonStrokingColor(255, 255, 255);
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(190, 795);
                contentStream.showText("COMPLEX MANAGEMENT");
                contentStream.endText();

                // TITLE & DATE
                contentStream.setNonStrokingColor(15, 23, 42);
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 740);
                contentStream.showText(title.toUpperCase());
                contentStream.endText();

                contentStream.setFont(PDType1Font.HELVETICA, 9);
                contentStream.beginText();
                contentStream.newLineAtOffset(420, 740);
                contentStream.showText("Exporté le: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date()));
                contentStream.endText();

                // Separator
                contentStream.setStrokingColor(255, 215, 0);
                contentStream.setLineWidth(1.5f);
                contentStream.moveTo(50, 730);
                contentStream.lineTo(560, 730);
                contentStream.stroke();

                float margin = 50;
                float tableWidth = 510;
                float yPosition = 700;
                int cols = table.getColumns().size();
                float rowHeight = 28f;
                float colWidth = tableWidth / (float) cols;

                // TABLE HEADER
                contentStream.setNonStrokingColor(30, 41, 59); // Slate header
                contentStream.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
                contentStream.fill();

                contentStream.setNonStrokingColor(255, 215, 0); // Gold text for header
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                float textx = margin + 8;
                float texty = yPosition - 18;
                
                for (TableColumn<?, ?> col : table.getColumns()) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(textx, texty);
                    String headerTxt = col.getText().toUpperCase();
                    contentStream.showText(headerTxt);
                    contentStream.endText();
                    textx += colWidth;
                }
                
                yPosition -= rowHeight;

                // ROWS
                contentStream.setFont(PDType1Font.HELVETICA, 9);
                boolean alternate = false;
                
                int rowIndex = 0;
                for (Object item : table.getItems()) {
                    if (yPosition - rowHeight < 60) break; // Simple paging check
                    
                    if (alternate) {
                        contentStream.setNonStrokingColor(248, 250, 252); // Very light gray
                        contentStream.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
                        contentStream.fill();
                    }
                    
                    // Border line below row
                    contentStream.setStrokingColor(226, 232, 240);
                    contentStream.setLineWidth(0.5f);
                    contentStream.moveTo(margin, yPosition - rowHeight);
                    contentStream.lineTo(margin + tableWidth, yPosition - rowHeight);
                    contentStream.stroke();

                    contentStream.setNonStrokingColor(15, 23, 42);
                    textx = margin + 8;
                    texty = yPosition - 18;
                    
                    for (TableColumn<?, ?> col : table.getColumns()) {
                        Object cellData = col.getCellData(rowIndex);
                        String txt = cellData == null ? "" : cellData.toString();
                        
                        // Simple wrap check
                        if (txt.length() > 25) txt = txt.substring(0, 22) + "...";
                        
                        contentStream.beginText();
                        contentStream.newLineAtOffset(textx, texty);
                        contentStream.showText(txt);
                        contentStream.endText();
                        textx += colWidth;
                    }
                    
                    yPosition -= rowHeight;
                    alternate = !alternate;
                    rowIndex++;
                }
                
                // FOOTER
                contentStream.setNonStrokingColor(100, 116, 139);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 30);
                contentStream.showText("Généré par Gambatta Esports System");
                contentStream.endText();
            }
            document.save(docFile);
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setContentText(title + " exporté avec succès !");
            a.show();
        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Erreur PDF: " + e.getMessage());
            a.show();
        }
    }

    private void showPurgeConfirm(String itemName, Runnable onConfirm) {
        javafx.stage.Stage st = new javafx.stage.Stage();
        st.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        st.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        VBox root = new VBox(25);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new javafx.geometry.Insets(30));
        root.setPrefWidth(420);
        root.setStyle("-fx-background-color: #0f172a; -fx-background-radius: 15; -fx-border-color: #ff4757; -fx-border-width: 2; -fx-effect: dropshadow(gaussian, rgba(255,71,87,0.3), 20, 0, 0, 0);");

        Label titleLbl = new Label("PURGE SYSTÈME");
        titleLbl.setStyle("-fx-text-fill: #ff4757; -fx-font-size: 32px; -fx-font-weight: 900; -fx-letter-spacing: 2px;");
        
        Label msgLbl = new Label("Supprimer définitivement " + itemName + " ?");
        msgLbl.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-text-alignment: center;");
        msgLbl.setWrapText(true);

        HBox btnBox = new HBox(20);
        btnBox.setAlignment(Pos.CENTER);

        Button btnAbort = new Button("ABORT");
        btnAbort.setPrefWidth(120);
        btnAbort.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-border-color: #475569; -fx-border-radius: 20; -fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 10;");
        btnAbort.setOnAction(e -> st.close());

        Button btnTerminate = new Button("TERMINATE");
        btnTerminate.setPrefWidth(140);
        btnTerminate.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff4757; -fx-border-color: #ff4757; -fx-border-radius: 20; -fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 10;");
        btnTerminate.setOnAction(e -> {
            st.close();
            onConfirm.run();
        });

        btnBox.getChildren().addAll(btnAbort, btnTerminate);
        root.getChildren().addAll(titleLbl, msgLbl, btnBox);

        Scene sc = new Scene(root);
        sc.setFill(Color.TRANSPARENT);
        st.setScene(sc);
        st.showAndWait();
    }

    /**
     * Rafraîchit les statistiques et le Dashboard avec un design professionnel
     */
    @FXML
    private void refreshStats() {
        // 1. Mise à jour des KPIs numériques
        int totalActs = masterActivites.size();
        int totalRes = masterReservations.size();
        lblTotalActs.setText(String.valueOf(totalActs));
        lblTotalRes.setText(String.valueOf(totalRes));

        // 2. Identification de la Top Activité (La plus réservée)
        java.util.Map<String, Long> resCountMap = masterReservations.stream()
            .collect(java.util.stream.Collectors.groupingBy(r -> {
                // Trouver le nom de l'activité associée
                return masterActivites.stream()
                    .filter(a -> a.getId() == r.getActiviteId())
                    .map(activite::getNoma)
                    .findFirst().orElse("Inconnue");
            }, java.util.stream.Collectors.counting()));

        String topActName = resCountMap.entrySet().stream()
            .max(java.util.Map.Entry.comparingByValue())
            .map(java.util.Map.Entry::getKey)
            .orElse("Aucune");
        
        lblTopAct.setText(topActName.toUpperCase());

        // 3. Mise à jour du PieChart (Répartition par Type)
        javafx.collections.ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        java.util.Map<String, Long> typeMap = masterActivites.stream()
            .collect(java.util.stream.Collectors.groupingBy(activite::getTypea, java.util.stream.Collectors.counting()));
        
        typeMap.forEach((type, count) -> pieData.add(new PieChart.Data(type + " (" + count + ")", count)));
        pieChartActivites.setData(pieData);

        // 4. Mise à jour du BarChart (Popularité)
        barChartReservations.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Réservations");

        // On prend les 5 plus populaires pour ne pas encombrer
        resCountMap.entrySet().stream()
            .sorted(java.util.Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .forEach(entry -> series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue())));

        barChartReservations.getData().add(series);
        
        System.out.println("📊 Dashboard mis à jour avec succès.");
    }
}
