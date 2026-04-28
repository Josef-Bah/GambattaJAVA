package gambatta.tn.ui.buvette;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import gambatta.tn.entites.buvette.produit;
import gambatta.tn.entites.buvette.vente;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatsController {

    @FXML private Label titleLabel;
    @FXML private Label pieTitle;
    @FXML private Label barTitle;
    @FXML private PieChart pieChart;
    @FXML private BarChart<String, Number> barChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    public void initProduitData(List<produit> produits) {
        titleLabel.setText("Statistiques des Produits");
        pieTitle.setText("Répartition du Stock par Catégorie (Description)");
        barTitle.setText("Prix des Produits (Comparaison)");

        pieChart.setAnimated(true);
        barChart.setAnimated(true);

        // Pie Chart: Group by description (descrip)
        Map<String, Double> stockByCategory = produits.stream()
            .collect(Collectors.groupingBy(
                (produit p) -> (p.getDescrip() == null || p.getDescrip().isEmpty()) ? "Sans Catégorie" : p.getDescrip(),
                Collectors.summingDouble((produit p) -> (double) p.getStockp())
            ));

        pieChart.getData().clear();
        stockByCategory.forEach((desc, stock) -> {
            if (stock > 0) {
                pieChart.getData().add(new PieChart.Data(desc, stock));
            }
        });

        // Bar Chart: Prices
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Prix (DT)");
        for (produit p : produits) {
            series.getData().add(new XYChart.Data<>(p.getNomp(), p.getPrixp()));
        }
        barChart.getData().clear();
        barChart.getData().add(series);
        xAxis.setLabel("Produits");
        yAxis.setLabel("Prix (DT)");
    }

    public void initVenteData(List<vente> ventes) {
        titleLabel.setText("Statistiques des Ventes");
        pieTitle.setText("Volume de Vente par Transaction");
        barTitle.setText("Évolution du Chiffre d'Affaires");

        pieChart.setAnimated(true);
        barChart.setAnimated(true);

        // Pie Chart: Quantities per sale
        pieChart.getData().clear();
        for (int i = 0; i < Math.min(ventes.size(), 10); i++) {
            vente v = ventes.get(i);
            pieChart.getData().add(new PieChart.Data("Vente #" + v.getId(), v.getQuantv()));
        }

        // Bar Chart: Revenue over time (simplified as transaction amounts)
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Montant (DT)");
        for (int i = 0; i < Math.min(ventes.size(), 15); i++) {
            vente v = ventes.get(i);
            series.getData().add(new XYChart.Data<>("V#" + v.getId(), v.getMontantv()));
        }
        barChart.getData().clear();
        barChart.getData().add(series);
        xAxis.setLabel("Transactions");
        yAxis.setLabel("Montant (DT)");
    }

    @FXML
    public void close() {
        ((Stage) titleLabel.getScene().getWindow()).close();
    }
}
