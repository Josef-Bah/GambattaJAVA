package gambatta.tn.ui;

import gambatta.tn.entites.user.user;
import gambatta.tn.services.user.PasswordAIService;
import gambatta.tn.services.user.UserService;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
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

public class RegisterController implements Initializable {

    @FXML private TextField     nomField;
    @FXML private TextField     prenomField;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField     numTelField;

    @FXML private Label errorLabel;
    @FXML private Label nomValidationLabel;
    @FXML private Label prenomValidationLabel;
    @FXML private Label emailValidationLabel;
    @FXML private Label passwordValidationLabel;       // affiche le score IA
    @FXML private Label confirmPasswordValidationLabel;
    @FXML private Label numTelValidationLabel;

    @FXML private Circle bubble1, bubble2, bubble3, bubble4, bubble5;
    @FXML private Circle bubble6, bubble7, bubble8, bubble9, bubble10;
    @FXML private Circle bubble11, bubble12;

    private final UserService       userService = new UserService();
    private final PasswordAIService passwordAI  = new PasswordAIService();

    // ── Debounce : attendre 500ms d'inactivité avant d'appeler Groq ──────────
    private Timeline debounceTimer;
    private static final int DEBOUNCE_MS = 500;

    // Score courant pour bloquer le submit si trop faible
    private PasswordAIService.PasswordAnalysis lastAnalysis = null;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\d{8,15}$");

    // ────────────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        errorLabel.setText("");
        animateBubble(bubble1, 0, -18, 4.5);
        animateBubble(bubble2, 0,  16, 5.5);
        animateBubble(bubble3, 0, -12, 6);
        animateBubble(bubble4, 0,  20, 5);
        animateBubble(bubble5, 0, -10, 4);
        setupValidation();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  VALIDATION LIVE
    // ═════════════════════════════════════════════════════════════════════════

    private void setupValidation() {
        nomField.textProperty().addListener((obs, o, n)    -> validateNomLive());
        prenomField.textProperty().addListener((obs, o, n) -> validatePrenomLive());
        emailField.textProperty().addListener((obs, o, n)  -> validateEmailLive());

        // Mot de passe → debounce + appel IA Groq
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateConfirmPasswordLive();
            schedulePasswordAnalysis(newVal);
        });

        confirmPasswordField.textProperty().addListener((obs, o, n) -> validateConfirmPasswordLive());
        numTelField.textProperty().addListener((obs, o, n)           -> validateNumTelLive());
    }

    /**
     * Lance un timer de 500ms.
     * Si l'utilisateur retape avant expiration, on repart de zéro.
     */
    private void schedulePasswordAnalysis(String password) {
        if (debounceTimer != null) debounceTimer.stop();

        if (password.length() >= 4) {
            passwordValidationLabel.setText("⏳ Analyse IA en cours...");
            passwordValidationLabel.setStyle("-fx-text-fill: #95a5a6;");
        }

        debounceTimer = new Timeline(new KeyFrame(
                Duration.millis(DEBOUNCE_MS),
                e -> analyzePasswordInBackground(password)
        ));
        debounceTimer.setCycleCount(1);
        debounceTimer.play();
    }

    /**
     * Appel Groq dans un thread daemon pour ne pas bloquer l'UI JavaFX.
     * Résultat affiché via Platform.runLater().
     */
    private void analyzePasswordInBackground(String password) {
        Thread thread = new Thread(() -> {
            PasswordAIService.PasswordAnalysis analysis = passwordAI.analyze(password);
            Platform.runLater(() -> {
                lastAnalysis = analysis;
                displayPasswordAnalysis(analysis);
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Affiche le résultat sous le champ : score coloré + 2 suggestions.
     */
    private void displayPasswordAnalysis(PasswordAIService.PasswordAnalysis analysis) {
        String display = analysis.label
                + "\n• " + analysis.suggestion1
                + "\n• " + analysis.suggestion2;

        passwordValidationLabel.setText(display);
        passwordValidationLabel.setStyle("-fx-text-fill: " + analysis.cssColor + ";");
        passwordValidationLabel.getStyleClass().removeAll("field-help-success", "field-help-error");

        boolean isStrong =
                analysis.score == PasswordAIService.PasswordAnalysis.Score.FORT ||
                        analysis.score == PasswordAIService.PasswordAnalysis.Score.TRES_FORT;

        passwordField.getStyleClass().removeAll("field-valid", "field-invalid");
        passwordField.getStyleClass().add(isStrong ? "field-valid" : "field-invalid");
    }

    // ── Validations classiques ────────────────────────────────────────────────

    private void validateNomLive() {
        String nom = safeText(nomField);
        if (nom.isEmpty()) { setFieldError(nomField, nomValidationLabel, "Le nom est obligatoire."); return; }
        if (nom.length() < 2) { setFieldError(nomField, nomValidationLabel, "Minimum 2 caractères."); return; }
        setFieldSuccess(nomField, nomValidationLabel, "Nom valide.");
    }

    private void validatePrenomLive() {
        String prenom = safeText(prenomField);
        if (prenom.isEmpty()) { setFieldError(prenomField, prenomValidationLabel, "Le prénom est obligatoire."); return; }
        if (prenom.length() < 2) { setFieldError(prenomField, prenomValidationLabel, "Minimum 2 caractères."); return; }
        setFieldSuccess(prenomField, prenomValidationLabel, "Prénom valide.");
    }

    private void validateEmailLive() {
        String email = safeText(emailField);
        if (email.isEmpty()) { setFieldError(emailField, emailValidationLabel, "L'email est obligatoire."); return; }
        if (!EMAIL_PATTERN.matcher(email).matches()) { setFieldError(emailField, emailValidationLabel, "Format email invalide."); return; }
        setFieldSuccess(emailField, emailValidationLabel, "Email valide.");
    }

    private void validateConfirmPasswordLive() {
        String password        = safeText(passwordField);
        String confirmPassword = safeText(confirmPasswordField);
        if (confirmPassword.isEmpty()) { setFieldError(confirmPasswordField, confirmPasswordValidationLabel, "Confirmation obligatoire."); return; }
        if (!confirmPassword.equals(password)) { setFieldError(confirmPasswordField, confirmPasswordValidationLabel, "Les mots de passe ne correspondent pas."); return; }
        setFieldSuccess(confirmPasswordField, confirmPasswordValidationLabel, "Confirmation valide.");
    }

    private void validateNumTelLive() {
        String numTel = safeText(numTelField);
        if (numTel.isEmpty()) { setFieldError(numTelField, numTelValidationLabel, "Le numéro est obligatoire."); return; }
        if (!PHONE_PATTERN.matcher(numTel).matches()) { setFieldError(numTelField, numTelValidationLabel, "Entre 8 et 15 chiffres."); return; }
        setFieldSuccess(numTelField, numTelValidationLabel, "Numéro valide.");
    }

    private boolean validateAllFields() {
        validateNomLive();
        validatePrenomLive();
        validateEmailLive();
        validateConfirmPasswordLive();
        validateNumTelLive();

        String nom             = safeText(nomField);
        String prenom          = safeText(prenomField);
        String email           = safeText(emailField);
        String password        = safeText(passwordField);
        String confirmPassword = safeText(confirmPasswordField);
        String numTel          = safeText(numTelField);

        // Le mot de passe doit être au moins FORT selon l'IA
        boolean passwordOk = lastAnalysis != null && (
                lastAnalysis.score == PasswordAIService.PasswordAnalysis.Score.FORT ||
                        lastAnalysis.score == PasswordAIService.PasswordAnalysis.Score.TRES_FORT
        );

        if (!passwordOk) {
            passwordValidationLabel.setText("⚠ Le mot de passe doit être au minimum Fort.");
            passwordValidationLabel.setStyle("-fx-text-fill: #e74c3c;");
        }

        return nom.length() >= 2
                && prenom.length() >= 2
                && EMAIL_PATTERN.matcher(email).matches()
                && password.length() >= 6
                && confirmPassword.equals(password)
                && PHONE_PATTERN.matcher(numTel).matches()
                && passwordOk;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  HANDLER INSCRIPTION
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleRegister(ActionEvent event) {
        errorLabel.setStyle("-fx-text-fill: #ff6b6b;");
        errorLabel.setText("");

        if (!validateAllFields()) {
            errorLabel.setText("Veuillez corriger les champs.");
            return;
        }

        String nom      = safeText(nomField);
        String prenom   = safeText(prenomField);
        String email    = safeText(emailField);
        String password = safeText(passwordField);
        String numTel   = safeText(numTelField);

        try {
            if (userService.emailExiste(email)) {
                errorLabel.setText("Cet email existe déjà.");
                return;
            }

            user nouveauUser = new user();
            nouveauUser.setEmail(email);
            nouveauUser.setPassword(password);
            nouveauUser.setRoles("[\"ROLE_USER\"]");
            nouveauUser.setFirstName(prenom);
            nouveauUser.setLastName(nom);
            nouveauUser.setNumTel(numTel);

            userService.ajouter(nouveauUser);

            errorLabel.setStyle("-fx-text-fill: #7bed9f;");
            errorLabel.setText("Compte créé avec succès.");
            goToLogin(event);

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setStyle("-fx-text-fill: #ff6b6b;");
            errorLabel.setText("Erreur d'inscription : " + e.getMessage());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  NAVIGATION + UTILITAIRES
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    private void goToLogin(ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource("/gambatta.tn.ui/Login.fxml");
            if (fxmlUrl == null) { errorLabel.setText("Login.fxml introuvable."); return; }
            Parent root = FXMLLoader.load(fxmlUrl);
            applyFadeIn(root);
            ((Node) event.getSource()).getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Impossible d'ouvrir la page de connexion.");
        }
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
}