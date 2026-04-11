package gambatta.tn.mains;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // Dashboard avec trois boutons
        Button btnTournoi = new Button("Ouvrir interface Tournoi");
        Button btnEquipe = new Button("Ouvrir interface Equipe");
        Button btnInscription = new Button("Ouvrir interface Inscription");

        btnTournoi.setPrefWidth(200);
        btnEquipe.setPrefWidth(200);
        btnInscription.setPrefWidth(200);

        String style = "-fx-background-color: #C5B358; -fx-text-fill: #010203; -fx-font-weight:bold;";
        btnTournoi.setStyle(style);
        btnEquipe.setStyle(style);
        btnInscription.setStyle(style);

        VBox root = new VBox(20, btnTournoi, btnEquipe, btnInscription);
        root.setStyle("-fx-padding: 50; -fx-alignment: center; -fx-background-color: #FFFFFF;");

        Scene scene = new Scene(root, 400, 350);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Dashboard Gambatta");
        stage.setScene(scene);
        stage.show();

        // Actions boutons
        btnTournoi.setOnAction(e -> openTournoiWindow());
        btnEquipe.setOnAction(e -> openEquipeWindow());
        btnInscription.setOnAction(e -> openInscriptionWindow());
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

    private void openEquipeWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/EquipeInterface.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 780);
            scene.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            Stage stage = new Stage();
            stage.setTitle("Interface Equipe");
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openInscriptionWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/InscriptionTournoiInterface.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            Stage stage = new Stage();
            stage.setTitle("Interface Inscription Tournoi");
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}