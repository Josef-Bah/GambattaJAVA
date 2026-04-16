package gambatta.tn.mains;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Chemin basé sur ta structure resources/gambatta.tn.ui/
            URL fxmlLocation = getClass().getResource("/gambatta.tn.ui/reclamation/portal.fxml");

            if (fxmlLocation == null) {
                System.err.println("❌ Fichier FXML introuvable !");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setTitle("Gambatta - Dashboard");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}