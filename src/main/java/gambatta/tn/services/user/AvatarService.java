package gambatta.tn.services.user;

import javafx.scene.image.Image;
import java.util.List;

public class AvatarService {

    // Chaque entrée = "style|seed" — 40 avatars mix de 4 styles modernes
    public static final List<String> AVATAR_SEEDS = List.of(
            // 🎨 Lorelei — minimaliste élégant (10)
            "lorelei|Felix",
            "lorelei|Zoe",
            "lorelei|Kai",
            "lorelei|Luna",
            "lorelei|Max",
            "lorelei|Sara",
            "lorelei|Leo",
            "lorelei|Mia",
            "lorelei|Alex",
            "lorelei|Nina",

            // 🖼 Notionists — style Notion moderne (10)
            "notionists|Omar",
            "notionists|Lena",
            "notionists|Adam",
            "notionists|Sofia",
            "notionists|Nour",
            "notionists|Yasmine",
            "notionists|Rami",
            "notionists|Hana",
            "notionists|Sami",
            "notionists|Dina",

            // 👤 Personas — réaliste stylisé (10)
            "personas|Gamer1",
            "personas|Gamer2",
            "personas|Gamer3",
            "personas|Gamer4",
            "personas|Gamer5",
            "personas|Gamer6",
            "personas|Gamer7",
            "personas|Gamer8",
            "personas|Gamer9",
            "personas|Gamer10",

            // 😊 Big-Smile — fun et coloré (10)
            "big-smile|Pro1",
            "big-smile|Pro2",
            "big-smile|Pro3",
            "big-smile|Pro4",
            "big-smile|Pro5",
            "big-smile|Pro6",
            "big-smile|Pro7",
            "big-smile|Pro8",
            "big-smile|Pro9",
            "big-smile|Pro10"
    );

    // Génère l'URL DiceBear depuis "style|seed"
    private static String buildUrl(String styleSeed, int size) {
        if (styleSeed == null || !styleSeed.contains("|")) {
            // Compatibilité avec anciens seeds sans style
            return "https://api.dicebear.com/7.x/adventurer/png?seed="
                    + styleSeed + "&size=" + size;
        }
        String[] parts = styleSeed.split("\\|", 2);
        String style = parts[0];
        String seed  = parts[1];
        return "https://api.dicebear.com/7.x/" + style + "/png?seed="
                + seed + "&size=" + size;
    }

    // Avatar grand (profil)
    public static Image loadAvatarAsync(String styleSeed) {
        if (styleSeed == null || styleSeed.isBlank()) styleSeed = "lorelei|Felix";
        String url = buildUrl(styleSeed, 120);
        return new Image(url, 110, 110, true, true, true);
    }

    // Avatar petit (grille de sélection)
    public static Image loadAvatarSmall(String styleSeed) {
        if (styleSeed == null || styleSeed.isBlank()) styleSeed = "lorelei|Felix";
        String url = buildUrl(styleSeed, 80);
        return new Image(url, 60, 60, true, true, true);
    }

    // Label affiché sous l'avatar sélectionné
    public static String getDisplayName(String styleSeed) {
        if (styleSeed == null || !styleSeed.contains("|")) return styleSeed;
        String[] parts = styleSeed.split("\\|", 2);
        String style = parts[0];
        String seed  = parts[1];
        String styleLabel = switch (style) {
            case "lorelei"    -> "🎨 Lorelei";
            case "notionists" -> "🖼 Notionists";
            case "personas"   -> "👤 Personas";
            case "big-smile"  -> "😊 Big Smile";
            default           -> style;
        };
        return styleLabel + " — " + seed;
    }
}