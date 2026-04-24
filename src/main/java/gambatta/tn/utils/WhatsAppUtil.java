package gambatta.tn.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WhatsAppUtil {

    // IMPORTANT : Remplissez ces valeurs avec vos vrais identifiants de l'API Meta Cloud
    private static final String ACCESS_TOKEN = "VOTRE_META_ACCESS_TOKEN";
    private static final String PHONE_NUMBER_ID = "VOTRE_PHONE_NUMBER_ID";
    private static final String API_VERSION = "v22.0";

    public static void sendReservationMessage(String toNumber, String activityName, String status) throws Exception {
        if (toNumber == null || toNumber.trim().isEmpty()) {
            throw new Exception("Numéro de téléphone invalide ou non fourni.");
        }

        if (ACCESS_TOKEN.equals("VOTRE_META_ACCESS_TOKEN") || ACCESS_TOKEN.isEmpty()) {
            throw new Exception("VOTRE_META_ACCESS_TOKEN n'a pas été remplacé dans WhatsAppUtil.java !");
        }

        if (!toNumber.startsWith("+") && !toNumber.startsWith("216")) {
            toNumber = "216" + toNumber; 
        } else if (toNumber.startsWith("+")) {
            toNumber = toNumber.substring(1);
        }

        String jsonPayload = "{"
                + "\"messaging_product\": \"whatsapp\","
                + "\"to\": \"" + toNumber + "\","
                + "\"type\": \"template\","
                + "\"template\": { \"name\": \"hello_world\", \"language\": { \"code\": \"en_US\" } }"
                + "}";

        String url = "https://graph.facebook.com/" + API_VERSION + "/" + PHONE_NUMBER_ID + "/messages";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + ACCESS_TOKEN)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            System.out.println("✅ Message WhatsApp envoyé avec succès au " + toNumber);
        } else {
            String error = response.body();
            if (error.contains("template") || error.contains("24 hours")) {
                throw new Exception("BLOCAGE META (Règle des 24h) : Envoyez d'abord un message de votre téléphone vers le numéro de test WhatsApp pour débloquer l'envoi !");
            } else if (error.contains("Invalid OAuth access token")) {
                throw new Exception("Le TOKEN Meta est invalide ou expiré !");
            } else {
                throw new Exception("Erreur API Meta (Code " + response.statusCode() + "): " + error);
            }
        }
    }
}
