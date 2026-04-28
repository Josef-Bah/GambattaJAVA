package gambatta.tn.ui;

import gambatta.tn.services.tournoi.GeminiService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ChatbotController {

    @FXML private VBox chatBox;
    @FXML private TextField txtInput;
    @FXML private Button btnSend;
    @FXML private ScrollPane scrollPane;

    @FXML
    public void initialize() {
        addMessage("IA Assistant", "Bonjour ! Je suis l'assistant Gambatta. Comment puis-je vous aider aujourd'hui ?", false);
        // Auto-scroll to bottom
        chatBox.heightProperty().addListener((observable, oldValue, newValue) -> scrollPane.setVvalue(1.0));
    }

    @FXML
    private void handleSend() {
        String userText = txtInput.getText().trim();
        if (userText.isEmpty()) return;

        addMessage("Vous", userText, true);
        txtInput.clear();
        btnSend.setDisable(true);

        GeminiService.getCompletion(userText).thenAccept(response -> {
            Platform.runLater(() -> {
                addMessage("Assistant", response, false);
                btnSend.setDisable(false);
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                addMessage("Erreur", "Oups, j'ai eu un problème : " + ex.getMessage(), false);
                btnSend.setDisable(false);
            });
            return null;
        });
    }

    private void addMessage(String sender, String text, boolean isUser) {
        VBox msgBox = new VBox(5);
        msgBox.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        
        Label lblSender = new Label(sender);
        lblSender.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (isUser ? "#ffd700" : "#4CAF50") + "; -fx-font-size: 10px;");
        
        Label lblText = new Label(text);
        lblText.setWrapText(true);
        lblText.setMaxWidth(300);
        lblText.setStyle("-fx-background-color: " + (isUser ? "#002d5a" : "#1a1a1a") + "; -fx-text-fill: white; -fx-padding: 8 12; -fx-background-radius: 10;");
        
        msgBox.getChildren().addAll(lblSender, lblText);
        chatBox.getChildren().add(msgBox);
    }

    @FXML
    private void handleClose() {
        ((Stage) txtInput.getScene().getWindow()).close();
    }
}
