package gambatta.tn.controllers;

import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.entites.tournois.inscriptiontournoi;
import gambatta.tn.services.tournoi.EquipeService;
import gambatta.tn.services.tournoi.InscritournoiService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class InscriptionController {
    @FXML
    private Button btnTrouverTournoi;
    @FXML
    private TextField txtNomJoueur;

    @FXML
    private ComboBox<equipe> comboEquipe;

    @FXML
    private Button btnEnvoyer;

    private EquipeService equipeService = new EquipeService();
    private InscritournoiService inscritService = new InscritournoiService();

    @FXML
    public void initialize() {
        // Charger les équipes dans la ComboBox
        comboEquipe.setItems(FXCollections.observableArrayList(equipeService.findAll()));

        // Action du bouton ENVOYER LA DEMANDE
        btnEnvoyer.setOnAction(e -> envoyerDemande());
        btnTrouverTournoi.setOnAction(e -> opentrouverTournoiWindow());
    }

    private void opentrouverTournoiWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gambatta.tn.ui/trouverTournoiInterface.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm());
            
            // Récupérer la fenêtre actuelle et changer la scène
            Stage stage = (Stage) btnTrouverTournoi.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Inscription au Tournoi");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void envoyerDemande() {
        String nomJoueur = txtNomJoueur.getText().trim();
        equipe equipeExistante = comboEquipe.getSelectionModel().getSelectedItem();

        if (nomJoueur.isEmpty() || equipeExistante == null) {
            showAlert(Alert.AlertType.WARNING, "Veuillez remplir le nom du joueur et sélectionner une équipe.");
            return;
        }

        // Créer l'inscription
        inscriptiontournoi inscription = new inscriptiontournoi();
        inscription.setEquipe(equipeExistante);
        inscription.setTournoi(null); // si nécessaire, lier un tournoi spécifique
        inscription.setStatus(inscriptiontournoi.STATUS_PENDING);

        boolean saved = inscritService.save(inscription);

        if (saved) {
            showAlert(Alert.AlertType.INFORMATION, "✅ Demande envoyée pour rejoindre l'équipe " + equipeExistante.getNom() + " !");
            txtNomJoueur.clear();
            comboEquipe.getSelectionModel().clearSelection();
        } else {
            showAlert(Alert.AlertType.ERROR, "Une erreur est survenue lors de l'envoi de la demande.");
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}