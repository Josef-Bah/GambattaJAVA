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

        if (currentActivity.getTypea() != null && currentActivity.getTypea().toLowerCase().contains("sport")) {
            showModernWeatherModal();
        } else {
            confirmReservation();
        }
    }

    private void showModernWeatherModal() {
        javafx.stage.Stage modalStage = new javafx.stage.Stage();
        modalStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        modalStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        modalStage.initOwner(lblNom.getScene().getWindow());

        VBox weatherBox = new VBox(20);
        weatherBox.setAlignment(Pos.CENTER);
        weatherBox.setStyle("-fx-background-color: rgba(15, 23, 42, 0.95); -fx-padding: 40; -fx-background-radius: 25; -fx-border-color: rgba(255, 215, 0, 0.5); -fx-border-radius: 25; -fx-border-width: 2px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 20, 0, 0, 10);");
        
        Label titleLabel = new Label("☀️ MÉTÉO EN DIRECT");
        titleLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 20px; -fx-font-weight: 900; -fx-effect: dropshadow(gaussian, rgba(255, 215, 0, 0.5), 10, 0.5, 0, 0);");

        Label locLabel = new Label("📍 " + currentActivity.getAdresse());
        locLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");

        // Fetch real weather data
        gambatta.tn.utils.WeatherUtil.WeatherData weatherData = gambatta.tn.utils.WeatherUtil.getCurrentWeather();
        
        Label tempLabel;
        Label condLabel;
        javafx.scene.image.ImageView iconView = new javafx.scene.image.ImageView();
        iconView.setFitWidth(100);
        iconView.setFitHeight(100);
        
        if (weatherData.isSuccess) {
            tempLabel = new Label(weatherData.getFormattedTemp());
            tempLabel.setStyle("-fx-text-fill: white; -fx-font-size: 54px; -fx-font-weight: bold;");
            
            condLabel = new Label(weatherData.getCapitalizedDescription());
            condLabel.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 16px; -fx-font-weight: bold;");

            try {
                iconView.setImage(new javafx.scene.image.Image("http://openweathermap.org/img/wn/" + weatherData.iconId + "@4x.png", true));
            } catch (Exception e) {}
        } else {
            tempLabel = new Label("N/A");
            tempLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 54px; -fx-font-weight: bold;");
            
            condLabel = new Label(weatherData.description);
            condLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 14px;");
        }

        condLabel.setAlignment(Pos.CENTER);
        condLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Label qLabel = new Label("Souhaitez-vous maintenir cette réservation ?");
        qLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 15 0 10 0;");

        HBox btnBox = new HBox(20);
        btnBox.setAlignment(Pos.CENTER);
        
        Button btnYes = new Button("OUI, RÉSERVER");
        btnYes.setStyle("-fx-background-color: linear-gradient(to bottom right, #FFD700, #ff9f43); -fx-text-fill: #020617; -fx-font-size: 14px; -fx-font-weight: 900; -fx-background-radius: 10; -fx-padding: 10 25; -fx-cursor: hand;");
        btnYes.setOnAction(e -> {
            modalStage.close();
            confirmReservation();
        });

        Button btnNo = new Button("ANNULER");
        btnNo.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 25; -fx-cursor: hand;");
        btnNo.setOnAction(e -> modalStage.close());

        btnBox.getChildren().addAll(btnNo, btnYes);
        weatherBox.getChildren().addAll(titleLabel, locLabel, iconView, tempLabel, condLabel, qLabel, btnBox);
        
        javafx.scene.Scene modalScene = new javafx.scene.Scene(weatherBox);
        modalScene.setFill(Color.TRANSPARENT);
        modalStage.setScene(modalScene);
        modalStage.showAndWait();
    }

    private void confirmReservation() {
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
        handleRetour(); // Close details view after reservation
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
