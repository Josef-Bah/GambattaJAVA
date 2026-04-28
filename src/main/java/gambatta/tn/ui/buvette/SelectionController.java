package gambatta.tn.ui.buvette;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

public class SelectionController {

    @FXML
    private void handleUserAccess(ActionEvent event) {
        loadView(event, "/buvette/ShopView.fxml", "Gambatta - Shop", true);
    }

    @FXML
    private void handleAdminAccess(ActionEvent event) {
        loadView(event, "/buvette/BuvetteMainView.fxml", "Gambatta - Admin Dashboard", false);
    }

    private void loadView(ActionEvent event, String fxmlPath, String title, boolean isUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // If it's a direct view like ShopView, we wrap it or handle it
            // but ShopView is already a large VBox/StackPane
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1280, 720);
            
            // Add a "Back" button to ShopView if needed? 
            // For now, let's keep it simple as requested.
            
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
