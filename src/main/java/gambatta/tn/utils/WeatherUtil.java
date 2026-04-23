package gambatta.tn.utils;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherUtil {

    private static final String API_KEY = "6353d17a9037001012eb186fe64ae259";
    private static final String LAT = "36.8065";
    private static final String LON = "10.1815";

    public static WeatherData getCurrentWeather() {
        WeatherData data = new WeatherData();
        try {
            String urlString = "https://api.openweathermap.org/data/2.5/weather?lat=" + LAT + "&lon=" + LON + "&appid=" + API_KEY + "&units=metric&lang=fr";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONObject main = jsonResponse.getJSONObject("main");
                JSONObject weather = jsonResponse.getJSONArray("weather").getJSONObject(0);

                data.temperature = main.getDouble("temp");
                data.description = weather.getString("description");
                data.iconId = weather.getString("icon");
                data.isSuccess = true;
            } else {
                data.isSuccess = false;
                data.description = "Erreur de connexion (Code " + responseCode + ")";
            }
        } catch (Exception e) {
            data.isSuccess = false;
            data.description = "Impossible de récupérer la météo: " + e.getMessage();
        }
        return data;
    }

    public static class WeatherData {
        public double temperature;
        public String description;
        public String iconId;
        public boolean isSuccess;

        public String getFormattedTemp() {
            return String.format("%.1f°C", temperature);
        }

        public String getCapitalizedDescription() {
            if (description == null || description.isEmpty()) return "";
            return description.substring(0, 1).toUpperCase() + description.substring(1);
        }
    }
}
