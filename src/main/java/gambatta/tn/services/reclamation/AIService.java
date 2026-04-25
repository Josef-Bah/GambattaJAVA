package gambatta.tn.services.reclamation;

import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AIService {

    // TA CLÉ GOOGLE AI STUDIO
    private static final String API_KEY = "AIzaSyDa0LjZEICZkAjhH2cIdz8omumqABfSIew";

    // CORRECTION DÉFINITIVE : Utilisation de "gemini-2.5-flash" (le seul modèle actif pour les nouvelles clés)
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    private final HttpClient client;

    public AIService() {
        this.client = HttpClient.newHttpClient();
    }

    public String optimiserTexte(String texteBrouillon) {
        if (texteBrouillon == null || texteBrouillon.isEmpty()) return texteBrouillon;

        try {
            // 1. On donne l'ordre à l'IA
            String prompt = "Tu es un assistant professionnel pour une plateforme e-sport. Corrige les fautes, améliore le style et rends ce texte de réclamation très respectueux et clair. Ne réponds QUE par le texte corrigé, sans ajouter de commentaires d'introduction ou de conclusion : " + texteBrouillon;

            // 2. On construit le JSON
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

            // 3. On envoie la requête
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            // 4. On récupère la réponse
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // On extrait le texte
                JSONObject jsonResponse = new JSONObject(response.body());
                String texteFinal = jsonResponse.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");

                return texteFinal.trim();
            } else {
                System.err.println("Erreur API Gemini : " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return texteBrouillon;
    }
}