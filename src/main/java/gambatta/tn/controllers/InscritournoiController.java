package gambatta.tn.controllers;

import gambatta.tn.entites.tournois.inscriptiontournoi;
import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.entites.tournois.tournoi;
import javafx.scene.control.*;
import javafx.scene.control.ComboBox;
import gambatta.tn.services.tournoi.EquipeService;
import gambatta.tn.services.tournoi.TournoiService;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class InscritournoiController {

    @FXML
    private TableView<inscriptiontournoi> tableInscriptions;
    @FXML
    private TableColumn<inscriptiontournoi, Long> colId;
    @FXML
    private TableColumn<inscriptiontournoi, String> colEquipe;
    @FXML
    private TableColumn<inscriptiontournoi, String> colTournoi;
    @FXML
    private TableColumn<inscriptiontournoi, String> colStatus;

    @FXML
    private ComboBox<equipe> comboEquipe;
    @FXML
    private ComboBox<tournoi> comboTournoi;
    @FXML
    private TextField txtSearch;

    @FXML
    private Button btnAjouter;
    @FXML
    private Button btnSupprimer;
    @FXML
    private Button btnPDF;
    @FXML
    private Button btnStats;

    private InscritournoiService service;
    private EquipeService equipeService = new EquipeService();
    private TournoiService tournoiService = new TournoiService();
    private ObservableList<inscriptiontournoi> inscriptions;

    public void initialize() {
        service = new InscritournoiService();

        // Charger les données dans les ComboBox
        comboEquipe.setItems(FXCollections.observableArrayList(equipeService.findAll()));
        comboTournoi.setItems(FXCollections.observableArrayList(tournoiService.findAll()));

        // Configurer les colonnes TableView
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleLongProperty(data.getValue().getId()).asObject());
        colEquipe.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEquipe().getNom()));
        colTournoi.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTournoi().getNomt()));
        colStatus.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));

        // Charger toutes les inscriptions
        loadInscriptions();

        // Boutons
        btnAjouter.setOnAction(e -> addInscription());
        btnSupprimer.setOnAction(e -> deleteInscription());
        btnPDF.setOnAction(e -> exportPDF());
        btnStats.setOnAction(e -> showStats());

        // Recherche en temps réel
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

        if (selectedEquipe != null && selectedTournoi != null) {
            // Crée l'inscription
            inscriptiontournoi i = new inscriptiontournoi();
            i.setEquipe(selectedEquipe);
            i.setTournoi(selectedTournoi);
            i.setStatus(inscriptiontournoi.STATUS_PENDING);

            boolean saved = service.save(i);
            if (saved) {
                inscriptions.add(i);
                comboEquipe.getSelectionModel().clearSelection();
                comboTournoi.getSelectionModel().clearSelection();
                tableInscriptions.refresh();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Impossible d'ajouter cette inscription !");
                alert.showAndWait();
            }
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
            // Ici tu peux utiliser iText ou PDFBox pour créer un vrai PDF
            System.out.println("PDF exporté vers : " + file.getAbsolutePath());
            System.out.println(pdfContent);
        }
    }

    private void showStats() {
        // Exemples de stats
        long accepted = inscriptions.stream().filter(i -> i.getStatus().equals(inscriptiontournoi.STATUS_ACCEPTED)).count();
        long pending = inscriptions.stream().filter(i -> i.getStatus().equals(inscriptiontournoi.STATUS_PENDING)).count();
        long refused = inscriptions.stream().filter(i -> i.getStatus().equals(inscriptiontournoi.STATUS_REFUSED)).count();

        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                "Accepted: " + accepted + "\n" +
                        "Pending: " + pending + "\n" +
                        "Refused: " + refused);
        alert.setHeaderText("Statistiques des inscriptions");
        alert.showAndWait();
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
}