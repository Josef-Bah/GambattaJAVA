package gambatta.tn.ui;

import gambatta.tn.entites.user.user;
import gambatta.tn.services.user.UserService;
import gambatta.tn.tools.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class UserProfileController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField numTelField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField roleField;
    @FXML private Label feedbackLabel;

    @FXML private Label displayNameLabel;
    @FXML private Label displayRoleLabel;
    @FXML private Label displayEmailLabel;
    @FXML private Label displayPhoneLabel;

    private final UserService userService = new UserService();
    private user currentUser;

    @FXML
    public void initialize() {
        loadCurrentUserData();
    }

    private void loadCurrentUserData() {
        currentUser = Session.getCurrentUser();

        if (currentUser == null) {
            feedbackLabel.setStyle("-fx-text-fill: #ff6b6b;");
            feedbackLabel.setText("Aucun utilisateur connecté.");
            return;
        }

        firstNameField.setText(safe(currentUser.getFirstName()));
        lastNameField.setText(safe(currentUser.getLastName()));
        emailField.setText(safe(currentUser.getEmail()));
        numTelField.setText(currentUser.getNumTel() == null ? "" : currentUser.getNumTel());
        passwordField.setText(safe(currentUser.getPassword()));
        confirmPasswordField.setText(safe(currentUser.getPassword()));
        roleField.setText(safe(currentUser.getRoles()));

        displayNameLabel.setText(safe(currentUser.getFirstName()) + " " + safe(currentUser.getLastName()));
        displayRoleLabel.setText(safe(currentUser.getRoles()));
        displayEmailLabel.setText(safe(currentUser.getEmail()));
        displayPhoneLabel.setText(
                currentUser.getNumTel() == null || currentUser.getNumTel().isBlank()
                        ? "-"
                        : currentUser.getNumTel()
        );
    }

    @FXML
    public void saveProfile() {
        feedbackLabel.setStyle("-fx-text-fill: #ff6b6b;");
        feedbackLabel.setText("");

        if (currentUser == null) {
            feedbackLabel.setText("Aucun utilisateur connecté.");
            return;
        }

        String firstName = safe(firstNameField.getText()).trim();
        String lastName = safe(lastNameField.getText()).trim();
        String email = safe(emailField.getText()).trim();
        String numTel = safe(numTelField.getText()).trim();
        String password = safe(passwordField.getText()).trim();
        String confirmPassword = safe(confirmPasswordField.getText()).trim();

        if (firstName.length() < 2) {
            feedbackLabel.setText("Le prénom doit contenir au moins 2 caractères.");
            return;
        }

        if (lastName.length() < 2) {
            feedbackLabel.setText("Le nom doit contenir au moins 2 caractères.");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            feedbackLabel.setText("Format email invalide.");
            return;
        }

        if (!numTel.isBlank() && !numTel.matches("^\\d{8,15}$")) {
            feedbackLabel.setText("Le numéro doit contenir entre 8 et 15 chiffres.");
            return;
        }

        if (password.length() < 6) {
            feedbackLabel.setText("Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        if (confirmPassword.isEmpty()) {
            feedbackLabel.setText("La confirmation du mot de passe est obligatoire.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            feedbackLabel.setText("Le mot de passe et sa confirmation ne correspondent pas.");
            return;
        }

        try {
            currentUser.setFirstName(firstName);
            currentUser.setLastName(lastName);
            currentUser.setEmail(email);
            currentUser.setNumTel(numTel);
            currentUser.setPassword(password);

            userService.modifier(currentUser);
            Session.setCurrentUser(currentUser);

            loadCurrentUserData();

            feedbackLabel.setStyle("-fx-text-fill: #7bed9f;");
            feedbackLabel.setText("Profil mis à jour avec succès.");

        } catch (Exception e) {
            feedbackLabel.setStyle("-fx-text-fill: #ff6b6b;");
            feedbackLabel.setText("Erreur mise à jour : " + e.getMessage());
        }
    }

    @FXML
    public void goBackHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/HomeUser.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) firstNameField.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            scene.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}