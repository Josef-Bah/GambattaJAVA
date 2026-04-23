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
        rightPanel.getChildren().clear();

        Label header = new Label("Réservation");
        header.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 22px; -fx-font-weight: bold;");
        
        Label activityName = new Label(a.getNoma());
        activityName.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        rightPanel.getChildren().addAll(header, activityName);

        if (a.getTypea() != null && a.getTypea().toLowerCase().contains("sport")) {
            VBox weatherBox = new VBox(15);
            weatherBox.setAlignment(Pos.CENTER);
            weatherBox.setStyle("-fx-background-color: rgba(30, 41, 59, 0.8); -fx-padding: 20; -fx-background-radius: 15;");
            
            Label titleLabel = new Label("☀️ Météo en direct");
            titleLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");

            // REAL WEATHER API FETCH
            gambatta.tn.utils.WeatherUtil.WeatherData weatherData = gambatta.tn.utils.WeatherUtil.getCurrentWeather();
            
            Label tempLabel;
            Label condLabel;
            ImageView iconView = new ImageView();
            iconView.setFitWidth(60);
            iconView.setFitHeight(60);
            
            if (weatherData.isSuccess) {
                tempLabel = new Label(weatherData.getFormattedTemp());
                tempLabel.setStyle("-fx-text-fill: white; -fx-font-size: 36px; -fx-font-weight: bold;");
                
                condLabel = new Label(weatherData.getCapitalizedDescription());
                condLabel.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 14px;");

                try {
                    iconView.setImage(new Image("http://openweathermap.org/img/wn/" + weatherData.iconId + "@2x.png", true));
                } catch (Exception e) {}
            } else {
                tempLabel = new Label("N/A");
                tempLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 36px; -fx-font-weight: bold;");
                
                condLabel = new Label("Météo indisponible");
                condLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 14px;");
            }

            condLabel.setAlignment(Pos.CENTER);
            condLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            weatherBox.getChildren().addAll(titleLabel, iconView, tempLabel, condLabel);
            rightPanel.getChildren().add(weatherBox);
        }

        Label qLabel = new Label("Confirmer la réservation ?");
        qLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 20 0 10 0;");

        Button btnConfirm = new Button("✔ Confirmer");
        btnConfirm.setStyle("-fx-background-color: linear-gradient(to right, #2ed573, #7bed9f); -fx-text-fill: #020617; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 20; -fx-pref-width: 300px; -fx-cursor: hand;");
        
        Button btnCancel = new Button("✖ Annuler");
        btnCancel.setStyle("-fx-background-color: transparent; -fx-border-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 10 20; -fx-pref-width: 300px; -fx-cursor: hand;");

        btnConfirm.setOnAction(e -> {
            ReservationActivite r = new ReservationActivite(new Date(), "10:00", "EN_ATTENTE", a.getId(), 1, null);
            reservationService.add(r);
            rightPanel.setVisible(false);
            rightPanel.setManaged(false);
            alert("Succès", "Votre demande de réservation pour " + a.getNoma() + " a été ajoutée !");
        });

        btnCancel.setOnAction(e -> {
            rightPanel.setVisible(false);
            rightPanel.setManaged(false);
        });

        rightPanel.getChildren().addAll(qLabel, btnConfirm, btnCancel);
        rightPanel.setVisible(true);
        rightPanel.setManaged(true);
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
            controller.setActivityLocation(a.getAdresse());

            Stage stage = new Stage();
            stage.setTitle("Emplacement - " + a.getNoma());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (Exception ex) {
            alert("Erreur", "Veuillez implémenter Map.fxml. " + ex.getMessage());
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
            rightPanel.setVisible(false);
            rightPanel.setManaged(false);
        });

        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        rightPanel.getChildren().addAll(header, desc, picker, selectionLabel, spacer, btnClose);
        rightPanel.setVisible(true);
        rightPanel.setManaged(true);
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
    void handleVoiceSearch() {
        try {
            searchBar.setPromptText("🎙 Écoute en cours (5s)...");
            searchBar.setText("");
            new Thread(() -> {
                try {
                    String psCommand = "Add-Type -AssemblyName System.Speech; " +
                        "$recognizer = New-Object System.Speech.Recognition.SpeechRecognitionEngine; " +
                        "$recognizer.LoadGrammar((New-Object System.Speech.Recognition.DictationGrammar)); " +
                        "$recognizer.SetInputToDefaultAudioDevice(); " +
                        "$result = $recognizer.Recognize((New-TimeSpan -Seconds 5)); " +
                        "if ($result -ne $null) { Write-Output $result.Text }";
                    
                    ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-Command", psCommand);
                    pb.redirectErrorStream(true);
                    Process process = pb.start();
                    
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
                    StringBuilder out = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        out.append(line).append(" ");
                    }
                    process.waitFor();
                    String recognizedText = out.toString().trim();
                    
                    javafx.application.Platform.runLater(() -> {
                        searchBar.setPromptText("🔍 Rechercher une activité...");
                        if (!recognizedText.isEmpty()) {
                            searchBar.setText(recognizedText);
                            handleSearch();
                        } else {
                            searchBar.setPromptText("Bruit non reconnu...");
                        }
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> searchBar.setPromptText("Erreur micro"));
                }
            }).start();
        } catch (Exception e) {}
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
        fc.setInitialFileName("Catalogue_Activites.pdf");
        File docFile = fc.showSaveDialog(activitiesPane.getScene().getWindow());
        if (docFile == null) return;

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Catalogue des Activites - Gambatta");
                contentStream.endText();

                contentStream.setFont(PDType1Font.HELVETICA, 12);
                int y = 700;
                for (activite a : allActivities) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, y);
                    contentStream.showText("- " + a.getNoma() + " (" + a.getTypea() + "): " + a.getAdresse());
                    contentStream.endText();
                    y -= 25;
                    if (y < 50) break; // simplistic paging
                }
            }
            document.save(docFile);
            alert("Succès", "Catalogue PDF exporté avec succès !");
        } catch (IOException e) {
            alert("Erreur", "Échec de création du PDF : " + e.getMessage());
        }
    }

    @FXML
    void handleTTS() {
        if (allActivities == null || allActivities.isEmpty()) return;
        
        StringBuilder txt = new StringBuilder("Bienvenue chez Gambatta. Voici nos activités : ");
        for (activite a : allActivities) {
            txt.append(a.getNoma()).append(", ");
        }

        try {
            String textToSpeak = txt.toString().replace("'", " ").replace("\"", " ");
            String psCommand = "Add-Type -AssemblyName System.speech; $synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; $synth.Speak('" + textToSpeak + "');";
            
            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-Command", psCommand);
            pb.start();
        } catch (Exception e) {
            alert("Erreur TTS", e.getMessage());
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

    private void navigate(String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            activitiesPane.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            alert("Erreur", "Impossible de charger " + path);
        }
    }

    private void alert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
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