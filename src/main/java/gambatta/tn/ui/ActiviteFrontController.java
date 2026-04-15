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
import javafx.scene.text.Text;
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

    @FXML private FlowPane activitiesPane;
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
        card.setPrefWidth(300);
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
        imageHeader.setPrefHeight(170);
        
        ImageView imageView = new ImageView();
        try {
            String dbImage = a.getImagea();
            Image img = null;
            
            // For local development reliability, directly reference the src folder paths
            String basePath = "src/main/resources/activites/images/";
            
            if (dbImage != null && !dbImage.trim().isEmpty()) {
                if (dbImage.startsWith("http") || dbImage.startsWith("file:")) {
                    img = new Image(dbImage);
                } else {
                    java.io.File file = new java.io.File(dbImage);
                    if (file.exists()) img = new Image(file.toURI().toString());
                }
            }
            
            // Smart Match if no specific image
            if (img == null || img.isError()) {
                String noma = a.getNoma().toLowerCase();
                String fileName = "default.png";
                if (noma.contains("nba") || noma.contains("2k")) fileName = "nba.png";
                else if (noma.contains("call of duty") || noma.contains("cod") || noma.contains("black ops")) fileName = "cod.png";
                else if (noma.contains("fifa") || noma.contains("fc")) fileName = "fifa.png";
                else if (noma.contains("counter") || noma.contains("csgo") || noma.contains("cs")) fileName = "csgo.png";
                
                java.io.File fileLocal = new java.io.File(basePath + fileName);
                if (fileLocal.exists()) {
                    img = new Image(fileLocal.toURI().toString());
                } else {
                    img = new Image(getClass().getResourceAsStream("/activites/images/" + fileName));
                }
            }
            if (img != null) imageView.setImage(img);
        } catch (Exception ex) {
            System.out.println("Image err: " + ex.getMessage());
        }
        
        imageView.setFitWidth(300);
        imageView.setFitHeight(170);
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
        
        // Primary Button
        Button btnReserver = new Button("RÉSERVER LA SESSION");
        btnReserver.getStyleClass().add("btn-primary");
        btnReserver.setMaxWidth(Double.MAX_VALUE);
        btnReserver.setStyle("-fx-font-size: 15px; -fx-padding: 12px;");
        btnReserver.setOnAction(e -> reserver(a));

        // Secondary Buttons
        HBox secondaryActions = new HBox(10);
        secondaryActions.setAlignment(Pos.CENTER);
        
        Button btnDetails = new Button("Info");
        btnDetails.getStyleClass().add("btn-secondary");
        btnDetails.setOnAction(e -> showDetails(a));
        
        Button btnMap = new Button("Map");
        btnMap.getStyleClass().add("btn-secondary");
        btnMap.setOnAction(e -> showMap(a));

        Button btnCalendar = new Button("Date");
        btnCalendar.getStyleClass().add("btn-secondary");
        btnCalendar.setOnAction(e -> showCalendar());

        Button btnFav = new Button(a.isAfav() ? "★ Fav" : "☆ Fav");
        btnFav.getStyleClass().add("btn-secondary");
        if(a.isAfav()) btnFav.setStyle("-fx-text-fill: #FFD700; -fx-border-color: #FFD700;");
        btnFav.setOnAction(e -> toggleFavText(a, btnFav));

        secondaryActions.getChildren().addAll(btnDetails, btnMap, btnCalendar, btnFav);

        actionsArea.getChildren().addAll(btnReserver, secondaryActions);

        card.getChildren().addAll(imageHeader, actionsArea);
        card.setPadding(new javafx.geometry.Insets(0)); // Remove default padding to let image bleed to edges
        return card;
    }
    
    private void toggleFavText(activite a, Button btn) {
        a.setAfav(!a.isAfav());
        activiteService.update(a);
        btn.setText(a.isAfav() ? "★ Fav" : "☆ Fav");
        if(a.isAfav()) {
            btn.setStyle("-fx-text-fill: #FFD700; -fx-border-color: #FFD700;");
        } else {
            btn.setStyle("");
        }
    }

    private void reserver(activite a) {
        // Hardcoded user 1 assuming no auth context is given yet.
        ReservationActivite r = new ReservationActivite(
                new Date(),
                "10:00", // Default or user input
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
        content.getChildren().add(new Text("Type: " + a.getTypea()));
        content.getChildren().add(new Text("Disponibilité: " + a.getDispoa()));
        content.getChildren().add(new Text("Description: \n" + a.getDescria()));
        content.getChildren().add(new Text("Adresse: " + a.getAdresse()));
        
        alert.getDialogPane().setContent(content);
        alert.show();
    }

    private void showMap(activite a) {
        try {
            // Passing the Activite object could be done via a setter if MapController exists
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

    private void toggleFav(activite a, Button btn) {
        a.setAfav(!a.isAfav());
        activiteService.update(a);
        btn.setText(a.isAfav() ? "❤️" : "🤍");
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
            // Using Powershell to run TTS on Windows. Zero dependency, works great!
            String textToSpeak = txt.toString().replace("'", " ").replace("\"", " ");
            String psCommand = "Add-Type -AssemblyName System.speech; $synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; $synth.Speak('" + textToSpeak + "');";
            
            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-Command", psCommand);
            pb.start();
        } catch (Exception e) {
            alert("Erreur TTS", e.getMessage());
        }
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