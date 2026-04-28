package gambatta.tn.services.tournoi;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class CloudinaryService {

    // Identifiants récupérés de la gestion réclamation
    private static final String CLOUD_NAME = "dgjihus18";
    private static final String API_KEY = "544787974269455";
    private static final String API_SECRET = "GRshvMmeMH6SErfZp83dQSHQigY";

    private final Cloudinary cloudinary;

    public CloudinaryService() {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", CLOUD_NAME,
                "api_key", API_KEY,
                "api_secret", API_SECRET,
                "secure", true
        ));
    }

    /**
     * Upload une image vers Cloudinary et retourne l'URL sécurisée (HTTPS).
     */
    public String uploadImage(File file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.asMap(
                    "folder", "gambatta/pdfs",
                    "resource_type", "auto" // Auto détecte PDF, Images, etc.
            ));
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            System.err.println("ERREUR_CLOUD : Échec de l'upload vers Cloudinary");
            e.printStackTrace();
            return null;
        }
    }
}
