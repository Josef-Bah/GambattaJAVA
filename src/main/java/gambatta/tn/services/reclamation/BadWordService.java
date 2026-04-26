package gambatta.tn.services.reclamation;

import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class BadWordService {

    // Utilise la même clé Gemini que dans ton AIService
    private static final String API_KEY = "AIzaSyDa0LjZEICZkAjhH2cIdz8omumqABfSIew";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    private final HttpClient client;

    public BadWordService() {
        this.client = HttpClient.newHttpClient();
    }

    /**
     * Analyse un texte via l'IA Gemini pour comprendre le vrai contexte.
     * @return true si une VRAIE insulte est détectée, false si c'est poli.
     */
    public boolean contientBadWord(String texte) {
        if (texte == null || texte.trim().isEmpty()) {
            return false;
        }

        try {
            // Le Prompt intelligent pour éviter les faux positifs en français
            String prompt = "Analyse ce texte en français. Contient-il des insultes, des mots vulgaires, ou des propos haineux ? "
                    + "Attention : Comprends le contexte. Les mots comme 'associée', 'passion', ou 'retard' sont normaux, ne les bloque pas. "
                    + "Réponds STRICTEMENT par le mot TRUE (si le texte est vraiment insultant ou inapproprié) "
                    + "ou FALSE (si le texte est poli ou normal). Texte : [" + texte + "]";

            // Construction du JSON
            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject partsObj = new JSONObject();
            JSONArray partsArr = new JSONArray();
            JSONObject textObj = new JSONObject();

            textObj.put("text", prompt);
            partsArr.put(textObj);
            partsObj.put("parts", partsArr);
            contents.put(partsObj);
            requestBody.put("contents", contents);

            // Envoi de la requête à l'IA
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                String resultatIA = jsonResponse.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text").trim().toUpperCase();

                // Si l'IA a répondu TRUE, on bloque le ticket
                return resultatIA.contains("TRUE");
            } else {
                System.err.println("Erreur API IA BadWord : " + response.body());
            }

        } catch (Exception e) {
            System.err.println("Erreur réseau API IA BadWord : " + e.getMessage());
        }

        // En cas de panne d'internet, on laisse passer le ticket pour ne pas bloquer le client
        return false;
    }
}