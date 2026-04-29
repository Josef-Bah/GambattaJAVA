package gambatta.tn.ui;

import gambatta.tn.entites.user.user;
import gambatta.tn.services.user.UserService;
import gambatta.tn.tools.Session;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class LoginController implements Initializable {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML private Label emailValidationLabel;
    @FXML private Label passwordValidationLabel;

    @FXML private Circle bubble1, bubble2, bubble3, bubble4, bubble5;
    @FXML private Circle bubble6, bubble7, bubble8, bubble9, bubble10;
    @FXML private Circle bubble11, bubble12;

    private final UserService userService = new UserService();

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    // ─── Limite de tentatives ────────────────────────────────────────────────
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 5;

    private static final Map<String, Integer>       failedAttempts = new HashMap<>();
    private static final Map<String, LocalDateTime> lockUntil      = new HashMap<>();

    private Timeline countdownTimeline;
    // ────────────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        errorLabel.setText("");
        animateBubble(bubble1, 0, -18, 4);
        animateBubble(bubble2, 0,  22, 5);
        animateBubble(bubble3, 0, -15, 6);
        animateBubble(bubble4, 0,  20, 4.5);
        animateBubble(bubble5, 0, -12, 5.5);
        setupValidation();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  LOGIQUE LIMITE DE TENTATIVES
    // ═════════════════════════════════════════════════════════════════════════

    private boolean isLocked(String email) {
        LocalDateTime until = lockUntil.get(email);
        if (until == null) return false;
        if (LocalDateTime.now().isAfter(until)) {
            lockUntil.remove(email);
            failedAttempts.remove(email);
            return false;
        }
        return true;
    }

    private void registerFailure(String email) {
        int attempts = failedAttempts.getOrDefault(email, 0) + 1;
        failedAttempts.put(email, attempts);

        if (attempts >= MAX_ATTEMPTS) {
            lockUntil.put(email, LocalDateTime.now().plusMinutes(LOCK_MINUTES));
            failedAttempts.put(email, 0);
            startCountdown(email);
        } else {
            int remaining = MAX_ATTEMPTS - attempts;
            errorLabel.setText("Mot de passe incorrect. " + remaining + " tentative(s) restante(s).");
            errorLabel.setStyle("-fx-text-fill: #e67e22;");
        }
    }

    private void startCountdown(String email) {
        if (countdownTimeline != null) countdownTimeline.stop();

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            LocalDateTime until = lockUntil.get(email);
            if (until == null || LocalDateTime.now().isAfter(until)) {
                countdownTimeline.stop();
                errorLabel.setText("Compte débloqué. Vous pouvez réessayer.");
                errorLabel.setStyle("-fx-text-fill: #27ae60;");
                return;
            }
            long secondsLeft = java.time.Duration.between(LocalDateTime.now(), until).getSeconds();
            long mins = secondsLeft / 60;
            long secs = secondsLeft % 60;
            errorLabel.setText(String.format("Compte bloqué. Réessayez dans %d:%02d", mins, secs));
            errorLabel.setStyle("-fx-text-fill: #e74c3c;");
        }));
        countdownTimeline.setCycleCount(Animation.INDEFINITE);
        countdownTimeline.play();

        errorLabel.setText(String.format(
                "Compte bloqué après %d tentatives. Réessayez dans %d:00", MAX_ATTEMPTS, LOCK_MINUTES));
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");
    }

    private void resetAttempts(String email) {
        failedAttempts.remove(email);
        lockUntil.remove(email);
        if (countdownTimeline != null) countdownTimeline.stop();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  HANDLER LOGIN
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleLogin(ActionEvent event) {
        errorLabel.setStyle("");
        errorLabel.setText("");

        if (!validateAllFields()) {
            errorLabel.setText("Veuillez corriger les champs.");
            return;
        }

        String email    = safeText(emailField);
        String password = safeText(passwordField);

        // 1. Vérifier si bloqué
        if (isLocked(email)) {
            startCountdown(email);
            return;
        }

        // 2. Tenter la connexion
        try {
            user connectedUser = userService.login(email, password);

            if (connectedUser == null) {
                registerFailure(email);
                return;
            }

            // 3. Succès
            resetAttempts(email);
            Session.setCurrentUser(connectedUser);

            try {
                userService.logLogin(connectedUser);
            } catch (Exception ex) {
                System.out.println("Log login non enregistré : " + ex.getMessage());
            }

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

    // ═════════════════════════════════════════════════════════════════════════
    //  VALIDATION
    // ═════════════════════════════════════════════════════════════════════════

    private void setupValidation() {
        emailField.textProperty().addListener((obs, o, n)    -> validateEmailLive());
        passwordField.textProperty().addListener((obs, o, n) -> validatePasswordLive());
    }

    private void validateEmailLive() {
        String email = safeText(emailField);
        if (email.isEmpty()) {
            setFieldError(emailField, emailValidationLabel, "L'email est obligatoire."); return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            setFieldError(emailField, emailValidationLabel, "Format email invalide."); return;
        }
        setFieldSuccess(emailField, emailValidationLabel, "Email valide.");
    }

    private void validatePasswordLive() {
        String password = safeText(passwordField);
        if (password.isEmpty()) {
            setFieldError(passwordField, passwordValidationLabel, "Le mot de passe est obligatoire."); return;
        }
        if (password.length() < 6) {
            setFieldError(passwordField, passwordValidationLabel, "Minimum 6 caractères."); return;
        }
        setFieldSuccess(passwordField, passwordValidationLabel, "Mot de passe valide.");
    }

    private boolean validateAllFields() {
        validateEmailLive();
        validatePasswordLive();
        String email    = safeText(emailField);
        String password = safeText(passwordField);
        return EMAIL_PATTERN.matcher(email).matches() && password.length() >= 6;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  NAVIGATION + UTILITAIRES
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    private void goToRegister(ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource("/gambatta.tn.ui/Register.fxml");
            if (fxmlUrl == null) { errorLabel.setText("Register.fxml introuvable."); return; }
            Parent root = FXMLLoader.load(fxmlUrl);
            applyFadeIn(root);
            ((Node) event.getSource()).getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Impossible d'ouvrir la page d'inscription.");
        }
    }

    private void loadScene(ActionEvent event, String fxmlPath, String errorMessage) throws IOException {
        URL fxmlUrl = getClass().getResource(fxmlPath);
        if (fxmlUrl == null) { errorLabel.setText(errorMessage); return; }
        Parent root = FXMLLoader.load(fxmlUrl);
        applyFadeIn(root);
        ((Node) event.getSource()).getScene().setRoot(root);
    }

    private String safeText(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private void setFieldError(TextField field, Label label, String message) {
        label.setText(message);
        label.getStyleClass().removeAll("field-help-success");
        if (!label.getStyleClass().contains("field-help-error"))
            label.getStyleClass().add("field-help-error");
        field.getStyleClass().remove("field-valid");
        if (!field.getStyleClass().contains("field-invalid"))
            field.getStyleClass().add("field-invalid");
    }

    private void setFieldSuccess(TextField field, Label label, String message) {
        label.setText(message);
        label.getStyleClass().removeAll("field-help-error");
        if (!label.getStyleClass().contains("field-help-success"))
            label.getStyleClass().add("field-help-success");
        field.getStyleClass().remove("field-invalid");
        if (!field.getStyleClass().contains("field-valid"))
            field.getStyleClass().add("field-valid");
    }

    private void applyFadeIn(Parent root) {
        FadeTransition fade = new FadeTransition(Duration.millis(250), root);
        fade.setFromValue(0.9);
        fade.setToValue(1.0);
        fade.play();
    }

    private void animateBubble(Circle bubble, double fromY, double toY, double seconds) {
        if (bubble == null) return;
        TranslateTransition t = new TranslateTransition(Duration.seconds(seconds), bubble);
        t.setFromY(fromY);
        t.setToY(toY);
        t.setAutoReverse(true);
        t.setCycleCount(Animation.INDEFINITE);
        t.play();
    }

    private boolean isAdminRole(String roleValue) {
        if (roleValue == null || roleValue.isBlank()) return false;
        String normalized = roleValue
                .replace("[", "").replace("]", "").replace("\"", "").trim();
        return normalized.equalsIgnoreCase("ROLE_ADMIN")
                || normalized.contains("ROLE_ADMIN");
    }
}