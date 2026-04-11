package gambatta.tn.controllers;

import gambatta.tn.entites.tournois.inscriptiontournoi;
import gambatta.tn.services.tournoi.InscritournoiService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.Map;

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
    private TextField txtEquipe;
    @FXML
    private TextField txtTournoi;
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
    private ObservableList<inscriptiontournoi> inscriptions;

    @FXML
    public void initialize() {
        service = new InscritournoiService();

        // Config colonnes TableView
        colId.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getId()));
        colEquipe.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getEquipe() != null ? cell.getValue().getEquipe().getNom() : ""));
        colTournoi.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getTournoi() != null ? cell.getValue().getTournoi().getNomt() : ""));
        colStatus.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getStatus()));

        // Charger les inscriptions
        loadInscriptions();

        // Actions boutons
        btnAjouter.setOnAction(e -> addInscription());
        btnSupprimer.setOnAction(e -> deleteInscription());
        btnPDF.setOnAction(e -> exportPDF());
        btnStats.setOnAction(e -> showStats());

        // Filtrage recherche
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterInscriptions(newVal));
    }

    private void loadInscriptions() {
        List<inscriptiontournoi> list = service.index();
        inscriptions = FXCollections.observableArrayList(list);
        tableInscriptions.setItems(inscriptions);
    }

    private void addInscription() {
        String equipeName = txtEquipe.getText().trim();
        String tournoiName = txtTournoi.getText().trim();
        if (!equipeName.isEmpty() && !tournoiName.isEmpty()) {
            inscriptiontournoi i = new inscriptiontournoi();
            i.getEquipe().setNom(equipeName);
            i.getTournoi().setNomt(tournoiName);
            i.setStatus(inscriptiontournoi.STATUS_PENDING);

            boolean saved = service.save(i);
            if (saved) {
                inscriptions.add(i);
                txtEquipe.clear();
                txtTournoi.clear();
            }
        }
    }

    private void deleteInscription() {
        inscriptiontournoi selected = tableInscriptions.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean deleted = service.delete(selected.getId());
            if (deleted) {
                inscriptions.remove(selected);
            }
        }
    }

    private void exportPDF() {
        // Pour l'instant export texte simple
        String content = service.generatePdf();
        System.out.println("Contenu PDF :\n" + content);
    }

    private void showStats() {
        Map<String, Object> stats = service.stats();
        int total = (int) stats.getOrDefault("totalInscriptions", 0);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Statistiques Inscriptions");
        alert.setHeaderText("Total inscriptions : " + total);
        alert.setContentText(stats.get("inscriptionsByStatus").toString());
        alert.showAndWait();
    }

    private void filterInscriptions(String search) {
        if (search == null || search.isEmpty()) {
            tableInscriptions.setItems(inscriptions);
        } else {
            ObservableList<inscriptiontournoi> filtered = FXCollections.observableArrayList();
            for (inscriptiontournoi i : inscriptions) {
                String eq = i.getEquipe() != null ? i.getEquipe().getNom() : "";
                String t = i.getTournoi() != null ? i.getTournoi().getNomt() : "";
                if (eq.toLowerCase().contains(search.toLowerCase()) ||
                        t.toLowerCase().contains(search.toLowerCase())) {
                    filtered.add(i);
                }
            }
            tableInscriptions.setItems(filtered);
        }
    }
}