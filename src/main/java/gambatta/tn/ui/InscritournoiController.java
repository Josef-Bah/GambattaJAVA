package gambatta.tn.ui;

import gambatta.tn.entites.tournois.inscriptiontournoi;
import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.entites.tournois.tournoi;
import javafx.scene.control.*;
import gambatta.tn.services.tournoi.EquipeService;
import gambatta.tn.services.tournoi.TournoiService;
import gambatta.tn.services.tournoi.InscritournoiService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class InscritournoiController {

    @FXML private TableView<inscriptiontournoi> tableInscriptions;
    @FXML private TableColumn<inscriptiontournoi, Long> colId;
    @FXML private TableColumn<inscriptiontournoi, String> colEquipe;
    @FXML private TableColumn<inscriptiontournoi, String> colTournoi;
    @FXML private TableColumn<inscriptiontournoi, String> colStatus;

    @FXML private ComboBox<equipe> comboEquipe;
    @FXML private ComboBox<tournoi> comboTournoi;
    @FXML private TextField txtSearch;

    @FXML private Button btnAjouter;
    @FXML private Button btnSupprimer;
    @FXML private Button btnPDF;
    @FXML private Button btnStats;

    private InscritournoiService service;
    private EquipeService equipeService = new EquipeService();
    private TournoiService tournoiService = new TournoiService();
    private ObservableList<inscriptiontournoi> inscriptions;

    public void initialize() {
        service = new InscritournoiService();

        comboEquipe.setItems(FXCollections.observableArrayList(equipeService.findAll()));
        comboTournoi.setItems(FXCollections.observableArrayList(tournoiService.findAll()));

        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleLongProperty(data.getValue().getId()).asObject());
        colEquipe.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEquipe().getNom()));
        colTournoi.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTournoi().getNomt()));
        colStatus.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));

        loadInscriptions();

        btnAjouter.setOnAction(e -> addInscription());
        btnSupprimer.setOnAction(e -> deleteInscription());
        btnPDF.setOnAction(e -> exportPDF());
        btnStats.setOnAction(e -> showStats());

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterInscriptions(newVal));
    }

    private void loadInscriptions() {
        List<inscriptiontournoi> list = service.findAll();
        inscriptions = FXCollections.observableArrayList(list);
        tableInscriptions.setItems(inscriptions);
    }

    private void addInscription() {
        equipe selectedEquipe = comboEquipe.getSelectionModel().getSelectedItem();
        tournoi selectedTournoi = comboTournoi.getSelectionModel().getSelectedItem();

        if (selectedEquipe == null || selectedTournoi == null) {
            showWarning("Veuillez sélectionner une équipe et un tournoi.");
            return;
        }

        inscriptiontournoi i = new inscriptiontournoi();
        i.setEquipe(selectedEquipe);
        i.setTournoi(selectedTournoi);
        i.setStatus(inscriptiontournoi.STATUS_PENDING);

        boolean saved = service.save(i);
        if (saved) {
            showAlert("L'équipe " + selectedEquipe.getNom() + " a été inscrite avec succès !");
            inscriptions.add(i);
            comboEquipe.getSelectionModel().clearSelection();
            comboTournoi.getSelectionModel().clearSelection();
            tableInscriptions.refresh();
        } else {
            showError("Impossible d'ajouter cette inscription. L'équipe est peut-être déjà inscrite.");
        }
    }

    private void deleteInscription() {
        inscriptiontournoi selected = tableInscriptions.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean deleted = service.delete(selected.getId());
            if (deleted) {
                inscriptions.remove(selected);
                tableInscriptions.refresh();
            }
        }
    }

    private void exportPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les inscriptions en PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(tableInscriptions.getScene().getWindow());
        if (file != null) {
            String pdfContent = service.generatePdf();
            System.out.println("PDF exporté vers : " + file.getAbsolutePath());
        }
    }

    private void showStats() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/StatsInterface.fxml"));
            AnchorPane root = loader.load();
            
            StatsController controller = loader.getController();
            
            java.util.Map<String, Long> stats = new java.util.HashMap<>();
            stats.put(inscriptiontournoi.STATUS_ACCEPTED, inscriptions.stream().filter(i -> i.getStatus().equals(inscriptiontournoi.STATUS_ACCEPTED)).count());
            stats.put(inscriptiontournoi.STATUS_PENDING, inscriptions.stream().filter(i -> i.getStatus().equals(inscriptiontournoi.STATUS_PENDING)).count());
            stats.put(inscriptiontournoi.STATUS_REFUSED, inscriptions.stream().filter(i -> i.getStatus().equals(inscriptiontournoi.STATUS_REFUSED)).count());
            
            controller.setData("Statistiques des Inscriptions", stats, inscriptiontournoi.STATUS_PENDING, inscriptiontournoi.STATUS_ACCEPTED);
            
            Stage stage = new Stage();
            stage.setTitle("Tableau de Bord des Inscriptions");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Impossible de charger les statistiques.");
        }
    }

    private void filterInscriptions(String search) {
        if (search == null || search.isEmpty()) {
            tableInscriptions.setItems(inscriptions);
        } else {
            ObservableList<inscriptiontournoi> filtered = FXCollections.observableArrayList();
            for (inscriptiontournoi i : inscriptions) {
                if (i.getEquipe().getNom().toLowerCase().contains(search.toLowerCase())
                        || i.getTournoi().getNomt().toLowerCase().contains(search.toLowerCase())) {
                    filtered.add(i);
                }
            }
            tableInscriptions.setItems(filtered);
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attention");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
