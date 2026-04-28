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
    private static final String API_SECRET = "LZ46A6rmY33J6_dWA";

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
        if (CLOUD_NAME.equals("votre_cloud_name")) {
            System.err.println("Avertissement : Cloudinary n'est pas configuré. Utilisation du chemin local.");
            return file.toURI().toString(); // Fallback silencieux au local
        }

        Map uploadResult = getInstance().uploader().upload(file, ObjectUtils.emptyMap());
        return uploadResult.get("secure_url").toString();
    }
}
