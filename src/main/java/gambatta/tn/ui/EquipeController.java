package gambatta.tn.ui;

import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.services.tournoi.EquipeService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
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

    @FXML private TextField txtSearch;

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
        btnPDF.setOnAction(e -> exportPDF());
        btnTournoi.setOnAction(e -> openTournoiWindow());
        btnNouvelleEquipe.setOnAction(e -> openInscriptionEquipeWindow());

        // Configuration des colonnes d'action
        colModifier.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Modifier");
            {
                btn.setOnAction(event -> {
                    equipe e = getTableView().getItems().get(getIndex());
                    showEditDialog(e);
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

        // Configuration des colonnes d'action
        // ... (colModifier and colSupprimer are initialized above)

        // Recherche en temps réel
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterEquipes(newVal));
    }

    @FXML
    public void showStats(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/StatsInterface.fxml"));
            AnchorPane root = loader.load();
            
            StatsController controller = loader.getController();
            
            java.util.Map<String, Long> stats = new java.util.HashMap<>();
            stats.put("EN_ATTENTE", equipes.stream().filter(e -> "EN_ATTENTE".equals(e.getStatus())).count());
            stats.put("VALIDE", equipes.stream().filter(e -> "VALIDE".equals(e.getStatus())).count());
            
            controller.setData("Statistiques des Équipes", stats, "EN_ATTENTE", "VALIDE");
            
            Stage stage = new Stage();
            stage.setTitle("Tableau de Bord des Équipes");
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les statistiques.");
        }
    }

    private void showEditDialog(equipe e) {
        TextInputDialog dialogNom = new TextInputDialog(e.getNom());
        dialogNom.setTitle("Modifier l'Équipe");
        dialogNom.setHeaderText("Modifier le nom de l'équipe");
        dialogNom.setContentText("Nom :");

        dialogNom.showAndWait().ifPresent(newNom -> {
            if (!newNom.trim().isEmpty()) {
                TextInputDialog dialogLeader = new TextInputDialog(e.getTeamLeader());
                dialogLeader.setTitle("Modifier l'Équipe");
                dialogLeader.setHeaderText("Modifier le leader de l'équipe");
                dialogLeader.setContentText("Leader :");

                dialogLeader.showAndWait().ifPresent(newLeader -> {
                    if (!newLeader.trim().isEmpty()) {
                        e.setNom(newNom);
                        e.setTeamLeader(newLeader);
                        if (equipeService.save(e)) {
                            tableEquipes.refresh();
                            showAlert(Alert.AlertType.INFORMATION, "Succès", "L'équipe a été mise à jour.");
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Erreur", "La mise à jour a échoué.");
                        }
                    }
                });
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // clearFields removed as the fields are no longer in this view

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
            stage.setMaximized(true);
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
            stage.setMaximized(true);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
