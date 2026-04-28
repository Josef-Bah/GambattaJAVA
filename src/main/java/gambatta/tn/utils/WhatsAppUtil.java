package gambatta.tn.utils;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WhatsAppUtil {

    private static final String DEFAULT_COUNTRY_CODE = "216";

    /**
     * Génère un lien wa.me comme dans le projet Symfony
     */
    public static String buildWhatsAppLink(String phone, String activityName, int reservationId, String date, String time) {
        if (phone == null || phone.trim().isEmpty()) return null;

        // Nettoyage du numéro (garder uniquement les chiffres)
        String digits = phone.replaceAll("\\D+", "");
        if (digits.isEmpty()) return null;

        // Ajout du code pays 216 par défaut si absent
        if (!digits.startsWith(DEFAULT_COUNTRY_CODE)) {
            digits = DEFAULT_COUNTRY_CODE + digits;
        }

        // Construction du message
        String message = String.format(
            "Bonjour, votre réservation #%d pour l'activité '%s' est CONFIRMÉE pour le %s à %s. L'équipe Gambatta.",
            reservationId, activityName, date, time
        );

        try {
            return "https://wa.me/" + digits + "?text=" + URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return "https://wa.me/" + digits;
        }
    }

    /**
     * Ouvre le lien dans le navigateur par défaut
     */
    public static void openInBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                // Fallback pour environnements restreints
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
                } else if (os.contains("mac")) {
                    Runtime.getRuntime().exec("open " + url);
                } else {
                    Runtime.getRuntime().exec("xdg-open " + url);
                }
            }
        } catch (Exception e) {
            System.err.println("Impossible d'ouvrir le navigateur : " + e.getMessage());
        }
    }
}
