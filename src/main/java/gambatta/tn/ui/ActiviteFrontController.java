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

public class ActiviteFrontController {

    @FXML private TilePane activitiesPane;
    @FXML private TextField searchBar;
    @FXML private ComboBox<String> sortCombo;

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
        for (activite a : list) {
            activitiesPane.getChildren().add(createCard(a));
        }
    }

    private VBox createCard(activite a) {
        VBox card = new VBox();
        card.setPrefWidth(320); // Slightly wider to fill screen better
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
        imageHeader.setPrefHeight(180);
        
        ImageView imageView = new ImageView();
        try {
            String dbImage = a.getImagea();
            Image finalImg = null;
            
            if (dbImage != null && !dbImage.trim().isEmpty()) {
                if (dbImage.startsWith("http") || dbImage.startsWith("file:")) {
                    finalImg = new Image(dbImage, true);
                } else {
                    java.io.File fb = new java.io.File(dbImage);
                    if (fb.exists()) finalImg = new Image(fb.toURI().toString(), true);
                    else {
                        try {
                            java.net.URL dbUrl = getClass().getResource("/activites/images/" + dbImage);
                            if (dbUrl != null) finalImg = new Image(dbUrl.toExternalForm());
                        } catch (Exception x) { }
                    }
                }
            }
            
            // Smart Match if no specific image
            if (finalImg == null || finalImg.isError()) {
                String noma = a.getNoma().toLowerCase();
                String fileName = null;
                if (noma.contains("nba") || noma.contains("2k")) fileName = "nba.png";
                else if (noma.contains("call of duty") || noma.contains("cod") || noma.contains("black ops")) fileName = "cod.png";
                else if (noma.contains("fifa") || noma.contains("fc")) fileName = "fifa.png";
                else if (noma.contains("counter") || noma.contains("csgo") || noma.contains("cs")) fileName = "csgo.png";
                
                if (fileName != null) {
                    try {
                         java.net.URL resUrl = getClass().getResource("/activites/images/" + fileName);
                         if (resUrl != null) finalImg = new Image(resUrl.toExternalForm(), true);
                    } catch (Exception ez) {}
                }
            }
            
            // Fallback for custom games (GOLF, WOW, TENNIS, ISRAAA) to guarantee unique images!
            if (finalImg == null || finalImg.isError()) {
                int seed = Math.abs(a.getNoma().hashCode());
                finalImg = new Image("https://picsum.photos/seed/" + seed + "/320/180", true);
            }
            
            imageView.setImage(finalImg);
        } catch (Exception ex) {
            System.out.println("Image loading err: " + ex.getMessage());
        }
        
        imageView.setFitWidth(320);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(false);

        // Gradient Overlay over the image
        VBox gradientOverlay = new VBox();
        gradientOverlay.setStyle("-fx-background-color: linear-gradient(to top, rgba(15,23,42,1) 0%, rgba(15,23,42,0) 100%);");
        gradientOverlay.setAlignment(Pos.BOTTOM_LEFT);
        gradientOverlay.setPadding(new javafx.geometry.Insets(15));
        
        Label name = new Label(a.getNoma().toUpperCase());
        name.getStyleClass().add("card-title");
        name.setStyle("-fx-font-size: 22px; -fx-text-fill: #FFFFFF;");
        
        Label type = new Label(a.getTypea() + " • " + a.getAdresse());
        type.getStyleClass().add("card-desc");
        type.setStyle("-fx-text-fill: #94a3b8;");

        gradientOverlay.getChildren().addAll(name, type);
        imageHeader.getChildren().addAll(imageView, gradientOverlay);

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
        btnReserver.setStyle("-fx-font-size: 15px; -fx-padding: 12px;");
        btnReserver.setOnAction(e -> reserver(a));

        Button btnDetails = new Button("Détails");
        btnDetails.getStyleClass().add("btn-secondary");
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

        Button btnFav = new Button(a.isAfav() ? "❤️" : "🤍");
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
        btn.setText(a.isAfav() ? "❤️" : "🤍");
    }

    private void reserver(activite a) {
        ReservationActivite r = new ReservationActivite(
                new Date(),
                "10:00",
                "EN_ATTENTE",
                a.getId(),
                1,
                null
        );
        reservationService.add(r);
        alert("Succès", "Demande de réservation envoyée pour " + a.getNoma());
    }

    private void showDetails(activite a) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de l'activité");
        alert.setHeaderText(a.getNoma());
        
        VBox content = new VBox(10);
        content.getChildren().add(new Label("Type: " + a.getTypea()));
        content.getChildren().add(new Label("Disponibilité: " + a.getDispoa()));
        content.getChildren().add(new Label("Description: \n" + a.getDescria()));
        content.getChildren().add(new Label("Adresse: " + a.getAdresse()));
        
        alert.getDialogPane().setContent(content);
        alert.show();
    }

    private void showMap(activite a) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/activites/Map.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Emplacement - " + a.getNoma());
            stage.setScene(new Scene(root, 600, 400));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (Exception ex) {
            alert("Erreur", "Veuillez implémenter Map.fxml. " + ex.getMessage());
        }
    }

    private void showCalendar() {
        DatePicker picker = new DatePicker();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Calendrier de réservation");
        alert.setHeaderText("Sélectionnez une date");
        alert.getDialogPane().setContent(picker);
        alert.show();
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
    void handleBackOffice() {
        navigate("/activites/ActiviteBack.fxml");
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
}