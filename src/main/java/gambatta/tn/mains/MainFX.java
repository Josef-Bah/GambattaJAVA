package gambatta.tn.mains;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/Login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);

        stage.setTitle("Gambatta");
        stage.setScene(scene);

        // taille de départ
        stage.setWidth(1320);
        stage.setHeight(820);

        // taille minimale
        stage.setMinWidth(1100);
        stage.setMinHeight(720);

        // autoriser le redimensionnement
        stage.setResizable(true);

        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}