package gambatta.tn.utils;

import gambatta.tn.entites.activites.activite;
import gambatta.tn.entites.activites.rules;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class GeminiUtil {

    private static final String API_KEY = "AIzaSyAtBT72L9P6IfLsIEGOAkqK8VpdjE5afzM";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;

    // ─────────────────────────────────────────────────────────────────
    //  GENERATE ACTIVITY  (reads existing DB activities for context)
    // ─────────────────────────────────────────────────────────────────
    public static String generateActivitySuggestion(List<activite> existingActivities) {
        // Build a readable summary of the DB context
        StringBuilder dbContext = new StringBuilder();
        if (existingActivities != null && !existingActivities.isEmpty()) {
            dbContext.append("CRITIQUE : Voici les noms d'activités déjà EXPLOITÉS. Tu as l'INTERDICTION STRICTE de suggérer l'un d'eux :\n");
            for (activite a : existingActivities) {
                dbContext.append("- ").append(a.getNoma()).append(" (Type: ").append(a.getTypea()).append(")\n");
            }
            dbContext.append("\nINSTRUCTION : Génère une activité TOTALEMENT INÉDITE, créative et innovante, qui ne ressemble pas à celles listées.");
        } else {
            dbContext.append("Démarre fort : génère une première activité emblématique.");
        }

        String prompt = dbContext +
                "\n\nTu es un expert en divertissement. Propose une nouvelle activité (Sport ou Esport) pour un complexe moderne. " +
                "Sois spécifique (ex: 'Tournoi Nocturne de [Jeu]', 'Ligue Master [Sport]'). " +
                "Lieu requis : Terrain 1, Terrain 2, Salle 1, Salle 2, Salle VIP ou Accueil. " +
                "Format JSON strict (pas de markdown) : " +
                "{\"nom\": \"Nom Unique\", \"type\": \"Sport ou Esport\", \"dispo\": \"OUI\", \"adresse\": \"Lieu\", \"desc\": \"2 phrases accrocheuses.\"}";

        return callGeminiApi(prompt, buildActivityFallback());
    }

    /** Convenience overload without DB context (backwards-compatible) */
    public static String generateActivitySuggestion() {
        return generateActivitySuggestion(null);
    }

    // ─────────────────────────────────────────────────────────────────
    //  GENERATE RULE  (reads existing DB rules for context)
    // ─────────────────────────────────────────────────────────────────
    public static String generateRuleSuggestion(String activityName, List<rules> existingRules) {
        // Build a readable summary of existing rules for this activity
        StringBuilder dbContext = new StringBuilder();
        if (existingRules != null && !existingRules.isEmpty()) {
            dbContext.append("Voici les règles déjà définies pour cette activité :\n");
            for (rules r : existingRules) {
                dbContext.append("- ").append(r.getRuleDescription()).append("\n");
            }
            dbContext.append("\nGénère une règle DIFFÉRENTE et COMPLÉMENTAIRE à celles listées. ");
        } else {
            dbContext.append("Il n'y a encore aucune règle définie pour cette activité. ");
        }

        String prompt = dbContext +
                "Génère une règle stricte, claire et pertinente pour l'activité : \"" + activityName + "\". " +
                "La règle doit être courte (1 phrase max) et adaptée au contexte d'un complexe sportif/esport. " +
                "Réponds UNIQUEMENT avec le texte de la règle, sans aucun autre commentaire ni formatage.";

        return callGeminiApi(prompt, buildRuleFallback());
    }

    /** Convenience overload without DB context (backwards-compatible) */
    public static String generateRuleSuggestion(String activityName) {
        return generateRuleSuggestion(activityName, null);
    }

    /** Generate a motivational tip specifically for Gaming or Sports */
    public static String generateGamingTip() {
        String prompt = "Tu es un coach expert en Gaming et Sport. Donne un conseil court, motivant et percutant (max 20 mots) pour un joueur qui s'apprête à faire une activité dans un complexe de loisirs. " +
                        "Le conseil peut concerner la stratégie, le mental, ou la santé (ex: s'hydrater, rester concentré). " +
                        "Réponds UNIQUEMENT avec le texte du conseil.";
        return callGeminiApi(prompt, "Reste concentré, garde l'esprit d'équipe et amuse-toi !");
    }

    // ─────────────────────────────────────────────────────────────────
    //  INTERNAL : single HTTP call to Gemini API
    // ─────────────────────────────────────────────────────────────────
    private static String callGeminiApi(String prompt, String fallback) {
        if (API_KEY == null || API_KEY.isBlank() || API_KEY.equals("VOTRE_CLE_API_GEMINI_ICI")) {
            System.out.println("[GeminiUtil] Clé API non configurée — retour au fallback local.");
            return fallback;
        }

        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(10000);

            // Build request JSON
            JSONObject textObj   = new JSONObject().put("text", prompt);
            JSONArray  parts     = new JSONArray().put(textObj);
            JSONObject partsObj  = new JSONObject().put("parts", parts);
            JSONArray  contents  = new JSONArray().put(partsObj);
            JSONObject requestJson = new JSONObject().put("contents", contents);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestJson.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }

                JSONObject jsonResponse = new JSONObject(response.toString());
                String textResponse = jsonResponse
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");

                // Strip markdown code fences if present
                textResponse = textResponse.replace("```json", "").replace("```", "").trim();
                System.out.println("[GeminiUtil] ✅ Réponse reçue : " + textResponse);
                return textResponse;

            } else {
                // Log error body for debugging
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
                StringBuilder errBody = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) errBody.append(line);
                System.err.println("[GeminiUtil] ❌ HTTP " + responseCode + " — " + errBody);
                return fallback;
            }

        } catch (Exception e) {
            System.err.println("[GeminiUtil] ❌ Exception : " + e.getMessage());
            return fallback;
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  FALLBACKS  (used only when API is unreachable)
    // ─────────────────────────────────────────────────────────────────
    private static String buildActivityFallback() {
        String[] fallbacks = {
            "{\"nom\": \"Ligue VR Beat Saber\", \"type\": \"Esport\", \"dispo\": \"OUI\", \"adresse\": \"Salle VIP\", \"desc\": \"Compétition de rythme en réalité virtuelle. Sensations fortes garanties !\"}",
            "{\"nom\": \"Tournoi de Tir à l'Arc\", \"type\": \"Sport\", \"dispo\": \"OUI\", \"adresse\": \"Terrain 2\", \"desc\": \"Épreuve de précision en plein air. Tout l'équipement de protection est fourni.\"}",
            "{\"nom\": \"Championship Rocket League 2v2\", \"type\": \"Esport\", \"dispo\": \"OUI\", \"adresse\": \"Salle 1\", \"desc\": \"Matchs de voitures propulsées par fusée. Esprit d'équipe indispensable !\"}",
            "{\"nom\": \"Marathon de Fitness Cyberpunk\", \"type\": \"Sport\", \"dispo\": \"OUI\", \"adresse\": \"Accueil\", \"desc\": \"Séance intensive sous néons avec musique synthwave. Repoussez vos limites !\"}",
            "{\"nom\": \"Draft de Cartes Magic\", \"type\": \"Esport\", \"dispo\": \"OUI\", \"adresse\": \"Salle 2\", \"desc\": \"Tournoi de cartes stratégiques pour les passionnés. De nombreux boosters à gagner !\"}"
        };
        return fallbacks[new java.util.Random().nextInt(fallbacks.length)];
    }

    private static String buildRuleFallback() {
        String[] fallbacks = {
            "Le respect absolu des adversaires et du matériel est obligatoire sous peine de disqualification immédiate.",
            "Veuillez vous présenter 15 minutes avant le début de l'activité.",
            "Le port d'une tenue de sport adaptée est exigé pour cette activité.",
            "Toute forme de tricherie ou de comportement anti-jeu entraînera une exclusion définitive du complexe.",
            "Il est interdit d'introduire de la nourriture ou des boissons dans les salles PC."
        };
        return fallbacks[new java.util.Random().nextInt(fallbacks.length)];
    }
}
