package gambatta.tn.controllers;

import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.services.tournoi.EquipeService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class EquipeController {

    @FXML private TableView<equipe> tableEquipes;
    @FXML private TableColumn<equipe, Long> colId;
    @FXML private TableColumn<equipe, String> colNom;
    @FXML private TableColumn<equipe, String> colLeader;
    @FXML private TableColumn<equipe, String> colStatus;

    @FXML private TextField txtNom;
    @FXML private TextField txtLeader;
    @FXML private TextField txtSearch;

    @FXML private Button btnAjouter;
    @FXML private Button btnSupprimer;
    @FXML private Button btnModifier;
    @FXML private Button btnPDF;

    private EquipeService equipeService;
    private ObservableList<equipe> equipes;

    @FXML
    public void initialize() {
        equipeService = new EquipeService();

        // Config TableView
        colId.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getId()));
        colNom.setCellValueFactory(cell -> cell.getValue().nomProperty());
        colLeader.setCellValueFactory(cell -> cell.getValue().teamLeaderProperty());
        colStatus.setCellValueFactory(cell -> cell.getValue().statusProperty());

        // Charge les équipes existantes
        equipes = FXCollections.observableArrayList(equipeService.findAll());
        tableEquipes.setItems(equipes);

        // Actions boutons
        btnAjouter.setOnAction(e -> addEquipe());
        btnSupprimer.setOnAction(e -> deleteEquipe());
        btnModifier.setOnAction(e -> updateEquipe());
        btnPDF.setOnAction(e -> exportPDF());

        // Pré-remplissage des TextFields pour modification
        tableEquipes.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtNom.setText(newSel.getNom());
                txtLeader.setText(newSel.getTeamLeader());
            }
        });

        // Recherche en temps réel
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterEquipes(newVal));
    }

    private void addEquipe() {
        String nom = txtNom.getText().trim();
        String leader = txtLeader.getText().trim();
        if (!nom.isEmpty() && !leader.isEmpty()) {
            equipe e = new equipe();
            e.setNom(nom);
            e.setTeamLeader(leader);
            e.setStatus("EN_ATTENTE");
            boolean saved = equipeService.save(e);
            if (saved) {
                // 🔹 Ajouter l'objet directement à l'ObservableList
                equipes.add(e);
                tableEquipes.refresh();
                txtNom.clear();
                txtLeader.clear();
            }
        }
    }

    private void deleteEquipe() {
        equipe selected = tableEquipes.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean deleted = equipeService.delete(selected.getId());
            if (deleted) {
                // 🔹 Retirer l'objet de l'ObservableList
                equipes.remove(selected);
                tableEquipes.refresh();
            }
        }
    }

    private void updateEquipe() {
        equipe selected = tableEquipes.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String nom = txtNom.getText().trim();
            String leader = txtLeader.getText().trim();
            if (!nom.isEmpty()) selected.setNom(nom);
            if (!leader.isEmpty()) selected.setTeamLeader(leader);
            equipeService.save(selected); // gère INSERT ou UPDATE selon id
            tableEquipes.refresh();
        }
    }

    private void filterEquipes(String search) {
        if (search == null || search.isEmpty()) {
            tableEquipes.setItems(equipes);
        } else {
            ObservableList<equipe> filtered = FXCollections.observableArrayList();
            for (equipe e : equipes) {
                if (e.getNom().toLowerCase().contains(search.toLowerCase())) {
                    filtered.add(e);
                }
            }
            tableEquipes.setItems(filtered);
        }
    }

    private void exportPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les équipes en PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(tableEquipes.getScene().getWindow());
        if (file != null) {
            String pdfContent = equipeService.generatePdf();
            System.out.println("PDF exporté vers : " + file.getAbsolutePath());
            System.out.println(pdfContent);
        }
    }
}