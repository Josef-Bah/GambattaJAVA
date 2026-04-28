package gambatta.tn.ui;

import gambatta.tn.entites.activites.ReservationActivite;
import gambatta.tn.entites.activites.activite;
import gambatta.tn.services.activites.ActiviteService;
import gambatta.tn.services.activites.ReservationActiviteService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.geometry.Insets;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;

import java.util.Date;
import java.util.List;

public class MesReservationsController {

    @FXML private TableView<ReservationActivite> tableReservations;
    @FXML private TableColumn<ReservationActivite, String> colDate;
    @FXML private TableColumn<ReservationActivite, String> colHeure;
    @FXML private TableColumn<ReservationActivite, String> colStatut;
    @FXML private TableColumn<ReservationActivite, String> colActivite;
    
    @FXML private ComboBox<activite> activiteCombo;
    @FXML private DatePicker datePicker;
    @FXML private TextField tfHeure;
    
    private ReservationActiviteService reservationService = new ReservationActiviteService();
    private ActiviteService activiteService = new ActiviteService();
    
    private ObservableList<ReservationActivite> reservationsList;

    private int currentUserId = 1; // Assuming connected user is 1

    @FXML
    public void initialize() {
        // Init table columns
        colDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDatedebut().toString()));
        colHeure.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHeurer()));
        colStatut.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatutr()));
        colActivite.setCellValueFactory(cellData -> {
            activite a = activiteService.getAll().stream()
                    .filter(act -> act.getId() == cellData.getValue().getActiviteId())
                    .findFirst().orElse(null);
            return new SimpleStringProperty(a != null ? a.getNoma() : "Inconnu");
        });

        loadReservations();
        
        // Load combo box
        List<activite> acts = activiteService.getAll();
        activiteCombo.setItems(FXCollections.observableArrayList(acts));
        
        tableReservations.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                tfHeure.setText(newSel.getHeurer());
                // Simple mapping back
                activite a = activiteService.getAll().stream()
                        .filter(act -> act.getId() == newSel.getActiviteId())
                        .findFirst().orElse(null);
                activiteCombo.setValue(a);
            }
        });
    }

    private void loadReservations() {
        reservationsList = FXCollections.observableArrayList(reservationService.getByUserId(currentUserId));
        tableReservations.setItems(reservationsList);
    }

    private boolean validateSaisie() {
        boolean valid = true;

        // Reset
        activiteCombo.getStyleClass().remove("error-field");
        datePicker.getStyleClass().remove("error-field");
        tfHeure.getStyleClass().remove("error-field");

        if (activiteCombo.getValue() == null) {
            activiteCombo.getStyleClass().add("error-field");
            valid = false;
        }
        if (datePicker.getValue() == null) {
            datePicker.getStyleClass().add("error-field");
            valid = false;
        }
        if (tfHeure.getText() == null || tfHeure.getText().trim().isEmpty()) {
            tfHeure.getStyleClass().add("error-field");
            valid = false;
        }

        return valid;
    }

    @FXML
    void handleAdd() {
        if (!validateSaisie()) return;

        ReservationActivite r = new ReservationActivite();
        r.setActiviteId(activiteCombo.getValue().getId());
        r.setUserId(currentUserId);
        r.setDatedebut(java.sql.Date.valueOf(datePicker.getValue()));
        r.setHeurer(tfHeure.getText());
        r.setStatutr("EN_ATTENTE");

        reservationService.add(r);
        loadReservations();
        clearFields();
    }

    @FXML
    void handleModify() {
        ReservationActivite selected = tableReservations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert("Erreur", "Veuillez sélectionner une réservation");
            return;
        }
        
        if (!validateSaisie()) return;
        
        selected.setActiviteId(activiteCombo.getValue().getId());
        selected.setDatedebut(java.sql.Date.valueOf(datePicker.getValue()));
        selected.setHeurer(tfHeure.getText());
        
        reservationService.update(selected);
        loadReservations();
    }

    @FXML
    void handleDelete() {
        ReservationActivite selected = tableReservations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert("Erreur", "Veuillez sélectionner une réservation");
            return;
        }
        showPurgeConfirm("la réservation du " + selected.getDatedebut(), () -> {
            reservationService.delete(selected.getId());
            loadReservations();
            clearFields();
        });
    }

    private void clearFields() {
        activiteCombo.setValue(null);
        datePicker.setValue(null);
        tfHeure.clear();
    }

    @FXML
    void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/activites/ActiviteFront.fxml"));
            tableReservations.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void alert(String header, String body) {
        javafx.stage.Stage st = new javafx.stage.Stage();
        st.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        st.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(15);
        root.setAlignment(javafx.geometry.Pos.CENTER);
        root.setPadding(new javafx.geometry.Insets(25));
        root.setPrefWidth(380);
        root.setStyle("-fx-background-color: #0f172a; -fx-background-radius: 20; -fx-border-color: #FFD700; -fx-border-width: 2; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 20, 0, 0, 10);");

        Label tLbl = new Label("MESSAGE GAMBATTA");
        tLbl.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 11px; -fx-font-weight: bold; -fx-letter-spacing: 1px;");
        
        Label hLbl = new Label(header);
        hLbl.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label bLbl = new Label(body);
        bLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 13px;");
        bLbl.setWrapText(true);
        bLbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Button btn = new Button("D'ACCORD");
        btn.setStyle("-fx-background-color: linear-gradient(to right, #FFD700, #ff9f43); -fx-text-fill: #020617; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 30; -fx-cursor: hand;");
        btn.setOnAction(e -> st.close());

        root.getChildren().addAll(tLbl, hLbl, bLbl, btn);
        javafx.scene.Scene sc = new javafx.scene.Scene(root);
        sc.setFill(javafx.scene.paint.Color.TRANSPARENT);
        st.setScene(sc);
        st.showAndWait();
    }
    private void showPurgeConfirm(String itemName, Runnable onConfirm) {
        Stage st = new Stage();
        st.initStyle(StageStyle.TRANSPARENT);
        st.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(25);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setPrefWidth(420);
        root.setStyle("-fx-background-color: #0f172a; -fx-background-radius: 15; -fx-border-color: #ff4757; -fx-border-width: 2; -fx-effect: dropshadow(gaussian, rgba(255,71,87,0.3), 20, 0, 0, 0);");

        Label titleLbl = new Label("PURGE SYSTÈME");
        titleLbl.setStyle("-fx-text-fill: #ff4757; -fx-font-size: 32px; -fx-font-weight: 900; -fx-letter-spacing: 2px;");
        
        Label msgLbl = new Label("Supprimer définitivement " + itemName + " ?");
        msgLbl.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-text-alignment: center;");
        msgLbl.setWrapText(true);

        HBox btnBox = new HBox(20);
        btnBox.setAlignment(Pos.CENTER);

        Button btnAbort = new Button("ABORT");
        btnAbort.setPrefWidth(120);
        btnAbort.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-border-color: #475569; -fx-border-radius: 20; -fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 10;");
        btnAbort.setOnAction(e -> st.close());

        Button btnTerminate = new Button("TERMINATE");
        btnTerminate.setPrefWidth(140);
        btnTerminate.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff4757; -fx-border-color: #ff4757; -fx-border-radius: 20; -fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 10;");
        btnTerminate.setOnAction(e -> {
            st.close();
            onConfirm.run();
        });

        btnBox.getChildren().addAll(btnAbort, btnTerminate);
        root.getChildren().addAll(titleLbl, msgLbl, btnBox);

        Scene sc = new Scene(root);
        sc.setFill(Color.TRANSPARENT);
        st.setScene(sc);
        st.showAndWait();
    }
}