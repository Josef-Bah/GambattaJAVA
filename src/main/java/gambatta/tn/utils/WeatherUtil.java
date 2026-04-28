package gambatta.tn.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WeatherUtil {

    private static final String API_KEY = "6353d17a9037001012eb186fe64ae259";
    private static final String LAT     = "36.8065";
    private static final String LON     = "10.1815";

    // ─────────────────────────────────────────
    //  CURRENT WEATHER  (enriched)
    // ─────────────────────────────────────────
    public static WeatherData getCurrentWeather() {
        WeatherData data = new WeatherData();
        try {
            String urlString = "https://api.openweathermap.org/data/2.5/weather?lat=" + LAT
                    + "&lon=" + LON + "&appid=" + API_KEY + "&units=metric&lang=fr";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(8000);

            if (conn.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) response.append(line);
                in.close();

                JSONObject json    = new JSONObject(response.toString());
                JSONObject main    = json.getJSONObject("main");
                JSONObject weather = json.getJSONArray("weather").getJSONObject(0);
                JSONObject wind    = json.optJSONObject("wind");

                data.temperature  = main.getDouble("temp");
                data.feelsLike    = main.getDouble("feels_like");
                data.tempMin      = main.getDouble("temp_min");
                data.tempMax      = main.getDouble("temp_max");
                data.humidity     = main.getInt("humidity");
                data.description  = weather.getString("description");
                data.iconId       = weather.getString("icon");
                data.windSpeed    = wind != null ? wind.optDouble("speed", 0.0) * 3.6 : 0.0; // m/s → km/h
                data.cityName     = json.optString("name", "Soukra");
                data.isSuccess    = true;
            } else {
                data.isSuccess   = false;
                data.description = "Erreur HTTP " + conn.getResponseCode();
            }
        } catch (Exception e) {
            data.isSuccess   = false;
            data.description = "Impossible de récupérer la météo";
            System.err.println("[WeatherUtil] " + e.getMessage());
        }
        return data;
    }

    // ─────────────────────────────────────────
    //  HOURLY FORECAST (next 24 h, 8 slots)
    // ─────────────────────────────────────────
    public static List<HourlyEntry> getHourlyForecast() {
        List<HourlyEntry> entries = new ArrayList<>();
        try {
            String urlString = "https://api.openweathermap.org/data/2.5/forecast?lat=" + LAT
                    + "&lon=" + LON + "&appid=" + API_KEY + "&units=metric&lang=fr&cnt=8";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(8000);

            if (conn.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) response.append(line);
                in.close();

                JSONObject json = new JSONObject(response.toString());
                JSONArray  list = json.getJSONArray("list");

                for (int i = 0; i < list.length(); i++) {
                    JSONObject item = list.getJSONObject(i);
                    HourlyEntry entry = new HourlyEntry();
                    entry.timeText   = item.getString("dt_txt").substring(11, 16); // "HH:mm"
                    entry.temperature = item.getJSONObject("main").getDouble("temp");
                    entry.description = item.getJSONArray("weather").getJSONObject(0).getString("description");
                    entry.iconId      = item.getJSONArray("weather").getJSONObject(0).getString("icon");
                    entry.pop         = (int) Math.round(item.optDouble("pop", 0.0) * 100); // 0–100 %
                    entries.add(entry);
                }
            }
        } catch (Exception e) {
            System.err.println("[WeatherUtil] Prévisions: " + e.getMessage());
        }
        return entries;
    }

    // ─────────────────────────────────────────
    //  DAILY FORECAST  (today + tomorrow)
    // ─────────────────────────────────────────
    public static List<DailyEntry> getDailyForecast() {
        List<DailyEntry> days = new ArrayList<>();
        try {
            // Using 5-day forecast, group by day
            String urlString = "https://api.openweathermap.org/data/2.5/forecast?lat=" + LAT
                    + "&lon=" + LON + "&appid=" + API_KEY + "&units=metric&lang=fr&cnt=16";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(8000);

            if (conn.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) response.append(line);
                in.close();

                JSONObject json = new JSONObject(response.toString());
                JSONArray  list = json.getJSONArray("list");

                java.util.Map<String, DailyEntry> dayMap = new java.util.LinkedHashMap<>();
                for (int i = 0; i < list.length(); i++) {
                    JSONObject item = list.getJSONObject(i);
                    String dateKey  = item.getString("dt_txt").substring(0, 10);
                    double temp     = item.getJSONObject("main").getDouble("temp");
                    double pop      = item.optDouble("pop", 0.0) * 100;
                    String iconId   = item.getJSONArray("weather").getJSONObject(0).getString("icon");

                    DailyEntry de = dayMap.getOrDefault(dateKey, new DailyEntry());
                    de.dateKey = dateKey;
                    de.tempMin = de.dateKey.equals(dateKey) ? Math.min(de.tempMin == 0 ? temp : de.tempMin, temp) : temp;
                    de.tempMax = Math.max(de.tempMax, temp);
                    de.maxPop  = Math.max(de.maxPop, pop);
                    if (de.iconId == null) de.iconId = iconId;
                    dayMap.put(dateKey, de);
                }
                days.addAll(dayMap.values());
                if (days.size() > 3) days = days.subList(0, 3);
            }
        } catch (Exception e) {
            System.err.println("[WeatherUtil] Daily: " + e.getMessage());
        }
        return days;
    }

    // ─────────────────────────────────────────
    //  DATA CLASSES
    // ─────────────────────────────────────────
    public static class WeatherData {
        public double  temperature;
        public double  feelsLike;
        public double  tempMin;
        public double  tempMax;
        public double  windSpeed;  // km/h
        public int     humidity;
        public String  description;
        public String  iconId;
        public String  cityName = "Soukra";
        public boolean isSuccess;

        public String getFormattedTemp()  { return String.format("%.0f°", temperature); }
        public String getFormattedFeels() { return String.format("Feels Like: %.0f°", feelsLike); }
        public String getFormattedHL()    { return String.format("H:%.0f°  L:%.0f°", tempMax, tempMin); }

        public String getCapitalizedDescription() {
            if (description == null || description.isEmpty()) return "";
            return description.substring(0, 1).toUpperCase() + description.substring(1);
        }
    }

    public static class HourlyEntry {
        public String timeText;
        public double temperature;
        public String description;
        public String iconId;
        public int    pop;   // rain probability %

        public String getFormattedTemp() { return String.format("%.0f°", temperature); }
    }

    public static class DailyEntry {
        public String dateKey;
        public double tempMin;
        public double tempMax;
        public double maxPop;   // max rain probability %
        public String iconId;

        public String getDayLabel() {
            if (dateKey == null) return "—";
            try {
                java.time.LocalDate d = java.time.LocalDate.parse(dateKey);
                java.time.LocalDate today = java.time.LocalDate.now();
                if (d.equals(today))      return "Today";
                if (d.equals(today.plusDays(1))) return "Tomorrow";
                return d.getDayOfWeek().getDisplayName(
                        java.time.format.TextStyle.SHORT,
                        java.util.Locale.ENGLISH);
            } catch (Exception e) { return dateKey; }
        }
    }
}
