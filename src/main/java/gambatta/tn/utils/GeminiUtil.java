package gambatta.tn.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GeminiUtil {

    // IMPORTANT : Remplacer par votre vraie clé API Gemini
    private static final String API_KEY = "VOTRE_CLE_API_GEMINI_ICI";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;

    public static String generateActivitySuggestion() throws Exception {
        if (API_KEY.equals("VOTRE_CLE_API_GEMINI_ICI")) {
            throw new RuntimeException("Clé API Gemini non configurée dans GeminiUtil.java");
        }

        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String prompt = "Génère une suggestion pour une nouvelle activité e-sport. " +
                "Réponds UNIQUEMENT avec un objet JSON ayant exactement ce format (sans markdown, sans autre texte) : " +
                "{\"nom\": \"Nom\", \"type\": \"Type\", \"dispo\": \"Dispo\", \"adresse\": \"Lieu\", \"desc\": \"Description détaillée\"}";

        JSONObject requestJson = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject partsObj = new JSONObject();
        JSONArray parts = new JSONArray();
        JSONObject textObj = new JSONObject();
        
        textObj.put("text", prompt);
        parts.put(textObj);
        partsObj.put("parts", parts);
        contents.put(partsObj);
        requestJson.put("contents", contents);

        try(OutputStream os = conn.getOutputStream()) {
            byte[] input = requestJson.toString().getBytes("utf-8");
            os.write(input, 0, input.length);			
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            JSONObject jsonResponse = new JSONObject(response.toString());
            String textResponse = jsonResponse.getJSONArray("candidates")
                    .getJSONObject(0).getJSONObject("content").getJSONArray("parts")
                    .getJSONObject(0).getString("text");
            
            // Remove markdown code block syntax if Gemini adds it despite instructions
            textResponse = textResponse.replace("```json", "").replace("```", "").trim();
            return textResponse;
        } else {
            throw new RuntimeException("Erreur de l'API Gemini : HTTP " + responseCode);
        }
    }

    public static String generateRuleSuggestion(String activityName) throws Exception {
        if (API_KEY.equals("VOTRE_CLE_API_GEMINI_ICI")) {
            throw new RuntimeException("Clé API Gemini non configurée dans GeminiUtil.java");
        }

        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String prompt = "Génère une règle stricte ou une consigne importante pour l'activité suivante : " + activityName + ". " +
                "La règle doit être courte, claire et pertinente pour les participants. " +
                "Réponds UNIQUEMENT avec le texte de la règle, sans aucun autre commentaire.";

        JSONObject requestJson = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject partsObj = new JSONObject();
        JSONArray parts = new JSONArray();
        JSONObject textObj = new JSONObject();
        
        textObj.put("text", prompt);
        parts.put(textObj);
        partsObj.put("parts", parts);
        contents.put(partsObj);
        requestJson.put("contents", contents);

        try(OutputStream os = conn.getOutputStream()) {
            byte[] input = requestJson.toString().getBytes("utf-8");
            os.write(input, 0, input.length);			
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            JSONObject jsonResponse = new JSONObject(response.toString());
            String textResponse = jsonResponse.getJSONArray("candidates")
                    .getJSONObject(0).getJSONObject("content").getJSONArray("parts")
                    .getJSONObject(0).getString("text");
            
            return textResponse.trim();
        } else {
            throw new RuntimeException("Erreur de l'API Gemini : HTTP " + responseCode);
        }
    }
}
