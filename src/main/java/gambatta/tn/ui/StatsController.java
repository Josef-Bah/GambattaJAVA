package gambatta.tn.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.Map;

public class StatsController {

    @FXML private Label lblTitle;
    @FXML private Label lblTotal;
    @FXML private Label lblStatTitle1;
    @FXML private Label lblStatVal1;
    @FXML private Label lblStatTitle2;
    @FXML private Label lblStatVal2;
    @FXML private PieChart pieChart;

    @FXML
    public void initialize() {
        // Any default initialization if needed
    }

    public void setData(String title, Map<String, Long> dataMap, String statTitle1, String statTitle2) {
        lblTitle.setText(title);
        
        long total = dataMap.values().stream().mapToLong(Long::longValue).sum();
        lblTotal.setText(String.valueOf(total));

        // Get values for specific cards
        long val1 = dataMap.getOrDefault(statTitle1, 0L);
        long val2 = dataMap.getOrDefault(statTitle2, 0L);

        lblStatTitle1.setText(statTitle1);
        lblStatVal1.setText(String.valueOf(val1));
        lblStatTitle2.setText(statTitle2);
        lblStatVal2.setText(String.valueOf(val2));

        // Populate PieChart
        ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList();
        dataMap.forEach((key, value) -> {
            if (value > 0) {
                chartData.add(new PieChart.Data(key + " (" + value + ")", value));
            }
        });

        pieChart.setData(chartData);
        pieChart.setAnimated(true);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) lblTitle.getScene().getWindow();
        stage.close();
    }
}
