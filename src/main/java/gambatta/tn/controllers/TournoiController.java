package gambatta.tn.controllers;

import gambatta.tn.entites.tournois.tournoi;
import gambatta.tn.services.tournoi.TournoiService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDateTime;
import java.util.List;

public class TournoiController {

    @FXML private TextField nomField;
    @FXML private TextField descField;
    @FXML private TextField statutField;

    @FXML private TableView<tournoi> table;
    @FXML private TableColumn<tournoi, Long> idCol;
    @FXML private TableColumn<tournoi, String> nomCol;
    @FXML private TableColumn<tournoi, String> statutCol;

    private TournoiService service = new TournoiService();

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleLongProperty(data.getValue().getId()).asObject());
        nomCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNomt()));
        statutCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatutt()));

        loadData();
    }

    private void loadData() {
        List<tournoi> list = service.findAll();
        table.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    public void addTournoi() {
        tournoi t = new tournoi();
        t.setNomt(nomField.getText());
        t.setDescrit(descField.getText());
        t.setStatutt(statutField.getText());
        t.setDatedebutt(LocalDateTime.now());
        t.setDatefint(LocalDateTime.now().plusDays(3));

        service.add(t);
        loadData();
    }

    @FXML
    public void deleteTournoi() {
        tournoi selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            service.delete(selected.getId());
            loadData();
        }
    }
}