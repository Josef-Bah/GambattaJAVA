package gambatta.tn.ui;

import gambatta.tn.entites.user.user;
import gambatta.tn.services.user.UserService;
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

public class RegisterController implements Initializable {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField numTelField;

    @FXML private Label errorLabel;

    @FXML private Label nomValidationLabel;
    @FXML private Label prenomValidationLabel;
    @FXML private Label emailValidationLabel;
    @FXML private Label passwordValidationLabel;
    @FXML private Label confirmPasswordValidationLabel;
    @FXML private Label numTelValidationLabel;

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

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\d{8,15}$");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        errorLabel.setText("");

        animateBubble(bubble1, 0, -18, 4.5);
        animateBubble(bubble2, 0, 16, 5.5);
        animateBubble(bubble3, 0, -12, 6);
        animateBubble(bubble4, 0, 20, 5);
        animateBubble(bubble5, 0, -10, 4);

        setupValidation();
    }

    private void setupValidation() {
        nomField.textProperty().addListener((obs, oldVal, newVal) -> validateNomLive());
        prenomField.textProperty().addListener((obs, oldVal, newVal) -> validatePrenomLive());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validateEmailLive());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            validatePasswordLive();
            validateConfirmPasswordLive();
        });
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validateConfirmPasswordLive());
        numTelField.textProperty().addListener((obs, oldVal, newVal) -> validateNumTelLive());
    }

    private void validateNomLive() {
        String nom = safeText(nomField);

        if (nom.isEmpty()) {
            setFieldError(nomField, nomValidationLabel, "Le nom est obligatoire.");
            return;
        }

        if (nom.length() < 2) {
            setFieldError(nomField, nomValidationLabel, "Minimum 2 caractères.");
            return;
        }

        setFieldSuccess(nomField, nomValidationLabel, "Nom valide.");
    }

    private void validatePrenomLive() {
        String prenom = safeText(prenomField);

        if (prenom.isEmpty()) {
            setFieldError(prenomField, prenomValidationLabel, "Le prénom est obligatoire.");
            return;
        }

        if (prenom.length() < 2) {
            setFieldError(prenomField, prenomValidationLabel, "Minimum 2 caractères.");
            return;
        }

        setFieldSuccess(prenomField, prenomValidationLabel, "Prénom valide.");
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

        if (!password.matches(".*[A-Z].*")) {
            setFieldError(passwordField, passwordValidationLabel, "Ajoute au moins une majuscule.");
            return;
        }

        if (!password.matches(".*\\d.*")) {
            setFieldError(passwordField, passwordValidationLabel, "Ajoute au moins un chiffre.");
            return;
        }

        setFieldSuccess(passwordField, passwordValidationLabel, "Mot de passe valide.");
    }

    private void validateConfirmPasswordLive() {
        String password = safeText(passwordField);
        String confirmPassword = safeText(confirmPasswordField);

        if (confirmPassword.isEmpty()) {
            setFieldError(confirmPasswordField, confirmPasswordValidationLabel, "Confirmation obligatoire.");
            return;
        }

        if (!confirmPassword.equals(password)) {
            setFieldError(confirmPasswordField, confirmPasswordValidationLabel, "Les mots de passe ne correspondent pas.");
            return;
        }

        setFieldSuccess(confirmPasswordField, confirmPasswordValidationLabel, "Confirmation valide.");
    }

    private void validateNumTelLive() {
        String numTel = safeText(numTelField);

        if (numTel.isEmpty()) {
            setFieldError(numTelField, numTelValidationLabel, "Le numéro est obligatoire.");
            return;
        }

        if (!PHONE_PATTERN.matcher(numTel).matches()) {
            setFieldError(numTelField, numTelValidationLabel, "Entre 8 et 15 chiffres.");
            return;
        }

        setFieldSuccess(numTelField, numTelValidationLabel, "Numéro valide.");
    }

    private boolean validateAllFields() {
        validateNomLive();
        validatePrenomLive();
        validateEmailLive();
        validatePasswordLive();
        validateConfirmPasswordLive();
        validateNumTelLive();

        String nom = safeText(nomField);
        String prenom = safeText(prenomField);
        String email = safeText(emailField);
        String password = safeText(passwordField);
        String confirmPassword = safeText(confirmPasswordField);
        String numTel = safeText(numTelField);

        return nom.length() >= 2
                && prenom.length() >= 2
                && EMAIL_PATTERN.matcher(email).matches()
                && password.length() >= 6
                && password.matches(".*[A-Z].*")
                && password.matches(".*\\d.*")
                && confirmPassword.equals(password)
                && PHONE_PATTERN.matcher(numTel).matches();
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        errorLabel.setStyle("-fx-text-fill: #ff6b6b;");
        errorLabel.setText("");

        if (!validateAllFields()) {
            errorLabel.setText("Veuillez corriger les champs.");
            return;
        }

        String nom = safeText(nomField);
        String prenom = safeText(prenomField);
        String email = safeText(emailField);
        String password = safeText(passwordField);
        String numTel = safeText(numTelField);

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

    @FXML
    private void goToLogin(ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource("/gambatta.tn.ui/Login.fxml");
            if (fxmlUrl == null) {
                errorLabel.setText("Login.fxml introuvable.");
                return;
            }

            Parent root = FXMLLoader.load(fxmlUrl);
            applyFadeIn(root);

            Scene currentScene = ((Node) event.getSource()).getScene();
            currentScene.setRoot(root);

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
}