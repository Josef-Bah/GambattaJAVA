package gambatta.tn.controllers;

import gambatta.tn.entites.tournois.tournoi;
import gambatta.tn.services.tournoi.TournoiService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    public void addTournoi() {
        String nom = nomField.getText().trim();
        String desc = descField.getText().trim();
        String statut = cmbStatut.getValue();

        if (!nom.isEmpty() && statut != null) {
            tournoi t = new tournoi();
            t.setNomt(nom);
            t.setDescrit(desc);
            t.setStatutt(statut);
            t.setDatedebutt(LocalDateTime.now());
            t.setDatefint(LocalDateTime.now().plusDays(3));

            boolean added = service.add(t);
            if (added) {
                tournois.add(t);
                nomField.clear();
                descField.clear();
                cmbStatut.getSelectionModel().clearSelection();
            } else {
                showAlert("Erreur", "Impossible d'ajouter ce tournoi !");
            }
        } else {
            showAlert("Attention", "Veuillez remplir le nom et sélectionner un statut !");
        }
    }

    @FXML
    public void deleteTournoi() {
        tournoi selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean deleted = service.delete(selected.getId());
            if (deleted) {
                tournois.remove(selected);
            } else {
                showAlert("Erreur", "Impossible de supprimer ce tournoi !");
            }
        } else {
            showAlert("Information", "Veuillez sélectionner un tournoi !");
        }
    }

    @FXML
    private void openEquipeWindow() {
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