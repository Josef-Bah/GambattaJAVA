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
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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
    @FXML private TableColumn<activite, Integer> colActId;
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
    @FXML private TableColumn<rules, String> colRuleId;
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
    @FXML private TextField tfSearchReservations;

    // --- STATISTIQUES ---
    @FXML private PieChart pieChartActivites;
    @FXML private BarChart<String, Number> barChartReservations;

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
        colActId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        
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
        colRuleId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
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
        updateStatistics();
    }
    
    private void updateStatistics() {
        // Pie Chart: Activités par type
        pieChartActivites.getData().clear();
        java.util.Map<String, Long> typesCount = masterActivites.stream()
            .collect(Collectors.groupingBy(activite::getTypea, Collectors.counting()));
            
        for (java.util.Map.Entry<String, Long> entry : typesCount.entrySet()) {
            PieChart.Data slice = new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue());
            pieChartActivites.getData().add(slice);
        }
        
        // Bar Chart: Reservations par activite
        barChartReservations.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Réservations");
        
        java.util.Map<Integer, Long> resCount = masterReservations.stream()
            .collect(Collectors.groupingBy(ReservationActivite::getActiviteId, Collectors.counting()));
            
        for (java.util.Map.Entry<Integer, Long> entry : resCount.entrySet()) {
            activite a = getActiviteById(entry.getKey());
            String actName = a != null ? a.getNoma() : "Inconnu (" + entry.getKey() + ")";
            series.getData().add(new XYChart.Data<>(actName, entry.getValue()));
        }
        barChartReservations.getData().add(series);
    }
    
    private activite getActiviteById(int id) {
        return activiteService.getAll().stream().filter(a -> a.getId() == id).findFirst().orElse(null);
    }

    // --- ACTIVITE HANDLERS ---
    @FXML void handleSelectImage() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File f = fc.showOpenDialog(tfActNom.getScene().getWindow());
        if (f != null) {
            tfActImage.setText("Uploading vers Cloudinary...");
            
            new Thread(() -> {
                try {
                    String secureUrl = gambatta.tn.utils.CloudinaryUtil.uploadFile(f);
                    javafx.application.Platform.runLater(() -> {
                        tfActImage.setText(secureUrl);
                        ivImagePreview.setImage(new Image(secureUrl, true));
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        tfActImage.setText(f.getAbsolutePath()); // Fallback au local en cas d'erreur
                        ivImagePreview.setImage(new Image(f.toURI().toString(), true));
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setHeaderText("Erreur Cloudinary");
                        alert.setContentText("Upload échoué : " + e.getMessage() + "\nLe chemin local a été conservé par défaut.");
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
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("Activité ajoutée avec succès !");
        alert.show();
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
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Activité modifiée avec succès !");
            alert.show();
        }
    }
    @FXML void deleteActivite() {
        activite selected = tableActivites.getSelectionModel().getSelectedItem();
        if (selected != null) {
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
                
                tfActNom.clear();
                tfActType.clear();
                tfActDispo.clear();
                tfActAdresse.clear();
                taActDesc.clear();
                tfActImage.clear();
                resetValidation();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Erreur lors de la suppression en cascade.");
                alert.show();
            }
        }
    }

    @FXML void suggestActivitiesAI() {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Génération IA");
        info.setHeaderText("Gemini est en train de réfléchir...");
        info.setContentText("Veuillez patienter quelques secondes.");
        info.show();

        new Thread(() -> {
            try {
                String jsonResult = gambatta.tn.utils.GeminiUtil.generateActivitySuggestion();
                org.json.JSONObject suggestion = new org.json.JSONObject(jsonResult);

                javafx.application.Platform.runLater(() -> {
                    info.close();
                    tfActNom.setText(suggestion.optString("nom", "Tournoi Inconnu"));
                    tfActType.setText(suggestion.optString("type", "Esports"));
                    tfActDispo.setText(suggestion.optString("dispo", "Weekends"));
                    tfActAdresse.setText(suggestion.optString("adresse", "Main Stage"));
                    taActDesc.setText(suggestion.optString("desc", "Description générée.") + " [Généré par IA]");

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Suggestion IA");
                    alert.setHeaderText("L'IA a généré une suggestion d'activité !");
                    alert.setContentText("Les champs ont été remplis automatiquement via Gemini API.");
                    alert.show();
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    info.close();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur IA");
                    alert.setHeaderText("Impossible de joindre Gemini");
                    alert.setContentText(e.getMessage() + "\n(Vérifiez votre clé API dans GeminiUtil.java)");
                    alert.show();
                });
            }
        }).start();
    }

    @FXML
    void handleVoiceSearch() {
        try {
            tfSearchActivites.setPromptText("🎙 Écoute en cours (5s)...");
            tfSearchActivites.setText("");
            new Thread(() -> {
                try {
                    String psCommand = "Add-Type -AssemblyName System.Speech; " +
                        "$recognizer = New-Object System.Speech.Recognition.SpeechRecognitionEngine; " +
                        "$recognizer.LoadGrammar((New-Object System.Speech.Recognition.DictationGrammar)); " +
                        "$recognizer.SetInputToDefaultAudioDevice(); " +
                        "$result = $recognizer.Recognize((New-TimeSpan -Seconds 5)); " +
                        "if ($result -ne $null) { Write-Output $result.Text }";
                    
                    ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-Command", psCommand);
                    pb.redirectErrorStream(true);
                    Process process = pb.start();
                    
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
                    StringBuilder out = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        out.append(line).append(" ");
                    }
                    process.waitFor();
                    String recognizedText = out.toString().trim();
                    
                    javafx.application.Platform.runLater(() -> {
                        tfSearchActivites.setPromptText("Rechercher par nom, type...");
                        if (!recognizedText.isEmpty()) {
                            tfSearchActivites.setText(recognizedText);
                        } else {
                            tfSearchActivites.setPromptText("Bruit non reconnu...");
                        }
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> tfSearchActivites.setPromptText("Erreur micro"));
                }
            }).start();
        } catch (Exception e) {}
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
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText("Règle ajoutée avec succès !");
        a.show();
    }
    
    @FXML void updateRule() {
        rules selected = tableRules.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (!validateRuleSaisie()) return;
            selected.setActiviteId(cbRuleActivite.getValue().getId());
            selected.setRuleDescription(taRuleDesc.getText());
            rulesService.update(selected);
            refreshAll();
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setContentText("Règle modifiée avec succès !");
            a.show();
        }
    }
    @FXML void deleteRule() {
        rules selected = tableRules.getSelectionModel().getSelectedItem();
        if (selected != null) {
            rulesService.delete(selected.getId());
            refreshAll();
        }
    }

    @FXML void suggestRuleAI() {
        resetRuleValidation();
        if (cbRuleActivite.getValue() == null) {
            setError(cbRuleActivite, errRuleAct, "[!] Sélectionnez une activité pour générer une règle.");
            return;
        }

        String activityName = cbRuleActivite.getValue().getNoma();
        taRuleDesc.setText("Génération en cours...");

        new Thread(() -> {
            try {
                String ruleText = gambatta.tn.utils.GeminiUtil.generateRuleSuggestion(activityName);
                javafx.application.Platform.runLater(() -> {
                    taRuleDesc.setText(ruleText);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    // Fallback local si l'API échoue ou clé absente (SILENT)
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
            String userEmail = selected.getEmail();
            String userPhone = selected.getTelephone();

            new Thread(() -> {
                gambatta.tn.utils.MailerUtil.sendConfirmationEmail(userEmail, actName, selected.getDatedebut().toString(), selected.getHeurer(), "ACCEPTEE");
                
                try {
                    gambatta.tn.utils.WhatsAppUtil.sendReservationMessage(userPhone, actName, "ACCEPTEE");
                    javafx.application.Platform.runLater(() -> {
                        Alert a = new Alert(Alert.AlertType.INFORMATION);
                        a.setContentText("✅ Réservation acceptée ! Le client a été notifié par E-mail et WhatsApp.");
                        a.show();
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        Alert a = new Alert(Alert.AlertType.WARNING);
                        a.setTitle("Attention - Erreur WhatsApp");
                        a.setHeaderText("Email envoyé, mais échec de WhatsApp !");
                        a.setContentText(e.getMessage());
                        a.show();
                    });
                }
            }).start();
        }
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
                // Title
                contentStream.setNonStrokingColor(15, 23, 42); // Dark slate
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 22);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText(title);
                contentStream.endText();

                // Separator line
                contentStream.setStrokingColor(255, 215, 0); // Gold
                contentStream.setLineWidth(2f);
                contentStream.moveTo(50, 740);
                contentStream.lineTo(550, 740);
                contentStream.stroke();

                float margin = 50;
                float tableWidth = 500;
                float yPosition = 710;
                int cols = table.getColumns().size();
                float rowHeight = 25f;
                float colWidth = tableWidth / (float) cols;

                // Draw Header Background
                contentStream.setNonStrokingColor(30, 41, 59); // Slate header
                contentStream.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
                contentStream.fill();

                // Draw Header Text
                contentStream.setNonStrokingColor(255, 255, 255); // White text
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
                float textx = margin + 5;
                float texty = yPosition - 17;
                
                for (TableColumn<?, ?> col : table.getColumns()) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(textx, texty);
                    contentStream.showText(col.getText().toUpperCase());
                    contentStream.endText();
                    textx += colWidth;
                }
                
                yPosition -= rowHeight;

                // Draw Rows
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                boolean alternate = false;
                
                for (Object item : table.getItems()) {
                    if (yPosition - rowHeight < 50) break; // Simplistic paging
                    
                    if (alternate) {
                        contentStream.setNonStrokingColor(241, 245, 249); // Light gray
                        contentStream.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
                        contentStream.fill();
                    }
                    alternate = !alternate;

                    contentStream.setNonStrokingColor(15, 23, 42); // Dark text
                    textx = margin + 5;
                    texty = yPosition - 17;
                    
                    for (TableColumn<?, ?> col : table.getColumns()) {
                        Object cellData = ((TableColumn<Object, Object>) col).getCellData(item);
                        String text = cellData != null ? cellData.toString() : "-";
                        
                        // Sanitize to avoid PDFBox exceptions with unprintable chars
                        text = text.replace("\n", " ").replace("\r", " ").replace("’", "'").replace("é", "e").replace("è", "e").replace("à", "a");
                        if (text.length() > 20) text = text.substring(0, 18) + "..";
                        
                        contentStream.beginText();
                        contentStream.newLineAtOffset(textx, texty);
                        contentStream.showText(text);
                        contentStream.endText();
                        textx += colWidth;
                    }
                    yPosition -= rowHeight;
                }
                
                // Footer
                contentStream.setNonStrokingColor(100, 116, 139);
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);
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
}
