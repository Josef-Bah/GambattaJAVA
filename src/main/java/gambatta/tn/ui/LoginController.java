package gambatta.tn.ui;

import gambatta.tn.entites.user.user;
import gambatta.tn.services.user.UserService;
import gambatta.tn.tools.Session;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class LoginController implements Initializable {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML private Label emailValidationLabel;
    @FXML private Label passwordValidationLabel;

    @FXML private Circle bubble1;
    @FXML private Circle bubble2;
    @FXML private Circle bubble3;
    @FXML private Circle bubble4;
    @FXML private Circle bubble5;
    @FXML private Circle bubble6;
    @FXML private Circle bubble7;
    @FXML private Circle bubble8;
    @FXML private Circle bubble9;
    @FXML private Circle bubble10;
    @FXML private Circle bubble11;
    @FXML private Circle bubble12;

    private final UserService userService = new UserService();

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        errorLabel.setText("");

        animateBubble(bubble1, 0, -18, 4);
        animateBubble(bubble2, 0, 22, 5);
        animateBubble(bubble3, 0, -15, 6);
        animateBubble(bubble4, 0, 20, 4.5);
        animateBubble(bubble5, 0, -12, 5.5);

        setupValidation();
    }

    private void setupValidation() {
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validateEmailLive());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validatePasswordLive());
    }

    private void validateEmailLive() {
        String email = safeText(emailField);

        if (email.isEmpty()) {
            setFieldError(emailField, emailValidationLabel, "L'email est obligatoire.");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            setFieldError(emailField, emailValidationLabel, "Format email invalide.");
            return;
        }

        setFieldSuccess(emailField, emailValidationLabel, "Email valide.");
    }

    private void validatePasswordLive() {
        String password = safeText(passwordField);

        if (password.isEmpty()) {
            setFieldError(passwordField, passwordValidationLabel, "Le mot de passe est obligatoire.");
            return;
        }

        if (password.length() < 6) {
            setFieldError(passwordField, passwordValidationLabel, "Minimum 6 caractères.");
            return;
        }

        setFieldSuccess(passwordField, passwordValidationLabel, "Mot de passe valide.");
    }

    private boolean validateAllFields() {
        validateEmailLive();
        validatePasswordLive();

        String email = safeText(emailField);
        String password = safeText(passwordField);

        return EMAIL_PATTERN.matcher(email).matches() && password.length() >= 6;
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        errorLabel.setText("");

        if (!validateAllFields()) {
            errorLabel.setText("Veuillez corriger les champs.");
            return;
        }

        String email = safeText(emailField);
        String password = safeText(passwordField);

        try {
            user connectedUser = userService.login(email, password);

            if (connectedUser == null) {
                errorLabel.setText("Email ou mot de passe incorrect.");
                return;
            }

            // sauvegarder l'utilisateur connecté
            Session.setCurrentUser(connectedUser);

            // journaliser la connexion
            try {
                userService.logLogin(connectedUser);
            } catch (Exception ex) {
                System.out.println("Log login non enregistré : " + ex.getMessage());
            }

            System.out.println("Connexion réussie : " + connectedUser);

            // redirection selon le rôle
            if (isAdminRole(connectedUser.getRoles())) {
                loadScene(event, "/gambatta.tn.ui/AdminDashboard.fxml", "AdminDashboard.fxml introuvable.");
            } else {
                loadScene(event, "/gambatta.tn.ui/HomeUser.fxml", "HomeUser.fxml introuvable.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Erreur de connexion : " + e.getMessage());
        }
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource("/gambatta.tn.ui/Register.fxml");
            if (fxmlUrl == null) {
                errorLabel.setText("Register.fxml introuvable.");
                return;
            }

            Parent root = FXMLLoader.load(fxmlUrl);
            applyFadeIn(root);

            Scene currentScene = ((Node) event.getSource()).getScene();
            currentScene.setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Impossible d'ouvrir la page d'inscription.");
        }
    }

    private void loadScene(ActionEvent event, String fxmlPath, String errorMessage) throws IOException {
        URL fxmlUrl = getClass().getResource(fxmlPath);
        if (fxmlUrl == null) {
            errorLabel.setText(errorMessage);
            return;
        }

        Parent root = FXMLLoader.load(fxmlUrl);
        applyFadeIn(root);

        Scene currentScene = ((Node) event.getSource()).getScene();
        currentScene.setRoot(root);
    }

    private String safeText(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private void setFieldError(TextField field, Label label, String message) {
        label.setText(message);
        label.getStyleClass().removeAll("field-help-success");
        if (!label.getStyleClass().contains("field-help-error")) {
            label.getStyleClass().add("field-help-error");
        }

        field.getStyleClass().remove("field-valid");
        if (!field.getStyleClass().contains("field-invalid")) {
            field.getStyleClass().add("field-invalid");
        }
    }

    private void setFieldSuccess(TextField field, Label label, String message) {
        label.setText(message);
        label.getStyleClass().removeAll("field-help-error");
        if (!label.getStyleClass().contains("field-help-success")) {
            label.getStyleClass().add("field-help-success");
        }

        field.getStyleClass().remove("field-invalid");
        if (!field.getStyleClass().contains("field-valid")) {
            field.getStyleClass().add("field-valid");
        }
    }

    private void applyFadeIn(Parent root) {
        FadeTransition fade = new FadeTransition(Duration.millis(250), root);
        fade.setFromValue(0.9);
        fade.setToValue(1.0);
        fade.play();
    }

    private void animateBubble(Circle bubble, double fromY, double toY, double seconds) {
        if (bubble == null) return;

        TranslateTransition transition = new TranslateTransition(Duration.seconds(seconds), bubble);
        transition.setFromY(fromY);
        transition.setToY(toY);
        transition.setAutoReverse(true);
        transition.setCycleCount(Animation.INDEFINITE);
        transition.play();
    }

    private boolean isAdminRole(String roleValue) {
        if (roleValue == null || roleValue.isBlank()) {
            return false;
        }

        String normalized = roleValue
                .replace("[", "")
                .replace("]", "")
                .replace("\"", "")
                .trim();

        return normalized.equalsIgnoreCase("ROLE_ADMIN")
                || normalized.contains("ROLE_ADMIN");
    }
}