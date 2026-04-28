package gambatta.tn.ui.buvette;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import gambatta.tn.entites.buvette.produit;
import gambatta.tn.entites.buvette.vente;
import gambatta.tn.services.buvette.ProduitService;
import gambatta.tn.services.buvette.VenteService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatsController {

    @FXML private Label titleLabel;
    @FXML private Label pieTitle;
    @FXML private Label barTitle;
    @FXML private Label totalSalesLabel;
    @FXML private Label lowStockLabel;
    @FXML private PieChart pieChart;
    @FXML private BarChart<String, Number> barChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    private BuvetteMainController mainController;
    private final ProduitService ps = new ProduitService();
    private final VenteService vs = new VenteService();

    public void setMainController(BuvetteMainController mainController) {
        this.mainController = mainController;
    }

    public void refreshData(String type) {
        List<produit> produits = ps.getAll();
        List<vente> ventes = vs.getAll();

        updateKPIs(produits, ventes);

        if ("PRODUITS".equals(type)) {
            showProduitStats(produits);
        } else {
            showVenteStats(ventes);
        }
    }

    private void updateKPIs(List<produit> produits, List<vente> ventes) {
        double totalRevenue = ventes.stream().mapToDouble(vente::getMontantv).sum();
        totalSalesLabel.setText(String.format("%.2f TND", totalRevenue));

        // Show Total products instead of Stock Critique
        lowStockLabel.setText(String.valueOf(produits.size()));
    }

    private void showProduitStats(List<produit> produits) {
        pieTitle.setText("Répartition du Stock par Catégorie");
        barTitle.setText("Top 10 : Comparaison des Prix");

        // Pie Chart: Category breakdown
        Map<String, Double> stockByCategory = produits.stream()
            .collect(Collectors.groupingBy(
                (produit p) -> (p.getDescrip() == null || p.getDescrip().isEmpty()) ? "Autres" : p.getDescrip(),
                Collectors.summingDouble((produit p) -> (double) p.getStockp())
            ));

        pieChart.getData().clear();
        stockByCategory.forEach((desc, stock) -> {
            if (stock > 0) pieChart.getData().add(new PieChart.Data(desc, stock));
        });

        // Bar Chart: Top 10 prices
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Prix (TND)");
        produits.stream()
            .sorted((p1, p2) -> Double.compare(p2.getPrixp(), p1.getPrixp()))
            .limit(10)
            .forEach(p -> series.getData().add(new XYChart.Data<>(p.getNomp(), p.getPrixp())));

        barChart.getData().clear();
        barChart.getData().add(series);
        xAxis.setLabel("Produits");
        yAxis.setLabel("Prix (TND)");
    }

    private void showVenteStats(List<vente> ventes) {
        pieTitle.setText("Volume de Ventes par Client");
        barTitle.setText("Historique des Recettes (15 dernières)");

        // Pie Chart: Top customers or recent volumes
        pieChart.getData().clear();
        for (int i = 0; i < Math.min(ventes.size(), 8); i++) {
            vente v = ventes.get(i);
            pieChart.getData().add(new PieChart.Data("Vente #" + v.getId(), v.getQuantv()));
        }

        // Bar Chart: Recent revenue
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Montant (TND)");
        for (int i = 0; i < Math.min(ventes.size(), 15); i++) {
            vente v = ventes.get(i);
            series.getData().add(new XYChart.Data<>("V#" + v.getId(), v.getMontantv()));
        }
        barChart.getData().clear();
        barChart.getData().add(series);
        xAxis.setLabel("Transactions");
        yAxis.setLabel("Montant (TND)");
    }

    @FXML
    public void close() {
        if (mainController != null) mainController.toggleStats();
    }
}
