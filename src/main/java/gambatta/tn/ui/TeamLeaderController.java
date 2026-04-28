package gambatta.tn.ui;

import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.entites.tournois.playerjoinrequest;
import gambatta.tn.services.tournoi.EquipeService;
import gambatta.tn.services.tournoi.PlayerJoinRequestService;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class TeamLeaderController {

    @FXML private ComboBox<equipe> cmbEquipe;
    @FXML private TableView<playerjoinrequest> table;

    @FXML private TableColumn<playerjoinrequest, String> playerCol;
    @FXML private TableColumn<playerjoinrequest, String> equipeCol;
    @FXML private TableColumn<playerjoinrequest, String> dateCol;
    @FXML private TableColumn<playerjoinrequest, String> statusCol;
    @FXML private TableColumn<playerjoinrequest, Void>   colAccepter;
    @FXML private TableColumn<playerjoinrequest, Void>   colRefuser;

    private PlayerJoinRequestService service     = new PlayerJoinRequestService();
    private EquipeService            equipeService = new EquipeService();
    private ObservableList<playerjoinrequest> data = FXCollections.observableArrayList();
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void initialize() {
        // Colonnes

        playerCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPlayerName()));
        equipeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEquipe() != null ? d.getValue().getEquipe().getNom() : "-"));
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCreatedAt() != null ? d.getValue().getCreatedAt().format(dtf) : "-"));
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));

        // Bouton Accepter
        colAccepter.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("✅ Accepter");
            {
                btn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-cursor: hand;");
                btn.setOnAction(e -> {
                    playerjoinrequest r = getTableView().getItems().get(getIndex());
                    r.setStatus(playerjoinrequest.STATUS_ACCEPTED);
                    if (service.save(r)) loadData(null);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        // Bouton Refuser
        colRefuser.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("❌ Refuser");
            {
                btn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-cursor: hand;");
                btn.setOnAction(e -> {
                    playerjoinrequest r = getTableView().getItems().get(getIndex());
                    r.setStatus(playerjoinrequest.STATUS_REFUSED);
                    if (service.save(r)) loadData(null);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        // ComboBox équipes
        List<equipe> equipes = equipeService.findAll();
        cmbEquipe.setItems(FXCollections.observableArrayList(equipes));
        cmbEquipe.setConverter(new StringConverter<equipe>() {
            @Override public String toString(equipe o) { return o != null ? o.getNom() : ""; }
            @Override public equipe fromString(String s) { return null; }
        });
        cmbEquipe.valueProperty().addListener((obs, ov, nv) -> loadData(nv));

        loadData(null);
    }

    private void loadData(equipe filter) {
        List<playerjoinrequest> all = service.findAll();
        if (filter != null) {
            all = all.stream()
                     .filter(r -> r.getEquipe() != null && r.getEquipe().getId().equals(filter.getId()))
                     .collect(Collectors.toList());
        }
        // N'afficher que les "pending"
        all = all.stream()
                 .filter(r -> playerjoinrequest.STATUS_PENDING.equals(r.getStatus()))
                 .collect(Collectors.toList());
        data.setAll(all);
        table.setItems(data);
    }

    @FXML
    public void showAll() {
        cmbEquipe.getSelectionModel().clearSelection();
        loadData(null);
    }
}
