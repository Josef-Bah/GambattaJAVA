package gambatta.tn.ui;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import java.util.Optional;

public class DetailsActiviteController {

    @FXML private javafx.scene.image.ImageView ivHero;
    @FXML private Label lblNom;
    @FXML private Label lblType;
    @FXML private Label lblDispo;
    @FXML private Label lblAdresse;
    @FXML private javafx.scene.control.TextArea taDesc;

    private gambatta.tn.entites.activites.activite currentActivity;
    private gambatta.tn.services.activites.ReservationActiviteService reservationService = new gambatta.tn.services.activites.ReservationActiviteService();

    @FXML
    public void initialize() {
    }

    @FXML
    private void handleRetour() {
        javafx.stage.Stage stage = (javafx.stage.Stage) lblNom.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void handleReserver() {
        if (currentActivity == null) return;

        boolean userConfirmed = true;

        if (currentActivity.getTypea() != null && currentActivity.getTypea().toLowerCase().contains("sport")) {
            Alert weatherAlert = new Alert(Alert.AlertType.CONFIRMATION);
            weatherAlert.setTitle("Météo & Confirmation");
            weatherAlert.setHeaderText(null);
            
            VBox weatherBox = new VBox(15);
            weatherBox.setAlignment(Pos.CENTER);
            weatherBox.setStyle("-fx-background-color: linear-gradient(to bottom, #1c2541, #0b132b); -fx-padding: 30; -fx-background-radius: 20;");
            
            Label locLabel = new Label("📍 Emplacement: " + currentActivity.getAdresse());
            locLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");

            Label tempLabel = new Label("22°C 🌤");
            tempLabel.setStyle("-fx-text-fill: white; -fx-font-size: 48px; -fx-font-weight: bold;");

            Label condLabel = new Label("Temps idéal pour faire du sport !\\nVent faible, partiellement nuageux.");
            condLabel.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 14px;");
            condLabel.setAlignment(Pos.CENTER);
            condLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

            Label qLabel = new Label("Voulez-vous vraiment réserver ?");
            qLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10 0 0 0;");

            weatherBox.getChildren().addAll(locLabel, tempLabel, condLabel, qLabel);
            
            weatherAlert.getDialogPane().setContent(weatherBox);
            weatherAlert.getDialogPane().setStyle("-fx-background-color: #0b132b; -fx-border-color: #f59e0b; -fx-border-width: 2px; -fx-border-radius: 10px;");
            
            ButtonType btnYes = new ButtonType("Oui, Réserver", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
            ButtonType btnNo = new ButtonType("Annuler", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
            weatherAlert.getButtonTypes().setAll(btnYes, btnNo);

            Optional<ButtonType> result = weatherAlert.showAndWait();
            if (!result.isPresent() || result.get() != btnYes) {
                userConfirmed = false;
            }
        }

        if (userConfirmed) {
            gambatta.tn.entites.activites.ReservationActivite r = new gambatta.tn.entites.activites.ReservationActivite(
                    new java.util.Date(),
                    "10:00",
                    "EN_ATTENTE",
                    currentActivity.getId(),
                    1,
                    null
            );
            reservationService.add(r);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Réservation confirmée");
            alert.setHeaderText("Félicitations");
            
            VBox box = new VBox(10);
            box.setStyle("-fx-background-color: #0f172a; -fx-padding: 20;");
            Label lbl = new Label("Votre demande de réservation pour " + currentActivity.getNoma() + " a été ajoutée à vos réservations.");
            lbl.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            lbl.setWrapText(true);
            box.getChildren().add(lbl);
            alert.getDialogPane().setContent(box);
            alert.getDialogPane().setStyle("-fx-background-color: #0f172a;");

            alert.showAndWait();
        }
    }

    public void setActivityDetails(gambatta.tn.entites.activites.activite activity, String imgUrl) {
        this.currentActivity = activity;
        lblNom.setText(activity.getNoma().toUpperCase());
        lblType.setText(activity.getTypea());
        lblDispo.setText(activity.getDispoa());
        lblAdresse.setText(activity.getAdresse());
        taDesc.setText(activity.getDescria());
        
        try {
            if(imgUrl != null && !imgUrl.isEmpty()){
                ivHero.setImage(new javafx.scene.image.Image(imgUrl, true));
            }
        } catch(Exception e) {}
    }
}
