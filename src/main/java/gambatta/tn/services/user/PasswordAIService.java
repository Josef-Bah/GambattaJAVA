package gambatta.tn.services.user;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Service IA d'analyse de mot de passe.
 * Utilise l'API Groq (GRATUITE) avec le modèle Llama3.
 * Clé gratuite sur : https://console.groq.com
 */
public class PasswordAIService {

    // ── Groq API (gratuite, même format qu'OpenAI) ───────────────────────────
    private static final String API_URL   = "https://api.groq.com/openai/v1/chat/completions";
    private static final String API_MODEL = "llama3-8b-8192";

    private final String     apiKey;
    private final HttpClient httpClient;

    // ── Résultat retourné au controller ──────────────────────────────────────
    public static class PasswordAnalysis {
        public enum Score { FAIBLE, MOYEN, FORT, TRES_FORT }

        public final Score  score;
        public final String label;
        public final String suggestion1;
        public final String suggestion2;
        public final String cssColor;

        public PasswordAnalysis(Score score, String label,
                                String suggestion1, String suggestion2) {
            this.score       = score;
            this.label       = label;
            this.suggestion1 = suggestion1;
            this.suggestion2 = suggestion2;
            this.cssColor    = switch (score) {
                case FAIBLE    -> "#e74c3c";
                case MOYEN     -> "#e67e22";
                case FORT      -> "#2980b9";
                case TRES_FORT -> "#27ae60";
            };
        }
    }

    // ────────────────────────────────────────────────────────────────────────

    public PasswordAIService() {
        this.apiKey     = loadApiKey();
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Analyse le mot de passe via Groq/Llama3.
     * À appeler depuis un thread non-UI.
     */
    public PasswordAnalysis analyze(String password) {
        if (password == null || password.isEmpty()) {
            return new PasswordAnalysis(PasswordAnalysis.Score.FAIBLE,
                    "–", "Saisissez un mot de passe.", "");
        }
        if (password.length() < 4) {
            return new PasswordAnalysis(PasswordAnalysis.Score.FAIBLE,
                    "⚠ Faible",
                    "Utilisez au moins 8 caractères.",
                    "Mélangez lettres, chiffres et symboles.");
        }
        try {
            String responseBody = callGroqAPI(password);
            return parseResponse(responseBody);
        } catch (Exception e) {
            System.err.println("PasswordAIService erreur : " + e.getMessage());
            return localFallback(password);
        }
    }

    // ── Appel HTTP vers Groq ─────────────────────────────────────────────────

    private String callGroqAPI(String password) throws IOException, InterruptedException {

        String systemPrompt = """
            Tu es un expert en sécurité informatique. Tu analyses des mots de passe.
            Réponds UNIQUEMENT en JSON valide, sans texte autour, sans backticks.
            Format exact :
            {
              "score": "FAIBLE" | "MOYEN" | "FORT" | "TRES_FORT",
              "label": "texte court en français avec emoji (ex: ✅ Fort)",
              "suggestion1": "conseil concret max 60 caractères",
              "suggestion2": "deuxième conseil concret max 60 caractères"
            }
            Critères :
            - FAIBLE   : moins de 8 caractères ou seulement minuscules
            - MOYEN    : 8+ caractères mais manque complexité
            - FORT     : 10+ caractères avec majuscule + chiffre + symbole
            - TRES_FORT: 14+ caractères, très complexe
            Les suggestions doivent être personnalisées selon ce qui manque réellement.
            """;

        // Format OpenAI (compatible Groq)
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", API_MODEL);
        requestBody.put("max_tokens", 200);
        requestBody.put("temperature", 0.3);

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "system")
                .put("content", systemPrompt));
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", "Analyse ce mot de passe : " + password));
        requestBody.put("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type",  "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(
                        requestBody.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Groq API erreur HTTP " + response.statusCode()
                    + " : " + response.body());
        }
        return response.body();
    }

    // ── Parse de la réponse Groq (format OpenAI) ─────────────────────────────

    private PasswordAnalysis parseResponse(String responseBody) {
        try {
            JSONObject root = new JSONObject(responseBody);
            String text = root
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim();

            // Nettoyer les éventuels backticks que le modèle ajouterait
            text = text.replaceAll("```json", "").replaceAll("```", "").trim();

            JSONObject json = new JSONObject(text);

            String scoreStr    = json.getString("score");
            String label       = json.getString("label");
            String suggestion1 = json.getString("suggestion1");
            String suggestion2 = json.getString("suggestion2");

            PasswordAnalysis.Score score = switch (scoreStr) {
                case "MOYEN"     -> PasswordAnalysis.Score.MOYEN;
                case "FORT"      -> PasswordAnalysis.Score.FORT;
                case "TRES_FORT" -> PasswordAnalysis.Score.TRES_FORT;
                default          -> PasswordAnalysis.Score.FAIBLE;
            };

            return new PasswordAnalysis(score, label, suggestion1, suggestion2);

        } catch (Exception e) {
            System.err.println("Erreur parsing réponse Groq : " + e.getMessage());
            return new PasswordAnalysis(PasswordAnalysis.Score.FAIBLE,
                    "Analyse indisponible", "Réessayez dans un instant.", "");
        }
    }

    // ── Fallback local si Groq est indisponible ──────────────────────────────

    private PasswordAnalysis localFallback(String password) {
        int score = 0;
        if (password.length() >= 8)                   score++;
        if (password.length() >= 12)                  score++;
        if (password.matches(".*[A-Z].*"))            score++;
        if (password.matches(".*\\d.*"))              score++;
        if (password.matches(".*[!@#$%^&*()_+\\-].*")) score++;

        return switch (score) {
            case 0, 1 -> new PasswordAnalysis(PasswordAnalysis.Score.FAIBLE,
                    "⚠ Faible", "Utilisez 8+ caractères.", "Ajoutez une majuscule.");
            case 2, 3 -> new PasswordAnalysis(PasswordAnalysis.Score.MOYEN,
                    "🔶 Moyen", "Ajoutez un symbole (!, @, #…).", "Utilisez 12+ caractères.");
            case 4    -> new PasswordAnalysis(PasswordAnalysis.Score.FORT,
                    "🔷 Fort", "Très bien !", "Ajoutez 2 caractères de plus pour être parfait.");
            default   -> new PasswordAnalysis(PasswordAnalysis.Score.TRES_FORT,
                    "✅ Très fort", "Mot de passe excellent !", "Rien à améliorer.");
        };
    }

    // ── Lecture de la clé dans config.properties ─────────────────────────────

    private String loadApiKey() {
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (is == null) {
                System.err.println("config.properties introuvable dans resources/");
                return "";
            }
            Properties props = new Properties();
            props.load(is);
            return props.getProperty("GROQ_API_KEY", "");
        } catch (IOException e) {
            System.err.println("Erreur lecture config.properties : " + e.getMessage());
            return "";
        }
    }
}