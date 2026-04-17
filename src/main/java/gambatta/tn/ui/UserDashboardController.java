package gambatta.tn.ui;

import gambatta.tn.entites.user.user;
import gambatta.tn.services.user.UserService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class UserDashboardController implements Initializable {

    @FXML private TableView<user> userTable;
    @FXML private TableColumn<user, String> colId;
    @FXML private TableColumn<user, String> colFirstName;
    @FXML private TableColumn<user, String> colLastName;
    @FXML private TableColumn<user, String> colEmail;
    @FXML private TableColumn<user, String> colTel;
    @FXML private TableColumn<user, String> colRole;
    @FXML private TableColumn<user, Void>   colActions;
    @FXML private TextField searchField;
    @FXML private Label feedbackLabel;

    private final UserService userService = new UserService();
    private ObservableList<user> allUsers = FXCollections.observableArrayList();
    private user currentUser;

    public void setCurrentUser(user u) { this.currentUser = u; }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colFirstName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFirstName()));
        colLastName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLastName()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colTel.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNumTel() != null ? c.getValue().getNumTel() : "-"));
        colRole.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getRoles().replace("[\"", "").replace("\"]", "")));

        addActionsColumn();
        loadUsers();
    }

    private void loadUsers() {
        try {
            allUsers = FXCollections.observableArrayList(userService.afficher());
            userTable.setItems(allUsers);
        } catch (Exception e) {
            feedbackLabel.setStyle("-fx-text-fill: #ff4757;");
            feedbackLabel.setText("❌ " + e.getMessage());
        }
    }

    @FXML
    public void handleSearch() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            userTable.setItems(allUsers);
            return;
        }
        List<user> filtered = allUsers.stream()
                .filter(u -> u.getFirstName().toLowerCase().contains(query)
                        || u.getLastName().toLowerCase().contains(query)
                        || u.getEmail().toLowerCase().contains(query))
                .collect(Collectors.toList());
        userTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void addActionsColumn() {
        colActions.setCellFactory(col -> new TableCell<>() {
            final Button btnEdit   = new Button("✏");
            final Button btnDelete = new Button("🗑");
            final HBox box = new HBox(8, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: #e6c86e; -fx-text-fill: #020617; " +
                        "-fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
                btnDelete.setStyle("-fx-background-color: #ff4757; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");

                btnEdit.setOnAction(e -> {
                    user u = getTableView().getItems().get(getIndex());
                    showEditDialog(u);
                });

                btnDelete.setOnAction(e -> {
                    user u = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Supprimer " + u.getFirstName() + " " + u.getLastName() + " ?",
                            ButtonType.YES, ButtonType.NO);
                    confirm.setHeaderText(null);
                    confirm.showAndWait().ifPresent(btn -> {
                        if (btn == ButtonType.YES) {
                            try {
                                userService.supprimer(u.getId());
                                feedbackLabel.setStyle("-fx-text-fill: #2ed573;");
                                feedbackLabel.setText("✅ Utilisateur supprimé.");
                                loadUsers();
                            } catch (Exception ex) {
                                feedbackLabel.setStyle("-fx-text-fill: #ff4757;");
                                feedbackLabel.setText("❌ " + ex.getMessage());
                            }
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    @FXML
    public void showAddDialog() {
        showUserForm(null);
    }

    private void showEditDialog(user u) {
        showUserForm(u);
    }

    private void showUserForm(user u) {
        // Champs du formulaire
        TextField fnField  = new TextField(u != null ? u.getFirstName() : "");
        TextField lnField  = new TextField(u != null ? u.getLastName()  : "");
        TextField emField  = new TextField(u != null ? u.getEmail()     : "");
        TextField telField = new TextField(u != null && u.getNumTel() != null ? u.getNumTel() : "");
        PasswordField pwField = new PasswordField();
        if (u != null) pwField.setText(u.getPassword());

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll(
                "[\"ROLE_USER\"]", "[\"ROLE_ADMIN_USER\"]",
                "[\"ROLE_ADMIN_BUVETTE\"]", "[\"ROLE_ADMIN_ACTIVITE\"]",
                "[\"ROLE_ADMIN_TOURNOI\"]", "[\"ROLE_ADMIN_RECLAMATION\"]"
        );
        roleBox.setValue(u != null ? u.getRoles() : "[\"ROLE_USER\"]");

        Label errLabel = new Label("");
        errLabel.setStyle("-fx-text-fill: #ff4757;");

        // Layout du dialog
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));
        grid.addRow(0, new Label("Prénom:"),  fnField);
        grid.addRow(1, new Label("Nom:"),     lnField);
        grid.addRow(2, new Label("Email:"),   emField);
        grid.addRow(3, new Label("Tél:"),     telField);
        grid.addRow(4, new Label("Password:"),pwField);
        grid.addRow(5, new Label("Rôle:"),    roleBox);
        grid.add(errLabel, 0, 6, 2, 1);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(u == null ? "Ajouter un utilisateur" : "Modifier l'utilisateur");
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                // Validation
                if (fnField.getText().trim().isEmpty() || lnField.getText().trim().isEmpty()
                        || emField.getText().trim().isEmpty() || pwField.getText().trim().isEmpty()) {
                    feedbackLabel.setStyle("-fx-text-fill: #ff4757;");
                    feedbackLabel.setText("⚠ Tous les champs obligatoires doivent être remplis.");
                    return;
                }
                if (!emField.getText().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                    feedbackLabel.setStyle("-fx-text-fill: #ff4757;");
                    feedbackLabel.setText("⚠ Format email invalide.");
                    return;
                }
                try {
                    if (u == null) {
                        user newUser = new user(
                                emField.getText().trim(), roleBox.getValue(),
                                pwField.getText().trim(), fnField.getText().trim(),
                                lnField.getText().trim(),
                                telField.getText().trim().isEmpty() ? null : telField.getText().trim()
                        );
                        userService.ajouter(newUser);
                        feedbackLabel.setStyle("-fx-text-fill: #2ed573;");
                        feedbackLabel.setText("✅ Utilisateur ajouté.");
                    } else {
                        u.setFirstName(fnField.getText().trim());
                        u.setLastName(lnField.getText().trim());
                        u.setEmail(emField.getText().trim());
                        u.setPassword(pwField.getText().trim());
                        u.setRoles(roleBox.getValue());
                        u.setNumTel(telField.getText().trim().isEmpty() ? null : telField.getText().trim());
                        userService.modifier(u);
                        feedbackLabel.setStyle("-fx-text-fill: #2ed573;");
                        feedbackLabel.setText("✅ Utilisateur modifié.");
                    }
                    loadUsers();
                } catch (Exception ex) {
                    feedbackLabel.setStyle("-fx-text-fill: #ff4757;");
                    feedbackLabel.setText("❌ " + ex.getMessage());
                }
            }
        });
    }

    @FXML
    public void showUsers() { loadUsers(); }

    @FXML
    public void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gambatta.tn.ui/Login.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) userTable.getScene().getWindow();
            Scene scene = new Scene(root, 1280, 720);
            scene.getStylesheets().add(
                    getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm()
            );
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }
}