package gambatta.tn.controllers;

import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.services.tournoi.EquipeService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class EquipeController {

    @FXML private TableView<equipe> tableEquipes;
    @FXML private TableColumn<equipe, Long> colId;
    @FXML private TableColumn<equipe, String> colNom;
    @FXML private TableColumn<equipe, String> colLeader;
    @FXML private TableColumn<equipe, String> colStatus;
    @FXML private TableColumn<equipe, Void> colModifier;
    @FXML private TableColumn<equipe, Void> colSupprimer;

    @FXML private TextField txtNom;
    @FXML private TextField txtLeader;
    @FXML private TextField txtSearch;

    @FXML private Button btnAjouter;
    @FXML private Button btnPDF;
    @FXML private Button btnTournoi;
    @FXML private Button btnNouvelleEquipe; // ✅ bouton pour InscriptionEquipeInterface

    private EquipeService equipeService;
    private ObservableList<equipe> equipes;

    @FXML
    public void initialize() {
        equipeService = new EquipeService();

        // Configuration des colonnes
        colId.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getId()));
        colNom.setCellValueFactory(cell -> cell.getValue().nomProperty());
        colLeader.setCellValueFactory(cell -> cell.getValue().teamLeaderProperty());
        colStatus.setCellValueFactory(cell -> cell.getValue().statusProperty());

        // Charger les données
        equipes = FXCollections.observableArrayList(equipeService.findAll());
        tableEquipes.setItems(equipes);

        // Actions des boutons
        btnAjouter.setOnAction(e -> addEquipe());
        btnPDF.setOnAction(e -> exportPDF());
        btnTournoi.setOnAction(e -> openTournoiWindow());
        btnNouvelleEquipe.setOnAction(e -> openInscriptionEquipeWindow());

        // Configuration des colonnes d'action
        colModifier.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Modifier");
            {
                btn.setOnAction(event -> {
                    equipe e = getTableView().getItems().get(getIndex());
                    txtNom.setText(e.getNom());
                    txtLeader.setText(e.getTeamLeader());
                    tableEquipes.getSelectionModel().select(e);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        colSupprimer.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Supprimer");
            {
                btn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
                btn.setOnAction(event -> {
                    equipe e = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer l'équipe " + e.getNom() + " ?", ButtonType.YES, ButtonType.NO);
                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            if (equipeService.delete(e.getId())) {
                                equipes.remove(e);
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
        tableEquipes.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtNom.setText(newSel.getNom());
                txtLeader.setText(newSel.getTeamLeader());
            }
        });

        // Recherche en temps réel
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterEquipes(newVal));
    }

    @FXML
    public void showStats(ActionEvent event) {
        long total = equipes.size();
        long enAttente = equipes.stream().filter(e -> "EN_ATTENTE".equals(e.getStatus())).count();
        long valide = equipes.stream().filter(e -> "VALIDE".equals(e.getStatus())).count();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Statistiques des Équipes");
        alert.setHeaderText("Résumé des participations");
        alert.setContentText("Total d'équipes : " + total + "\n" +
                             "En attente : " + enAttente + "\n" +
                             "Validées : " + valide);
        alert.showAndWait();
    }

    private void addEquipe() {
        String nom = txtNom.getText().trim();
        String leader = txtLeader.getText().trim();

        if (nom.isEmpty() || leader.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs requis", "Veuillez remplir les champs Nom et Capitaine.");
            return;
        }

        equipe selected = tableEquipes.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Modification
            selected.setNom(nom);
            selected.setTeamLeader(leader);
            if (equipeService.save(selected)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "L'équipe a été modifiée avec succès.");
                tableEquipes.refresh();
                clearFields();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier l'équipe. Le nom est peut-être déjà pris.");
            }
        } else {
            // Ajout
            equipe e = new equipe();
            e.setNom(nom);
            e.setTeamLeader(leader);
            e.setStatus("EN_ATTENTE");
            if (equipeService.save(e)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "L'équipe a été ajoutée avec succès.");
                equipes.add(e);
                clearFields();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter l'équipe. Le nom est peut-être déjà pris.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        txtNom.clear();
        txtLeader.clear();
        tableEquipes.getSelectionModel().clearSelection();
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

    private void openTournoiWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/tournoi.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 780);
            scene.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            Stage stage = new Stage();
            stage.setTitle("Interface Tournoi");
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openInscriptionEquipeWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/InscriptionEquipeInterface.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            Stage stage = new Stage();
            stage.setTitle("Inscription Nouvelle Équipe");
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}