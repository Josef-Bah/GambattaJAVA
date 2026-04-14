package gambatta.tn.controllers;

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

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

public class TournoiController {

    @FXML private TextField nomField;
    @FXML private TextField descField;
    @FXML private ComboBox<String> cmbStatut;
    @FXML private Button btnPDF;


    @FXML private TableView<tournoi> table;
    @FXML private TableColumn<tournoi, Long> idCol;
    @FXML private TableColumn<tournoi, String> nomCol;
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

        if (nom.isEmpty() || statut == null) {
            showAlert("Attention", "Veuillez remplir le nom et sélectionner un statut !");
            return;
        }

        tournoi selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Modification
            selected.setNomt(nom);
            selected.setDescrit(desc);
            selected.setStatutt(statut);
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
            t.setDatedebutt(LocalDateTime.now());
            t.setDatefint(LocalDateTime.now().plusDays(3));

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
        long enAttente = tournois.stream().filter(t -> "EN_ATTENTE".equals(t.getStatutt())).count();
        long valide = tournois.stream().filter(t -> "VALIDE".equals(t.getStatutt())).count();
        long termine = tournois.stream().filter(t -> "TERMINE".equals(t.getStatutt())).count();

        showAlert("Statistiques des Tournois", 
            "Tournois en attente : " + enAttente + "\n" +
            "Tournois validés : " + valide + "\n" +
            "Tournois terminés : " + termine);
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