package gambatta.tn.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
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
    @FXML private Button btnAnalyseIA;
    @FXML private TextArea txtAIAnalysis;

    private Map<String, Long> currentData;

    @FXML
    public void initialize() {
        // Any default initialization if needed
    }

    public void setData(String title, Map<String, Long> dataMap, String statTitle1, String statTitle2) {
        this.currentData = dataMap;
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
    private void handleAnalyseIA() {
        if (currentData == null || currentData.isEmpty()) {
            txtAIAnalysis.setText("Aucune donnée disponible pour l'analyse.");
            return;
        }

        txtAIAnalysis.setText("Analyse en cours... Veuillez patienter ✨");
        btnAnalyseIA.setDisable(true);

        String prompt = "En tant qu'expert en gestion de tournois sportifs pour l'application Gambatta, " +
                "analyse les statistiques d'inscription suivantes et donne des conseils stratégiques (max 3 phrases) : " +
                currentData.toString();

        gambatta.tn.services.tournoi.GeminiService.getCompletion(prompt).thenAccept(response -> {
            javafx.application.Platform.runLater(() -> {
                txtAIAnalysis.setText(response);
                btnAnalyseIA.setDisable(false);
            });
        }).exceptionally(ex -> {
            javafx.application.Platform.runLater(() -> {
                txtAIAnalysis.setText("Erreur : " + ex.getMessage());
                btnAnalyseIA.setDisable(false);
            });
            return null;
        });
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) lblTitle.getScene().getWindow();
        stage.close();
    }
}
