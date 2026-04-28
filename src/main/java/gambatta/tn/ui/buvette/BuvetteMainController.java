package gambatta.tn.ui.buvette;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;

public class BuvetteMainController {

    @FXML private StackPane contentArea;
    @FXML private StackPane statsSidebar; // Now acts as a modal
    @FXML private Region statsOverlay;
    @FXML private StatsController statsViewController;

    private boolean isStatsOpen = false;

    @FXML
    public void initialize() {
        if (statsViewController != null) {
            statsViewController.setMainController(this);
        }
        showCrud();
    }

    @FXML
    public void showProduitStats() {
        if (!isStatsOpen) {
            statsViewController.refreshData("PRODUITS");
            toggleStatsAnimation(true);
        }
    }

    @FXML
    public void showVenteStats() {
        if (!isStatsOpen) {
            statsViewController.refreshData("VENTES");
            toggleStatsAnimation(true);
        }
    }

    @FXML
    public void toggleStats() {
        toggleStatsAnimation(!isStatsOpen);
    }

    private void toggleStatsAnimation(boolean open) {
        if (open) {
            statsSidebar.setVisible(true);
            statsOverlay.setVisible(true);
            
            FadeTransition ft = new FadeTransition(Duration.millis(300), statsSidebar);
            ft.setToValue(1);
            ScaleTransition st = new ScaleTransition(Duration.millis(300), statsSidebar);
            st.setToX(1); st.setToY(1);
            
            FadeTransition ftOverlay = new FadeTransition(Duration.millis(300), statsOverlay);
            ftOverlay.setToValue(1);

            new ParallelTransition(ft, st, ftOverlay).play();
            isStatsOpen = true;
        } else {
            FadeTransition ft = new FadeTransition(Duration.millis(200), statsSidebar);
            ft.setToValue(0);
            ScaleTransition st = new ScaleTransition(Duration.millis(200), statsSidebar);
            st.setToX(0.8); st.setToY(0.8);
            
            FadeTransition ftOverlay = new FadeTransition(Duration.millis(200), statsOverlay);
            ftOverlay.setToValue(0);

            ParallelTransition pt = new ParallelTransition(ft, st, ftOverlay);
            pt.setOnFinished(e -> {
                statsSidebar.setVisible(false);
                statsOverlay.setVisible(false);
            });
            pt.play();
            isStatsOpen = false;
        }
    }

    @FXML
    public void showCrud() {
        if (isStatsOpen) toggleStats();
        loadPage("/buvette/CrudProduitManagement.xml");
    }

    @FXML
    public void showVentesCrud() {
        if (isStatsOpen) toggleStats();
        loadPage("/buvette/CrudVenteManagement.fxml");
    }

    @FXML
    public void logout() {
        loadPageToRoot("/buvette/SelectionView.fxml", "Bienvenue sur Gambatta");
    }

    private void loadPageToRoot(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root, 1280, 720));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node page = loader.load();
            
            // Pass main controller to sub-controller if it supports it
            Object controller = loader.getController();
            if (controller instanceof CrudProduitController) {
                ((CrudProduitController) controller).setMainController(this);
            } else if (controller instanceof CrudVenteController) {
                ((CrudVenteController) controller).setMainController(this);
            }
            
            contentArea.getChildren().setAll(page);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
