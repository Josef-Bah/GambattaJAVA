package gambatta.tn.ui;

import gambatta.tn.services.user.ChatbotService;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ChatbotPanelController implements Initializable {

    @FXML private VBox      chatPanel;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox      messagesBox;
    @FXML private TextField inputField;
    @FXML private HBox      typingBox;

    private static final double PANEL_WIDTH = 380;
    private boolean panelOpen = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chatPanel.setTranslateX(PANEL_WIDTH);
        addBubble("👋 Bonjour ! Je suis l'assistant virtuel de Gambatta.\n" +
                  "Comment puis-je vous aider aujourd'hui ?", false);
    }

    // ─── Slide in / out ───────────────────────────────────────────────────────

    public void toggle() {
        if (panelOpen) slideOut(); else slideIn();
    }

    private void slideIn() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(320), chatPanel);
        tt.setToX(0);
        tt.setInterpolator(Interpolator.EASE_OUT);
        tt.play();
        panelOpen = true;
        Platform.runLater(() -> inputField.requestFocus());
    }

    private void slideOut() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(280), chatPanel);
        tt.setToX(PANEL_WIDTH);
        tt.setInterpolator(Interpolator.EASE_IN);
        tt.play();
        panelOpen = false;
    }

    @FXML
    public void close() {
        if (panelOpen) slideOut();
    }

    // ─── Send message ─────────────────────────────────────────────────────────

    @FXML
    public void sendMessage() {
        String text = inputField.getText() == null ? "" : inputField.getText().trim();
        if (text.isEmpty()) return;

        addBubble(text, true);
        inputField.clear();
        typingBox.setVisible(true);

        ChatbotService.sendMessage(text,
                response -> {
                    typingBox.setVisible(false);
                    addBubble(response, false);
                },
                () -> {
                    typingBox.setVisible(false);
                    addBubble("❌ Erreur de connexion. Réessayez dans quelques instants.", false);
                }
        );
    }

    // ─── Bubble factory ───────────────────────────────────────────────────────

    private void addBubble(String text, boolean isUser) {
        HBox row = new HBox();
        row.setPadding(new Insets(3, 0, 3, 0));
        row.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        VBox bubble = new VBox(4);
        bubble.setMaxWidth(268);
        bubble.setPadding(new Insets(10, 14, 8, 14));

        if (isUser) {
            bubble.setStyle(
                    "-fx-background-color: #FFD700;" +
                    "-fx-background-radius: 12 12 0 12;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.20), 6, 0, 0, 2);"
            );
        } else {
            bubble.setStyle(
                    "-fx-background-color: rgba(30,41,59,0.96);" +
                    "-fx-background-radius: 12 12 12 0;" +
                    "-fx-border-color: rgba(255,255,255,0.07);" +
                    "-fx-border-radius: 12 12 12 0;" +
                    "-fx-border-width: 1;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.20), 6, 0, 0, 2);"
            );
        }

        Label msgLabel = new Label(text);
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(244);
        msgLabel.setStyle(isUser
                ? "-fx-text-fill: #08111f; -fx-font-size: 13px;"
                : "-fx-text-fill: white; -fx-font-size: 13px;");

        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        Label timeLabel = new Label(time);
        timeLabel.setStyle(isUser
                ? "-fx-text-fill: rgba(8,17,31,0.50); -fx-font-size: 10px;"
                : "-fx-text-fill: #64748b; -fx-font-size: 10px;");
        timeLabel.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        timeLabel.setMaxWidth(Double.MAX_VALUE);

        bubble.getChildren().addAll(msgLabel, timeLabel);
        row.getChildren().add(bubble);
        messagesBox.getChildren().add(row);

        // Animate the new bubble
        FadeTransition fade = new FadeTransition(Duration.millis(200), row);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        // Auto-scroll to bottom
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }
}
