package gambatta.tn.ui;

import gambatta.tn.services.user.UserService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ChartsPopupController implements Initializable {

    @FXML private VBox pieContainer;
    @FXML private VBox barContainer;
    @FXML private Label lblTotal;
    @FXML private Label lblAdmins;
    @FXML private Label lblSimples;
    @FXML private Label lblActifs;
    @FXML private Label lblInactifs;
    @FXML private Label lblToday;

    private final UserService userService = new UserService();

    // Couleurs gaming
    private static final Color[] COLORS = {
            Color.web("#FFD700"), Color.web("#ff9f43"), Color.web("#2ed573"),
            Color.web("#a29bfe"), Color.web("#ff4757"), Color.web("#1e90ff"),
            Color.web("#ff6b81")
    };

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Cartes chiffres
        lblTotal.setText(String.valueOf(userService.countUsers()));
        lblAdmins.setText(String.valueOf(userService.countAdmins()));
        lblSimples.setText(String.valueOf(userService.countSimpleUsers()));
        lblActifs.setText(String.valueOf(userService.countActifs()));
        lblInactifs.setText(String.valueOf(userService.countInactifs()));
        lblToday.setText(String.valueOf(userService.countLoginsToday()));

        // Dessiner les graphiques
        drawPieChart();
        drawBarChart();
    }

    // ── CAMEMBERT ────────────────────────────────────────────────
    private void drawPieChart() {
        Map<String, Integer> roles = userService.countParRole();
        if (roles.isEmpty()) return;

        Canvas canvas = new Canvas(460, 320);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        int total = roles.values().stream().mapToInt(Integer::intValue).sum();
        double startAngle = 0;
        int colorIdx = 0;

        double cx = 160, cy = 150, radius = 120;

        for (Map.Entry<String, Integer> entry : roles.entrySet()) {
            double angle = (entry.getValue() * 360.0) / total;
            Color color = COLORS[colorIdx % COLORS.length];

            // Tranche
            gc.setFill(color);
            gc.fillArc(cx - radius, cy - radius,
                    radius * 2, radius * 2,
                    startAngle, angle,
                    javafx.scene.shape.ArcType.ROUND);

            // Bordure
            gc.setStroke(Color.web("#050b14"));
            gc.setLineWidth(2);
            gc.strokeArc(cx - radius, cy - radius,
                    radius * 2, radius * 2,
                    startAngle, angle,
                    javafx.scene.shape.ArcType.ROUND);

            startAngle += angle;
            colorIdx++;
        }

        // Légende
        double legendX = 300, legendY = 40;
        colorIdx = 0;
        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        for (Map.Entry<String, Integer> entry : roles.entrySet()) {
            Color color = COLORS[colorIdx % COLORS.length];
            gc.setFill(color);
            gc.fillRoundRect(legendX, legendY + colorIdx * 26, 14, 14, 4, 4);
            gc.setFill(Color.web("#cbd5e1"));
            String label = entry.getKey();
            if (label.length() > 18) label = label.substring(0, 16) + "..";
            gc.fillText(label + " (" + entry.getValue() + ")", legendX + 20, legendY + colorIdx * 26 + 11);
            colorIdx++;
        }

        pieContainer.getChildren().add(canvas);
    }

    // ── BARRES ───────────────────────────────────────────────────
    private void drawBarChart() {
        Map<String, Integer> parMois = userService.countUsersParMois();
        if (parMois.isEmpty()) return;

        Canvas canvas = new Canvas(460, 300);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        List<Map.Entry<String, Integer>> entries = List.copyOf(parMois.entrySet());
        int maxVal = entries.stream().mapToInt(Map.Entry::getValue).max().orElse(1);

        double marginLeft = 50, marginBottom = 50;
        double chartW = 390, chartH = 210;
        double barW = chartW / entries.size() * 0.6;
        double gap  = chartW / entries.size();

        // Axes
        gc.setStroke(Color.web("#334155"));
        gc.setLineWidth(1);

        // Grilles horizontales
        for (int i = 0; i <= 5; i++) {
            double y = marginBottom + chartH - (chartH * i / 5.0);
            gc.setStroke(Color.web("#1e293b"));
            gc.strokeLine(marginLeft, y, marginLeft + chartW, y);

            gc.setFill(Color.web("#64748b"));
            gc.setFont(Font.font("Segoe UI", 10));
            gc.fillText(String.valueOf(maxVal * i / 5), 4, y + 4);
        }

        // Barres
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<String, Integer> entry = entries.get(i);
            double barH = (entry.getValue() * chartH) / (double) maxVal;
            double x = marginLeft + i * gap + gap * 0.2;
            double y = marginBottom + chartH - barH;

            // Barre avec dégradé simulé
            gc.setFill(Color.web("#FFD700", 0.85));
            gc.fillRoundRect(x, y, barW, barH, 6, 6);

            // Bordure
            gc.setStroke(Color.web("#FFD700"));
            gc.setLineWidth(1.5);
            gc.strokeRoundRect(x, y, barW, barH, 6, 6);

            // Valeur au-dessus
            gc.setFill(Color.web("#FFD700"));
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(String.valueOf(entry.getValue()), x + barW / 2, y - 5);

            // Label mois en dessous
            gc.setFill(Color.web("#94a3b8"));
            gc.setFont(Font.font("Segoe UI", 10));
            gc.fillText(entry.getKey(), x + barW / 2, marginBottom + chartH + 16);
        }

        // Axe X
        gc.setStroke(Color.web("#475569"));
        gc.setLineWidth(1.5);
        gc.strokeLine(marginLeft, marginBottom + chartH,
                marginLeft + chartW, marginBottom + chartH);

        barContainer.getChildren().add(canvas);
    }
}