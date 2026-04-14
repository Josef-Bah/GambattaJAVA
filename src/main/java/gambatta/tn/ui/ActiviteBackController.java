package gambatta.tn.ui;

import gambatta.tn.entites.activites.ReservationActivite;
import gambatta.tn.entites.activites.activite;
import gambatta.tn.entites.activites.rules;
import gambatta.tn.services.activites.ActiviteService;
import gambatta.tn.services.activites.ReservationActiviteService;
import gambatta.tn.services.activites.RulesService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;

public class ActiviteBackController {

    // --- TAB ACTIVITES ---
    @FXML private TableView<activite> tableActivites;
    @FXML private TableColumn<activite, String> colActNom;
    @FXML private TableColumn<activite, String> colActType;
    @FXML private TableColumn<activite, String> colActAdresse;
    
    @FXML private TextField tfActNom;
    @FXML private TextField tfActType;
    @FXML private TextField tfActDispo;
    @FXML private TextField tfActAdresse;
    @FXML private TextArea taActDesc;

    // --- TAB RULES ---
    @FXML private TableView<rules> tableRules;
    @FXML private TableColumn<rules, String> colRuleId;
    @FXML private TableColumn<rules, String> colRuleDesc;
    @FXML private TableColumn<rules, String> colRuleAct;

    @FXML private ComboBox<activite> cbRuleActivite;
    @FXML private TextArea taRuleDesc;

    // --- TAB RESERVATIONS ---
    @FXML private TableView<ReservationActivite> tableReservations;
    @FXML private TableColumn<ReservationActivite, String> colResDate;
    @FXML private TableColumn<ReservationActivite, String> colResHeure;
    @FXML private TableColumn<ReservationActivite, String> colResStatut;
    @FXML private TableColumn<ReservationActivite, String> colResAct;

    private ActiviteService activiteService = new ActiviteService();
    private RulesService rulesService = new RulesService();
    private ReservationActiviteService reservationService = new ReservationActiviteService();

    @FXML
    public void initialize() {
        // Init Activites table
        colActNom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNoma()));
        colActType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTypea()));
        colActAdresse.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAdresse()));
        
        tableActivites.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                tfActNom.setText(newSel.getNoma());
                tfActType.setText(newSel.getTypea());
                tfActDispo.setText(newSel.getDispoa());
                tfActAdresse.setText(newSel.getAdresse());
                taActDesc.setText(newSel.getDescria());
            }
        });

        // Init Rules table
        colRuleId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colRuleDesc.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRuleDescription()));
        colRuleAct.setCellValueFactory(data -> {
            activite a = getActiviteById(data.getValue().getActiviteId());
            return new SimpleStringProperty(a != null ? a.getNoma() : "Inconnu");
        });
        
        tableRules.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                taRuleDesc.setText(newSel.getRuleDescription());
                cbRuleActivite.setValue(getActiviteById(newSel.getActiviteId()));
            }
        });

        // Init Reservations table
        colResDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDatedebut().toString()));
        colResHeure.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getHeurer()));
        colResStatut.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatutr()));
        colResAct.setCellValueFactory(data -> {
            activite a = getActiviteById(data.getValue().getActiviteId());
            return new SimpleStringProperty(a != null ? a.getNoma() : "Inconnu");
        });

        refreshAll();
    }

    private void refreshAll() {
        tableActivites.setItems(FXCollections.observableArrayList(activiteService.getAll()));
        tableRules.setItems(FXCollections.observableArrayList(rulesService.getAll()));
        tableReservations.setItems(FXCollections.observableArrayList(reservationService.getAll()));
        cbRuleActivite.setItems(FXCollections.observableArrayList(activiteService.getAll()));
    }
    
    private activite getActiviteById(int id) {
        return activiteService.getAll().stream().filter(a -> a.getId() == id).findFirst().orElse(null);
    }

    // --- ACTIVITE HANDLERS ---
    @FXML void addActivite() {
        activite a = new activite(tfActNom.getText(), tfActType.getText(), tfActDispo.getText(), taActDesc.getText(), "", tfActAdresse.getText(), false);
        activiteService.add(a);
        refreshAll();
    }
    @FXML void updateActivite() {
        activite selected = tableActivites.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setNoma(tfActNom.getText());
            selected.setTypea(tfActType.getText());
            selected.setDispoa(tfActDispo.getText());
            selected.setDescria(taActDesc.getText());
            selected.setAdresse(tfActAdresse.getText());
            activiteService.update(selected);
            refreshAll();
        }
    }
    @FXML void deleteActivite() {
        activite selected = tableActivites.getSelectionModel().getSelectedItem();
        if (selected != null) {
            activiteService.delete(selected.getId());
            refreshAll();
        }
    }

    // --- RULES HANDLERS ---
    @FXML void addRule() {
        if (cbRuleActivite.getValue() != null) {
            rules r = new rules();
            r.setActiviteId(cbRuleActivite.getValue().getId());
            r.setRuleDescription(taRuleDesc.getText());
            rulesService.add(r);
            refreshAll();
        }
    }
    @FXML void updateRule() {
        rules selected = tableRules.getSelectionModel().getSelectedItem();
        if (selected != null && cbRuleActivite.getValue() != null) {
            selected.setActiviteId(cbRuleActivite.getValue().getId());
            selected.setRuleDescription(taRuleDesc.getText());
            rulesService.update(selected);
            refreshAll();
        }
    }
    @FXML void deleteRule() {
        rules selected = tableRules.getSelectionModel().getSelectedItem();
        if (selected != null) {
            rulesService.delete(selected.getId());
            refreshAll();
        }
    }

    // --- RESERVATIONS HANDLERS ---
    @FXML void validerReservation() {
        ReservationActivite selected = tableReservations.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setStatutr("VALIDE");
            reservationService.update(selected);
            refreshAll();
        }
    }
    @FXML void refuserReservation() {
        ReservationActivite selected = tableReservations.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setStatutr("REFUSE");
            reservationService.update(selected);
            refreshAll();
        }
    }

    @FXML void handleBackFront() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/activites/ActiviteFront.fxml"));
            tableActivites.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
