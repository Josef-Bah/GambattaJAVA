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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class AdminProfileController {

    @FXML private Label welcomeLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalAdminsLabel;
    @FXML private Label totalSimpleUsersLabel;
    @FXML private Label loginsTodayLabel;
    @FXML private Label feedbackLabel;
    @FXML private ImageView profileImageView;

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField numTelField;
    @FXML private PasswordField passwordField;
    @FXML private TextField roleField;

    private final UserService userService = new UserService();
    private user currentUser;

    @FXML
    public void initialize() {
        loadCurrentUserData();
        loadStats();
        loadProfileImage();
    }

    private void loadCurrentUserData() {
        currentUser = Session.getCurrentUser();

        if (currentUser == null) {
            feedbackLabel.setText("Aucun administrateur connecté.");
            return;
        }

        welcomeLabel.setText("Bienvenue, " + currentUser.getFirstName() + " " + currentUser.getLastName());

        firstNameField.setText(safe(currentUser.getFirstName()));
        lastNameField.setText(safe(currentUser.getLastName()));
        emailField.setText(safe(currentUser.getEmail()));
        numTelField.setText(currentUser.getNumTel() == null ? "" : currentUser.getNumTel());
        passwordField.setText(safe(currentUser.getPassword()));
        roleField.setText(safe(currentUser.getRoles()));

    }

    private void loadStats() {
        totalUsersLabel.setText(String.valueOf(userService.countUsers()));
        totalAdminsLabel.setText(String.valueOf(userService.countAdmins()));
        totalSimpleUsersLabel.setText(String.valueOf(userService.countSimpleUsers()));
        loginsTodayLabel.setText(String.valueOf(userService.countLoginsToday()));
    }

    @FXML
    public void saveProfile() {
        feedbackLabel.setStyle("-fx-text-fill: #ff6b6b;");
        feedbackLabel.setText("");

        if (currentUser == null) {
            feedbackLabel.setText("Aucun admin connecté.");
            return;
        }

        String firstName = safe(firstNameField.getText()).trim();
        String lastName = safe(lastNameField.getText()).trim();
        String email = safe(emailField.getText()).trim();
        String numTel = safe(numTelField.getText()).trim();
        String password = safe(passwordField.getText()).trim();

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

        if (password.length() < 6) {
            feedbackLabel.setText("Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        if (!numTel.isBlank() && !numTel.matches("^\\d{8,15}$")) {
            feedbackLabel.setText("Le numéro doit contenir entre 8 et 15 chiffres.");
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
            loadStats();

            feedbackLabel.setStyle("-fx-text-fill: #7bed9f;");
            feedbackLabel.setText("Informations mises à jour avec succès.");

        } catch (Exception e) {
            feedbackLabel.setStyle("-fx-text-fill: #ff6b6b;");
            feedbackLabel.setText("Erreur mise à jour : " + e.getMessage());
        }
    }

    @FXML
    public void reloadProfile() {
        feedbackLabel.setStyle("-fx-text-fill: #ff6b6b;");
        feedbackLabel.setText("");
        loadCurrentUserData();
        loadStats();
    }

    @FXML
    public void backToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/AdminDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) firstNameField.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            scene.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            feedbackLabel.setStyle("-fx-text-fill: #ff6b6b;");
            feedbackLabel.setText("Erreur retour dashboard : " + e.getMessage());
        }
    }

    @FXML
    public void logout() {
        try {
            Session.clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) firstNameField.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            scene.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            feedbackLabel.setStyle("-fx-text-fill: #ff6b6b;");
            feedbackLabel.setText("Erreur logout : " + e.getMessage());
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void loadProfileImage() {
        try {
            Image image = new Image(getClass().getResource("/gambatta.tn.ui/images/admin-profile.png").toExternalForm());
            profileImageView.setImage(image);
        } catch (Exception e) {
            System.out.println("Image profil introuvable : " + e.getMessage());
        }
    }
}