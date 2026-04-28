package gambatta.tn.ui;

import gambatta.tn.entites.tournois.tournoi;
import gambatta.tn.services.tournoi.TournoiService;
import gambatta.tn.tools.CloudinaryService;
import gambatta.tn.tools.PdfService;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TournoiController {

    // ── Drawer fields ──
    @FXML private VBox drawerPanel;
    @FXML private Label drawerTitle;
    @FXML private TextField nomField;
    @FXML private TextArea  descField;
    @FXML private ComboBox<String> cmbStatut;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private Label errNom;
    @FXML private Label errStatut;
    @FXML private Label errDateDebut;
    @FXML private Label errDateFin;
    @FXML private Label globalMsg;
    @FXML private Button btnPDF;
    @FXML private TextField logoField;
    @FXML private ImageView logoPreview;

    // ── Table ──
    @FXML private TableView<tournoi>              table;

    @FXML private TableColumn<tournoi, String>    nomCol;
    @FXML private TableColumn<tournoi, String>    dateDebutCol;
    @FXML private TableColumn<tournoi, String>    dateFinCol;
    @FXML private TableColumn<tournoi, String>    statutCol;
    @FXML private TableColumn<tournoi, Void>      colModifier;
    @FXML private TableColumn<tournoi, Void>      colSupprimer;

    private TournoiService service = new TournoiService();
    private ObservableList<tournoi> tournois = FXCollections.observableArrayList();
    private tournoi editingTournoi = null;
    private static final double DRAWER_WIDTH = 380;

    @FXML
    public void initialize() {
        cmbStatut.setItems(FXCollections.observableArrayList("EN_ATTENTE", "VALIDE", "TERMINE"));
        btnPDF.setOnAction(e -> exportPDF());

        // Listener pour l'aperçu du logo
        logoField.textProperty().addListener((obs, o, n) -> {
            if (n == null || n.isEmpty()) logoPreview.setImage(null);
            else {
                try { logoPreview.setImage(new Image(n, true)); }
                catch (Exception ex) { logoPreview.setImage(null); }
            }
        });


        nomCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNomt()));
        dateDebutCol.setCellValueFactory(d -> {
            LocalDateTime dt = d.getValue().getDatedebutt();
            return new javafx.beans.property.SimpleStringProperty(dt != null ? dt.toLocalDate().toString() : "");
        });
        dateFinCol.setCellValueFactory(d -> {
            LocalDateTime dt = d.getValue().getDatefint();
            return new javafx.beans.property.SimpleStringProperty(dt != null ? dt.toLocalDate().toString() : "");
        });
        statutCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getStatutt()));

        colModifier.setCellFactory(p -> new TableCell<>() {
            private final Button btn = new Button("✏ Modifier");
            { btn.setStyle("-fx-background-color: #1B3A5C; -fx-text-fill: #C5B358; -fx-cursor: hand; -fx-border-color: #C5B358; -fx-border-width:1; -fx-border-radius:6; -fx-background-radius:6;");
              btn.setOnAction(e -> openDrawerEdit(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(Void i, boolean empty) { super.updateItem(i, empty); setGraphic(empty ? null : btn); }
        });

        colSupprimer.setCellFactory(p -> new TableCell<>() {
            private final Button btn = new Button("🗑 Supprimer");
            { btn.setStyle("-fx-background-color: #3B0A0A; -fx-text-fill: #ff6b6b; -fx-cursor: hand; -fx-border-color: #8B1E2D; -fx-border-width:1; -fx-border-radius:6; -fx-background-radius:6;");
              btn.setOnAction(e -> deleteTournoi(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(Void i, boolean empty) { super.updateItem(i, empty); setGraphic(empty ? null : btn); }
        });

        loadData();
    }

    // ── DRAWER ──────────────────────────────────────────────

    @FXML public void openDrawerNew() {
        editingTournoi = null;
        drawerTitle.setText("➕  Nouveau Tournoi");
        clearDrawer();
        showDrawer();
    }

    private void openDrawerEdit(tournoi t) {
        editingTournoi = t;
        drawerTitle.setText("✏  Modifier — " + t.getNomt());
        nomField.setText(t.getNomt());
        descField.setText(t.getDescrit());
        cmbStatut.setValue(t.getStatutt());
        dateDebutPicker.setValue(t.getDatedebutt() != null ? t.getDatedebutt().toLocalDate() : null);
        dateFinPicker.setValue(t.getDatefint() != null ? t.getDatefint().toLocalDate() : null);
        logoField.setText(t.getLogo());
        
        if (t.getLogo() != null && !t.getLogo().isEmpty()) {
            logoPreview.setImage(new Image(t.getLogo(), true));
        } else {
            logoPreview.setImage(null);
        }

        clearErrors();
        showDrawer();
    }

    @FXML public void closeDrawer() { hideDrawer(); }

    private void showDrawer() {
        drawerPanel.setVisible(true); drawerPanel.setManaged(true);
        new Timeline(
            new KeyFrame(Duration.ZERO,       new KeyValue(drawerPanel.prefWidthProperty(), 0)),
            new KeyFrame(Duration.millis(250), new KeyValue(drawerPanel.prefWidthProperty(), DRAWER_WIDTH))
        ).play();
    }

    private void hideDrawer() {
        Timeline tl = new Timeline(
            new KeyFrame(Duration.ZERO,       new KeyValue(drawerPanel.prefWidthProperty(), DRAWER_WIDTH)),
            new KeyFrame(Duration.millis(200), new KeyValue(drawerPanel.prefWidthProperty(), 0))
        );
        tl.setOnFinished(e -> { drawerPanel.setVisible(false); drawerPanel.setManaged(false); });
        tl.play();
    }

    @FXML
    private void handleUploadLogo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner l'Affiche");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(drawerPanel.getScene().getWindow());

        if (file != null) {
            showInlineMsg("⏳ Téléchargement...", false);
            new Thread(() -> {
                try {
                    String url = new CloudinaryService().uploadImage(file);
                    Platform.runLater(() -> {
                        logoField.setText(url);
                        showInlineMsg("✅ Image hébergée !", false);
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showInlineMsg("❌ Erreur Upload", true));
                }
            }).start();
        }
    }

    // ── SAVE ────────────────────────────────────────────────

    @FXML public void addTournoi(ActionEvent ev) {
        if (!validate()) return;
        String nom    = nomField.getText().trim();
        String desc   = descField.getText().trim();
        String statut = cmbStatut.getValue();
        LocalDateTime debut = dateDebutPicker.getValue().atStartOfDay();
        LocalDateTime fin   = dateFinPicker.getValue().atTime(23, 59, 59);

        if (editingTournoi == null) {
            tournoi t = new tournoi();
            t.setNomt(nom); t.setDescrit(desc); t.setStatutt(statut);
            t.setDatedebutt(debut); t.setDatefint(fin);
            t.setLogo(logoField.getText().trim());
            if (service.add(t)) { 
                tournois.add(t); 
                showInlineMsg("✅ Succès: Tournoi créé !", false); 
                new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2)).setOnFinished(e -> closeDrawer());
            }
            else showInlineMsg("⚠ Erreur: Impossible de créer ce tournoi.", true);
        } else {
            editingTournoi.setNomt(nom); editingTournoi.setDescrit(desc); editingTournoi.setStatutt(statut);
            editingTournoi.setDatedebutt(debut); editingTournoi.setDatefint(fin);
            editingTournoi.setLogo(logoField.getText().trim());
            if (service.update(editingTournoi)) { 
                table.refresh(); 
                showInlineMsg("✅ Succès: Tournoi modifié !", false); 
                new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2)).setOnFinished(e -> closeDrawer());
            }
            else showInlineMsg("⚠ Erreur: Impossible de modifier ce tournoi.", true);
        }
    }

    // ── DELETE ──────────────────────────────────────────────

    private void deleteTournoi(tournoi t) {
        if (service.delete(t.getId())) tournois.remove(t);
    }

    // ── VALIDATION ──────────────────────────────────────────

    private boolean validate() {
        clearErrors(); boolean ok = true;
        if (nomField.getText().trim().isEmpty()) { errNom.setText("⚠ Le nom est obligatoire."); ok = false; }
        if (cmbStatut.getValue() == null)        { errStatut.setText("⚠ Choisissez un statut."); ok = false; }
        if (dateDebutPicker.getValue() == null)  { errDateDebut.setText("⚠ Date début obligatoire."); ok = false; }
        if (dateFinPicker.getValue() == null)    { errDateFin.setText("⚠ Date fin obligatoire."); ok = false; }
        if (ok && dateDebutPicker.getValue().isAfter(dateFinPicker.getValue()))
            { errDateFin.setText("⚠ Date fin doit être après date début."); ok = false; }
        return ok;
    }

    private void clearErrors() {
        errNom.setText(""); errStatut.setText(""); errDateDebut.setText(""); errDateFin.setText("");
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

    private void clearDrawer() {
        nomField.clear(); descField.clear(); cmbStatut.getSelectionModel().clearSelection();
        dateDebutPicker.setValue(null); dateFinPicker.setValue(null); 
        logoField.clear(); logoPreview.setImage(null);
        clearErrors();
    }

    // ── DATA ────────────────────────────────────────────────

    private void loadData() { tournois.setAll(service.findAll()); table.setItems(tournois); }

    private void exportPDF() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Exporter Liste des Tournois");
        fc.setInitialFileName("Liste_Tournois.pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File file = fc.showSaveDialog(table.getScene().getWindow());
        if (file != null) {
            try {
                // On va créer une nouvelle méthode spécifique dans PdfService pour la liste
                generateTournamentListPdf(file.getAbsolutePath());
                showInlineMsg("✅ Liste exportée avec succès !", false);
            } catch (Exception e) {
                e.printStackTrace();
                showInlineMsg("❌ Erreur lors de l'export PDF.", true);
            }
        }
    }

    private void generateTournamentListPdf(String path) throws Exception {
        // Optionnel : Je peux aussi ajouter une méthode directe dans PdfService
        // Pour gagner du temps, j'utilise la logique iText ici ou j'appelle une version étendue de PdfService
        // Mais pour rester propre, je vais ajouter 'generateTournamentList' dans PdfService.java
        new PdfService().generateTournamentList(tournois, path);
    }

    // ── NAVIGATION ──────────────────────────────────────────

    @FXML public void goBack() { ((Stage) table.getScene().getWindow()).close(); }

    @FXML public void showStats(ActionEvent ev) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/StatsInterface.fxml"));
            AnchorPane root = loader.load();
            StatsController ctrl = loader.getController();
            java.util.Map<String, Long> stats = new java.util.HashMap<>();
            stats.put("EN_ATTENTE", tournois.stream().filter(t -> "EN_ATTENTE".equals(t.getStatutt())).count());
            stats.put("VALIDE",     tournois.stream().filter(t -> "VALIDE".equals(t.getStatutt())).count());
            stats.put("TERMINE",    tournois.stream().filter(t -> "TERMINE".equals(t.getStatutt())).count());
            ctrl.setData("Stats Tournois", stats, "EN_ATTENTE", "VALIDE");
            Stage s = new Stage(); s.setTitle("Statistiques");
            Scene sc = new Scene(root); sc.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            s.setScene(sc); s.setMaximized(true); s.show();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    @FXML public void openEquipeWindow(ActionEvent ev) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/EquipeInterface.fxml"));
            Scene sc = new Scene(loader.load()); sc.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            Stage s = new Stage(); s.setTitle("Gestion des Équipes"); s.setScene(sc); s.setMaximized(true); s.show();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void showAlert(String title, String msg) {
        // Obsolete pop-up - removed for inline messages.
    }

    // ── Legacy stubs kept for compatibility ──────────────────
    @FXML public void handleNewTournoi(ActionEvent e) { openDrawerNew(); }
}
