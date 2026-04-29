package gambatta.tn.ui;

import gambatta.tn.entites.user.user;
import gambatta.tn.services.user.AvatarService;
import gambatta.tn.tools.Session;
import gambatta.tn.tools.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

public class HomeUserController {

    @FXML private Label welcomeLabel;
    @FXML private ImageView homeAvatarView;

    private ChatbotPanelController chatbotController;

    @FXML
    public void initialize() {
        user currentUser = Session.getCurrentUser();
        if (currentUser != null) {
            String firstName = currentUser.getFirstName() == null ? "" : currentUser.getFirstName().trim();
            welcomeLabel.setText("Bienvenue " + firstName + " dans votre espace Gambatta.");
        } else {
            welcomeLabel.setText("Bienvenue dans votre espace Gambatta.");
        }

        if (homeAvatarView != null && currentUser != null) {
            String seed = (currentUser.getProfileImage() != null && !currentUser.getProfileImage().isBlank())
                    ? currentUser.getProfileImage() : currentUser.getFirstName();
            homeAvatarView.setImage(AvatarService.loadAvatarAsync(seed));
            homeAvatarView.setClip(new Circle(33, 33, 33));
        }

        startSessionManager();
    }

    private void startSessionManager() {
        Platform.runLater(() -> {
            Scene scene = welcomeLabel.getScene();
            if (scene == null) return;

            SessionManager.start(scene, () -> {
                Session.clear();
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/gambatta.tn.ui/Login.fxml"));
                    scene.setRoot(root);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            loadChatbotPanel(scene);
        });
    }

    private void loadChatbotPanel(Scene scene) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/ChatbotPanel.fxml"));
            Parent chatPanel = loader.load();
            chatbotController = loader.getController();

            StackPane root = (StackPane) scene.getRoot();
            StackPane.setAlignment(chatPanel, Pos.CENTER_RIGHT);
            root.getChildren().add(chatPanel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void toggleChatbot() {
        if (chatbotController != null) chatbotController.toggle();
    }

    @FXML
    private void openProfile() {
        navigate("/gambatta.tn.ui/UserProfile.fxml");
    }

    @FXML
    private void logout() {
        SessionManager.stop();
        Session.clear();
        navigate("/gambatta.tn.ui/Login.fxml");
    }

    @FXML
    public void ouvrirMap() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/MapPanel.fxml"));
            Parent root = loader.load();
            MapPanelController controller = loader.getController();
            controller.setOnBack(() -> navigate("/gambatta.tn.ui/HomeUser.fxml"));
            navigate(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigate(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            navigate(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigate(Parent root) {
        welcomeLabel.getScene().setRoot(root);
    }
}
