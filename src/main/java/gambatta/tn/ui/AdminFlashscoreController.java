package gambatta.tn.ui;

import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.entites.tournois.inscriptiontournoi;
import gambatta.tn.entites.tournois.rencontre;
import gambatta.tn.entites.tournois.tournoi;
import gambatta.tn.services.tournoi.InscritournoiService;
import gambatta.tn.services.tournoi.RencontreService;
import gambatta.tn.services.tournoi.TournoiService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class AdminFlashscoreController {

    @FXML private ComboBox<tournoi> cmbTournoi;
    @FXML private TableView<rencontre> table;
    @FXML private TableColumn<rencontre, Long> idCol;
    @FXML private TableColumn<rencontre, String> equipeACol;
    @FXML private TableColumn<rencontre, String> scoreACol;
    @FXML private TableColumn<rencontre, String> scoreBCol;
    @FXML private TableColumn<rencontre, String> equipeBCol;
    @FXML private TableColumn<rencontre, String> dateCol;
    @FXML private TextField txtScoreA;
    @FXML private TextField txtScoreB;

    private TournoiService tournoiService = new TournoiService();
    private RencontreService rencontreService = new RencontreService();
    private InscritournoiService inscritournoiService = new InscritournoiService();
    private ObservableList<rencontre> rencontres = FXCollections.observableArrayList();

    public void initialize() {
        cmbTournoi.setItems(FXCollections.observableArrayList(tournoiService.findAll()));
        cmbTournoi.setConverter(new StringConverter<tournoi>() {
            @Override public String toString(tournoi object) { return object != null ? object.getNomt() : ""; }
            @Override public tournoi fromString(String string) { return null; }
        });

        cmbTournoi.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) loadRencontres(newVal.getId());
        });

        idCol.setCellValueFactory(d -> new SimpleLongProperty(d.getValue().getId()).asObject());
        equipeACol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEquipeA().getNom()));
        equipeBCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEquipeB().getNom()));
        
        scoreACol.setCellValueFactory(d -> {
            Integer s = d.getValue().getScoreA();
            return new SimpleStringProperty(s != null ? s.toString() : "-");
        });
        scoreBCol.setCellValueFactory(d -> {
            Integer s = d.getValue().getScoreB();
            return new SimpleStringProperty(s != null ? s.toString() : "-");
        });
        dateCol.setCellValueFactory(d -> {
            LocalDateTime dt = d.getValue().getPlayedAt();
            return new SimpleStringProperty(dt != null ? dt.toString() : "TBD");
        });

        table.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
            if (nv != null && nv.getScoreA() != null && nv.getScoreB() != null) {
                txtScoreA.setText(nv.getScoreA().toString());
                txtScoreB.setText(nv.getScoreB().toString());
            } else {
                txtScoreA.clear();
                txtScoreB.clear();
            }
        });
    }

    private void loadRencontres(Long idTournoi) {
        rencontres.setAll(rencontreService.findByTournoi(idTournoi));
        table.setItems(rencontres);
    }

    @FXML
    public void genererMatchsIA() {
        tournoi t = cmbTournoi.getValue();
        if (t == null) {
            showAlert("Attention", "Veuillez sélectionner un tournoi.");
            return;
        }

        // Récupérer les équipes de ce tournoi (validées)
        List<inscriptiontournoi> inscriptions = inscritournoiService.findAll().stream()
                .filter(i -> i.getTournoi().getId().equals(t.getId()) && "ACCEPTED".equals(i.getStatus()))
                .collect(Collectors.toList());

        if (inscriptions.size() < 2) {
            showAlert("Erreur IA", "Il faut au moins 2 équipes acceptées dans ce tournoi pour générer des matchs.");
            return;
        }

        List<equipe> equipes = inscriptions.stream().map(inscriptiontournoi::getEquipe).collect(Collectors.toList());
        Collections.shuffle(equipes); // Tirage IA simple

        int matchCreated = 0;
        // Créer des paires
        for (int i = 0; i < equipes.size() - 1; i += 2) {
            rencontre r = new rencontre();
            r.setTournoi(t);
            r.setEquipeA(equipes.get(i));
            r.setEquipeB(equipes.get(i+1));
            r.setPlayedAt(LocalDateTime.now().plusDays(i)); // simul date
            if (rencontreService.save(r)) matchCreated++;
        }

        showAlert("Succès IA", "L'IA a généré " + matchCreated + " matchs de ligue !");
        loadRencontres(t.getId());
    }

    @FXML
    public void simulerScoresIA() {
        tournoi t = cmbTournoi.getValue();
        if (t == null) return;
        List<rencontre> currentMatches = rencontreService.findByTournoi(t.getId());
        Random random = new Random();
        int simulated = 0;
        for (rencontre r : currentMatches) {
            if (r.getScoreA() == null || r.getScoreB() == null) {
                // Simulation intelligente simple
                r.setScoreA(random.nextInt(4)); // 0 à 3
                r.setScoreB(random.nextInt(4));
                r.setPlayedAt(LocalDateTime.now());
                if (rencontreService.save(r)) simulated++;
            }
        }
        showAlert("Simulation IA", simulated + " scores ont été simulés par l'IA.");
        loadRencontres(t.getId());
    }

    @FXML
    public void enregistrerScore() {
        rencontre r = table.getSelectionModel().getSelectedItem();
        if (r == null) {
            showAlert("Erreur", "Sélectionnez un match.");
            return;
        }
        try {
            int scoreA = Integer.parseInt(txtScoreA.getText().trim());
            int scoreB = Integer.parseInt(txtScoreB.getText().trim());
            r.setScoreA(scoreA);
            r.setScoreB(scoreB);
            r.setPlayedAt(LocalDateTime.now());
            if (rencontreService.save(r)) {
                table.refresh();
                txtScoreA.clear();
                txtScoreB.clear();
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez saisir des nombres valides.");
        }
    }

    @FXML
    public void supprimerMatch() {
        rencontre r = table.getSelectionModel().getSelectedItem();
        if (r != null) {
            if (rencontreService.delete(r.getId())) {
                rencontres.remove(r);
            }
        }
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
