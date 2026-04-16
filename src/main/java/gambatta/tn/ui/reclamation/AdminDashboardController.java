package gambatta.tn.ui.reclamation;

import gambatta.tn.entites.reclamation.reclamation;
import gambatta.tn.services.reclamation.ServiceReclamation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminDashboardController implements Initializable {

    @FXML private FlowPane cardsContainer;
    @FXML private TextField searchField; // Le "Scanner de flux"
    @FXML private ScrollPane cyberScroll; // Pour forcer la transparence

    private ServiceReclamation service = new ServiceReclamation();
    private List<reclamation> touteLaListe;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Rendre le ScrollPane totalement transparent par code (sécurité)
        if (cyberScroll != null) {
            cyberScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            cyberScroll.setFitToWidth(true);
        }

        // 2. Charger les données initiales
        chargerTableau();

        // 3. Ajouter l'écouteur sur la barre de recherche (SCAN_FLOW)
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filtrerDonnees(newValue);
            });
        }
    }

    @FXML
    public void chargerTableau() {
        cardsContainer.getChildren().clear();
        touteLaListe = service.afficher(); // On stocke la liste complète en mémoire

        for (reclamation r : touteLaListe) {
            VBox card = createAdminCard(r);
            cardsContainer.getChildren().add(card);
        }
    }

    private void filtrerDonnees(String query) {
        cardsContainer.getChildren().clear();
        String lowerCaseQuery = query.toLowerCase();

        List<reclamation> filtre = touteLaListe.stream()
                .filter(r -> r.getTitre().toLowerCase().contains(lowerCaseQuery) ||
                        String.valueOf(r.getIdrec()).contains(lowerCaseQuery) ||
                        (r.getStatutrec() != null && r.getStatutrec().toLowerCase().contains(lowerCaseQuery)))
                .collect(Collectors.toList());

        for (reclamation r : filtre) {
            cardsContainer.getChildren().add(createAdminCard(r));
        }
    }

    private VBox createAdminCard(reclamation r) {
        VBox card = new VBox(15);
        card.setPrefWidth(380);
        card.setPrefHeight(240);

        // --- STYLE CYBER-GLASS ---
        String baseStyle = "-fx-background-color: rgba(30, 41, 59, 0.7); " +
                "-fx-background-radius: 15; " +
                "-fx-border-color: rgba(56, 189, 248, 0.3); " +
                "-fx-border-width: 1.5; " +
                "-fx-border-radius: 15; " +
                "-fx-padding: 20;";

        card.setStyle(baseStyle);

        // --- EFFET HOVER (Néon Jaune au survol) ---
        card.setOnMouseEntered(e -> {
            card.setStyle(baseStyle + "-fx-border-color: #fcc033; -fx-effect: dropshadow(three-pass-box, rgba(252, 192, 51, 0.4), 15, 0, 0, 0);");
        });
        card.setOnMouseExited(e -> {
            card.setStyle(baseStyle);
        });

        // En-tête (ID & Badge)
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label("ID: #" + r.getIdrec());
        idLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-family: 'Consolas'; -fx-font-size: 10px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String st = (r.getStatutrec() != null) ? r.getStatutrec().toUpperCase() : "EN ATTENTE";
        Label statusBadge = new Label(st);
        String badgeColor = st.equals("EN COURS") ? "#0ea5e9" : st.equals("RÉSOLU") ? "#10b981" : "#f59e0b";
        statusBadge.setStyle("-fx-background-color: " + badgeColor + "; -fx-text-fill: white; -fx-padding: 3 10; -fx-background-radius: 5; -fx-font-size: 10px; -fx-font-weight: bold;");

        header.getChildren().addAll(idLabel, spacer, statusBadge);

        // Corps de la carte
        Label title = new Label(r.getTitre().toUpperCase());
        title.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 16px;");
        title.setWrapText(true);

        Label moduleLabel = new Label("MODULE: " + (r.getCategorierec() != null ? r.getCategorierec().toUpperCase() : "GÉNÉRAL"));
        moduleLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 10px; -fx-font-weight: bold;");

        // Actions (Boutons Néon)
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnVoir = new Button("👁 VIEW");
        styleNeonButton(btnVoir, "#38bdf8", "#020617");
        btnVoir.setOnAction(e -> handleVoir(r));

        Button btnTraiter = new Button("⚡ PROCESS");
        styleNeonButton(btnTraiter, "#fcc033", "#020617");
        btnTraiter.setOnAction(e -> handleTraiter(r));

        Button btnArchiver = new Button("🚫 ARCHIVE");
        styleNeonButton(btnArchiver, "#ef4444", "white");
        btnArchiver.setOnAction(e -> handleArchiver(r));

        actions.getChildren().addAll(btnVoir, btnTraiter, btnArchiver);

        card.getChildren().addAll(header, title, moduleLabel, new Separator(), actions);
        return card;
    }

    private void styleNeonButton(Button btn, String color, String hoverTextColor) {
        String base = "-fx-background-color: transparent; -fx-border-color: " + color + "; -fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 10px; -fx-border-radius: 5; -fx-cursor: hand; -fx-padding: 6 12;";
        String hover = "-fx-background-color: " + color + "; -fx-text-fill: " + hoverTextColor + "; -fx-effect: dropshadow(three-pass-box, " + color + ", 10, 0, 0, 0);";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(base + hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
    }

    // --- HANDLERS ---

    private void handleVoir(reclamation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/reclamation/admin_voir.fxml"));
            Parent root = loader.load();
            AdminVoirController controller = loader.getController();
            controller.initData(r);
            Stage stage = new Stage();
            stage.setTitle("SYSTEM_SCAN // #" + r.getIdrec());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleTraiter(reclamation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/reclamation/admin_traiter.fxml"));
            Parent root = loader.load();
            AdminTraiterController controller = loader.getController();
            controller.initData(r, this);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleArchiver(reclamation r) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("PURGE_CONFIRMATION");
        confirm.setHeaderText("ARCHIVER LE LOG #" + r.getIdrec() + " ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                service.archiver(r.getIdrec());
                chargerTableau();
            }
        });
    }

    @FXML
    private void handleLogout() throws Exception {
        Stage stage = (Stage) cardsContainer.getScene().getWindow();
        stage.getScene().setRoot(FXMLLoader.load(getClass().getResource("/gambatta.tn.ui/reclamation/portal.fxml")));
    }
}