package gambatta.tn.services.reclamation;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class BadWordService {

    // L'URL de l'API gratuite PurgoMalum
    private static final String API_URL = "https://www.purgomalum.com/service/containsprofanity?text=";
    private final HttpClient client;

    public BadWordService() {
        this.client = HttpClient.newHttpClient();
    }

    /**
     * Analyse un texte via l'API.
     * @return true si un mot interdit est détecté, false sinon.
     */
    public boolean contientBadWord(String texte) {
        if (texte == null || texte.trim().isEmpty()) {
            return false;
        }

        try {
            // On encode le texte pour qu'il puisse voyager dans une URL (ex: les espaces deviennent %20)
            String encodedText = URLEncoder.encode(texte, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + encodedText))
                    .GET()
                    .build();

            // On envoie la requête et on récupère la réponse (qui sera "true" ou "false")
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return Boolean.parseBoolean(response.body());

        } catch (Exception e) {
            System.err.println("ERREUR API BAD WORD : " + e.getMessage());
            // En cas de panne de l'API, on retourne false pour ne pas bloquer l'utilisateur
            return false;
        }
    }
}