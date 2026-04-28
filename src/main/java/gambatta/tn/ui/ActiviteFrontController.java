package gambatta.tn.ui;

import gambatta.tn.entites.activites.activite;
import gambatta.tn.entites.activites.ReservationActivite;
import gambatta.tn.services.activites.ActiviteService;
import gambatta.tn.services.activites.ReservationActiviteService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

public class ActiviteFrontController {

    @FXML private TilePane activitiesPane;
    @FXML private TextField searchBar;
    @FXML private ComboBox<String> sortCombo;
    @FXML private VBox rightPanel;
    @FXML private ScrollPane rightScroll;

    private ActiviteService activiteService = new ActiviteService();
    private ReservationActiviteService reservationService = new ReservationActiviteService();
    
    private List<activite> allActivities;

    @FXML
    public void initialize() {
        sortCombo.setItems(FXCollections.observableArrayList("Nom (A-Z)", "Nom (Z-A)"));
        loadActivities();
    }

    private void loadActivities() {
        allActivities = activiteService.getAll();
        displayActivities(allActivities);
    }

    private void displayActivities(List<activite> list) {
        activitiesPane.getChildren().clear();
        
        // Trier pour mettre les favoris en haut ! (coeur de l'activite favoris devient au premier)
        List<activite> sortedList = list.stream()
                .sorted(Comparator.comparing(activite::isAfav).reversed()
                        .thenComparing(activite::getNoma))
                .collect(Collectors.toList());

        for (activite a : sortedList) {
            activitiesPane.getChildren().add(createCard(a));
        }
    }

    private VBox createCard(activite a) {
        VBox card = new VBox();
        card.setPrefWidth(380); // Much wider to fill screen better
        card.setMaxWidth(380);
        card.getStyleClass().add("card");
        
        // Interactive E-Sport effect
        card.setOnMouseEntered(e -> {
            card.setScaleX(1.04);
            card.setScaleY(1.04);
            card.setStyle("-fx-effect: dropshadow(gaussian, #FFD700, 25, 0.4, 0, 0);");
        });
        card.setOnMouseExited(e -> {
            card.setScaleX(1.0);
            card.setScaleY(1.0);
            card.setStyle("");
        });

        // 1. IMAGE HEADER with Overlays (StackPane)
        StackPane imageHeader = new StackPane();
        imageHeader.setPrefHeight(220);
        
        ImageView imageView = new ImageView();
        try {
            String imgUrl = getImageUrlForActivite(a);
            imageView.setImage(new Image(imgUrl, true));
        } catch (Exception ex) {
            System.out.println("Image loading err: " + ex.getMessage());
        }
        
        imageView.setFitWidth(380);
        imageView.setFitHeight(220);
        imageView.setPreserveRatio(false);

        // Gradient Overlay over the image
        VBox gradientOverlay = new VBox();
        gradientOverlay.setStyle("-fx-background-color: linear-gradient(to top, rgba(15,23,42,1) 0%, rgba(15,23,42,0) 100%);");
        gradientOverlay.setAlignment(Pos.BOTTOM_LEFT);
        gradientOverlay.setPadding(new javafx.geometry.Insets(15));
        
        Label name = new Label(a.getNoma().toUpperCase());
        name.getStyleClass().add("card-title");
        name.setStyle("-fx-font-size: 26px; -fx-text-fill: #FFFFFF; -fx-font-weight: 900; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 5, 0, 0, 2);");
        
        Label type = new Label(a.getTypea() + " • " + a.getAdresse());
        type.getStyleClass().add("card-desc");
        type.setStyle("-fx-text-fill: #94a3b8;");

        gradientOverlay.getChildren().addAll(name, type);
        
        imageHeader.getChildren().addAll(imageView, gradientOverlay);

        // BADGE FAVORIS EN HAUT DROITE SI C'EST UN FAVORI
        if (a.isAfav()) {
            Label favBadge = new Label("⭐ FAVORIS");
            favBadge.setStyle("-fx-background-color: #FFD700; -fx-text-fill: #1e293b; -fx-font-weight: 900; -fx-padding: 5 10; -fx-background-radius: 20;");
            StackPane.setAlignment(favBadge, Pos.TOP_RIGHT);
            StackPane.setMargin(favBadge, new javafx.geometry.Insets(10));
            imageHeader.getChildren().add(favBadge);
        }

        // 2. ACTIONS AREA
        VBox actionsArea = new VBox(15);
        actionsArea.setPadding(new javafx.geometry.Insets(15));
        
        // Ligne 1: Reserver et Details
        HBox primaryActions = new HBox(10);
        primaryActions.setAlignment(Pos.CENTER);
        Button btnReserver = new Button("Réserver");
        btnReserver.getStyleClass().add("btn-primary");
        btnReserver.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnReserver, Priority.ALWAYS);
        btnReserver.setStyle("-fx-font-size: 16px; -fx-padding: 14px; -fx-font-weight: bold;");
        btnReserver.setOnAction(e -> reserver(a));

        Button btnDetails = new Button("Détails");
        btnDetails.getStyleClass().add("btn-secondary");
        btnDetails.setStyle("-fx-font-size: 15px; -fx-padding: 14px;");
        btnDetails.setOnAction(e -> showDetails(a));
        
        primaryActions.getChildren().addAll(btnReserver, btnDetails);

        // Ligne 2 : Boutons d'icone comme avant!
        HBox secondaryActions = new HBox(10);
        secondaryActions.setAlignment(Pos.CENTER_LEFT);
        
        Button btnMap = new Button("📍");
        btnMap.getStyleClass().add("btn-icon");
        btnMap.setTooltip(new Tooltip("Emplacement"));
        btnMap.setOnAction(e -> showMap(a));

        Button btnCalendar = new Button("📅");
        btnCalendar.getStyleClass().add("btn-icon");
        btnCalendar.setTooltip(new Tooltip("Calendrier"));
        btnCalendar.setOnAction(e -> showCalendar());

        Button btnFav = new Button(a.isAfav() ? "❤" : "♡");
        btnFav.getStyleClass().add("btn-fav");
        btnFav.setStyle("-fx-font-size: 20px;");
        btnFav.setTooltip(new Tooltip("Favoris"));
        btnFav.setOnAction(e -> toggleFavText(a, btnFav));

        secondaryActions.getChildren().addAll(btnMap, btnCalendar, btnFav);

        actionsArea.getChildren().addAll(primaryActions, secondaryActions);

        card.getChildren().addAll(imageHeader, actionsArea);
        card.setPadding(new javafx.geometry.Insets(0)); // Remove default padding
        
        // ADDED: Make whole card clickable to select it for TTS and Reservation
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setOnMouseClicked(e -> reserver(a));
        
        return card;
    }
    
    private void toggleFavText(activite a, Button btn) {
        a.setAfav(!a.isAfav());
        activiteService.update(a);
        btn.setText(a.isAfav() ? "❤" : "♡");
        // Reload all activities to apply the exact sorting
        loadActivities();
    }

    private void reserver(activite a) {
        this.selectedActivite = a;
        rightPanel.getChildren().clear();

        // Automatic Speech on selection
        speakActivity(a);

        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label header = new Label("Réservation");
        header.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 22px; -fx-font-weight: bold;");
        
        Button btnListen = new Button("🎙 Ré-écouter");
        btnListen.setStyle("-fx-background-color: rgba(255,215,0,0.1); -fx-text-fill: #FFD700; -fx-border-color: #FFD700; -fx-border-radius: 12; -fx-background-radius: 12; -fx-font-size: 11px; -fx-cursor: hand;");
        btnListen.setOnAction(e -> speakActivity(a));

        headerBox.getChildren().addAll(header, btnListen);
        
        Label activityName = new Label(a.getNoma());
        activityName.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        rightPanel.getChildren().addAll(headerBox, activityName);

        if (a.getTypea() != null && a.getTypea().equalsIgnoreCase("sport")) {
            // ── FULL iOS-style weather card (Sidebar version) ────────
            VBox weatherCard = new VBox(0);
            weatherCard.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #3d5a80); -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 4);");

            VBox headerSec = new VBox(1);
            headerSec.setAlignment(Pos.CENTER);
            headerSec.setPadding(new javafx.geometry.Insets(16, 12, 8, 12));
            Label myLocLbl = new Label("MY LOCATION");
            myLocLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 10px; -fx-font-weight: bold;");
            Label cityLbl = new Label("Soukra");
            cityLbl.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
            Label bigTempLbl = new Label("--°");
            bigTempLbl.setStyle("-fx-text-fill: white; -fx-font-size: 58px; -fx-font-weight: 300;");
            headerSec.getChildren().addAll(myLocLbl, cityLbl, bigTempLbl);

            VBox summaryCard = new VBox(6);
            summaryCard.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 12; -fx-border-color: rgba(255,255,255,0.15); -fx-border-radius: 12; -fx-border-width: 1;");
            summaryCard.setPadding(new javafx.geometry.Insets(10, 12, 0, 12));
            Label summaryLbl = new Label("Chargement...");
            summaryLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 11px;");
            summaryLbl.setWrapText(true);
            HBox hourlyStrip = new HBox(0);
            hourlyStrip.setAlignment(Pos.CENTER_LEFT);
            ScrollPane hScroll = new ScrollPane(hourlyStrip);
            hScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            hScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            hScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            hScroll.setPrefViewportHeight(78);
            summaryCard.getChildren().addAll(summaryLbl, hScroll);
            VBox.setMargin(summaryCard, new javafx.geometry.Insets(0, 10, 0, 10));

            VBox forecastCard = new VBox(0);
            forecastCard.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 12; -fx-border-color: rgba(255,255,255,0.15); -fx-border-radius: 12; -fx-border-width: 1;");
            forecastCard.setPadding(new javafx.geometry.Insets(8, 12, 8, 12));
            VBox.setMargin(forecastCard, new javafx.geometry.Insets(0, 10, 8, 10));
            Label fcHdr = new Label("\uD83D\uDCC5  10-DAY FORECAST");
            fcHdr.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 9px; -fx-font-weight: bold;");
            VBox dailyRows = new VBox(0);
            forecastCard.getChildren().addAll(fcHdr, dailyRows);

            weatherCard.getChildren().addAll(headerSec, summaryCard, forecastCard);
            rightPanel.getChildren().add(weatherCard);

            // Click listener for full screen modal if they want even bigger view
            weatherCard.setCursor(javafx.scene.Cursor.HAND);
            weatherCard.setOnMouseClicked(e -> showDetailedWeatherModal(a));

            new Thread(() -> {
                gambatta.tn.utils.WeatherUtil.WeatherData wd = gambatta.tn.utils.WeatherUtil.getCurrentWeather();
                java.util.List<gambatta.tn.utils.WeatherUtil.HourlyEntry> hrly = gambatta.tn.utils.WeatherUtil.getHourlyForecast();
                java.util.List<gambatta.tn.utils.WeatherUtil.DailyEntry> dly = gambatta.tn.utils.WeatherUtil.getDailyForecast();
                javafx.application.Platform.runLater(() -> {
                    if (wd.isSuccess) {
                        cityLbl.setText(wd.cityName != null && !wd.cityName.isBlank() ? wd.cityName : "Soukra");
                        bigTempLbl.setText(wd.getFormattedTemp());
                        summaryLbl.setText(wd.getCapitalizedDescription() + ".");
                    }
                    hourlyStrip.getChildren().clear();
                    boolean first = true;
                    for (gambatta.tn.utils.WeatherUtil.HourlyEntry he : hrly) {
                        hourlyStrip.getChildren().add(buildWHourSlot(first ? "Now" : he.timeText, he.iconId, he.pop, he.getFormattedTemp()));
                        first = false;
                    }
                    dailyRows.getChildren().clear();
                    for (int i = 0; i < dly.size(); i++) {
                        gambatta.tn.utils.WeatherUtil.DailyEntry de = dly.get(i);
                        if (i > 0) { javafx.scene.shape.Line s = new javafx.scene.shape.Line(0,0,265,0); s.setStroke(Color.rgb(255,255,255,0.12)); dailyRows.getChildren().add(s); }
                        dailyRows.getChildren().add(buildWDayRow(de.getDayLabel(), de.iconId, (int) de.maxPop, de.tempMin, de.tempMax));
                    }
                });
            }).start();
        }

        Label qLabel = new Label("Coordonnées de réservation");
        qLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 0 5 0;");

        TextField emailField = new TextField();
        emailField.setPromptText("Votre adresse e-mail");
        emailField.setStyle("-fx-background-color: #1e293b; -fx-text-fill: white; -fx-prompt-text-fill: #64748b; -fx-border-color: #334155; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8; -fx-pref-width: 300px;");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Votre numéro WhatsApp (ex: +216...)");
        phoneField.setStyle("-fx-background-color: #1e293b; -fx-text-fill: white; -fx-prompt-text-fill: #64748b; -fx-border-color: #334155; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8; -fx-pref-width: 300px;");

        Button btnConfirm = new Button("✔ Confirmer la réservation");
        btnConfirm.setStyle("-fx-background-color: linear-gradient(to right, #2ed573, #7bed9f); -fx-text-fill: #020617; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 20; -fx-pref-width: 300px; -fx-cursor: hand; -fx-margin-top: 10px;");
        
        Button btnCancel = new Button("✖ Annuler");
        btnCancel.setStyle("-fx-background-color: transparent; -fx-border-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 10 20; -fx-pref-width: 300px; -fx-cursor: hand;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px;");

        btnCancel.setOnAction(e -> {
            rightScroll.setVisible(false);
            rightScroll.setManaged(false);
        });

        // Add error labels specific to fields
        Label errEmail = new Label("[!] Email requis.");
        errEmail.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-font-weight: bold;");
        errEmail.setVisible(false); errEmail.setManaged(false);

        Label errPhone = new Label("[!] Numéro requis.");
        errPhone.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-font-weight: bold;");
        errPhone.setVisible(false); errPhone.setManaged(false);

        VBox emailBox = new VBox(5, emailField, errEmail);
        VBox phoneBox = new VBox(5, phoneField, errPhone);

        btnConfirm.setOnAction(e -> {
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            boolean valid = true;

            String defaultStyle = "-fx-background-color: #1e293b; -fx-text-fill: white; -fx-prompt-text-fill: #64748b; -fx-border-color: #334155; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8; -fx-pref-width: 300px;";
            String errorStyle = "-fx-background-color: #1e293b; -fx-text-fill: white; -fx-prompt-text-fill: #64748b; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8; -fx-pref-width: 300px;";

            if (email.isEmpty()) {
                emailField.setStyle(errorStyle);
                errEmail.setVisible(true); errEmail.setManaged(true);
                valid = false;
            } else {
                emailField.setStyle(defaultStyle);
                errEmail.setVisible(false); errEmail.setManaged(false);
            }

            if (phone.isEmpty()) {
                phoneField.setStyle(errorStyle);
                errPhone.setVisible(true); errPhone.setManaged(true);
                valid = false;
            } else {
                phoneField.setStyle(defaultStyle);
                errPhone.setVisible(false); errPhone.setManaged(false);
            }

            if (!valid) return;

            try {
                ReservationActivite r = new ReservationActivite(new Date(), "10:00", "EN_ATTENTE", a.getId(), 1, null, email, phone);
                reservationService.add(r);
                rightScroll.setVisible(false);
                rightScroll.setManaged(false);
                alert("Succès", "Votre demande de réservation a été ajoutée !\nVous serez notifié dès que l'administrateur l'aura acceptée.");
            } catch (Throwable ex) {
                alert("Erreur", "Veuillez compiler le projet ! L'erreur est : " + ex.getMessage());
            }
        });

        HBox btnBox = new HBox(10, btnConfirm, btnCancel);
        btnBox.setAlignment(Pos.CENTER);

        rightPanel.getChildren().addAll(qLabel, emailBox, phoneBox, btnBox);
        rightScroll.setVisible(true);
        rightScroll.setManaged(true);
    }

    private void showDetailedWeatherModal(activite a) {
        javafx.stage.Stage modalStage = new javafx.stage.Stage();
        modalStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initOwner(activitiesPane.getScene().getWindow());

        // ── ROOT: wrapper to allow scrolling if too tall ─────────────
        VBox root = new VBox(0);
        root.setPrefWidth(380);
        root.setMaxWidth(380);
        root.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #2c3e50, #3d5a80, #2c3e50);" +
            "-fx-background-radius: 22;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.85), 30, 0, 0, 12);"
        );

        ScrollPane modalScroll = new ScrollPane(root);
        modalScroll.setFitToWidth(true);
        modalScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        modalScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        modalScroll.setMaxHeight(650); // Prevent overflow on small screens
        modalScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 0; -fx-background-radius: 22; -fx-border-width: 0;");
        modalScroll.setPannable(true);

        VBox mainWrapper = new VBox(modalScroll);
        mainWrapper.setAlignment(Pos.CENTER);
        mainWrapper.setPadding(new javafx.geometry.Insets(40));
        mainWrapper.setStyle("-fx-background-color: transparent;");

        // TOP BAR with Close Button
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new javafx.geometry.Insets(15, 15, 0, 0));
        Button btnX = new Button("✕");
        btnX.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-font-size: 16px; -fx-background-radius: 20; -fx-cursor: hand;");
        btnX.setOnAction(e -> modalStage.close());
        topBar.getChildren().add(btnX);

        // Header
        VBox headerBox = new VBox(2);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new javafx.geometry.Insets(10, 20, 12, 20));
        Label locLbl = new Label("MY LOCATION");
        locLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11px; -fx-font-weight: bold;");
        Label cityLbl = new Label("Chargement...");
        cityLbl.setStyle("-fx-text-fill: white; -fx-font-size: 32px; -fx-font-weight: bold;");
        Label bigTempLbl = new Label("--°");
        bigTempLbl.setStyle("-fx-text-fill: white; -fx-font-size: 76px; -fx-font-weight: 300;");
        headerBox.getChildren().addAll(locLbl, cityLbl, bigTempLbl);

        // Frosted Hourly Card
        VBox hourlyCard = new VBox(10);
        hourlyCard.setStyle("-fx-background-color: rgba(255,255,255,0.10); -fx-background-radius: 16; -fx-border-color: rgba(255,255,255,0.18); -fx-border-radius: 16; -fx-border-width: 1;");
        hourlyCard.setPadding(new javafx.geometry.Insets(12, 16, 0, 16));
        Label summaryLbl = new Label("Récupération...");
        summaryLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.88); -fx-font-size: 13px;");
        summaryLbl.setWrapText(true);
        HBox hourlyStrip = new HBox(0);
        hourlyStrip.setAlignment(Pos.CENTER_LEFT);
        ScrollPane hScroll = new ScrollPane(hourlyStrip);
        hScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        hScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        hScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        hScroll.setPrefViewportHeight(90);
        hourlyCard.getChildren().addAll(summaryLbl, hScroll);
        VBox.setMargin(hourlyCard, new javafx.geometry.Insets(0, 14, 15, 14));

        // Frosted 10-Day Card
        VBox dailyCard = new VBox(0);
        dailyCard.setStyle("-fx-background-color: rgba(255,255,255,0.10); -fx-background-radius: 16; -fx-border-color: rgba(255,255,255,0.18); -fx-border-radius: 16; -fx-border-width: 1;");
        dailyCard.setPadding(new javafx.geometry.Insets(10, 16, 10, 16));
        Label dHeader = new Label("\uD83D\uDCC5  10-DAY FORECAST");
        dHeader.setStyle("-fx-text-fill: rgba(255,255,255,0.65); -fx-font-size: 11px; -fx-font-weight: bold;");
        VBox dailyRows = new VBox(0);
        dailyCard.getChildren().addAll(dHeader, dailyRows);
        VBox.setMargin(dailyCard, new javafx.geometry.Insets(0, 14, 20, 14));

        // CONFIRMATION BUTTONS
        HBox btnBox = new HBox(16);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new javafx.geometry.Insets(0, 20, 25, 20));

        Button btnNo = new Button("FERMER");
        btnNo.setPrefWidth(140);
        btnNo.setStyle("-fx-background-color: rgba(255,255,255,0.12); -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 22; -fx-padding: 11 0; -fx-cursor: hand; -fx-border-color: rgba(255,255,255,0.3);");
        btnNo.setOnAction(e -> modalStage.close());

        Button btnYes = new Button("RESERVER ✓");
        btnYes.setPrefWidth(160);
        btnYes.setStyle("-fx-background-color: linear-gradient(to right, #FFD700, #ff9f43); -fx-text-fill: #1a1a2e; -fx-font-size: 13px; -fx-font-weight: 900; -fx-background-radius: 22; -fx-padding: 11 0; -fx-cursor: hand;");
        btnYes.setOnAction(e -> {
            modalStage.close();
            alert("Information", "Veuillez cliquer sur 'Confirmer' dans le panneau de droite pour valider.");
        });

        btnBox.getChildren().addAll(btnNo, btnYes);
        root.getChildren().addAll(topBar, headerBox, hourlyCard, dailyCard, btnBox);
        mainWrapper.getChildren().add(root);

        javafx.scene.Scene scene = new javafx.scene.Scene(mainWrapper);
        scene.setFill(Color.TRANSPARENT);
        modalStage.setScene(scene);

        // Async fetch
        new Thread(() -> {
            gambatta.tn.utils.WeatherUtil.WeatherData wd = gambatta.tn.utils.WeatherUtil.getCurrentWeather();
            java.util.List<gambatta.tn.utils.WeatherUtil.HourlyEntry> hrly = gambatta.tn.utils.WeatherUtil.getHourlyForecast();
            java.util.List<gambatta.tn.utils.WeatherUtil.DailyEntry> dly = gambatta.tn.utils.WeatherUtil.getDailyForecast();
            javafx.application.Platform.runLater(() -> {
                if (wd.isSuccess) {
                    cityLbl.setText(wd.cityName != null && !wd.cityName.isBlank() ? wd.cityName : "Soukra");
                    bigTempLbl.setText(wd.getFormattedTemp());
                    summaryLbl.setText(wd.getCapitalizedDescription() + ". Vent " + String.format("%.0f km/h", wd.windSpeed) + ".");
                }
                hourlyStrip.getChildren().clear();
                boolean first = true;
                for (gambatta.tn.utils.WeatherUtil.HourlyEntry he : hrly) {
                    hourlyStrip.getChildren().add(buildWHourSlot(first ? "Now" : he.timeText, he.iconId, he.pop, he.getFormattedTemp()));
                    first = false;
                }
                dailyRows.getChildren().clear();
                for (int i = 0; i < dly.size(); i++) {
                    gambatta.tn.utils.WeatherUtil.DailyEntry de = dly.get(i);
                    if (i > 0) { javafx.scene.shape.Line sep = new javafx.scene.shape.Line(0, 0, 310, 0); sep.setStroke(Color.rgb(255, 255, 255, 0.12)); dailyRows.getChildren().add(sep); }
                    dailyRows.getChildren().add(buildWDayRow(de.getDayLabel(), de.iconId, (int) de.maxPop, de.tempMin, de.tempMax));
                }
            });
        }).start();

        modalStage.showAndWait();
    }

    private void showDetails(activite a) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/activites/DetailsActivite.fxml"));
            Parent root = loader.load();
            
            DetailsActiviteController controller = loader.getController();
            
            // On récupère exactement la même URL !
            String imgUrl = getImageUrlForActivite(a);
            
            controller.setActivityDetails(a, imgUrl);

            Stage stage = new Stage();
            stage.setTitle("Détails - " + a.getNoma());
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            alert("Erreur", "Veuillez implémenter DetailsActivite.fxml. " + ex.getMessage());
        }
    }

    private void showMap(activite a) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/activites/Map.fxml"));
            Parent root = loader.load();

            MapController controller = loader.getController();
            controller.setActivityData(a.getNoma(), a.getAdresse());

            Stage stage = new Stage();
            stage.setTitle("📍 Emplacement dans le Complexe - " + a.getNoma());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (Throwable ex) {
            alert("Erreur", "Impossible de charger le plan du complexe. " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void showCalendar() {
        rightPanel.getChildren().clear();

        Label header = new Label("Calendrier");
        header.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 22px; -fx-font-weight: bold;");
        
        Label desc = new Label("Sélectionnez une date pour voir les disponibilités.");
        desc.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");
        desc.setWrapText(true);

        DatePicker picker = new DatePicker();
        picker.getStyleClass().add("date-picker");
        picker.setPrefWidth(300);
        
        Label selectionLabel = new Label("Aucune date sélectionnée");
        selectionLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 20 0 0 0;");

        picker.setOnAction(e -> {
            if (picker.getValue() != null) {
                selectionLabel.setText("Date choisie : " + picker.getValue().toString() + "\n(Affichage des horaires en développement)");
            }
        });

        Button btnClose = new Button("✖ Fermer");
        btnClose.setStyle("-fx-background-color: transparent; -fx-border-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 10 20; -fx-pref-width: 300px; -fx-cursor: hand; -fx-margin-top: 30px;");
        btnClose.setOnAction(e -> {
            rightScroll.setVisible(false);
            rightScroll.setManaged(false);
        });

        VBox spacer2 = new VBox();
        VBox.setVgrow(spacer2, Priority.ALWAYS);

        rightPanel.getChildren().addAll(header, desc, picker, selectionLabel, spacer2, btnClose);
        rightScroll.setVisible(true);
        rightScroll.setManaged(true);
    }

    private void showHourlyForecast() {
        javafx.stage.Stage fs = new javafx.stage.Stage();
        fs.setTitle("Météo — Soukra");
        fs.initModality(Modality.APPLICATION_MODAL);

        // Root: dark sky gradient
        VBox root = new VBox(0);
        root.setPrefWidth(380);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #3d5a80, #2c3e50);");

        // Header placeholders
        VBox hdr = new VBox(1);
        hdr.setAlignment(Pos.CENTER);
        hdr.setPadding(new javafx.geometry.Insets(22, 14, 10, 14));
        Label myLoc = new Label("MY LOCATION");
        myLoc.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11px; -fx-font-weight: bold;");
        Label city = new Label("Soukra");
        city.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");
        Label bigT = new Label("--°");
        bigT.setStyle("-fx-text-fill: white; -fx-font-size: 72px; -fx-font-weight: 300;");
        Label feels = new Label("Feels Like: --°");
        feels.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 13px;");
        Label hl = new Label("H:--°  L:--°");
        hl.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 13px;");
        hdr.getChildren().addAll(myLoc, city, bigT, feels, hl);

        // Summary + hourly card
        VBox sCard = new VBox(8);
        sCard.setStyle("-fx-background-color: rgba(255,255,255,0.10); -fx-background-radius: 14; -fx-border-color: rgba(255,255,255,0.18); -fx-border-radius: 14; -fx-border-width: 1;");
        sCard.setPadding(new javafx.geometry.Insets(12, 14, 0, 14));
        VBox.setMargin(sCard, new javafx.geometry.Insets(0, 12, 0, 12));
        Label sumLbl = new Label("Chargement...");
        sumLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.88); -fx-font-size: 12px;");
        sumLbl.setWrapText(true);
        javafx.scene.shape.Line ln = new javafx.scene.shape.Line(0,0,340,0);
        ln.setStroke(javafx.scene.paint.Color.rgb(255,255,255,0.18));
        HBox strip = new HBox(0);
        strip.setAlignment(Pos.CENTER_LEFT);
        ScrollPane sc = new ScrollPane(strip);
        sc.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sc.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sc.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        sc.setPrefViewportHeight(88);
        sCard.getChildren().addAll(sumLbl, ln, sc);

        // Daily forecast card
        VBox dCard = new VBox(0);
        dCard.setStyle("-fx-background-color: rgba(255,255,255,0.10); -fx-background-radius: 14; -fx-border-color: rgba(255,255,255,0.18); -fx-border-radius: 14; -fx-border-width: 1;");
        dCard.setPadding(new javafx.geometry.Insets(10, 14, 10, 14));
        VBox.setMargin(dCard, new javafx.geometry.Insets(0, 12, 0, 12));
        Label dHdr = new Label("\uD83D\uDCC5  10-DAY FORECAST");
        dHdr.setStyle("-fx-text-fill: rgba(255,255,255,0.65); -fx-font-size: 10px; -fx-font-weight: bold;");
        VBox dRows = new VBox(0);
        dCard.getChildren().addAll(dHdr, dRows);

        // Close button
        Button btnClose = new Button("Fermer");
        btnClose.setStyle("-fx-background-color: rgba(255,255,255,0.12); -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 20; -fx-padding: 10 40; -fx-cursor: hand; -fx-border-color: rgba(255,255,255,0.3); -fx-border-radius: 20; -fx-border-width: 1;");
        btnClose.setOnAction(e -> fs.close());
        HBox btnRow = new HBox(btnClose);
        btnRow.setAlignment(Pos.CENTER);
        btnRow.setPadding(new javafx.geometry.Insets(12, 0, 18, 0));

        root.getChildren().addAll(hdr, sCard, dCard, btnRow);

        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        fs.setScene(scene);
        fs.show();

        // Async load
        new Thread(() -> {
            gambatta.tn.utils.WeatherUtil.WeatherData wd = gambatta.tn.utils.WeatherUtil.getCurrentWeather();
            java.util.List<gambatta.tn.utils.WeatherUtil.HourlyEntry> hrly = gambatta.tn.utils.WeatherUtil.getHourlyForecast();
            java.util.List<gambatta.tn.utils.WeatherUtil.DailyEntry> dly = gambatta.tn.utils.WeatherUtil.getDailyForecast();
            javafx.application.Platform.runLater(() -> {
                if (wd.isSuccess) {
                    city.setText(wd.cityName != null && !wd.cityName.isBlank() ? wd.cityName : "Soukra");
                    bigT.setText(wd.getFormattedTemp());
                    feels.setText(wd.getFormattedFeels());
                    hl.setText(wd.getFormattedHL());
                    sumLbl.setText(wd.getCapitalizedDescription() + ". Vent " + String.format("%.0f km/h", wd.windSpeed) + ", ressenti " + String.format("%.0f°.", wd.feelsLike));
                } else { sumLbl.setText("Météo indisponible."); }
                strip.getChildren().clear();
                boolean first = true;
                for (gambatta.tn.utils.WeatherUtil.HourlyEntry he : hrly) {
                    strip.getChildren().add(buildWHourSlot(first ? "Now" : he.timeText, he.iconId, he.pop, he.getFormattedTemp()));
                    first = false;
                }
                dRows.getChildren().clear();
                for (int i = 0; i < dly.size(); i++) {
                    gambatta.tn.utils.WeatherUtil.DailyEntry de = dly.get(i);
                    if (i > 0) { javafx.scene.shape.Line sep = new javafx.scene.shape.Line(0,0,340,0); sep.setStroke(javafx.scene.paint.Color.rgb(255,255,255,0.12)); dRows.getChildren().add(sep); }
                    dRows.getChildren().add(buildWDayRow(de.getDayLabel(), de.iconId, (int) de.maxPop, de.tempMin, de.tempMax));
                }
            });
        }).start();
    }

    /** Hourly forecast slot (time / icon / rain% / temp) */
    private VBox buildWHourSlot(String time, String iconId, int pop, String temp) {
        VBox slot = new VBox(3);
        slot.setAlignment(Pos.CENTER);
        slot.setPadding(new javafx.geometry.Insets(6, 10, 7, 10));
        slot.setMinWidth(58);
        Label tLbl = new Label(time);
        tLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 11px; -fx-font-weight: bold;");
        ImageView ico = new ImageView();
        ico.setFitWidth(30); ico.setFitHeight(30); ico.setPreserveRatio(true);
        if (iconId != null && !iconId.isBlank()) {
            try { ico.setImage(new Image("http://openweathermap.org/img/wn/" + iconId + ".png", true)); } catch (Exception ignored) {}
        }
        Label pLbl = new Label(pop > 0 ? pop + "%" : "");
        pLbl.setStyle("-fx-text-fill: #64b5f6; -fx-font-size: 10px; -fx-font-weight: bold;");
        Label tmpLbl = new Label(temp);
        tmpLbl.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
        slot.getChildren().addAll(tLbl, ico, pLbl, tmpLbl);
        return slot;
    }

    /** Daily forecast row (day / icon+rain% / minT | bar | maxT) */
    private HBox buildWDayRow(String day, String iconId, int pop, double tMin, double tMax) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new javafx.geometry.Insets(7, 0, 7, 0));
        Label dLbl = new Label(day);
        dLbl.setMinWidth(70);
        dLbl.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        VBox iBox = new VBox(1);
        iBox.setAlignment(Pos.CENTER);
        iBox.setMinWidth(38);
        ImageView ico = new ImageView();
        ico.setFitWidth(24); ico.setFitHeight(24); ico.setPreserveRatio(true);
        if (iconId != null && !iconId.isBlank()) {
            try { ico.setImage(new Image("http://openweathermap.org/img/wn/" + iconId + ".png", true)); } catch (Exception ignored) {}
        }
        Label pLbl = new Label(pop > 0 ? pop + "%" : "");
        pLbl.setStyle("-fx-text-fill: #64b5f6; -fx-font-size: 9px; -fx-font-weight: bold;");
        iBox.getChildren().addAll(ico, pLbl);
        Label minLbl = new Label(String.format("%.0f°", tMin));
        minLbl.setMinWidth(28);
        minLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.55); -fx-font-size: 13px;");
        javafx.scene.layout.StackPane bar = new javafx.scene.layout.StackPane();
        bar.setPrefWidth(70); bar.setPrefHeight(4);
        javafx.scene.shape.Rectangle bg = new javafx.scene.shape.Rectangle(70, 4, javafx.scene.paint.Color.rgb(255,255,255,0.2));
        bg.setArcWidth(4); bg.setArcHeight(4);
        double ratio = tMax > tMin ? Math.min((tMax - tMin) / 30.0, 1.0) : 0.5;
        javafx.scene.shape.Rectangle fg = new javafx.scene.shape.Rectangle(70 * ratio, 4);
        fg.setArcWidth(4); fg.setArcHeight(4);
        fg.setFill(new javafx.scene.paint.LinearGradient(0,0,1,0,true, javafx.scene.paint.CycleMethod.NO_CYCLE,
            new javafx.scene.paint.Stop(0, javafx.scene.paint.Color.rgb(255,213,0)),
            new javafx.scene.paint.Stop(1, javafx.scene.paint.Color.rgb(255,120,50))));
        javafx.scene.layout.StackPane.setAlignment(fg, Pos.CENTER_LEFT);
        bar.getChildren().addAll(bg, fg);
        HBox.setHgrow(bar, Priority.ALWAYS);
        Label maxLbl = new Label(String.format("%.0f°", tMax));
        maxLbl.setMinWidth(28);
        maxLbl.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        row.getChildren().addAll(dLbl, iBox, minLbl, bar, maxLbl);
        return row;
    }


    @FXML
    void handleSearch() {
        String kw = searchBar.getText().toLowerCase();
        List<activite> filtered = allActivities.stream()
                .filter(a -> a.getNoma().toLowerCase().contains(kw) || a.getTypea().toLowerCase().contains(kw))
                .collect(Collectors.toList());
        displayActivities(filtered);
    }



    @FXML
    void handleSort() {
        String order = sortCombo.getValue();
        if (order == null) return;
        List<activite> sorted = allActivities.stream().sorted(new Comparator<activite>() {
            @Override
            public int compare(activite o1, activite o2) {
                if (order.equals("Nom (A-Z)")) return o1.getNoma().compareToIgnoreCase(o2.getNoma());
                else return o2.getNoma().compareToIgnoreCase(o1.getNoma());
            }
        }).collect(Collectors.toList());
        displayActivities(sorted);
    }

    @FXML
    void handleExportPDF() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fc.setInitialFileName("Catalogue_Activites_Gambatta.pdf");
        File docFile = fc.showSaveDialog(activitiesPane.getScene().getWindow());
        if (docFile == null) return;

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // TOP HEADER ACCENT
                contentStream.setNonStrokingColor(15, 23, 42); // #0f172a
                contentStream.addRect(0, 770, 612, 60);
                contentStream.fill();

                // LOGO
                contentStream.setNonStrokingColor(255, 215, 0); // Gold
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 26);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 790);
                contentStream.showText("GAMBATTA");
                contentStream.endText();

                contentStream.setNonStrokingColor(255, 255, 255);
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(200, 793);
                contentStream.showText("LE COMPLEXE ESPORT & SPORT DE RÉFÉRENCE");
                contentStream.endText();

                // TITLE
                contentStream.setNonStrokingColor(15, 23, 42);
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 730);
                contentStream.showText("CATALOGUE DES ACTIVITÉS");
                contentStream.endText();

                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(420, 730);
                contentStream.showText("Mis à jour le: " + new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date()));
                contentStream.endText();

                // Separator
                contentStream.setStrokingColor(255, 215, 0);
                contentStream.setLineWidth(1.5f);
                contentStream.moveTo(50, 720);
                contentStream.lineTo(560, 720);
                contentStream.stroke();

                int y = 680;
                for (activite a : allActivities) {
                    if (y < 80) break;

                    // Activity Box
                    contentStream.setNonStrokingColor(248, 250, 252);
                    contentStream.addRect(50, y - 45, 510, 40);
                    contentStream.fill();

                    // Left Indicator (Gold bar)
                    contentStream.setNonStrokingColor(255, 215, 0);
                    contentStream.addRect(50, y - 45, 4, 40);
                    contentStream.fill();

                    // Name
                    contentStream.setNonStrokingColor(15, 23, 42);
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 13);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(65, y - 22);
                    contentStream.showText(a.getNoma().toUpperCase());
                    contentStream.endText();

                    // Type Badge
                    String type = a.getTypea().toUpperCase();
                    contentStream.setNonStrokingColor(30, 41, 59);
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 8);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(65, y - 35);
                    contentStream.showText(type);
                    contentStream.endText();

                    // Location
                    contentStream.setNonStrokingColor(100, 116, 139);
                    contentStream.setFont(PDType1Font.HELVETICA, 10);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(250, y - 22);
                    contentStream.showText("\uD83D\uDCCD " + a.getAdresse());
                    contentStream.endText();

                    // Availability
                    contentStream.setNonStrokingColor(15, 23, 42);
                    contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(450, y - 22);
                    contentStream.showText("Status: " + a.getDispoa());
                    contentStream.endText();

                    y -= 60;
                }

                // FOOTER
                contentStream.setNonStrokingColor(100, 116, 139);
                contentStream.setFont(PDType1Font.HELVETICA, 8);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 40);
                contentStream.showText("Pour toute réservation, veuillez visiter notre plateforme ou nous contacter directement.");
                contentStream.endText();
            }
            document.save(docFile);
            alert("Succès", "Catalogue PDF exporté avec succès !");
        } catch (IOException e) {
            alert("Erreur", "Échec de création du PDF : " + e.getMessage());
        }
    }

    private activite selectedActivite;

    @FXML
    void handleTTS() {
        if (selectedActivite != null) {
            speakActivity(selectedActivite);
        } else {
            speak("Veuillez sélectionner une activité pour l'écouter.");
        }
    }

    @FXML
    void handleShowStats() {
        if (allActivities == null || allActivities.isEmpty()) return;
        
        javafx.scene.chart.PieChart pie = new javafx.scene.chart.PieChart();
        pie.setTitle("Répartition");
        java.util.Map<String, Long> typesCount = allActivities.stream()
            .collect(Collectors.groupingBy(activite::getTypea, Collectors.counting()));
        for (java.util.Map.Entry<String, Long> entry : typesCount.entrySet()) {
            pie.getData().add(new javafx.scene.chart.PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        }
        
        javafx.scene.chart.CategoryAxis xAxis = new javafx.scene.chart.CategoryAxis();
        javafx.scene.chart.NumberAxis yAxis = new javafx.scene.chart.NumberAxis();
        javafx.scene.chart.BarChart<String, Number> bar = new javafx.scene.chart.BarChart<>(xAxis, yAxis);
        bar.setTitle("Populaires (Réservations)");
        xAxis.setLabel("Activité");
        yAxis.setLabel("Volume");
        
        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
        java.util.Map<Integer, Long> resCount = reservationService.getAll().stream()
            .collect(Collectors.groupingBy(ReservationActivite::getActiviteId, Collectors.counting()));
        for (java.util.Map.Entry<Integer, Long> entry : resCount.entrySet()) {
            activite a = allActivities.stream().filter(act -> act.getId() == entry.getKey()).findFirst().orElse(null);
            series.getData().add(new javafx.scene.chart.XYChart.Data<>(a != null ? a.getNoma() : "Inconnu", entry.getValue()));
        }
        bar.getData().add(series);
        
        HBox hbox = new HBox(20);
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().addAll(pie, bar);
        hbox.setStyle("-fx-padding: 20; -fx-background-color: #0f172a;");
        
        Stage st = new Stage();
        st.setTitle("Statistiques Publiques Gambatta");
        Scene sc = new Scene(hbox, 800, 400);
        sc.getStylesheets().add(getClass().getResource("/activites/style.css").toExternalForm());
        st.setScene(sc);
        st.initModality(Modality.APPLICATION_MODAL);
        st.show();
    }

    @FXML
    void handleMesReservations() {
        navigate("/activites/MesReservations.fxml");
    }

    @FXML
    void handleLogout() {
        navigate("/activites/Portal.fxml");
    }

    private void speakActivity(activite a) {
        if (a == null) return;
        String textToSpeak = "Activité : " + a.getNoma() + ". Type : " + a.getTypea() + 
                            ". Lieu : " + a.getAdresse() + ". Description : " + a.getDescria();
        speak(textToSpeak);
    }



    private void navigate(String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            activitiesPane.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            alert("Erreur", "Impossible de charger " + path);
        }
    }

    private Process currentSpeakProcess;
    private void speak(String text) {
        try {
            if (currentSpeakProcess != null && currentSpeakProcess.isAlive()) {
                currentSpeakProcess.destroy();
            }
            String safeText = text.replace("'", " ").replace("\"", " ").replace("\n", " ").replace("\r", " ");
            String psCommand = "Add-Type -AssemblyName System.speech; " +
                             "$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                             "$synth.Speak('" + safeText + "');";
            new Thread(() -> {
                try {
                    ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-NoProfile", "-Command", psCommand);
                    currentSpeakProcess = pb.start();
                    currentSpeakProcess.waitFor();
                } catch (Exception e) { }
            }).start();
        } catch (Exception e) { }
    }

    @FXML
    void handleVoiceSearch() {
        try {
            java.util.Set<String> keywords = new java.util.HashSet<>();
            if (allActivities != null) {
                for (activite a : allActivities) {
                    if (a.getNoma() != null) {
                        keywords.add(a.getNoma());
                        for (String word : a.getNoma().split("\\s+")) {
                            if (word.length() > 2) keywords.add(word);
                        }
                    }
                    if (a.getTypea() != null) keywords.add(a.getTypea());
                }
            }

            if (keywords.isEmpty()) {
                keywords.add("Activité");
                keywords.add("Sport");
                keywords.add("Esport");
            }

            String choicesStr = keywords.stream()
                .map(s -> s.replace("'", "''"))
                .collect(Collectors.joining("','", "'", "'"));

            searchBar.setPromptText("🎙 ÉCOUTE EN COURS...");
            searchBar.setText("");

            new Thread(() -> {
                try {
                    String psCommand = "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; " +
                        "$ErrorActionPreference = 'Stop'; " +
                        "try { " +
                        "  Add-Type -AssemblyName System.Speech; " +
                        "  $engine = $null; " +
                        "  try { " +
                        "    $culture = Get-Culture; " +
                        "    $engine = New-Object System.Speech.Recognition.SpeechRecognitionEngine($culture); " +
                        "  } catch { " +
                        "    $engine = New-Object System.Speech.Recognition.SpeechRecognitionEngine; " +
                        "  } " +
                        "  $engine.SetInputToDefaultAudioDevice(); " +
                        "  $keywords = @(" + (choicesStr.isEmpty() ? "''" : choicesStr) + "); " +
                        "  if ($keywords.Count -gt 0 -and $keywords[0] -ne '') { " +
                        "    $choices = New-Object System.Speech.Recognition.Choices; " +
                        "    $choices.Add($keywords); " +
                        "    $gb = New-Object System.Speech.Recognition.GrammarBuilder($choices); " +
                        "    $g = New-Object System.Speech.Recognition.Grammar($gb); " +
                        "    $g.Priority = 127; " +
                        "    $engine.LoadGrammar($g); " +
                        "  } " +
                        "  $dict = New-Object System.Speech.Recognition.DictationGrammar; " +
                        "  $dict.Priority = 0; " +
                        "  $engine.LoadGrammar($dict); " +
                        "  $res = $engine.Recognize((New-TimeSpan -Seconds 8)); " +
                        "  if ($res) { Write-Output $res.Text } " +
                        "} catch { Write-Output ('ERROR: ' + $_.Exception.Message) }";
                    
                    ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", psCommand);
                    pb.redirectErrorStream(true);
                    Process process = pb.start();
                    
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream(), "UTF-8"));
                    String line = reader.readLine();
                    process.waitFor();
                    
                    final String resultText = (line != null) ? line.trim() : "";
                    
                    javafx.application.Platform.runLater(() -> {
                        searchBar.setPromptText("🔍 Rechercher...");
                        if (resultText.startsWith("ERROR:")) {
                            searchBar.setPromptText("Erreur micro");
                            System.err.println("VOICE ERROR (Front): " + resultText);
                        } else if (!resultText.isEmpty()) {
                            searchBar.setText(resultText);
                            handleSearch();
                        } else {
                            searchBar.setPromptText("Rien entendu...");
                        }
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> searchBar.setPromptText("Erreur micro"));
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void alert(String header, String body) {
        javafx.stage.Stage st = new javafx.stage.Stage();
        st.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        st.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new javafx.geometry.Insets(25));
        root.setPrefWidth(380);
        root.setStyle("-fx-background-color: #0f172a; -fx-background-radius: 20; -fx-border-color: #FFD700; -fx-border-width: 2; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 20, 0, 0, 10);");

        Label tLbl = new Label("MESSAGE GAMBATTA");
        tLbl.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 11px; -fx-font-weight: bold; -fx-letter-spacing: 1px;");
        
        Label hLbl = new Label(header);
        hLbl.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label bLbl = new Label(body);
        bLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 13px;");
        bLbl.setWrapText(true);
        bLbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Button btn = new Button("D'ACCORD");
        btn.setStyle("-fx-background-color: linear-gradient(to right, #FFD700, #ff9f43); -fx-text-fill: #020617; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 30; -fx-cursor: hand;");
        btn.setOnAction(e -> st.close());

        root.getChildren().addAll(tLbl, hLbl, bLbl, btn);
        Scene sc = new Scene(root);
        sc.setFill(Color.TRANSPARENT);
        st.setScene(sc);
        st.showAndWait();
    }

    private String getImageUrlForActivite(activite a) {
        String dbImage = a.getImagea();
        if (dbImage != null && !dbImage.trim().isEmpty()) {
            if (dbImage.startsWith("http") || dbImage.startsWith("file:")) return dbImage;
            java.io.File fb = new java.io.File(dbImage);
            if (fb.exists()) return fb.toURI().toString();
            try {
                java.net.URL dbUrl = getClass().getResource("/activites/images/" + dbImage);
                if (dbUrl != null) return dbUrl.toExternalForm();
            } catch (Exception x) { }
        }
        
        String noma = a.getNoma().toLowerCase();
        String fileName = null;
        if (noma.contains("nba") || noma.contains("2k")) fileName = "nba.png";
        else if (noma.contains("call of duty") || noma.contains("cod") || noma.contains("black ops")) fileName = "cod.png";
        else if (noma.contains("fifa") || noma.contains("fc")) fileName = "fifa.png";
        else if (noma.contains("counter") || noma.contains("csgo") || noma.contains("cs")) fileName = "csgo.png";
        
        if (fileName != null) {
            try {
                 java.net.URL resUrl = getClass().getResource("/activites/images/" + fileName);
                 if (resUrl != null) return resUrl.toExternalForm();
            } catch (Exception ez) {}
        }
        
        return "https://picsum.photos/seed/" + Math.abs(a.getNoma().hashCode()) + "/380/220";
    }
}