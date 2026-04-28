package gambatta.tn.ui;

import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.services.tournoi.EquipeService;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

public class EquipeController {

    // ── Tableau ──
    @FXML private TableView<equipe>              tableEquipes;

    @FXML private TableColumn<equipe, String>    colLogo;
    @FXML private TableColumn<equipe, String>    colNom;
    @FXML private TableColumn<equipe, String>    colLeader;
    @FXML private TableColumn<equipe, String>    colStatus;
    @FXML private TableColumn<equipe, Void>      colModifier;
    @FXML private TableColumn<equipe, Void>      colSupprimer;
    @FXML private TextField                      txtSearch;
    @FXML private Button                         btnPDF;

    // ── Drawer (panneau glissant) ──
    @FXML private VBox      drawerPanel;
    @FXML private Label     drawerTitle;
    @FXML private TextField drawerNom;
    @FXML private Label     errNom;
    @FXML private TextField drawerLeader;
    @FXML private Label     errLeader;
    @FXML private TextField drawerCoach;
    @FXML private TextField drawerLogo;
    @FXML private Label     errLogo;
    @FXML private ComboBox<String> drawerStatut;
    @FXML private Label     errStatut;
    @FXML private Label     globalMsg;
    @FXML private TextField drawerTitres;
    @FXML private TextArea  drawerObjectifs;

    private EquipeService equipeService = new EquipeService();
    private ObservableList<equipe> equipes = FXCollections.observableArrayList();
    /** Équipe en cours de modification (null = nouvelle) */
    private equipe editingEquipe = null;
    private static final double DRAWER_WIDTH = 350;

    @FXML
    public void initialize() {
        equipeService = new EquipeService();

        drawerStatut.setItems(FXCollections.observableArrayList("EN_ATTENTE", "VALIDE", "REFUSEE"));


        colLogo.setCellValueFactory(c -> c.getValue().logoProperty());
        colLogo.setCellFactory(p -> new TableCell<>() {
            private final ImageView img = new ImageView();
            { 
                img.setFitWidth(35); img.setFitHeight(35); img.setPreserveRatio(true);
                Circle clip = new Circle(17.5, 17.5, 17.5); img.setClip(clip);
            }
            @Override protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);
                if (empty || url == null || url.isEmpty()) setGraphic(null);
                else {
                    try { img.setImage(new Image(url, true)); setGraphic(img); }
                    catch (Exception ex) { setGraphic(null); }
                }
            }
        });

        colNom.setCellValueFactory(c -> c.getValue().nomProperty());
        colLeader.setCellValueFactory(c -> c.getValue().teamLeaderProperty());
        colStatus.setCellValueFactory(c -> c.getValue().statusProperty());

        colModifier.setCellFactory(p -> new TableCell<>() {
            private final Button btn = new Button("✏ Modifier");
            { btn.setStyle("-fx-background-color: #1B3A5C; -fx-text-fill: #C5B358; -fx-cursor: hand; -fx-border-color: #C5B358; -fx-border-width:1; -fx-border-radius:6; -fx-background-radius:6;");
              btn.setOnAction(e -> openDrawerEdit(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(Void i, boolean empty) { super.updateItem(i, empty); setGraphic(empty ? null : btn); }
        });

        colSupprimer.setCellFactory(p -> new TableCell<>() {
            private final Button btn = new Button("🗑 Supprimer");
            { btn.setStyle("-fx-background-color: #3B0A0A; -fx-text-fill: #ff6b6b; -fx-cursor: hand; -fx-border-color: #8B1E2D; -fx-border-width:1; -fx-border-radius:6; -fx-background-radius:6;");
              btn.setOnAction(e -> deleteEquipe(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(Void i, boolean empty) { super.updateItem(i, empty); setGraphic(empty ? null : btn); }
        });

        btnPDF.setOnAction(e -> exportPDF());
        txtSearch.textProperty().addListener((obs, o, n) -> filterEquipes(n));

        loadData();
    }

    // ── DRAWER CONTROL ──────────────────────────────────────

    @FXML public void openDrawerNew() {
        editingEquipe = null;
        drawerTitle.setText("➕  Nouvelle Équipe");
        clearDrawerFields();
        showDrawer();
    }

    private void openDrawerEdit(equipe e) {
        editingEquipe = e;
        drawerTitle.setText("✏  Modifier — " + e.getNom());
        drawerNom.setText(e.getNom());
        drawerLeader.setText(e.getTeamLeader());
        drawerCoach.setText(e.getCoach() != null ? e.getCoach() : "");
        drawerLogo.setText(e.getLogo() != null ? e.getLogo() : "");
        drawerStatut.setValue(e.getStatus());
        drawerTitres.setText(e.getTitres() != null ? e.getTitres() : "");
        drawerObjectifs.setText(e.getObjectifs() != null ? e.getObjectifs() : "");
        clearErrors();
        showDrawer();
    }

    @FXML public void closeDrawer() { hideDrawer(); }

    private void showDrawer() {
        drawerPanel.setVisible(true);
        drawerPanel.setManaged(true);
        Timeline tl = new Timeline(
            new KeyFrame(Duration.ZERO,       new KeyValue(drawerPanel.prefWidthProperty(), 0)),
            new KeyFrame(Duration.millis(250), new KeyValue(drawerPanel.prefWidthProperty(), DRAWER_WIDTH))
        );
        tl.play();
    }

    private void hideDrawer() {
        Timeline tl = new Timeline(
            new KeyFrame(Duration.ZERO,       new KeyValue(drawerPanel.prefWidthProperty(), DRAWER_WIDTH)),
            new KeyFrame(Duration.millis(200), new KeyValue(drawerPanel.prefWidthProperty(), 0))
        );
        tl.setOnFinished(e -> { drawerPanel.setVisible(false); drawerPanel.setManaged(false); });
        tl.play();
    }

    // ── SAVE (Ajout & Modification) ──────────────────────────

    @FXML public void saveFromDrawer() {
        if (!validateDrawer()) return;

        String nom       = drawerNom.getText().trim();
        String leader    = drawerLeader.getText().trim();
        String coach     = drawerCoach.getText().trim();
        String logo      = drawerLogo.getText().trim();
        String statut    = drawerStatut.getValue();
        String titres    = drawerTitres.getText().trim();
        String objectifs = drawerObjectifs.getText().trim();

        if (editingEquipe == null) {
            // ── Création
            equipe e = new equipe();
            e.setNom(nom); e.setTeamLeader(leader); e.setCoach(coach);
            e.setLogo(logo);
            e.setStatus(statut);
            e.setTitres(titres); e.setObjectifs(objectifs);
            if (equipeService.save(e)) {
                equipes.add(e);
                showInlineMsg("✅ Succès: Équipe créée avec succès !", false);
                new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2)).setOnFinished(ev -> closeDrawer());
            } else {
                showInlineMsg("⚠ Ce nom existe déjà ou une erreur est survenue.", true);
            }
        } else {
            // ── Modification
            editingEquipe.setNom(nom); editingEquipe.setTeamLeader(leader);
            editingEquipe.setCoach(coach); editingEquipe.setLogo(logo);
            editingEquipe.setStatus(statut);
            editingEquipe.setTitres(titres); editingEquipe.setObjectifs(objectifs);
            if (equipeService.save(editingEquipe)) {
                tableEquipes.refresh();
                showInlineMsg("✅ Succès: Équipe mise à jour !", false);
                new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2)).setOnFinished(ev -> closeDrawer());
            } else {
                showInlineMsg("⚠ Impossible de modifier cette équipe.", true);
            }
        }
    }

    // ── DELETE ──────────────────────────────────────────────

    private void deleteEquipe(equipe e) {
        if (equipeService.delete(e.getId())) equipes.remove(e);
    }

    // ── VALIDATION ───────────────────────────────────────────

    private boolean validateDrawer() {
        boolean ok = true;
        clearErrors();
        if (drawerNom.getText().trim().isEmpty()) {
            errNom.setText("⚠ Le nom est obligatoire."); ok = false;
        } else if (drawerNom.getText().trim().length() < 3) {
            errNom.setText("⚠ Minimum 3 caractères."); ok = false;
        }
        if (drawerLeader.getText().trim().isEmpty()) {
            errLeader.setText("⚠ Le capitaine est obligatoire."); ok = false;
        }
        if (drawerStatut.getValue() == null) {
            errStatut.setText("⚠ Veuillez sélectionner un statut."); ok = false;
        }
        if (drawerLogo.getText().trim().isEmpty()) {
            errLogo.setText("⚠ Le logo est obligatoire."); ok = false;
        }
        return ok;
    }

    private void clearErrors() {
        errNom.setText(""); errLeader.setText(""); errStatut.setText(""); errLogo.setText("");
        if (globalMsg != null) {
            globalMsg.setText("");
            globalMsg.getStyleClass().removeAll("msg-success", "msg-error");
        }
    }

    private void showInlineMsg(String msg, boolean isError) {
        if (globalMsg != null) {
            globalMsg.setText(msg);
            globalMsg.getStyleClass().removeAll("msg-success", "msg-error");
            globalMsg.getStyleClass().add(isError ? "msg-error" : "msg-success");
            if (!isError) {
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
                pause.setOnFinished(e -> globalMsg.setText(""));
                pause.play();
            }
        }
    }

    private void clearDrawerFields() {
        drawerNom.clear(); drawerLeader.clear(); drawerCoach.clear(); drawerLogo.clear();
        drawerStatut.getSelectionModel().clearSelection();
        drawerTitres.clear(); drawerObjectifs.clear();
        clearErrors();
    }

    // ── DATA ─────────────────────────────────────────────────

    private void loadData() {
        equipes.setAll(equipeService.findAll());
        tableEquipes.setItems(equipes);
    }

    private void filterEquipes(String q) {
        if (q == null || q.isEmpty()) { tableEquipes.setItems(equipes); return; }
        ObservableList<equipe> f = FXCollections.observableArrayList();
        for (equipe e : equipes)
            if (e.getNom().toLowerCase().contains(q.toLowerCase())) f.add(e);
        tableEquipes.setItems(f);
    }

    private void exportPDF() {
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Exporter PDF"); fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF", "*.pdf"));
        java.io.File file = fc.showSaveDialog(tableEquipes.getScene().getWindow());
        if (file != null) System.out.println(equipeService.generatePdf());
    }

    // ── NAVIGATION ───────────────────────────────────────────

    @FXML public void goBack() {
        Stage stage = (Stage) tableEquipes.getScene().getWindow();
        stage.close();
    }

    @FXML public void showStats(ActionEvent ev) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/StatsInterface.fxml"));
            javafx.scene.layout.AnchorPane root = loader.load();
            StatsController ctrl = loader.getController();
            java.util.Map<String, Long> stats = new java.util.HashMap<>();
            stats.put("EN_ATTENTE", equipes.stream().filter(e -> "EN_ATTENTE".equals(e.getStatus())).count());
            stats.put("VALIDE",     equipes.stream().filter(e -> "VALIDE".equals(e.getStatus())).count());
            ctrl.setData("Stats Équipes", stats, "EN_ATTENTE", "VALIDE");
            Stage s = new Stage(); s.setTitle("Statistiques");
            Scene sc = new Scene(root); sc.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            s.setScene(sc); s.setMaximized(true); s.show();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void showAlert(Alert.AlertType t, String title, String msg) {
        // Obsolete pop-up - removed for inline messages.
    }

    private void openInscriptionEquipeWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/InscriptionEquipeInterface.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            Stage stage = new Stage(); stage.setTitle("Inscription Équipe"); stage.setScene(scene); stage.setMaximized(true); stage.show();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ── Keep old setStatutt shim for entity ──────────────────
    // equipe entity uses setStatus not setStatutt — already correct
}
