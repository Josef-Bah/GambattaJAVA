package gambatta.tn.mains;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFx extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/activites/ActiviteFront.fxml")
        );

        Scene scene = new Scene(loader.load(), 1400, 800);

        scene.getStylesheets().add(
                getClass().getResource("/activites/style.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.setTitle("Gambatta");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}