package gambatta.tn.controllers;

import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.entites.tournois.inscriptiontournoi;
import gambatta.tn.services.tournoi.EquipeService;
import gambatta.tn.services.tournoi.InscritournoiService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class InscriptionController {

    @FXML
    private TextField txtNomJoueur;

    @FXML
    private TextField txtEquipe;

    @FXML
    private Button btnEnvoyer;

    private EquipeService equipeService = new EquipeService();
    private InscritournoiService inscritService = new InscritournoiService();

    @FXML
    public void initialize() {
        // Action du bouton ENVOYER LA DEMANDE
        btnEnvoyer.setOnAction(e -> envoyerDemande());
    }

    private void envoyerDemande() {
        String nomJoueur = txtNomJoueur.getText().trim();
        String nomEquipe = txtEquipe.getText().trim();

        if (nomJoueur.isEmpty() || nomEquipe.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Veuillez remplir le nom du joueur et l'équipe.");
            return;
        }

        // Chercher l'équipe par nom
        equipe equipeExistante = equipeService.findByName(nomEquipe);
        if (equipeExistante == null) {
            showAlert(Alert.AlertType.ERROR, "L'équipe \"" + nomEquipe + "\" n'existe pas.");
            return;
        }

        // Créer l'inscription
        inscriptiontournoi inscription = new inscriptiontournoi();
        equipe newEquipe = equipeExistante; // relier l'équipe existante
        inscription.setEquipe(newEquipe);
        inscription.setTournoi(null); // si nécessaire, lier un tournoi spécifique
        inscription.setStatus(inscriptiontournoi.STATUS_PENDING);

        boolean saved = inscritService.save(inscription);

        if (saved) {
            showAlert(Alert.AlertType.INFORMATION, "✅ Demande envoyée pour rejoindre l'équipe " + nomEquipe + " !");
            txtNomJoueur.clear();
            txtEquipe.clear();
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