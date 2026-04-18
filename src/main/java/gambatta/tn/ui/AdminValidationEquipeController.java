package gambatta.tn.ui;

import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.services.tournoi.EquipeService;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.stream.Collectors;

public class AdminValidationEquipeController {

    @FXML private TableView<equipe> table;
    @FXML private TableColumn<equipe, Long> idCol;
    @FXML private TableColumn<equipe, String> nomCol;
    @FXML private TableColumn<equipe, String> leaderCol;
    @FXML private TableColumn<equipe, String> objectifsCol;
    @FXML private TableColumn<equipe, String> statusCol;
    @FXML private TableColumn<equipe, Void> colAccepter;
    @FXML private TableColumn<equipe, Void> colRefuser;

    private EquipeService service = new EquipeService();
    private ObservableList<equipe> pendingEquipes = FXCollections.observableArrayList();

    public void initialize() {
        idCol.setCellValueFactory(data -> new SimpleLongProperty(data.getValue().getId()).asObject());
        nomCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNom()));
        leaderCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTeamLeader()));
        objectifsCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getObjectifs()));
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        colAccepter.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("✅ Accepter");
            {
                btn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-cursor: hand;");
                btn.setOnAction(event -> {
                    equipe e = getTableView().getItems().get(getIndex());
                    e.setStatus("VALIDE");
                    if (service.save(e)) {
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
                    equipe e = getTableView().getItems().get(getIndex());
                    e.setStatus("REFUSEE");
                    if (service.save(e)) { // Or service.delete(e.getId())
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
        List<equipe> all = service.findAll();
        List<equipe> pending = all.stream().filter(e -> "EN_ATTENTE".equals(e.getStatus())).collect(Collectors.toList());
        pendingEquipes.setAll(pending);
        table.setItems(pendingEquipes);
    }
}
