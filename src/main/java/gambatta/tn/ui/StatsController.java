package gambatta.tn.ui;

import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.chart.*;
import javafx.scene.control.*;

public class StatsController {

    @FXML private PieChart chart;
    @FXML private Button btnRetour;

    @FXML
    public void initialize() {
        chart.getData().add(new PieChart.Data("Sport", 60));
        chart.getData().add(new PieChart.Data("Esport", 40));
    }

    @FXML
    void handleRetour() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/activites/ActiviteFront.fxml"));
            btnRetour.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}