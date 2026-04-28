package gambatta.tn.ui;

import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.services.tournoi.EquipeService;
import gambatta.tn.services.tournoi.CloudinaryService;
import gambatta.tn.services.tournoi.PdfService;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;

import java.util.List;

public class EquipeController {

    @FXML private Button                         btnPDF;
    @FXML private TextField                      txtSearch;
    @FXML private FlowPane                       cardsContainer;

    // ── Drawer (panneau glissant) ──
    @FXML private VBox      drawerPanel;
    @FXML private Label     drawerTitle;
    @FXML private TextField drawerNom;
    @FXML private Label     errNom;
    @FXML private TextField drawerLeader;
    @FXML private Label     errLeader;
    @FXML private TextField drawerCoach;
    @FXML private TextField drawerLogo;
    @FXML private ImageView logoPreview;
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

        // Mise à jour de l'aperçu si l'URL change (clavier ou upload)
        drawerLogo.textProperty().addListener((obs, o, n) -> {
            if (n == null || n.isEmpty()) logoPreview.setImage(null);
            else {
                try { logoPreview.setImage(new Image(n, true)); }
                catch (Exception ex) { logoPreview.setImage(null); }
            }
        });

        btnPDF.setOnAction(e -> exportPDF());
        txtSearch.textProperty().addListener((obs, o, n) -> filterEquipes(n));

        loadData();
    }

    private void renderCards() {
        cardsContainer.getChildren().clear();
        for (equipe e : equipes) {
            cardsContainer.getChildren().add(createEquipeCard(e));
        }
    }

    private VBox createEquipeCard(equipe e) {
        VBox card = new VBox(15);
        card.getStyleClass().add("premium-card");

        // Top: ID & Status
        HBox top = new HBox();
        top.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label idLab = new Label("ID: #" + (e.getId() != 0 ? e.getId() : "??"));
        idLab.getStyleClass().add("card-id");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Label statusBadge = new Label(e.getStatus() != null ? e.getStatus() : "EN_ATTENTE");
        statusBadge.getStyleClass().addAll("status-badge", "status-" + (e.getStatus() != null ? e.getStatus().toLowerCase() : "en_attente"));
        top.getChildren().addAll(idLab, spacer, statusBadge);

        // Center: Logo & Name
        HBox center = new HBox(15);
        center.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        ImageView img = new ImageView();
        img.setFitWidth(50); img.setFitHeight(50); img.setPreserveRatio(true);
        Circle clip = new Circle(25, 25, 25); img.setClip(clip);
        if (e.getLogo() != null && !e.getLogo().isEmpty()) {
            try { img.setImage(new Image(e.getLogo(), true)); } catch (Exception ex) {}
        }
        
        VBox texts = new VBox(2);
        Label nom = new Label(e.getNom());
        nom.getStyleClass().add("card-title");
        Label leader = new Label("👤 " + (e.getTeamLeader() != null ? e.getTeamLeader() : "Inconnu"));
        leader.getStyleClass().add("card-subtitle");
        texts.getChildren().addAll(nom, leader);
        
        center.getChildren().addAll(img, texts);

        // Separator
        Region sep = new Region();
        sep.getStyleClass().add("card-separator");
        sep.setMinHeight(1);

        // Bottom: Actions
        HBox actions = new HBox(10);
        actions.setAlignment(javafx.geometry.Pos.CENTER);
        
        Button btnEdit = new Button("[MODIFIER]");
        btnEdit.getStyleClass().add("action-card-btn");
        btnEdit.setOnAction(ev -> openDrawerEdit(e));

        Button btnDel = new Button("[SUPPRIMER]");
        btnDel.getStyleClass().addAll("action-card-btn", "btn-delete-card");
        btnDel.setOnAction(ev -> deleteEquipe(e));

        actions.getChildren().addAll(btnEdit, btnDel);

        card.getChildren().addAll(top, center, sep, actions);
        return card;
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
        
        if (e.getLogo() != null && !e.getLogo().isEmpty()) {
            logoPreview.setImage(new Image(e.getLogo(), true));
        } else {
            logoPreview.setImage(null);
        }

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

    @FXML
    private void handleUploadLogo() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Sélectionner le Logo");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        java.io.File file = fileChooser.showOpenDialog(drawerPanel.getScene().getWindow());

        if (file != null) {
            showInlineMsg("⏳ Téléchargement...", false);
            new Thread(() -> {
                try {
                    String url = new CloudinaryService().uploadImage(file);
                    javafx.application.Platform.runLater(() -> {
                        drawerLogo.setText(url);
                        showInlineMsg("✅ Logo hébergé !", false);
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> showInlineMsg("❌ Erreur Upload", true));
                    e.printStackTrace();
                }
            }).start();
        }
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
                renderCards();
                showInlineMsg("✅ Succès: Équipe mise à jour !", false);
                new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2)).setOnFinished(ev -> closeDrawer());
            } else {
                showInlineMsg("⚠ Impossible de modifier cette équipe.", true);
            }
        }
    }

    // ── DELETE ──────────────────────────────────────────────

    private void deleteEquipe(equipe e) {
        if (equipeService.delete(e.getId())) {
            equipes.remove(e);
            renderCards();
        }
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
        logoPreview.setImage(null);
        clearErrors();
    }

    // ── DATA ─────────────────────────────────────────────────

    private void loadData() {
        equipes.setAll(equipeService.findAll());
        renderCards();
    }

    private void filterEquipes(String q) {
        if (q == null || q.isEmpty()) { renderCards(); return; }
        ObservableList<equipe> f = FXCollections.observableArrayList();
        for (equipe e : equipes)
            if (e.getNom().toLowerCase().contains(q.toLowerCase())) f.add(e);
        
        cardsContainer.getChildren().clear();
        for (equipe e : f) cardsContainer.getChildren().add(createEquipeCard(e));
    }

    private void exportPDF() {
        if (equipes.isEmpty()) {
            showInlineMsg("⚠ Aucune donnée à exporter.", true);
            return;
        }

        try {
            PdfService pdfService = new PdfService();
            CloudinaryService cloudinary = new CloudinaryService();

            showInlineMsg("⏳ Génération du rapport cloud...", false);
            File tempFile = pdfService.generateEquipeListFile(equipes);
            String url = cloudinary.uploadImage(tempFile);

            if (url != null) {
                showInlineMsg("✅ Liste d'équipes disponible !", false);
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showInlineMsg("❌ Erreur lors de l'export cloud.", true);
        }
    }

    // ── NAVIGATION ───────────────────────────────────────────

    @FXML public void goBack() {
        Stage stage = (Stage) cardsContainer.getScene().getWindow();
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
