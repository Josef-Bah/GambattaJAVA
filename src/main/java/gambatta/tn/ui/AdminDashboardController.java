package gambatta.tn.ui;

import gambatta.tn.entites.user.user;
import gambatta.tn.services.user.AvatarService;
import gambatta.tn.services.user.UserService;
import gambatta.tn.tools.Session;
import gambatta.tn.tools.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalAdminsLabel;
    @FXML private Label totalActifsLabel;
    @FXML private Label loginsTodayLabel;
    @FXML private Label feedbackLabel;
    @FXML private TextField searchField;
    @FXML private FlowPane cardsContainer;

    private final UserService userService = new UserService();
    private List<user> allUsers = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        user current = Session.getCurrentUser();
        if (current != null) {
            welcomeLabel.setText("Bienvenue, " + current.getFirstName() + " " + current.getLastName());
        }
        loadStats();
        loadUsers();
        startSessionManager();
    }

    private void startSessionManager() {
        Platform.runLater(() -> {
            Scene scene = cardsContainer.getScene();
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
        });
    }

    // ─── Data loading ─────────────────────────────────────────────────────────

    private void loadStats() {
        totalUsersLabel.setText(String.valueOf(userService.countUsers()));
        totalAdminsLabel.setText(String.valueOf(userService.countAdmins()));
        totalActifsLabel.setText(String.valueOf(userService.countActifs()));
        loginsTodayLabel.setText(String.valueOf(userService.countLoginsToday()));
    }

    private void loadUsers() {
        allUsers = userService.afficher();
        renderCards(allUsers);
    }

    private void refresh() {
        loadStats();
        loadUsers();
    }

    // ─── Search ───────────────────────────────────────────────────────────────

    @FXML
    public void handleSearch() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            renderCards(allUsers);
            return;
        }
        List<user> filtered = allUsers.stream()
                .filter(u -> safe(u.getFirstName()).contains(query)
                        || safe(u.getLastName()).contains(query)
                        || safe(u.getEmail()).contains(query)
                        || safe(u.getNumTel()).contains(query)
                        || safe(u.getRoles()).contains(query)
                        || safe(u.getStatus()).contains(query))
                .collect(Collectors.toList());
        renderCards(filtered);
    }

    // ─── Cards rendering ──────────────────────────────────────────────────────

    private void renderCards(List<user> users) {
        cardsContainer.getChildren().clear();
        for (user u : users) {
            cardsContainer.getChildren().add(buildUserCard(u));
        }
    }

    private VBox buildUserCard(user u) {
        VBox card = new VBox(14);
        card.setPrefWidth(330);
        card.setMaxWidth(330);
        card.getStyleClass().add("user-card");

        // ── Avatar + Info row ──────────────────────────────────────────────
        HBox topRow = new HBox(14);
        topRow.setAlignment(Pos.CENTER_LEFT);

        ImageView avatarView = new ImageView();
        avatarView.setFitWidth(56);
        avatarView.setFitHeight(56);
        avatarView.setPreserveRatio(false);
        Circle clip = new Circle(28, 28, 28);
        avatarView.setClip(clip);
        String seed = (u.getProfileImage() != null && !u.getProfileImage().isBlank())
                ? u.getProfileImage() : u.getFirstName();
        avatarView.setImage(AvatarService.loadAvatarAsync(seed));

        VBox infoCol = new VBox(5);
        infoCol.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoCol, Priority.ALWAYS);

        Label nameLabel = new Label(u.getFirstName() + " " + u.getLastName());
        nameLabel.getStyleClass().add("user-card-name");

        Label emailLabel = new Label(u.getEmail());
        emailLabel.getStyleClass().add("user-card-email");

        HBox badges = new HBox(8);
        badges.setAlignment(Pos.CENTER_LEFT);
        badges.getChildren().addAll(buildRoleBadge(u.getRoles()), buildStatusBadge(u.getStatus()));

        infoCol.getChildren().addAll(nameLabel, emailLabel, badges);
        topRow.getChildren().addAll(avatarView, infoCol);

        // ── Separator ─────────────────────────────────────────────────────
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(255,255,255,0.06);");

        // ── Action buttons ────────────────────────────────────────────────
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button editBtn = new Button("✏ Modifier");
        editBtn.getStyleClass().add("action-btn-edit");
        editBtn.setOnAction(e -> showEditDialog(u));

        boolean isBanned = "banned".equals(u.getStatus());
        Button banBtn = new Button(isBanned ? "🔓 Débannir" : "🔒 Bannir");
        banBtn.getStyleClass().add(isBanned ? "action-btn-unban" : "action-btn-ban");
        banBtn.setOnAction(e -> toggleBan(u));

        Button deleteBtn = new Button("🗑");
        deleteBtn.getStyleClass().add("action-btn-delete");
        deleteBtn.setOnAction(e -> confirmDelete(u));

        actions.getChildren().addAll(editBtn, banBtn, deleteBtn);
        card.getChildren().addAll(topRow, sep, actions);
        return card;
    }

    private Label buildRoleBadge(String roles) {
        boolean isAdmin = roles != null && roles.contains("ADMIN");
        Label badge = new Label(isAdmin ? "⭐ Admin" : "👤 Utilisateur");
        badge.getStyleClass().add(isAdmin ? "badge-admin" : "badge-user");
        return badge;
    }

    private Label buildStatusBadge(String status) {
        boolean isBanned = "banned".equals(status);
        Label badge = new Label(isBanned ? "🔴 Banni" : "🟢 Actif");
        badge.getStyleClass().add(isBanned ? "badge-banned" : "badge-active");
        return badge;
    }

    // ─── Ban / Unban ──────────────────────────────────────────────────────────

    private void toggleBan(user u) {
        boolean isBanned = "banned".equals(u.getStatus());
        String newStatus = isBanned ? "active" : "banned";
        String verb = isBanned ? "débannir" : "bannir";

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Voulez-vous vraiment " + verb + " cet utilisateur ?");
        alert.setContentText(u.getFirstName() + " " + u.getLastName() + "\n" + u.getEmail());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                userService.updateStatus(u.getId(), newStatus);
                setFeedback(isBanned
                        ? "✅ Utilisateur débanni avec succès."
                        : "🔒 Utilisateur banni avec succès.", false);
                refresh();
            } catch (Exception e) {
                setFeedback("Erreur : " + e.getMessage(), true);
            }
        }
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    private void confirmDelete(user u) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer utilisateur");
        alert.setHeaderText("Confirmer la suppression");
        alert.setContentText("Voulez-vous vraiment supprimer "
                + u.getFirstName() + " " + u.getLastName() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                userService.supprimer(u.getId());
                setFeedback("✅ Utilisateur supprimé avec succès.", false);
                refresh();
            } catch (Exception e) {
                setFeedback("Erreur suppression : " + e.getMessage(), true);
            }
        }
    }

    // ─── Edit dialog ──────────────────────────────────────────────────────────

    private void showEditDialog(user u) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier l'utilisateur");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("edit-dialog-pane");

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setPadding(new Insets(24));
        grid.setStyle("-fx-background-color: #0f172a;");

        TextField firstNameField = styledField(u.getFirstName());
        TextField lastNameField  = styledField(u.getLastName());
        TextField emailField     = styledField(u.getEmail());
        TextField telField       = styledField(u.getNumTel() != null ? u.getNumTel() : "");

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("[\"ROLE_USER\"]", "[\"ROLE_ADMIN\"]");
        roleCombo.setValue(u.getRoles());
        roleCombo.getStyleClass().add("combo-box");
        roleCombo.setPrefWidth(240);

        grid.add(dialogLabel("Prénom"),    0, 0); grid.add(firstNameField, 1, 0);
        grid.add(dialogLabel("Nom"),       0, 1); grid.add(lastNameField,  1, 1);
        grid.add(dialogLabel("Email"),     0, 2); grid.add(emailField,     1, 2);
        grid.add(dialogLabel("Téléphone"), 0, 3); grid.add(telField,       1, 3);
        grid.add(dialogLabel("Rôle"),      0, 4); grid.add(roleCombo,      1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.getStyleClass().add("btn-primary");
        okBtn.setText("💾 Enregistrer");

        Button cancelBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelBtn.getStyleClass().add("btn-secondary");

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            u.setFirstName(firstNameField.getText().trim());
            u.setLastName(lastNameField.getText().trim());
            u.setEmail(emailField.getText().trim());
            u.setNumTel(telField.getText().trim());
            u.setRoles(roleCombo.getValue());
            try {
                userService.modifier(u);
                setFeedback("✅ Utilisateur modifié avec succès.", false);
                refresh();
            } catch (Exception e) {
                setFeedback("Erreur modification : " + e.getMessage(), true);
            }
        }
    }

    private TextField styledField(String value) {
        TextField field = new TextField(value);
        field.getStyleClass().add("auth-field");
        field.setPrefWidth(240);
        return field;
    }

    private Label dialogLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px; -fx-font-weight: bold;");
        return l;
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    @FXML
    public void openAdminProfile() {
        navigate("/gambatta.tn.ui/AdminProfile.fxml");
    }

    @FXML
    public void logout() {
        SessionManager.stop();
        Session.clear();
        navigate("/gambatta.tn.ui/Login.fxml");
    }

    @FXML
    public void openStats() {
        navigate("/gambatta.tn.ui/Stats.fxml");
    }

    private void navigate(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            cardsContainer.getScene().setRoot(root);
        } catch (Exception e) {
            setFeedback("Erreur navigation : " + e.getMessage(), true);
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void setFeedback(String message, boolean isError) {
        feedbackLabel.setText(message);
        feedbackLabel.setStyle(isError ? "-fx-text-fill: #ff4757;" : "-fx-text-fill: #2ed573;");
    }

    private String safe(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
