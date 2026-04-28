package gambatta.tn.utils;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class CloudinaryUtil {

    // IMPORTANT : Remplacez ces valeurs par vos vrais identifiants Cloudinary !
    private static final String CLOUD_NAME = "dchqj3gsg";
    private static final String API_KEY = "675283748754298";
    private static final String API_SECRET = "LZ46A6rmY33J6_dWAkogOilJgJU";

    private static Cloudinary cloudinary;

    public static Cloudinary getInstance() {
        if (cloudinary == null) {
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", CLOUD_NAME,
                    "api_key", API_KEY,
                    "api_secret", API_SECRET,
                    "secure", true
            ));
        }
        return cloudinary;
    }

    /**
     * Upload un fichier local vers Cloudinary et retourne l'URL sécurisée.
     */
    public static String uploadFile(File file) throws IOException {
        // Si les identifiants ne sont pas configurés
        if (CLOUD_NAME.contains("votre") || API_KEY.isEmpty()) {
            System.out.println("⚠️ Cloudinary non configuré, retour du chemin local.");
            return file.toURI().toString();
        }

        try {
            // Utiliser "image" pour les PDF permet à Cloudinary de les traiter avec plus de flexibilité
            // et évite souvent les erreurs 401 liées aux fichiers "raw"
            String resourceType = file.getName().toLowerCase().endsWith(".pdf") ? "image" : "auto";
            
            Map uploadResult = getInstance().uploader().upload(file.getAbsolutePath(), ObjectUtils.asMap(
                "resource_type", resourceType,
                "type", "upload",         // Retour au mode public pour tester si "image" passe mieux
                "invalidate", true
            ));
            
            // Récupérer l'URL sécurisée
            String secureUrl = (String) uploadResult.get("secure_url");
            
            System.out.println("✅ URL PDF (mode image) : " + secureUrl);
            return secureUrl;
        } catch (Exception e) {
            System.err.println("❌ Erreur Cloudinary détaillée : " + e.getMessage());
            // En cas d'erreur de signature, on retourne le chemin local pour ne pas bloquer l'utilisateur
            return file.toURI().toString();
        }
    }
}
