package gambatta.tn.ui;

import gambatta.tn.entites.tournois.tournoi;
import gambatta.tn.services.tournoi.TournoiService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

public class TournoiController {

    @FXML private TextField nomField;
    @FXML private TextField descField;
    @FXML private ComboBox<String> cmbStatut;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private Button btnPDF;


    @FXML private TableView<tournoi> table;
    @FXML private TableColumn<tournoi, Long> idCol;
    @FXML private TableColumn<tournoi, String> nomCol;
    @FXML private TableColumn<tournoi, String> dateDebutCol;
    @FXML private TableColumn<tournoi, String> dateFinCol;
    @FXML private TableColumn<tournoi, String> statutCol;
    @FXML private TableColumn<tournoi, Void> colModifier;
    @FXML private TableColumn<tournoi, Void> colSupprimer;

    private TournoiService service = new TournoiService();
    private ObservableList<tournoi> tournois = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialiser ComboBox pour le statut
        cmbStatut.setItems(FXCollections.observableArrayList("EN_ATTENTE", "VALIDE", "TERMINE"));
        btnPDF.setOnAction(e -> exportPDF());
        // Configurer les colonnes du tableau
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleLongProperty(data.getValue().getId()).asObject());
        nomCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNomt()));
        dateDebutCol.setCellValueFactory(data -> {
            LocalDateTime date = data.getValue().getDatedebutt();
            return new javafx.beans.property.SimpleStringProperty(date != null ? date.toLocalDate().toString() : "");
        });
        dateFinCol.setCellValueFactory(data -> {
            LocalDateTime date = data.getValue().getDatefint();
            return new javafx.beans.property.SimpleStringProperty(date != null ? date.toLocalDate().toString() : "");
        });
        statutCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatutt()));
        tournois = FXCollections.observableArrayList(service.findAll());
        table.setItems(tournois);

        // Configuration des colonnes d'action Modifier
        colModifier.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Modifier");
            {
                btn.setOnAction(event -> {
                    tournoi t = getTableView().getItems().get(getIndex());
                    nomField.setText(t.getNomt());
                    descField.setText(t.getDescrit());
                    cmbStatut.setValue(t.getStatutt());
                    if (t.getDatedebutt() != null) dateDebutPicker.setValue(t.getDatedebutt().toLocalDate());
                    if (t.getDatefint() != null) dateFinPicker.setValue(t.getDatefint().toLocalDate());
                    table.getSelectionModel().select(t);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        // Configuration des colonnes d'action Supprimer
        colSupprimer.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Supprimer");
            {
                btn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
                btn.setOnAction(event -> {
                    tournoi t = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer le tournoi " + t.getNomt() + " ?", ButtonType.YES, ButtonType.NO);
                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            if (service.delete(t.getId())) {
                                tournois.remove(t);
                            }
                        }
                    });
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        // Sélection dans le tableau
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                nomField.setText(newSel.getNomt());
                descField.setText(newSel.getDescrit());
                cmbStatut.setValue(newSel.getStatutt());
                dateDebutPicker.setValue(newSel.getDatedebutt() != null ? newSel.getDatedebutt().toLocalDate() : null);
                dateFinPicker.setValue(newSel.getDatefint() != null ? newSel.getDatefint().toLocalDate() : null);
            }
        });

        // Charger les données depuis la base
        loadData();
    }

    private void loadData() {
        List<tournoi> list = service.findAll();
        tournois.setAll(list);
        table.setItems(tournois);
    }
    private void exportPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les équipes en PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(table.getScene().getWindow());
        if (file != null) {
            String pdfContent = service.generatePdf();
            System.out.println("PDF exporté vers : " + file.getAbsolutePath());
            System.out.println(pdfContent);
        }
    }


    @FXML
    public void addTournoi(ActionEvent event) {
        String nom = nomField.getText().trim();
        String desc = descField.getText().trim();
        String statut = cmbStatut.getValue();
        java.time.LocalDate dateDebutVal = dateDebutPicker.getValue();
        java.time.LocalDate dateFinVal = dateFinPicker.getValue();

        if (nom.isEmpty() || statut == null || dateDebutVal == null || dateFinVal == null) {
            showAlert("Attention", "Veuillez remplir tous les champs obligatoires (nom, statut, dates) !");
            return;
        }

        if (dateDebutVal.isAfter(dateFinVal)) {
            showAlert("Attention", "La date de fin doit être après ou le même jour que la date de début.");
            return;
        }

        tournoi selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Modification
            selected.setNomt(nom);
            selected.setDescrit(desc);
            selected.setStatutt(statut);
            selected.setDatedebutt(dateDebutVal.atStartOfDay());
            selected.setDatefint(dateFinVal.atTime(23, 59, 59));
            if (service.update(selected)) {
                showAlert("Succès", "Le tournoi a été modifié avec succès.");
                table.refresh();
                clearFields();
            } else {
                showAlert("Erreur", "Impossible de modifier ce tournoi !");
            }
        } else {
            // Ajout
            tournoi t = new tournoi();
            t.setNomt(nom);
            t.setDescrit(desc);
            t.setStatutt(statut);
            t.setDatedebutt(dateDebutVal.atStartOfDay());
            t.setDatefint(dateFinVal.atTime(23, 59, 59));

            boolean added = service.add(t);
            if (added) {
                showAlert("Succès", "Le tournoi a été ajouté avec succès.");
                tournois.add(t);
                clearFields();
            } else {
                showAlert("Erreur", "Impossible d'ajouter ce tournoi !");
            }
        }
    }

    @FXML
    public void showStats(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/StatsInterface.fxml"));
            AnchorPane root = loader.load();
            
            StatsController controller = loader.getController();
            
            java.util.Map<String, Long> stats = new java.util.HashMap<>();
            stats.put("EN_ATTENTE", tournois.stream().filter(t -> "EN_ATTENTE".equals(t.getStatutt())).count());
            stats.put("VALIDE", tournois.stream().filter(t -> "VALIDE".equals(t.getStatutt())).count());
            stats.put("TERMINE", tournois.stream().filter(t -> "TERMINE".equals(t.getStatutt())).count());
            
            controller.setData("Statistiques des Tournois", stats, "EN_ATTENTE", "VALIDE");
            
            Stage stage = new Stage();
            stage.setTitle("Tableau de Bord des Tournois");
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les statistiques.");
        }
    }

    @FXML
    public void handleNewTournoi(ActionEvent event) {
        clearFields();
        showAlert("Prêt", "Les champs ont été réinitialisés. Vous pouvez maintenant ajouter un nouveau tournoi.");
    }

    private void clearFields() {
        nomField.clear();
        descField.clear();
        cmbStatut.getSelectionModel().clearSelection();
        dateDebutPicker.setValue(null);
        dateFinPicker.setValue(null);
        table.getSelectionModel().clearSelection();
    }

    @FXML
    public void openEquipeWindow(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/EquipeInterface.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 780);
            scene.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            Stage stage = new Stage();
            stage.setTitle("Interface Equipe");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir l'interface équipe !");
        }
    }

    // Méthode utilitaire pour afficher les alertes
    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
