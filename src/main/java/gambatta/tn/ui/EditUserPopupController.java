package gambatta.tn.ui;

import gambatta.tn.entites.user.user;
import gambatta.tn.services.user.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.regex.Pattern;

public class EditUserPopupController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField numTelField;
    @FXML private ComboBox<String> roleComboBox;

    @FXML private Label firstNameErrorLabel;
    @FXML private Label lastNameErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label passwordErrorLabel;
    @FXML private Label numTelErrorLabel;
    @FXML private Label roleErrorLabel;
    @FXML private Label feedbackLabel;

    private final UserService userService = new UserService();
    private user currentUser;
    private Runnable onSaveCallback;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\d{8,15}$");

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("ROLE_USER", "ROLE_ADMIN");
    }

    public void setUser(user u) {
        this.currentUser = u;

        firstNameField.setText(u.getFirstName());
        lastNameField.setText(u.getLastName());
        emailField.setText(u.getEmail());
        passwordField.setText(u.getPassword());
        numTelField.setText(u.getNumTel() == null ? "" : u.getNumTel());
        roleComboBox.setValue(u.getRoles());
    }

    public void setOnSaveCallback(Runnable onSaveCallback) {
        this.onSaveCallback = onSaveCallback;
    }

    @FXML
    private void handleSave() {
        clearMessages();

        if (!validateFields()) {
            feedbackLabel.setText("Veuillez corriger les champs.");
            return;
        }

        try {
            currentUser.setFirstName(firstNameField.getText().trim());
            currentUser.setLastName(lastNameField.getText().trim());
            currentUser.setEmail(emailField.getText().trim());
            currentUser.setPassword(passwordField.getText().trim());
            currentUser.setNumTel(numTelField.getText().trim());
            currentUser.setRoles(roleComboBox.getValue());

            userService.modifier(currentUser);

            if (onSaveCallback != null) {
                onSaveCallback.run();
            }

            closeWindow();

        } catch (Exception e) {
            feedbackLabel.setText("Erreur modification : " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateFields() {
        boolean valid = true;

        String firstName = firstNameField.getText() == null ? "" : firstNameField.getText().trim();
        String lastName = lastNameField.getText() == null ? "" : lastNameField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();
        String numTel = numTelField.getText() == null ? "" : numTelField.getText().trim();
        String role = roleComboBox.getValue();

        if (firstName.isEmpty() || firstName.length() < 2) {
            firstNameErrorLabel.setText("Minimum 2 caractères.");
            valid = false;
        }

        if (lastName.isEmpty() || lastName.length() < 2) {
            lastNameErrorLabel.setText("Minimum 2 caractères.");
            valid = false;
        }

        if (email.isEmpty()) {
            emailErrorLabel.setText("Email obligatoire.");
            valid = false;
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            emailErrorLabel.setText("Format email invalide.");
            valid = false;
        }

        if (password.isEmpty() || password.length() < 6) {
            passwordErrorLabel.setText("Minimum 6 caractères.");
            valid = false;
        }

        if (!numTel.isEmpty() && !PHONE_PATTERN.matcher(numTel).matches()) {
            numTelErrorLabel.setText("Entre 8 et 15 chiffres.");
            valid = false;
        }

        if (role == null || role.isBlank()) {
            roleErrorLabel.setText("Choisir un rôle.");
            valid = false;
        }

        return valid;
    }

    private void clearMessages() {
        firstNameErrorLabel.setText("");
        lastNameErrorLabel.setText("");
        emailErrorLabel.setText("");
        passwordErrorLabel.setText("");
        numTelErrorLabel.setText("");
        roleErrorLabel.setText("");
        feedbackLabel.setText("");
    }

    private void closeWindow() {
        Stage stage = (Stage) firstNameField.getScene().getWindow();
        stage.close();
    }
}