package gambatta.tn.ui;

import gambatta.tn.entites.tournois.TeamStanding;
import gambatta.tn.entites.tournois.rencontre;
import gambatta.tn.entites.tournois.tournoi;
import gambatta.tn.services.tournoi.RencontreService;
import gambatta.tn.services.tournoi.TournoiService;
import gambatta.tn.ui.components.BracketMatchCard;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FlashscoreController {

    @FXML private ComboBox<tournoi> cmbTournoi;
    @FXML private TableView<rencontre> table;
    @FXML private TableColumn<rencontre, String> dateCol;
    @FXML private TableColumn<rencontre, String> equipeACol;
    @FXML private TableColumn<rencontre, String> scoreCol;
    @FXML private TableColumn<rencontre, String> equipeBCol;
    @FXML private HBox bracketPane;

    @FXML private TableView<TeamStanding> standingsTable;
    @FXML private TableColumn<TeamStanding, String> teamCol;
    @FXML private TableColumn<TeamStanding, Integer> playedCol;
    @FXML private TableColumn<TeamStanding, Integer> wonCol;
    @FXML private TableColumn<TeamStanding, Integer> drawnCol;
    @FXML private TableColumn<TeamStanding, Integer> lostCol;
    @FXML private TableColumn<TeamStanding, Integer> gfCol;
    @FXML private TableColumn<TeamStanding, Integer> gaCol;
    @FXML private TableColumn<TeamStanding, Integer> gdCol;
    @FXML private TableColumn<TeamStanding, Integer> ptsCol;

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

        setupStandingsColumns();

        table.setPlaceholder(new Label("Aucun match trouvé pour ce tournoi."));
        standingsTable.setPlaceholder(new Label("Aucun classement disponible."));
    }

    private void setupStandingsColumns() {
        teamCol.setCellValueFactory(new PropertyValueFactory<>("teamName"));
        playedCol.setCellValueFactory(new PropertyValueFactory<>("played"));
        wonCol.setCellValueFactory(new PropertyValueFactory<>("won"));
        drawnCol.setCellValueFactory(new PropertyValueFactory<>("drawn"));
        lostCol.setCellValueFactory(new PropertyValueFactory<>("lost"));
        gfCol.setCellValueFactory(new PropertyValueFactory<>("goalsFor"));
        gaCol.setCellValueFactory(new PropertyValueFactory<>("goalsAgainst"));
        gdCol.setCellValueFactory(new PropertyValueFactory<>("goalDifference"));
        ptsCol.setCellValueFactory(new PropertyValueFactory<>("points"));
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
        buildBracket(list);
        buildStandings(list);
    }

    private void buildStandings(List<rencontre> list) {
        Map<String, TeamStanding> map = new HashMap<>();

        for (rencontre r : list) {
            if (r.getEquipeA() != null && r.getEquipeB() != null) {
                String nameA = r.getEquipeA().getNom();
                String nameB = r.getEquipeB().getNom();

                map.putIfAbsent(nameA, new TeamStanding(nameA));
                map.putIfAbsent(nameB, new TeamStanding(nameB));

                if (r.getScoreA() != null && r.getScoreB() != null) {
                    map.get(nameA).update(r.getScoreA(), r.getScoreB());
                    map.get(nameB).update(r.getScoreB(), r.getScoreA());
                }
            }
        }

        List<TeamStanding> sortedList = new ArrayList<>(map.values());
        // Trier par PTS (desc), puis Différence de buts (desc)
        sortedList.sort((s1, s2) -> {
            if (s1.getPoints() != s2.getPoints()) {
                return s2.getPoints() - s1.getPoints();
            }
            return s2.getGoalDifference() - s1.getGoalDifference();
        });

        standingsTable.setItems(FXCollections.observableArrayList(sortedList));
    }

    private void buildBracket(List<rencontre> list) {
        bracketPane.getChildren().clear();
        
        // Stages ordonnées
        String[] stages = {"HUITIEME", "QUART", "DEMI", "FINALE"};
        Map<String, List<rencontre>> grouped = new LinkedHashMap<>();
        for (String s : stages) grouped.put(s, new ArrayList<>());

        // Remplir avec les matchs existants
        for (rencontre r : list) {
            if (r.getStage() == null) continue;
            String key = r.getStage().toUpperCase();
            if (grouped.containsKey(key)) {
                grouped.get(key).add(r);
            }
        }

        boolean previousHadMatches = false;
        String lastFoundStage = null;

        for (String stageKey : stages) {
            List<rencontre> matches = grouped.get(stageKey);
            
            // Si pas de matchs mais l'étape précédente en avait : on force l'affichage d'un "Ghost" match
            if (matches.isEmpty() && previousHadMatches && !"FINALE".equals(lastFoundStage)) {
                VBox column = new VBox();
                column.getStyleClass().add("bracket-column");
                
                Label lblTitle = new Label(getStageLabel(stageKey));
                lblTitle.getStyleClass().add("bracket-stage-title");
                column.getChildren().add(lblTitle);

                // Ajouter un match "Fantôme" TBD
                rencontre ghost = new rencontre();
                ghost.setStage(stageKey);
                column.getChildren().add(new BracketMatchCard(ghost));
                
                bracketPane.getChildren().add(column);
                break; // On n'affiche qu'une seule étape "Ghost" d'avance
            }

            if (!matches.isEmpty()) {
                // Ajouter un connecteur si ce n'est pas la première colonne
                if (previousHadMatches) {
                    VBox connector = new VBox();
                    connector.getStyleClass().add("bracket-connector");
                    bracketPane.getChildren().add(connector);
                }

                VBox column = new VBox();
                column.getStyleClass().add("bracket-column");
                
                Label lblTitle = new Label(getStageLabel(stageKey));
                lblTitle.getStyleClass().add("bracket-stage-title");
                column.getChildren().add(lblTitle);

                for (rencontre r : matches) {
                    column.getChildren().add(new BracketMatchCard(r));
                }
                bracketPane.getChildren().add(column);
                previousHadMatches = true;
                lastFoundStage = stageKey;
            }
        }
    }

    private String getStageLabel(String stage) {
        switch (stage) {
            case "HUITIEME": return "HUITIÈMES";
            case "QUART": return "QUARTS";
            case "DEMI": return "DEMI-FINALES";
            case "FINALE": return "FINALE";
            default: return stage;
        }
    }

    @FXML
    public void actualiser() {
        if (cmbTournoi.getValue() != null) {
            loadRencontres(cmbTournoi.getValue().getId());
        }
    }
}
