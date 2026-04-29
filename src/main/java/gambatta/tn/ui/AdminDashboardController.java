package gambatta.tn.ui;

import gambatta.tn.entites.user.user;
import gambatta.tn.services.user.UserService;
import gambatta.tn.tools.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import gambatta.tn.services.user.AvatarService;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalAdminsLabel;
    @FXML private Label totalSimpleUsersLabel;
    @FXML private Label loginsTodayLabel;
    @FXML private Label feedbackLabel;

    @FXML private TextField searchField;

    @FXML private TableView<user> userTable;
    @FXML private TableColumn<user, String> colId;
    @FXML private TableColumn<user, String> colFirstName;
    @FXML private TableColumn<user, String> colLastName;
    @FXML private TableColumn<user, String> colEmail;
    @FXML private TableColumn<user, String> colTel;
    @FXML private TableColumn<user, String> colRole;
    @FXML private TableColumn<user, Void> colActions;

    private final UserService userService = new UserService();
    private ObservableList<user> allUsers = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        user current = Session.getCurrentUser();
        if (current != null) {
            welcomeLabel.setText("Bienvenue, " + current.getFirstName() + " " + current.getLastName());
        }

        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        userTable.setFixedCellSize(50);
        userTable.setPlaceholder(new Label("Aucun utilisateur trouvé."));

        initTable();
        configureTableAlignment();
        loadStats();
        loadUsers();
    }

    private void initTable() {
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colFirstName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFirstName()));
        colLastName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLastName()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colTel.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNumTel() == null || c.getValue().getNumTel().isBlank() ? "-" : c.getValue().getNumTel()
        ));
        colRole.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoles()));

        addActionsColumn();
        userTable.setPlaceholder(new Label("Aucun utilisateur trouvé."));
        userTable.setFixedCellSize(48);
        // Colonne avatar
        TableColumn<user, Void> colAvatar = new TableColumn<>("");
        colAvatar.setPrefWidth(52);
        colAvatar.setMinWidth(52);
        colAvatar.setMaxWidth(52);
        colAvatar.setSortable(false);
        colAvatar.setReorderable(false);
        colAvatar.setCellFactory(col -> new TableCell<>() {
            final ImageView iv = new ImageView();
            {
                iv.setFitWidth(36);
                iv.setFitHeight(36);
                Circle clip = new Circle(18, 18, 18);
                iv.setClip(clip);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    user u = getTableView().getItems().get(getIndex());
                    iv.setImage(AvatarService.loadAvatarAsync(u.getFirstName()));
                    setGraphic(iv);
                }
            }
        });
        userTable.getColumns().add(0, colAvatar);
    }

    private void loadStats() {
        totalUsersLabel.setText(String.valueOf(userService.countUsers()));
        totalAdminsLabel.setText(String.valueOf(userService.countAdmins()));
        totalSimpleUsersLabel.setText(String.valueOf(userService.countSimpleUsers()));
        loginsTodayLabel.setText(String.valueOf(userService.countLoginsToday()));
    }

    private void loadUsers() {
        List<user> users = userService.afficher();
        allUsers = FXCollections.observableArrayList(users);
        userTable.setItems(allUsers);
    }

    @FXML
    public void handleSearch() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        if (query.isEmpty()) {
            userTable.setItems(allUsers);
            return;
        }

        List<user> filtered = allUsers.stream()
                .filter(u ->
                        safe(u.getFirstName()).contains(query) ||
                                safe(u.getLastName()).contains(query) ||
                                safe(u.getEmail()).contains(query) ||
                                safe(u.getNumTel()).contains(query) ||
                                safe(u.getRoles()).contains(query)
                )
                .collect(Collectors.toList());

        userTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private String safe(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private void addActionsColumn() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("✏");
            private final Button deleteBtn = new Button("🗑");
            private final HBox box = new HBox(8, editBtn, deleteBtn);

            {
                box.setAlignment(Pos.CENTER);

                editBtn.getStyleClass().add("action-btn-edit");
                deleteBtn.getStyleClass().add("action-btn-delete");

                editBtn.setOnAction(event -> {
                    user selected = getTableView().getItems().get(getIndex());
                    showEditDialog(selected);
                });

                deleteBtn.setOnAction(event -> {
                    user selected = getTableView().getItems().get(getIndex());
                    confirmDelete(selected);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void confirmDelete(user u) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer utilisateur");
        alert.setHeaderText("Confirmer la suppression");
        alert.setContentText("Voulez-vous vraiment supprimer " + u.getFirstName() + " " + u.getLastName() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                userService.supprimer(u.getId());
                feedbackLabel.setText("Utilisateur supprimé avec succès.");
                loadUsers();
                loadStats();
            } catch (Exception e) {
                feedbackLabel.setText("Erreur suppression : " + e.getMessage());
            }
        }
    }

    private void showEditDialog(user u) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/EditUserPopup.fxml"));
            Parent root = loader.load();

            EditUserPopupController controller = loader.getController();
            controller.setUser(u);
            controller.setOnSaveCallback(() -> {
                feedbackLabel.setText("Utilisateur modifié avec succès.");
                loadUsers();
                loadStats();
            });

            Stage popupStage = new Stage();
            popupStage.setTitle("Modifier utilisateur");
            popupStage.initOwner(userTable.getScene().getWindow());
            popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());

            popupStage.setScene(scene);
            popupStage.setWidth(900);
            popupStage.setHeight(620);
            popupStage.setResizable(false);
            popupStage.showAndWait();

        } catch (Exception e) {
            feedbackLabel.setText("Erreur ouverture popup : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void openAdminProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/AdminProfile.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userTable.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            scene.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            feedbackLabel.setText("Erreur profil admin : " + e.getMessage());
        }
    }

    @FXML
    public void logout() {
        try {
            Session.clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userTable.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            scene.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            feedbackLabel.setText("Erreur logout : " + e.getMessage());
        }
    }

    private void configureTableAlignment() {
        colId.setStyle("-fx-alignment: CENTER;");
        colFirstName.setStyle("-fx-alignment: CENTER;");
        colLastName.setStyle("-fx-alignment: CENTER;");
        colEmail.setStyle("-fx-alignment: CENTER;");
        colTel.setStyle("-fx-alignment: CENTER;");
        colRole.setStyle("-fx-alignment: CENTER;");
        colActions.setStyle("-fx-alignment: CENTER;");

        colId.setSortable(false);
        colFirstName.setSortable(false);
        colLastName.setSortable(false);
        colEmail.setSortable(false);
        colTel.setSortable(false);
        colRole.setSortable(false);
        colActions.setSortable(false);

        colId.setReorderable(false);
        colFirstName.setReorderable(false);
        colLastName.setReorderable(false);
        colEmail.setReorderable(false);
        colTel.setReorderable(false);
        colRole.setReorderable(false);
        colActions.setReorderable(false);
    }
    @FXML
    public void openStats() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gambatta.tn.ui/Stats.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) userTable.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            scene.getStylesheets().add(
                    getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm()
            );
            stage.setScene(scene);
        } catch (Exception e) {
            feedbackLabel.setText("Erreur stats : " + e.getMessage());
        }
    }

}