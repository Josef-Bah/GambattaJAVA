package gambatta.tn.ui;

import gambatta.tn.entites.tournois.rencontre;
import gambatta.tn.entites.tournois.tournoi;
import gambatta.tn.services.tournoi.RencontreService;
import gambatta.tn.services.tournoi.TournoiService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class FlashscoreController {

    @FXML private ComboBox<tournoi> cmbTournoi;
    @FXML private TableView<rencontre> table;
    @FXML private TableColumn<rencontre, String> dateCol;
    @FXML private TableColumn<rencontre, String> equipeACol;
    @FXML private TableColumn<rencontre, String> scoreCol;
    @FXML private TableColumn<rencontre, String> equipeBCol;

    private TournoiService tournoiService = new TournoiService();
    private RencontreService rencontreService = new RencontreService();
    private ObservableList<rencontre> rencontres = FXCollections.observableArrayList();
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void initialize() {
        cmbTournoi.setItems(FXCollections.observableArrayList(tournoiService.findAll()));
        cmbTournoi.setConverter(new StringConverter<tournoi>() {
            @Override public String toString(tournoi object) { return object != null ? object.getNomt() : ""; }
            @Override public tournoi fromString(String string) { return null; }
        });

        cmbTournoi.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) loadRencontres(newVal.getId());
        });

        dateCol.setCellValueFactory(d -> {
            if (d.getValue().getPlayedAt() != null) {
                return new SimpleStringProperty(d.getValue().getPlayedAt().format(dtf));
            }
            return new SimpleStringProperty("À venir");
        });

        equipeACol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEquipeA().getNom()));
        equipeBCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEquipeB().getNom()));
        
        scoreCol.setCellValueFactory(d -> {
            Integer sA = d.getValue().getScoreA();
            Integer sB = d.getValue().getScoreB();
            if (sA != null && sB != null) {
                return new SimpleStringProperty(sA + " - " + sB);
            } else {
                return new SimpleStringProperty("vs");
            }
        });

        table.setPlaceholder(new Label("Aucun match trouvé pour ce tournoi."));
    }

    private void loadRencontres(Long idTournoi) {
        List<rencontre> list = rencontreService.findByTournoi(idTournoi);
        // Trier par date décroissante (les plus récents en premier)
        list.sort((r1, r2) -> {
            if (r1.getPlayedAt() == null) return 1;
            if (r2.getPlayedAt() == null) return -1;
            return r2.getPlayedAt().compareTo(r1.getPlayedAt());
        });
        rencontres.setAll(list);
        table.setItems(rencontres);
    }

    @FXML
    public void actualiser() {
        if (cmbTournoi.getValue() != null) {
            loadRencontres(cmbTournoi.getValue().getId());
        }
    }
}
