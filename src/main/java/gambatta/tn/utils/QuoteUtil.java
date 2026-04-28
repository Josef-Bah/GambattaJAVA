package gambatta.tn.utils;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class QuoteUtil {

    private static final String API_URL = "https://api.adviceslip.com/advice";

    public static String getDailyAdvice() {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                return json.getJSONObject("slip").getString("advice");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération du conseil: " + e.getMessage());
        }
        return "Le sport va chercher la peur pour la dominer, la fatigue pour en triompher, la difficulté pour la vaincre."; // Fallback motivational quote
    }
}
