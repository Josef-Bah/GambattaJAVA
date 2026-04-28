package gambatta.tn.ui;

import gambatta.tn.utils.WeatherUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

import java.util.List;

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

        if (currentActivity.getTypea() != null && currentActivity.getTypea().equalsIgnoreCase("sport")) {
            showModernWeatherModal();
        } else {
            confirmReservation();
        }
    }

    private void showModernWeatherModal() {
        javafx.stage.Stage modalStage = new javafx.stage.Stage();
        modalStage.initStyle(StageStyle.TRANSPARENT);
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initOwner(lblNom.getScene().getWindow());
        modalStage.setTitle("Météo - Réservation");

        // ── ROOT: dark sky gradient ──────────────────────────────────
        VBox root = new VBox(0);
        root.setPrefWidth(370);
        root.setMaxWidth(370);
        root.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #2c3e50, #3d5a80, #2c3e50);" +
            "-fx-background-radius: 22;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.85), 30, 0, 0, 12);"
        );

        // ── SECTION 1: HEADER (location + temp) ─────────────────────
        VBox headerBox = new VBox(2);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(28, 20, 12, 20));

        Label myLocationLbl = new Label("MY LOCATION");
        myLocationLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.75); -fx-font-size: 12px; -fx-font-weight: bold; -fx-letter-spacing: 2;");

        Label cityLbl = new Label("Soukra");           // updated async
        cityLbl.setStyle("-fx-text-fill: white; -fx-font-size: 32px; -fx-font-weight: bold;");

        Label bigTempLbl = new Label("--°");           // updated async
        bigTempLbl.setStyle("-fx-text-fill: white; -fx-font-size: 76px; -fx-font-weight: 300;");

        Label feelsLbl = new Label("Feels Like: --°"); // updated async
        feelsLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 14px;");

        Label hlLbl = new Label("H:--°  L:--°");      // updated async
        hlLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 14px;");

        headerBox.getChildren().addAll(myLocationLbl, cityLbl, bigTempLbl, feelsLbl, hlLbl);

        // ── SECTION 2: FROSTED SUMMARY CARD ─────────────────────────
        VBox summaryCard = new VBox(10);
        summaryCard.setStyle(
            "-fx-background-color: rgba(255,255,255,0.10);" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: rgba(255,255,255,0.18);" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 1;"
        );
        summaryCard.setPadding(new Insets(12, 16, 0, 16));

        Label summaryLbl = new Label("Chargement des prévisions...");  // updated async
        summaryLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.88); -fx-font-size: 13px;");
        summaryLbl.setWrapText(true);
        summaryLbl.setMaxWidth(320);

        // Thin divider
        javafx.scene.shape.Line divider1 = new javafx.scene.shape.Line(0, 0, 330, 0);
        divider1.setStroke(Color.rgb(255, 255, 255, 0.18));

        // Hourly scroll strip
        HBox hourlyStrip = new HBox(0);
        hourlyStrip.setAlignment(Pos.CENTER_LEFT);

        ScrollPane hourlyScroll = new ScrollPane(hourlyStrip);
        hourlyScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        hourlyScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        hourlyScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 4 0;");
        hourlyScroll.setPrefViewportHeight(90);

        summaryCard.getChildren().addAll(summaryLbl, divider1, hourlyScroll);
        VBox.setMargin(summaryCard, new Insets(0, 14, 0, 14));

        // ── SECTION 3: 10-DAY FORECAST ───────────────────────────────
        VBox forecastCard = new VBox(0);
        forecastCard.setStyle(
            "-fx-background-color: rgba(255,255,255,0.10);" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: rgba(255,255,255,0.18);" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 1;"
        );
        forecastCard.setPadding(new Insets(10, 16, 10, 16));
        VBox.setMargin(forecastCard, new Insets(0, 14, 0, 14));

        Label forecastHeader = new Label("\uD83D\uDCC5  10-DAY FORECAST");
        forecastHeader.setStyle("-fx-text-fill: rgba(255,255,255,0.65); -fx-font-size: 11px; -fx-font-weight: bold; -fx-letter-spacing: 1;");
        forecastCard.getChildren().add(forecastHeader);

        VBox dailyRows = new VBox(0);   // filled async
        forecastCard.getChildren().add(dailyRows);

        // ── SECTION 4: RESERVATION BUTTONS ──────────────────────────
        HBox btnBox = new HBox(16);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(14, 20, 22, 20));

        Button btnNo = new Button("ANNULER");
        btnNo.setPrefWidth(140);
        btnNo.setStyle(
            "-fx-background-color: rgba(255,255,255,0.12);" +
            "-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;" +
            "-fx-background-radius: 22; -fx-padding: 11 0; -fx-cursor: hand;" +
            "-fx-border-color: rgba(255,255,255,0.3); -fx-border-radius: 22; -fx-border-width: 1;"
        );
        btnNo.setOnAction(e -> modalStage.close());

        Button btnYes = new Button("OUI, RÉSERVER ✓");
        btnYes.setPrefWidth(168);
        btnYes.setStyle(
            "-fx-background-color: linear-gradient(to right, #FFD700, #ff9f43);" +
            "-fx-text-fill: #1a1a2e; -fx-font-size: 13px; -fx-font-weight: 900;" +
            "-fx-background-radius: 22; -fx-padding: 11 0; -fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(255,215,0,0.4), 10, 0, 0, 3);"
        );
        btnYes.setOnAction(e -> { modalStage.close(); confirmReservation(); });

        btnBox.getChildren().addAll(btnNo, btnYes);

        // ── ASSEMBLE ROOT ────────────────────────────────────────────
        root.getChildren().addAll(headerBox, summaryCard, forecastCard, btnBox);

        javafx.scene.Scene modalScene = new javafx.scene.Scene(root);
        modalScene.setFill(Color.TRANSPARENT);
        modalStage.setScene(modalScene);

        // ── ASYNC DATA FETCH → UPDATE UI ────────────────────────────
        new Thread(() -> {
            WeatherUtil.WeatherData wd       = WeatherUtil.getCurrentWeather();
            List<WeatherUtil.HourlyEntry> hourly = WeatherUtil.getHourlyForecast();
            List<WeatherUtil.DailyEntry>  daily  = WeatherUtil.getDailyForecast();

            Platform.runLater(() -> {
                // ── Update header labels ──
                if (wd.isSuccess) {
                    cityLbl.setText(wd.cityName != null && !wd.cityName.isBlank() ? wd.cityName : "Soukra");
                    bigTempLbl.setText(wd.getFormattedTemp());
                    feelsLbl.setText(wd.getFormattedFeels());
                    hlLbl.setText(wd.getFormattedHL());

                    // Build weather summary text
                    String windKmh = String.format("%.0f km/h", wd.windSpeed);
                    String summary = wd.getCapitalizedDescription() + "." +
                            " Rafales de vent jusqu'à " + windKmh +
                            " rendent la température ressentie de " +
                            String.format("%.0f°.", wd.feelsLike);
                    summaryLbl.setText(summary);
                } else {
                    summaryLbl.setText("Météo indisponible. Vérifiez votre connexion.");
                }

                // ── Build hourly slots ──
                hourlyStrip.getChildren().clear();
                boolean isFirst = true;
                for (WeatherUtil.HourlyEntry he : hourly) {
                    VBox slot = buildHourlySlot(isFirst ? "Now" : he.timeText, he.iconId, he.pop, he.getFormattedTemp());
                    hourlyStrip.getChildren().add(slot);
                    isFirst = false;
                }
                if (hourly.isEmpty()) {
                    // fallback placeholder
                    for (String t : new String[]{"Now", "+3h", "+6h", "+9h"}) {
                        hourlyStrip.getChildren().add(buildHourlySlot(t, null, 0, "--°"));
                    }
                }

                // ── Build daily rows ──
                dailyRows.getChildren().clear();
                for (int i = 0; i < daily.size(); i++) {
                    WeatherUtil.DailyEntry de = daily.get(i);
                    HBox row = buildDailyRow(de.getDayLabel(), de.iconId, (int) de.maxPop, de.tempMin, de.tempMax);
                    if (i > 0) {
                        javafx.scene.shape.Line sep = new javafx.scene.shape.Line(0, 0, 310, 0);
                        sep.setStroke(Color.rgb(255, 255, 255, 0.12));
                        dailyRows.getChildren().add(sep);
                    }
                    dailyRows.getChildren().add(row);
                }
            });
        }).start();

        modalStage.showAndWait();
    }

    // ── Helper: single hourly forecast column ────────────────────────
    private VBox buildHourlySlot(String time, String iconId, int popPct, String temp) {
        VBox slot = new VBox(4);
        slot.setAlignment(Pos.CENTER);
        slot.setPadding(new Insets(6, 12, 8, 12));
        slot.setMinWidth(60);

        Label timeLbl = new Label(time);
        timeLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 12px; -fx-font-weight: bold;");

        ImageView icon = new ImageView();
        icon.setFitWidth(32);
        icon.setFitHeight(32);
        icon.setPreserveRatio(true);
        if (iconId != null && !iconId.isBlank()) {
            try { icon.setImage(new Image("http://openweathermap.org/img/wn/" + iconId + ".png", true)); } catch (Exception ignored) {}
        }

        Label popLbl = new Label(popPct > 0 ? popPct + "%" : "");
        popLbl.setStyle("-fx-text-fill: #64b5f6; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label tempLbl = new Label(temp);
        tempLbl.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");

        slot.getChildren().addAll(timeLbl, icon, popLbl, tempLbl);
        return slot;
    }

    // ── Helper: single daily forecast row ────────────────────────────
    private HBox buildDailyRow(String dayLabel, String iconId, int popPct, double tMin, double tMax) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 0, 8, 0));

        Label dayLbl = new Label(dayLabel);
        dayLbl.setMinWidth(75);
        dayLbl.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");

        VBox iconBox = new VBox(2);
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setMinWidth(42);
        ImageView icon = new ImageView();
        icon.setFitWidth(26);
        icon.setFitHeight(26);
        icon.setPreserveRatio(true);
        if (iconId != null && !iconId.isBlank()) {
            try { icon.setImage(new Image("http://openweathermap.org/img/wn/" + iconId + ".png", true)); } catch (Exception ignored) {}
        }
        Label popLbl2 = new Label(popPct > 0 ? popPct + "%" : "");
        popLbl2.setStyle("-fx-text-fill: #64b5f6; -fx-font-size: 10px; -fx-font-weight: bold;");
        iconBox.getChildren().addAll(icon, popLbl2);

        // Min temp
        Label minLbl = new Label(String.format("%.0f°", tMin));
        minLbl.setMinWidth(32);
        minLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.55); -fx-font-size: 14px;");

        // Temperature bar (progress-like)
        StackPane barPane = new StackPane();
        barPane.setPrefWidth(80);
        barPane.setPrefHeight(4);
        Rectangle barBg = new Rectangle(80, 4, Color.rgb(255, 255, 255, 0.2));
        barBg.setArcWidth(4); barBg.setArcHeight(4);
        double ratio = (tMax > tMin) ? Math.min((tMax - tMin) / 30.0, 1.0) : 0.5;
        Rectangle barFg = new Rectangle(80 * ratio, 4);
        barFg.setArcWidth(4); barFg.setArcHeight(4);
        barFg.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(255, 213, 0)),
                new Stop(1, Color.rgb(255, 120, 50))));
        StackPane.setAlignment(barFg, Pos.CENTER_LEFT);
        barPane.getChildren().addAll(barBg, barFg);
        HBox.setHgrow(barPane, Priority.ALWAYS);

        // Max temp
        Label maxLbl = new Label(String.format("%.0f°", tMax));
        maxLbl.setMinWidth(32);
        maxLbl.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        row.getChildren().addAll(dayLbl, iconBox, minLbl, barPane, maxLbl);
        return row;
    }

    private void confirmReservation() {
        java.util.Date date = new java.util.Date();
        String heure = "10:00"; // Valeur par défaut pour l'instant
        int userId = 1; // Simulation d'un utilisateur connecté (Placeholder)

        System.out.println("🚀 Tentative de réservation pour l'activité ID: " + currentActivity.getId());

        if (reservationService.exists(userId, currentActivity.getId(), date, heure)) {
            System.out.println("🛑 DOUBLON DÉTECTÉ ! Affichage du GIF...");
            showDuplicateAlert();
            return;
        }

        gambatta.tn.entites.activites.ReservationActivite r = new gambatta.tn.entites.activites.ReservationActivite(
                date,
                heure,
                "EN_ATTENTE",
                currentActivity.getId(),
                userId,
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

    private void showDuplicateAlert() {
        javafx.stage.Stage st = new javafx.stage.Stage();
        st.initStyle(StageStyle.TRANSPARENT);
        st.initModality(Modality.APPLICATION_MODAL);
        st.initOwner(lblNom.getScene().getWindow());

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setPrefWidth(400);
        root.setStyle("-fx-background-color: #0f172a; -fx-background-radius: 25; -fx-border-color: #ff4757; -fx-border-width: 3; -fx-effect: dropshadow(gaussian, rgba(255,71,87,0.4), 20, 0, 0, 0);");

        Label title = new Label("OUPS DÉJÀ RÉSERVÉ !");
        title.setStyle("-fx-text-fill: #ff4757; -fx-font-size: 24px; -fx-font-weight: 900;");

        // GIF COOL - Michael Scott "NO NO NO"
        ImageView gifView = new ImageView();
        try {
            gifView.setImage(new Image("https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExNHJqZ3R4Z2N4Z2N4Z2N4Z2N4Z2N4Z2N4Z2N4Z2N4Z2N4Z2N4Z2N4Z2N4Z2N4Z2N/3o7TKwmnDgQb5jemGk/giphy.gif", true));
            gifView.setFitWidth(280);
            gifView.setPreserveRatio(true);
            
            // On ajoute un clip arrondi au GIF pour le style
            Rectangle clip = new Rectangle(280, 160);
            clip.setArcWidth(20); clip.setArcHeight(20);
            gifView.setClip(clip);
        } catch (Exception e) {}

        Label msg = new Label("NON NON NON !\nVous avez déjà une réservation pour cette activité à cette heure-là.");
        msg.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-text-alignment: center; -fx-font-weight: bold;");
        msg.setWrapText(true);

        Button btnClose = new Button("COMPRIS !");
        btnClose.setStyle("-fx-background-color: #ff4757; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 30; -fx-font-weight: bold; -fx-cursor: hand;");
        btnClose.setOnAction(e -> st.close());

        root.getChildren().addAll(title, gifView, msg, btnClose);

        javafx.scene.Scene sc = new javafx.scene.Scene(root);
        sc.setFill(Color.TRANSPARENT);
        st.setScene(sc);
        st.showAndWait();
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
