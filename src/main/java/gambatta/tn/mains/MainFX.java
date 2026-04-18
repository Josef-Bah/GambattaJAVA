package gambatta.tn.mains;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        String styleAdmin = "-fx-background-color: #0a192f; -fx-text-fill: #C5B358; -fx-font-weight:bold; -fx-border-color: #C5B358; -fx-border-width: 2px; -fx-padding: 12 20;";
        String styleGold  = "-fx-background-color: #C5B358; -fx-text-fill: #010203; -fx-font-weight:bold; -fx-padding: 12 20;";
        String styleDark  = "-fx-background-color: #0a192f; -fx-text-fill: white; -fx-font-weight:bold; -fx-padding: 12 20;";
        String styleLabel = "-fx-text-fill: #8892b0; -fx-font-size: 13px; -fx-padding: 10 0 4 0;";

        // ─── SECTION JOUEUR ───
        Label lblFront = new Label("\u2500\u2500\u2500 ESPACE JOUEUR \u2500\u2500\u2500");
        lblFront.setStyle(styleLabel);

        Button btnEquipe       = new Button("\ud83d\udc65 Gestion des \u00c9quipes");
        Button btnTournoi      = new Button("\ud83c\udfc6 Gestion des Tournois");
        Button btnInscriptionT = new Button("\ud83d\udcdd Inscription \u00e0 un Tournoi");
        Button btnInscriptionE = new Button("\u2795 Cr\u00e9er / Rejoindre une \u00c9quipe");
        Button btnRecrutement  = new Button("\ud83d\udc64 Recrutement \u2014 Demandes Joueurs");

        for (Button b : new Button[]{btnEquipe, btnTournoi, btnInscriptionT, btnInscriptionE, btnRecrutement}) {
            b.setPrefWidth(340);
            b.setStyle(styleGold);
        }

        // ─── SECTION ADMIN / LIVE ───
        Label lblAdmin = new Label("\u2500\u2500\u2500 ESPACE ADMIN & LIVE \u2500\u2500\u2500");
        lblAdmin.setStyle(styleLabel);

        Button btnAdmin      = new Button("\ud83d\udee1\ufe0f Espace Administrateur");
        Button btnFlashscore = new Button("\u26a1 Flashscore Live");

        btnAdmin.setPrefWidth(340);
        btnFlashscore.setPrefWidth(340);
        btnAdmin.setStyle(styleAdmin);
        btnFlashscore.setStyle(styleDark);

        VBox root = new VBox(10,
                lblFront,
                btnEquipe, btnTournoi, btnInscriptionT, btnInscriptionE, btnRecrutement,
                new Separator(),
                lblAdmin,
                btnAdmin, btnFlashscore
        );
        root.setStyle("-fx-padding: 50; -fx-alignment: center; -fx-background-color: #010203;");

        Scene scene = new Scene(root, 460, 600);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Dashboard Gambatta");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

        // Actions
        btnEquipe.setOnAction(e       -> openWindow("/gambatta.tn.ui/EquipeInterface.fxml",             "Gestion des \u00c9quipes"));
        btnTournoi.setOnAction(e      -> openWindow("/gambatta.tn.ui/tournoi.fxml",                     "Gestion des Tournois"));
        btnInscriptionT.setOnAction(e -> openWindow("/gambatta.tn.ui/InscriptionTournoiInterface.fxml", "Inscription Tournoi"));
        btnInscriptionE.setOnAction(e -> openWindow("/gambatta.tn.ui/InscriptionEquipeInterface.fxml",  "Inscription \u00c9quipe"));
        btnRecrutement.setOnAction(e  -> openWindow("/gambatta.tn.ui/TeamLeaderInterface.fxml",         "Recrutement \u00c9quipes"));
        btnAdmin.setOnAction(e        -> openWindow("/gambatta.tn.ui/AdminDashboard.fxml",               "Espace Administrateur"));
        btnFlashscore.setOnAction(e   -> openWindow("/gambatta.tn.ui/FlashscoreInterface.fxml",         "Flashscore Live"));
    }

    private void openWindow(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Scene scene = new Scene(loader.load(), 1280, 800);
            scene.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            showErrorAlert("Erreur lors de l'ouverture de : " + title + "\n" + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        launch();
    }

    private void showErrorAlert(String message) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir l'interface");
            alert.setContentText(message + "\n\nAssurez-vous que votre serveur MySQL (ex: XAMPP/WAMP) est d\u00e9marr\u00e9 et que la base 'gambatta_db' existe bien.");
            alert.showAndWait();
        });
    }
}