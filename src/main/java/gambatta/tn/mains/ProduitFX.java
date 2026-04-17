package gambatta.tn.mains;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ProduitFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/buvette/BuvetteMainView.fxml"));
        Scene scene = new Scene(loader.load(), 1100, 650);
        stage.setTitle("Gambatta - Buvette");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(java.lang.String[] args) {
        launch(args);
    }
}