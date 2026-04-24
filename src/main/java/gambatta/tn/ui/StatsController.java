package gambatta.tn.ui;

import gambatta.tn.services.user.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.ResourceBundle;

public class StatsController implements Initializable {

    @FXML private Label totalUsersLabel;
    @FXML private Label totalAdminsLabel;
    @FXML private Label totalSimpleLabel;
    @FXML private Label totalActifsLabel;
    @FXML private Label totalInactifsLabel;
    @FXML private Label loginsTodayLabel;
    @FXML private Label feedbackLabel;

    private final UserService userService = new UserService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadCartes();
    }

    private void loadCartes() {
        totalUsersLabel.setText(String.valueOf(userService.countUsers()));
        totalAdminsLabel.setText(String.valueOf(userService.countAdmins()));
        totalSimpleLabel.setText(String.valueOf(userService.countSimpleUsers()));
        totalActifsLabel.setText(String.valueOf(userService.countActifs()));
        totalInactifsLabel.setText(String.valueOf(userService.countInactifs()));
        loginsTodayLabel.setText(String.valueOf(userService.countLoginsToday()));
    }

    // Ouvre le popup Chart.js
    @FXML
    public void ouvrirCharts() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gambatta.tn.ui/ChartsPopup.fxml")
            );
            Parent root = loader.load();

            Stage popup = new Stage();
            popup.setTitle("📊 Statistiques — Graphiques");
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setWidth(1020);
            popup.setHeight(720);
            popup.setResizable(true);
            popup.setScene(new Scene(root));
            popup.show();

        } catch (Exception e) {
            feedbackLabel.setStyle("-fx-text-fill: #ff4757;");
            feedbackLabel.setText("❌ Erreur graphiques : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Export CSV
    @FXML
    public void exporterCSV() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer le rapport CSV");
        fc.setInitialFileName("stats_users_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".csv");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fc.showSaveDialog(feedbackLabel.getScene().getWindow());
        if (file == null) return;

        // UTF-8 BOM pour que Excel reconnaisse l'encodage automatiquement
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
             java.io.PrintWriter pw = new java.io.PrintWriter(
                     new java.io.OutputStreamWriter(fos, java.nio.charset.StandardCharsets.UTF_8))) {

            // BOM UTF-8 — indispensable pour Excel
            fos.write(0xEF);
            fos.write(0xBB);
            fos.write(0xBF);

            // Séparateur ";" pour Excel français/tunisien
            pw.println("GAMBATTA E-SPORTS - Rapport Statistiques Utilisateurs");
            pw.println("Généré le;" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            pw.println();

            pw.println("=== CHIFFRES CLES ===");
            pw.println("Indicateur;Valeur");
            pw.println("Total utilisateurs;" + userService.countUsers());
            pw.println("Administrateurs;"    + userService.countAdmins());
            pw.println("Utilisateurs simples;" + userService.countSimpleUsers());
            pw.println("Comptes actifs;"     + userService.countActifs());
            pw.println("Comptes inactifs;"   + userService.countInactifs());
            pw.println("Connexions aujourd'hui;" + userService.countLoginsToday());
            pw.println("Total connexions;"   + userService.countAllLogins());
            pw.println();

            pw.println("=== REPARTITION DES ROLES ===");
            pw.println("Role;Nombre");
            for (Map.Entry<String, Integer> e : userService.countParRole().entrySet()) {
                pw.println(e.getKey() + ";" + e.getValue());
            }
            pw.println();

            pw.println("=== INSCRIPTIONS PAR MOIS (6 derniers mois) ===");
            pw.println("Mois;Nouveaux utilisateurs");
            for (Map.Entry<String, Integer> e : userService.countUsersParMois().entrySet()) {
                pw.println(e.getKey() + ";" + e.getValue());
            }

            feedbackLabel.setStyle("-fx-text-fill: #2ed573;");
            feedbackLabel.setText("✅ CSV exporté : " + file.getName());

        } catch (Exception e) {
            feedbackLabel.setStyle("-fx-text-fill: #ff4757;");
            feedbackLabel.setText("❌ Erreur export : " + e.getMessage());
        }
    }
    @FXML
    public void retourDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gambatta.tn.ui/AdminDashboard.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) feedbackLabel.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            scene.getStylesheets().add(
                    getClass().getResource("/gambatta.tn.ui/style.css").toExternalForm()
            );
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}