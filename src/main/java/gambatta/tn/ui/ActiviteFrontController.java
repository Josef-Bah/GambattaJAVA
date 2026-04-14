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
        VBox card = new VBox(15);
        card.setPrefWidth(280);
        card.getStyleClass().add("card");

        // Top Row (Name & Heart)
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        Label name = new Label(a.getNoma());
        name.getStyleClass().add("card-title");
        name.setWrapText(true);
        name.setMaxWidth(200);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnFav = new Button(a.isAfav() ? "❤️" : "🤍");
        btnFav.getStyleClass().add("btn-fav");
        btnFav.setOnAction(e -> toggleFav(a, btnFav));

        topRow.getChildren().addAll(name, spacer, btnFav);

        // Subtitle / Type
        Label type = new Label(a.getTypea() + " - " + a.getAdresse());
        type.getStyleClass().add("card-desc");

        // Actions
        HBox actions1 = new HBox(10);
        actions1.setAlignment(Pos.CENTER);
        Button btnReserver = new Button("Réserver");
        btnReserver.getStyleClass().add("btn-primary");
        btnReserver.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnReserver, Priority.ALWAYS);
        btnReserver.setOnAction(e -> reserver(a));

        Button btnDetails = new Button("Détails");
        btnDetails.getStyleClass().add("btn-secondary");
        btnDetails.setOnAction(e -> showDetails(a));

        actions1.getChildren().addAll(btnReserver, btnDetails);

        // Icons row
        HBox actions2 = new HBox(10);
        actions2.setAlignment(Pos.CENTER_LEFT);
        
        Button btnMap = new Button("📍");
        btnMap.getStyleClass().add("btn-icon");
        btnMap.setTooltip(new Tooltip("Emplacement"));
        btnMap.setOnAction(e -> showMap(a));

        Button btnCalendar = new Button("📅");
        btnCalendar.getStyleClass().add("btn-icon");
        btnCalendar.setTooltip(new Tooltip("Calendrier"));
        btnCalendar.setOnAction(e -> showCalendar());

        actions2.getChildren().addAll(btnMap, btnCalendar);

        card.getChildren().addAll(topRow, type, actions1, actions2);
        return card;
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