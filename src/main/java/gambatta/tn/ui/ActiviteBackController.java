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
import javafx.stage.FileChooser;
import java.io.File;
import javafx.scene.image.Image;
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
    @FXML private TableColumn<activite, String> colActNom;
    @FXML private TableColumn<activite, String> colActType;
    @FXML private TableColumn<activite, String> colActAdresse;
    @FXML private TextField tfSearchActivites;

    
    @FXML private TextField tfActNom;
    @FXML private TextField tfActType;
    @FXML private TextField tfActDispo;
    @FXML private TextField tfActAdresse;
    @FXML private TextArea taActDesc;
    @FXML private TextField tfActImage;
    @FXML private javafx.scene.image.ImageView ivImagePreview;

    // --- TAB RULES ---
    @FXML private TableView<rules> tableRules;
    @FXML private TableColumn<rules, String> colRuleId;
    @FXML private TableColumn<rules, String> colRuleDesc;
    @FXML private TableColumn<rules, String> colRuleAct;
    @FXML private TextField tfSearchRules;

    @FXML private ComboBox<activite> cbRuleActivite;
    @FXML private TextArea taRuleDesc;

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
    public void initialize() {
        // Init Activites table
        colActNom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNoma()));
        colActType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTypea()));
        colActAdresse.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAdresse()));
        
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
                            ivImagePreview.setImage(new Image(newSel.getImagea()));
                        } else {
                            File f = new File(newSel.getImagea());
                            if (f.exists()) ivImagePreview.setImage(new Image(f.toURI().toString()));
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
            tfActImage.setText(f.getAbsolutePath());
            ivImagePreview.setImage(new Image(f.toURI().toString()));
        }
    }

    private boolean validateSaisie() {
        if (tfActNom.getText() == null || tfActNom.getText().trim().isEmpty() ||
            tfActType.getText() == null || tfActType.getText().trim().isEmpty() ||
            tfActAdresse.getText() == null || tfActAdresse.getText().trim().isEmpty()) {
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de Saisie");
            alert.setHeaderText("Champs obligatoires manquants");
            alert.setContentText("Le nom, le type et l'adresse sont obligatoires !");
            alert.showAndWait();
            return false;
        }
        if (taActDesc.getText() != null && taActDesc.getText().length() < 10) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attention");
            alert.setHeaderText("Description très courte");
            alert.setContentText("Veuillez fournir une description claire et détaillée (minimum 10 caractères).");
            alert.showAndWait();
            return false;
        }
        return true;
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
            activiteService.delete(selected.getId());
            refreshAll();
        }
    }

    // --- RULES HANDLERS ---
    private boolean validateRuleSaisie() {
        if (cbRuleActivite.getValue() == null) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Veuillez sélectionner une activité.");
            a.show();
            return false;
        }
        if (taRuleDesc.getText() == null || taRuleDesc.getText().trim().isEmpty() || taRuleDesc.getText().length() < 10) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("La description de la règle doit comporter au moins 10 caractères.");
            a.show();
            return false;
        }
        return true;
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

    // --- RESERVATIONS HANDLERS ---
    @FXML void validerReservation() {
        ReservationActivite selected = tableReservations.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setStatutr("VALIDE");
            reservationService.update(selected);
            refreshAll();
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

    @FXML void handleBackFront() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/activites/ActiviteFront.fxml"));
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
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText(title);
                contentStream.endText();

                contentStream.setFont(PDType1Font.HELVETICA, 10);
                int y = 700;
                
                // Write headers
                contentStream.beginText();
                contentStream.newLineAtOffset(50, y);
                StringBuilder headerLine = new StringBuilder();
                for (TableColumn<?, ?> col : table.getColumns()) {
                    headerLine.append(col.getText()).append("   |   ");
                }
                contentStream.showText(headerLine.toString());
                contentStream.endText();
                y -= 20;

                // Write rows
                for (Object item : table.getItems()) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, y);
                    StringBuilder rowLine = new StringBuilder();
                    for (TableColumn<?, ?> col : table.getColumns()) {
                        Object cellData = ((TableColumn<Object, Object>) col).getCellData(item);
                        rowLine.append(cellData != null ? cellData.toString() : "").append("   |   ");
                    }
                    contentStream.showText(rowLine.toString());
                    contentStream.endText();
                    y -= 15;
                    
                    if (y < 50) break; // Simplistic paging max limit
                }
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
