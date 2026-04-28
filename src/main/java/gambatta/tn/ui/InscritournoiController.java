package gambatta.tn.ui;

import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.entites.tournois.inscriptiontournoi;
import gambatta.tn.entites.tournois.tournoi;
import gambatta.tn.services.tournoi.EquipeService;
import gambatta.tn.services.tournoi.InscritournoiService;
import gambatta.tn.services.tournoi.TournoiService;
import gambatta.tn.services.tournoi.CloudinaryService;
import gambatta.tn.services.tournoi.PdfService;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.List;

public class InscritournoiController {

    // ── Drawer fields ──
    @FXML
    private VBox drawerPanel;
    @FXML
    private ComboBox<equipe> comboEquipe;
    @FXML
    private ComboBox<tournoi> comboTournoi;
    @FXML
    private Label errEquipe;
    @FXML
    private Label errTournoi;
    @FXML
    private Label globalMsg;

    // ── Table ──
    @FXML
    private TableView<inscriptiontournoi> tableInscriptions;

    @FXML
    private TableColumn<inscriptiontournoi, String> colEquipe;
    @FXML
    private TableColumn<inscriptiontournoi, String> colTournoi;
    @FXML
    private TableColumn<inscriptiontournoi, String> colStatus;
    @FXML
    private TableColumn<inscriptiontournoi, Void> colSupprimer;

    @FXML
    private TextField txtSearch;
    @FXML
    private Button btnPDF;
    @FXML
    private Button btnStats;

    private InscritournoiService service = new InscritournoiService();
    private EquipeService equipeService = new EquipeService();
    private TournoiService tournoiService = new TournoiService();
    private ObservableList<inscriptiontournoi> inscriptions = FXCollections.observableArrayList();
    private static final double DRAWER_WIDTH = 340;

    @FXML
    public void initialize() {
        // Peupler les combos
        comboEquipe.setItems(FXCollections.observableArrayList(equipeService.findAll()));
        comboTournoi.setItems(FXCollections.observableArrayList(tournoiService.findAll()));

        // Colonnes

        colEquipe.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEquipe().getNom()));
        colTournoi.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTournoi().getNomt()));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));

        // Bouton Supprimer dans la table
        colSupprimer.setCellFactory(p -> new TableCell<>() {
            private final Button btn = new Button("🗑 Retirer");
            {
                btn.setStyle(
                        "-fx-background-color: #3B0A0A; -fx-text-fill: #ff6b6b; -fx-cursor: hand; -fx-border-color: #8B1E2D; -fx-border-width:1; -fx-border-radius:6; -fx-background-radius:6;");
                btn.setOnAction(e -> deleteInscription(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void i, boolean empty) {
                super.updateItem(i, empty);
                setGraphic(empty ? null : btn);
            }
        });

        // Boutons header
        btnPDF.setOnAction(e -> exportPDF());
        btnStats.setOnAction(e -> showStats());

        // Search
        txtSearch.textProperty().addListener((obs, o, n) -> filterInscriptions(n));

        loadData();
    }

    // ── DRAWER ──────────────────────────────────────────────

    @FXML
    public void openDrawer() {
        comboEquipe.getSelectionModel().clearSelection();
        comboTournoi.getSelectionModel().clearSelection();
        clearErrors();
        showDrawer();
    }

    @FXML
    public void closeDrawer() {
        hideDrawer();
    }

    private void showDrawer() {
        drawerPanel.setVisible(true);
        drawerPanel.setManaged(true);
        new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(drawerPanel.prefWidthProperty(), 0)),
                new KeyFrame(Duration.millis(250), new KeyValue(drawerPanel.prefWidthProperty(), DRAWER_WIDTH))).play();
    }

    private void hideDrawer() {
        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(drawerPanel.prefWidthProperty(), DRAWER_WIDTH)),
                new KeyFrame(Duration.millis(200), new KeyValue(drawerPanel.prefWidthProperty(), 0)));
        tl.setOnFinished(e -> {
            drawerPanel.setVisible(false);
            drawerPanel.setManaged(false);
        });
        tl.play();
    }

    // ── SAVE ────────────────────────────────────────────────

    @FXML
    public void addInscription() {
        clearErrors();
        boolean ok = true;
        equipe eq = comboEquipe.getSelectionModel().getSelectedItem();
        tournoi t = comboTournoi.getSelectionModel().getSelectedItem();
        if (eq == null) {
            errEquipe.setText("⚠ Veuillez choisir une équipe.");
            ok = false;
        }
        if (t == null) {
            errTournoi.setText("⚠ Veuillez choisir un tournoi.");
            ok = false;
        }
        if (!ok)
            return;

        inscriptiontournoi i = new inscriptiontournoi();
        i.setEquipe(eq);
        i.setTournoi(t);
        i.setStatus(inscriptiontournoi.STATUS_PENDING);

        if (service.save(i)) {
            inscriptions.add(i);
            showInlineMsg("✅ Succès: " + eq.getNom() + " inscrite !", false);
            new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2)).setOnFinished(e -> closeDrawer());
        } else {
            showInlineMsg("⚠ Cette équipe est déjà inscrite à ce tournoi.", true);
        }
    }

    // ── DELETE ──────────────────────────────────────────────

    private void deleteInscription(inscriptiontournoi i) {
        if (service.delete(i.getId()))
            inscriptions.remove(i);
    }

    private void clearErrors() {
        errEquipe.setText("");
        errTournoi.setText("");
        if (globalMsg != null) {
            globalMsg.getStyleClass().removeAll("msg-success", "msg-error");
            globalMsg.setText("");
        }
    }

    private void showInlineMsg(String msg, boolean isError) {
        if (globalMsg != null) {
            globalMsg.setText(msg);
            globalMsg.getStyleClass().removeAll("msg-success", "msg-error");
            globalMsg.getStyleClass().add(isError ? "msg-error" : "msg-success");
            if (!isError) {
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                        javafx.util.Duration.seconds(3));
                pause.setOnFinished(e -> globalMsg.setText(""));
                pause.play();
            }
        }
    }

    // ── DATA ────────────────────────────────────────────────

    private void loadData() {
        inscriptions.setAll(service.findAll());
        tableInscriptions.setItems(inscriptions);
    }

    private void filterInscriptions(String q) {
        if (q == null || q.isEmpty()) {
            tableInscriptions.setItems(inscriptions);
            return;
        }
        ObservableList<inscriptiontournoi> f = FXCollections.observableArrayList();
        for (inscriptiontournoi i : inscriptions) {
            if (i.getEquipe().getNom().toLowerCase().contains(q.toLowerCase())
                    || i.getTournoi().getNomt().toLowerCase().contains(q.toLowerCase()))
                f.add(i);
        }
        tableInscriptions.setItems(f);
    }

    private void exportPDF() {
        inscriptiontournoi selected = tableInscriptions.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInlineMsg("⚠ Veuillez sélectionner une inscription.", true);
            return;
        }

        try {
            PdfService pdfService = new PdfService();
            CloudinaryService cloudinary = new CloudinaryService();

            showInlineMsg("⏳ Génération du ticket cloud...", false);
            File tempFile = pdfService.generateTicketFile(selected);
            String url = cloudinary.uploadImage(tempFile);

            if (url != null) {
                showInlineMsg("✅ Ticket disponible en ligne !", false);
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showInlineMsg("❌ Erreur lors de l'export cloud.", true);
        }
    }

    // ── NAVIGATION ──────────────────────────────────────────

    @FXML
    public void goBack() {
        ((Stage) tableInscriptions.getScene().getWindow()).close();
    }

    private void showStats() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/StatsInterface.fxml"));
            AnchorPane root = loader.load();
            StatsController ctrl = loader.getController();
            java.util.Map<String, Long> stats = new java.util.HashMap<>();
            stats.put(inscriptiontournoi.STATUS_ACCEPTED, inscriptions.stream()
                    .filter(i -> i.getStatus().equals(inscriptiontournoi.STATUS_ACCEPTED)).count());
            stats.put(inscriptiontournoi.STATUS_PENDING,
                    inscriptions.stream().filter(i -> i.getStatus().equals(inscriptiontournoi.STATUS_PENDING)).count());
            ctrl.setData("Stats Inscriptions", stats, inscriptiontournoi.STATUS_PENDING,
                    inscriptiontournoi.STATUS_ACCEPTED);
            Stage s = new Stage();
            s.setTitle("Statistiques Inscriptions");
            Scene sc = new Scene(root);
            sc.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            s.setScene(sc);
            s.setMaximized(true);
            s.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleOpenChatbot() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/ChatbotInterface.fxml"));
            VBox root = loader.load();
            Stage s = new Stage();
            s.setTitle("Assistant IA");
            s.setScene(new Scene(root));
            s.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showAlert(String title, String msg) {
        // Obsolete pop-up - removed for inline messages.
    }
}
