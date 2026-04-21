package gambatta.tn.ui;

import gambatta.tn.entites.tournois.inscriptiontournoi;
import gambatta.tn.services.tournoi.InscritournoiService;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.stream.Collectors;

public class AdminValidationTournoiController {

    @FXML private TableView<inscriptiontournoi> table;
    @FXML private TableColumn<inscriptiontournoi, Long> idCol;
    @FXML private TableColumn<inscriptiontournoi, String> equipeCol;
    @FXML private TableColumn<inscriptiontournoi, String> tournoiCol;
    @FXML private TableColumn<inscriptiontournoi, String> statusCol;
    @FXML private TableColumn<inscriptiontournoi, Void> colAccepter;
    @FXML private TableColumn<inscriptiontournoi, Void> colRefuser;

    private InscritournoiService service = new InscritournoiService();
    private ObservableList<inscriptiontournoi> pendingInscriptions = FXCollections.observableArrayList();

    public void initialize() {
        idCol.setCellValueFactory(data -> new SimpleLongProperty(data.getValue().getId()).asObject());
        equipeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEquipe().getNom()));
        tournoiCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTournoi().getNomt()));
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        colAccepter.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("✅ Accepter");
            {
                btn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-cursor: hand;");
                btn.setOnAction(event -> {
                    inscriptiontournoi i = getTableView().getItems().get(getIndex());
                    i.setStatus(inscriptiontournoi.STATUS_ACCEPTED);
                    if (service.save(i)) {
                        loadData();
                    }
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        colRefuser.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("❌ Refuser");
            {
                btn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-cursor: hand;");
                btn.setOnAction(event -> {
                    inscriptiontournoi i = getTableView().getItems().get(getIndex());
                    i.setStatus(inscriptiontournoi.STATUS_REFUSED);
                    if (service.save(i)) {
                        loadData();
                    }
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        loadData();
    }
    private void loadData() {
        List<inscriptiontournoi> all = service.findAll();
        List<inscriptiontournoi> pending = all.stream()
                .filter(i -> inscriptiontournoi.STATUS_PENDING.equalsIgnoreCase(i.getStatus()) 
                        || "pending".equalsIgnoreCase(i.getStatus())
                        || "EN_ATTENTE".equalsIgnoreCase(i.getStatus()))
                .collect(Collectors.toList());
        pendingInscriptions.setAll(pending);
        table.setItems(pendingInscriptions);
    }
}
