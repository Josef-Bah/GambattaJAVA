package gambatta.tn.services.buvette;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import io.github.cdimascio.dotenv.Dotenv;

public class CloudinaryService {
    private static Cloudinary cloudinary;

    static {
        Dotenv dotenv = Dotenv.load();
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", dotenv.get("CLOUDINARY_CLOUD_NAME"),
                "api_key", dotenv.get("CLOUDINARY_API_KEY"),
                "api_secret", dotenv.get("CLOUDINARY_API_SECRET")));
    }

    public static String uploadFile(File file, String resourceType) {
        try {
            // Use "image" for PDFs to allow browser viewing, or "auto" for general files
            Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.asMap("resource_type", resourceType));
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String uploadImage(File file) {
        return uploadFile(file, "image");
    }
}
