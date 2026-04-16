package gambatta.tn.ui.buvette;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class BuvetteMainController {

    @FXML private StackPane contentArea;

    @FXML
    public void initialize() {
        showDashboard(); // default page on open
    }

    @FXML
    public void showDashboard() {
        loadPage("/buvette/DashboardBP.fxml");
    }

    @FXML
    public void showCrud() {
        loadPage("/buvette/CrudProduitManagement.xml");
    }

    @FXML
    public void showVentesCrud() {
        loadPage("/buvette/CrudVenteManagement.fxml");
    }

    @FXML
    public void showShop() {
        loadPage("/buvette/ShopView.fxml");
    }

    private void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node page = loader.load();
            contentArea.getChildren().setAll(page);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
