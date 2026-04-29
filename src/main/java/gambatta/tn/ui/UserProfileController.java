package gambatta.tn.ui;

import gambatta.tn.entites.user.user;
import gambatta.tn.services.user.AvatarService;
import gambatta.tn.services.user.UserService;
import gambatta.tn.tools.Session;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
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
    @FXML private ImageView avatarImageView;
    @FXML private FlowPane avatarGrid;
    @FXML private Label selectedAvatarLabel;

    private String selectedSeed = null;
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
                        ? "-" : currentUser.getNumTel()
        );

        // Charger l'avatar — priorité : profile_image en BDD, sinon prénom
        String seed = currentUser.getProfileImage();
        if (seed == null || seed.isBlank()) seed = currentUser.getFirstName();
        if (avatarImageView != null) {
            Image avatar = AvatarService.loadAvatarAsync(seed);
            avatarImageView.setImage(avatar);
            Circle clip = new Circle(50, 50, 50);
            avatarImageView.setClip(clip);
        }

        // Charger la grille d'avatars
        Platform.runLater(this::loadAvatarGrid);
    }

    private void loadAvatarGrid() {
        if (avatarGrid == null || currentUser == null) return;
        avatarGrid.getChildren().clear();

        String currentSeed = currentUser.getProfileImage();
        if (currentSeed == null || currentSeed.isBlank()) {
            currentSeed = currentUser.getFirstName();
        }
        selectedSeed = currentSeed;

        final String activeSeed = currentSeed;

        for (String seed : AvatarService.AVATAR_SEEDS) {
            final String finalSeed = seed;

            ImageView iv = new ImageView();
            iv.setFitWidth(60);
            iv.setFitHeight(60);
            Circle clip = new Circle(30, 30, 30);
            iv.setClip(clip);
            iv.setImage(AvatarService.loadAvatarSmall(seed));

            StackPane container = new StackPane(iv);
            container.setPrefSize(72, 72);
            container.setStyle(
                    seed.equals(activeSeed)
                            ? "-fx-background-color: #FFD700; -fx-background-radius: 50%; -fx-padding: 3; -fx-cursor: hand;"
                            : "-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 50%; -fx-padding: 3; -fx-cursor: hand;"
            );

            javafx.scene.control.Tooltip.install(container,
                    new javafx.scene.control.Tooltip(AvatarService.getDisplayName(seed)));

            // Clic — sélectionner l'avatar
            container.setOnMouseClicked(e -> {
                selectedSeed = finalSeed;

                if (selectedAvatarLabel != null)
                    selectedAvatarLabel.setText("✅ Avatar sélectionné : " + AvatarService.getDisplayName(finalSeed));

                // Reset toutes les bordures
                avatarGrid.getChildren().forEach(node -> {
                    if (node instanceof StackPane sp) {
                        sp.setStyle(
                                "-fx-background-color: rgba(255,255,255,0.05); " +
                                        "-fx-background-radius: 50%; -fx-padding: 3; -fx-cursor: hand;"
                        );
                    }
                });

                // Bordure dorée sur le sélectionné
                container.setStyle(
                        "-fx-background-color: #FFD700; " +
                                "-fx-background-radius: 50%; -fx-padding: 3; -fx-cursor: hand;"
                );

                // Prévisualisation immédiate
                if (avatarImageView != null) {
                    avatarImageView.setImage(AvatarService.loadAvatarAsync(finalSeed));
                    Circle newClip = new Circle(50, 50, 50);
                    avatarImageView.setClip(newClip);
                }
            });

            // Hover
            container.setOnMouseEntered(e -> {
                if (!finalSeed.equals(selectedSeed)) {
                    container.setStyle(
                            "-fx-background-color: rgba(255,215,0,0.3); " +
                                    "-fx-background-radius: 50%; -fx-padding: 3; -fx-cursor: hand;"
                    );
                }
            });
            container.setOnMouseExited(e -> {
                if (!finalSeed.equals(selectedSeed)) {
                    container.setStyle(
                            "-fx-background-color: rgba(255,255,255,0.05); " +
                                    "-fx-background-radius: 50%; -fx-padding: 3; -fx-cursor: hand;"
                    );
                }
            });

            avatarGrid.getChildren().add(container);
        }
    }

    @FXML
    public void saveProfile() {
        feedbackLabel.setStyle("-fx-text-fill: #ff6b6b;");
        feedbackLabel.setText("");

        if (currentUser == null) {
            feedbackLabel.setText("Aucun utilisateur connecté.");
            return;
        }

        String firstName       = safe(firstNameField.getText()).trim();
        String lastName        = safe(lastNameField.getText()).trim();
        String email           = safe(emailField.getText()).trim();
        String numTel          = safe(numTelField.getText()).trim();
        String password        = safe(passwordField.getText()).trim();
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

            // Sauvegarder l'avatar choisi en BDD
            if (selectedSeed != null && !selectedSeed.isBlank()) {
                userService.updateProfileImage(currentUser.getId(), selectedSeed);
                currentUser.setProfileImage(selectedSeed);
            }

            Session.setCurrentUser(currentUser);
            loadCurrentUserData();

            feedbackLabel.setStyle("-fx-text-fill: #7bed9f;");
            feedbackLabel.setText("✅ Profil mis à jour avec succès.");

        } catch (Exception e) {
            feedbackLabel.setStyle("-fx-text-fill: #ff6b6b;");
            feedbackLabel.setText("Erreur mise à jour : " + e.getMessage());
        }
    }

    @FXML
    public void goBackHome() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gambatta.tn.ui/HomeUser.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) firstNameField.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            scene.getStylesheets().add(
                    getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}