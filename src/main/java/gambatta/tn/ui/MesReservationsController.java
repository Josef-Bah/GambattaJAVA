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
        reservationService.delete(selected.getId());
        loadReservations();
        clearFields();
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

    private void alert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}