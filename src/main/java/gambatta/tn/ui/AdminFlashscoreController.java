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

    @FXML private TableColumn<rencontre, String> equipeACol;
    @FXML private TableColumn<rencontre, String> scoreACol;
    @FXML private TableColumn<rencontre, String> scoreBCol;
    @FXML private TableColumn<rencontre, String> equipeBCol;
    @FXML private TableColumn<rencontre, String> dateCol;
    @FXML private TextField txtScoreA;
    @FXML private TextField txtScoreB;
    @FXML private Label globalMsg;

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
            showInlineMsg("⚠ Attention: Veuillez sélectionner un tournoi.", true);
            return;
        }

        // Récupérer les équipes de ce tournoi (validées)
        List<inscriptiontournoi> inscriptions = inscritournoiService.findAll().stream()
                .filter(i -> i.getTournoi() != null && i.getTournoi().getId().equals(t.getId()) 
                        && inscriptiontournoi.STATUS_ACCEPTED.equalsIgnoreCase(i.getStatus()))
                .collect(Collectors.toList());

        if (inscriptions.size() < 2) {
            showInlineMsg("⚠ Erreur IA: Il faut au moins 2 équipes acceptées pour générer des Playoffs.", true);
            return;
        }

        List<equipe> equipes = inscriptions.stream()
                .map(inscriptiontournoi::getEquipe)
                .filter(e -> e != null && e.getId() != null)
                .collect(Collectors.toList());

        Collections.shuffle(equipes);

        // Supprimer les anciens matchs pour ce tournoi avant de régénérer proprement
        List<rencontre> old = rencontreService.findByTournoi(t.getId());
        for(rencontre r : old) rencontreService.delete(r.getId());

        int n = equipes.size();
        int matchCreated = 0;

        // Déterminer le point de départ en fonction du nombre d'équipes (puissance de 2)
        if (n >= 8) {
            // Quarts de finale (on prend les 8 premières)
            for (int i = 0; i < 7; i += 2) {
                if (i + 1 < n) {
                    createMatch(t, equipes.get(i), equipes.get(i+1), "QUART", i);
                    matchCreated++;
                }
            }
            // Générer les placeholders pour Demis et Finale
            createPlaceholderMatches(t, 2, "DEMI", 4);
            createPlaceholderMatches(t, 1, "FINALE", 6);
        } else if (n >= 4) {
            // Demi-finales
            for (int i = 0; i < 3; i += 2) {
                if (i + 1 < n) {
                    createMatch(t, equipes.get(i), equipes.get(i+1), "DEMI", i);
                    matchCreated++;
                }
            }
            createPlaceholderMatches(t, 1, "FINALE", 2);
        } else if (n >= 2) {
            // Finale
            createMatch(t, equipes.get(0), equipes.get(1), "FINALE", 0);
            matchCreated++;
        }

        if (matchCreated > 0) {
            showInlineMsg("✅ Succès IA: Playoffs générés (" + n + " équipes) !", false);
        } else {
            showInlineMsg("⚠ Erreur IA: Impossible de générer des Playoffs.", true);
        }
        loadRencontres(t.getId());
    }

    private void createMatch(tournoi t, equipe a, equipe b, String stage, int dayOffset) {
        rencontre r = new rencontre();
        r.setTournoi(t);
        r.setEquipeA(a);
        r.setEquipeB(b);
        r.setStage(stage);
        r.setPlayedAt(LocalDateTime.now().plusDays(dayOffset));
        rencontreService.save(r);
    }

    private void createPlaceholderMatches(tournoi t, int count, String stage, int dayOffset) {
        for (int i = 0; i < count; i++) {
            rencontre r = new rencontre();
            r.setTournoi(t);
            r.setStage(stage);
            r.setPlayedAt(LocalDateTime.now().plusDays(dayOffset + i));
            // Pour les placeholders, equipeA et equipeB restent null (TBD)
            rencontreService.save(r);
        }
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
                if (rencontreService.save(r)) {
                    rencontreService.advanceWinner(r);
                    simulated++;
                }
            }
        }
        showInlineMsg("✅ Simulation IA: " + simulated + " scores ont été simulés.", false);
        loadRencontres(t.getId());
    }

    @FXML
    public void enregistrerScore() {
        rencontre r = table.getSelectionModel().getSelectedItem();
        if (r == null) {
            showInlineMsg("⚠ Erreur: Sélectionnez un match.", true);
            return;
        }
        try {
            int scoreA = Integer.parseInt(txtScoreA.getText().trim());
            int scoreB = Integer.parseInt(txtScoreB.getText().trim());
            r.setScoreA(scoreA);
            r.setScoreB(scoreB);
            r.setPlayedAt(LocalDateTime.now());
            if (rencontreService.save(r)) {
                rencontreService.advanceWinner(r); // Propager le vainqueur (crée le match suivant si besoin)
                loadRencontres(r.getTournoi().getId()); // Recharger tout pour voir le nouveau match
                txtScoreA.clear();
                txtScoreB.clear();
                showInlineMsg("✅ Score enregistré avec succès !", false);
            }
        } catch (NumberFormatException e) {
            showInlineMsg("⚠ Erreur: Veuillez saisir des nombres valides.", true);
        }
    }

    @FXML
    public void supprimerMatch() {
        rencontre r = table.getSelectionModel().getSelectedItem();
        if (r != null) {
            if (rencontreService.delete(r.getId())) {
                rencontres.remove(r);
                showInlineMsg("✅ Match supprimé.", false);
            }
        }
    }

    private void showInlineMsg(String msg, boolean isError) {
        if (globalMsg != null) {
            globalMsg.setText(msg);
            globalMsg.getStyleClass().removeAll("msg-success", "msg-error");
            globalMsg.getStyleClass().add(isError ? "msg-error" : "msg-success");
            if (!isError) {
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
                pause.setOnFinished(e -> globalMsg.setText(""));
                pause.play();
            }
        }
    }

    private void showAlert(String title, String msg) {
        // Obsolete pop-up - removed for inline messages.
    }
}
