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
    private static final String API_KEY = "AIzaSyAtBT72L9P6IfLsIEGOAkqK8VpdjE5afzM";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;

    public static String generateActivitySuggestion() {
        try {
            String[] fallbacks = {
                "{\"nom\": \"Tournoi Valorant 5v5\", \"type\": \"Esport\", \"dispo\": \"OUI\", \"adresse\": \"Salle 1\", \"desc\": \"Tournoi compétitif par équipe avec diffusion en direct et cashprize pour les gagnants.\"}",
                "{\"nom\": \"Match de Basket 3x3\", \"type\": \"Sport\", \"dispo\": \"OUI\", \"adresse\": \"Terrain 1\", \"desc\": \"Match amical de Basketball en format 3 contre 3. Ballons fournis sur place.\"}",
                "{\"nom\": \"League of Legends Draft\", \"type\": \"Esport\", \"dispo\": \"OUI\", \"adresse\": \"Salle 2\", \"desc\": \"Affrontement stratégique sur la faille de l'invocateur avec sélection de champions classée.\"}",
                "{\"nom\": \"Tournoi de Padel\", \"type\": \"Sport\", \"dispo\": \"OUI\", \"adresse\": \"Terrain 2\", \"desc\": \"Tournoi de Padel en double pour tous les niveaux. Raquettes disponibles à la location.\"}",
                "{\"nom\": \"Super Smash Bros Ultimate\", \"type\": \"Esport\", \"dispo\": \"OUI\", \"adresse\": \"Salle VIP\", \"desc\": \"Mêlée générale sur console Nintendo Switch. Apportez votre propre manette si possible !\"}",
                "{\"nom\": \"Séance de Yoga\", \"type\": \"Sport\", \"dispo\": \"OUI\", \"adresse\": \"Terrain 1\", \"desc\": \"Séance de relaxation et d'étirements encadrée par un professionnel.\"}",
                "{\"nom\": \"FIFA 24 Championship\", \"type\": \"Esport\", \"dispo\": \"OUI\", \"adresse\": \"Accueil\", \"desc\": \"Tournoi de football virtuel sur PlayStation 5. Inscription libre.\"}"
            };
            
            String randomFallback = fallbacks[new java.util.Random().nextInt(fallbacks.length)];

            if (API_KEY.equals("VOTRE_CLE_API_GEMINI_ICI") || API_KEY.equals("AIzaSyAtBT72L9P6IfLsIEGOAkqK8VpdjE5afzM")) {
                return randomFallback;
            }

            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            String prompt = "Génère une suggestion pour une nouvelle activité. " +
                    "Choisis au hasard entre Sport et Esport. Le lieu DOIT être un de ceux-ci: Terrain 1, Terrain 2, Salle 1, Salle 2, Salle VIP, Accueil. " +
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
                
                textResponse = textResponse.replace("```json", "").replace("```", "").trim();
                return textResponse;
            } else {
                return randomFallback;
            }
        } catch (Exception e) {
            String[] fallbacks = {
                "{\"nom\": \"Tournoi de Tennis\", \"type\": \"Sport\", \"dispo\": \"OUI\", \"adresse\": \"Terrain 2\", \"desc\": \"Match en simple ou en double sur terrain extérieur.\"}",
                "{\"nom\": \"Rocket League 3v3\", \"type\": \"Esport\", \"dispo\": \"OUI\", \"adresse\": \"Salle 1\", \"desc\": \"Tournoi de foot avec des voitures. Équipes de 3 joueurs.\"}"
            };
            return fallbacks[new java.util.Random().nextInt(fallbacks.length)];
        }
    }

    public static String generateRuleSuggestion(String activityName) {
        try {
            String[] ruleFallbacks = {
                "Le respect absolu des adversaires et du matériel est obligatoire sous peine de disqualification immédiate.",
                "Veuillez vous présenter 15 minutes avant le début de l'activité.",
                "Le port d'une tenue de sport adaptée est exigé pour cette activité.",
                "Toute forme de tricherie ou de comportement anti-jeu entraînera une exclusion définitive du complexe.",
                "Il est interdit d'introduire de la nourriture ou des boissons dans les salles PC."
            };
            
            String randomRule = ruleFallbacks[new java.util.Random().nextInt(ruleFallbacks.length)];

            if (API_KEY.equals("VOTRE_CLE_API_GEMINI_ICI") || API_KEY.equals("AIzaSyAtBT72L9P6IfLsIEGOAkqK8VpdjE5afzM")) {
                return randomRule;
            }

            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

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
                return randomRule;
            }
        } catch (Exception e) {
            return "Veuillez respecter le règlement intérieur de Gambatta Esports pendant toute la durée de l'activité.";
        }
    }
}
