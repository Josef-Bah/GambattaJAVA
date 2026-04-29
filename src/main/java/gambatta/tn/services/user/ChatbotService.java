package gambatta.tn.services.user;

import javafx.application.Platform;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Properties;
import java.util.function.Consumer;

public class ChatbotService {

    private static final String SYSTEM_CONTEXT =
            "Tu es l'assistant virtuel de Gambatta, complexe e-sports en Tunisie. " +
                    "Jeux: FIFA 24, Valorant, League of Legends, Fortnite, CS2, Rocket League. " +
                    "Horaires: 9h-23h semaine, 10h-00h weekend. " +
                    "Tarifs: 1h=5DT, 3h=12DT, journée=25DT, abonnement=150DT. " +
                    "Lieu: Géant Tunis. " +
                    "Réponds en français, de manière courte et amicale.";

    private static final String API_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    private static String apiKey = "";

    static {
        try (InputStream is = ChatbotService.class.getResourceAsStream("/config.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                apiKey = props.getProperty("groq.api.key", "");
            }
        } catch (Exception e) {
            System.out.println("Erreur chargement config.properties");
        }
    }

    public static void sendMessage(String userMessage,
                                   Consumer<String> onResponse,
                                   Runnable onError) {

        if (apiKey == null || apiKey.isBlank()) {
            Platform.runLater(() ->
                    onResponse.accept("⚠ Clé API Groq manquante dans config.properties"));
            return;
        }

        Thread thread = new Thread(() -> {
            try {
                String response = callGroqApi(userMessage);
                Platform.runLater(() -> onResponse.accept(response));
            } catch (Exception e) {
                e.printStackTrace();

                Platform.runLater(() ->
                        onResponse.accept("❌ Erreur: " + e.getMessage()));
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    private static String callGroqApi(String userMessage) throws Exception {

        JSONObject systemMsg = new JSONObject()
                .put("role", "system")
                .put("content", SYSTEM_CONTEXT);

        JSONObject userMsg = new JSONObject()
                .put("role", "user")
                .put("content", userMessage);

        JSONArray messages = new JSONArray()
                .put(systemMsg)
                .put(userMsg);

        JSONObject body = new JSONObject()
                .put("model", "llama-3.1-8b-instant")
                .put("messages", messages);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP " + response.statusCode() + " : " + response.body());
        }

        return new JSONObject(response.body())
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");
    }
}