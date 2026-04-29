package gambatta.tn.services.user;

import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class MapService {

    // OpenStreetMap Static Maps — free, no API key required
    private static final String STATIC_MAP_URL =
            "https://staticmap.openstreetmap.de/staticmap.php" +
            "?center=36.8065,10.1815" +
            "&zoom=15" +
            "&size=600x380" +
            "&markers=36.8065,10.1815,red-pushpin";

    private static final String GOOGLE_MAPS_URL =
            "https://www.google.com/maps/search/Gambatta+ESports+Tunis+Geant";

    private static final String DIRECTIONS_URL =
            "https://www.google.com/maps/dir/?api=1&destination=36.8065,10.1815";

    /**
     * Fetches the OpenStreetMap static map image synchronously.
     * Must be called from a background thread.
     * Returns null on failure.
     */
    public static Image fetchStaticMap() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(STATIC_MAP_URL))
                    .timeout(Duration.ofSeconds(20))
                    .header("User-Agent", "GambattaJAVA/1.0")
                    .GET()
                    .build();

            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 200 && response.body().length > 0) {
                return new Image(new ByteArrayInputStream(response.body()));
            }
            return null;
        } catch (Exception e) {
            System.out.println("MapService: échec du chargement — " + e.getMessage());
            return null;
        }
    }

    public static String getGoogleMapsUrl()   { return GOOGLE_MAPS_URL; }
    public static String getDirectionsUrl()   { return DIRECTIONS_URL; }
}
