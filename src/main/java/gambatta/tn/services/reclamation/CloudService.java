package gambatta.tn.services.reclamation;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class CloudService {

    // REMPLACE CES VALEURS PAR LES TIENNES (Dashboard Cloudinary)
    private static final String CLOUD_NAME = "dh0jz5ruc";
    private static final String API_KEY = "887475919413442";
    private static final String API_SECRET = "VntQ0sqXcoPvTyiwStJ3KbTH5GA";

    private final Cloudinary cloudinary;

    public CloudService() {
        // Initialisation de la connexion sécurisée
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", CLOUD_NAME,
                "api_key", API_KEY,
                "api_secret", API_SECRET,
                "secure", true
        ));
    }

    /**
     * Envoie l'image au Cloud et retourne l'URL HTTPS
     */
    public String uploadPreuve(File file) {
        try {
            // On crée un dossier "reclamations_preuves" sur le cloud pour ranger tes images
            Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.asMap(
                    "folder", "gambatta/preuves",
                    "resource_type", "image"
            ));

            // On retourne l'URL sécurisée générée par Cloudinary
            return (String) uploadResult.get("secure_url");

        } catch (IOException e) {
            System.err.println("ERREUR_CLOUD : Échec de l'upload vers Cloudinary");
            e.printStackTrace();
            return null;
        }
    }
}