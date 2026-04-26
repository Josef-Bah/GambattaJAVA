package gambatta.tn.services.reclamation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import gambatta.tn.entites.reclamation.reclamation;
import java.util.List;

public class AIService {

    // TA CLÉ GOOGLE AI STUDIO
    private static final String API_KEY = "AIzaSyCZuIcWto7rOvqJSvoDQrpYP_GSWRo1OXo";

    // Modèle actif : gemini-2.5-flash
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    private final HttpClient client;

    public AIService() {
        this.client = HttpClient.newHttpClient();
    }

    // ==========================================
    // MÉTHODE CENTRALE POUR GÉRER L'API ET LES QUOTAS
    // ==========================================
    private String executerRequeteGenerique(String prompt) {
        try {
            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject partsObj = new JSONObject();
            JSONArray partsArr = new JSONArray();
            JSONObject textObj = new JSONObject();

            textObj.put("text", prompt);
            partsArr.put(textObj);
            partsObj.put("parts", partsArr);
            contents.put(partsObj);
            requestBody.put("contents", contents);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return new JSONObject(response.body()).getJSONArray("candidates")
                        .getJSONObject(0).getJSONObject("content").getJSONArray("parts")
                        .getJSONObject(0).getString("text").trim();
            } else if (response.statusCode() == 429) {
                System.err.println("⚠️ ALERTE QUOTA IA : Vous avez dépassé les 15 requêtes/minute. Attendez 60 secondes.");
                return "QUOTA_ATTEINT";
            } else {
                System.err.println("❌ Erreur API Gemini - HTTP " + response.statusCode() + " : " + response.body());
                return "ERREUR_API";
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur de connexion réseau vers Google Gemini.");
            e.printStackTrace();
            return "ERREUR_RESEAU";
        }
    }

    // ==========================================
    // FONCTIONNALITÉS IA EXISTANTES
    // ==========================================

    public String optimiserTexte(String texteBrouillon) {
        if (texteBrouillon == null || texteBrouillon.isEmpty()) return texteBrouillon;
        String prompt = "Tu es un assistant professionnel pour une plateforme e-sport. Corrige les fautes, améliore le style et rends ce texte de réclamation très respectueux et clair. Ne réponds QUE par le texte corrigé, sans ajouter de commentaires d'introduction ou de conclusion : " + texteBrouillon;

        String resultat = executerRequeteGenerique(prompt);
        return (resultat.equals("QUOTA_ATTEINT") || resultat.startsWith("ERREUR")) ? texteBrouillon : resultat;
    }

    public String analyserSentiment(String texte) {
        if (texte == null || texte.trim().isEmpty()) return "NEUTRE";
        String prompt = "Analyse le sentiment de ce ticket de support e-sport : [" + texte + "].\n"
                + "RÈGLES STRICTES DE CLASSIFICATION :\n"
                + "- NEUTRE : Une simple question, une demande d'aide ou un signalement sans émotion forte. ATTENTION : La présence de politesse ('Bonjour', 'Merci', 'S'il vous plaît') ne rend pas le texte POSITIF, cela reste NEUTRE.\n"
                + "- CRITIQUE : Le client exprime de la colère, de la frustration, de l'urgence, se plaint d'un bug majeur ou d'un problème d'argent.\n"
                + "- POSITIF : Le client exprime de la joie, fait un compliment explicite sur le jeu, ou remercie l'équipe APRÈS la résolution d'un problème.\n"
                + "Réponds UNIQUEMENT par un seul mot parmi cette liste exacte : POSITIF, NEUTRE, CRITIQUE. Ne rajoute rien d'autre.";

        String resultat = executerRequeteGenerique(prompt).toUpperCase();
        if (resultat.contains("CRITIQUE")) return "CRITIQUE";
        if (resultat.contains("POSITIF")) return "POSITIF";
        return "NEUTRE";
    }

    public String determinerCategorie(String texte) {
        if (texte == null || texte.trim().isEmpty()) return "Autre Demande";
        String prompt = "Tu es le système de routage automatique pour la plateforme e-sport Gambatta.\n"
                + "Analyse cette réclamation : [" + texte + "].\n"
                + "Classe ce ticket dans UNE SEULE de ces catégories exactes (copie exactement le nom) :\n"
                + "1. 'Service Technique' | 2. 'Facturation & Paiement' | 3. 'Comportement Joueur' | 4. 'Gestion de Compte' | 5. 'Gestion des Activités' | 6. 'Tournois & Compétitions' | 7. 'Restauration & Nourriture' | 8. 'Gestion d'Équipe' | 9. 'Autre Demande'\n"
                + "Réponds UNIQUEMENT par le nom exact de la catégorie. Ne mets ni guillemets, ni explication.";

        String resultat = executerRequeteGenerique(prompt);
        return (resultat.equals("QUOTA_ATTEINT") || resultat.startsWith("ERREUR")) ? "Autre Demande" : resultat.replace("\"", "").replace("'", "");
    }

    public String detecterMensonge(String texte) {
        if (texte == null || texte.trim().isEmpty()) return "0% - Impossible d'analyser un texte vide.";
        String prompt = "Tu es un expert en analyse comportementale et détection de fraude pour le support client e-sport Gambatta.\n"
                + "Analyse cette réclamation de joueur : [" + texte + "].\n"
                + "Cherche des incohérences, des excuses classiques de joueurs, ou un ton excessivement dramatique pour forcer un remboursement.\n"
                + "Génère une réponse STRICTEMENT dans ce format (ne rajoute rien d'autre) :\n"
                + "[SCORE]%\n"
                + "MOTIF : [Ton explication courte en 1 phrase]";

        String resultat = executerRequeteGenerique(prompt);
        if (resultat.equals("QUOTA_ATTEINT")) return "⚠️ Quota dépassé. Attendez 1 minute.";
        if (resultat.startsWith("ERREUR")) return "❌ Erreur d'analyse IA.";
        return resultat;
    }

    public String suggererReponseIntelligente(String texteClient) {
        if (texteClient == null || texteClient.trim().isEmpty()) return "0% - Erreur|||Veuillez vérifier la description.";
        String prompt = "Tu es un agent de support e-sport expert en détection de fraude pour Gambatta.\n"
                + "Analyse cette réclamation : [" + texteClient + "].\n"
                + "1. Évalue la probabilité de mensonge ou d'excuse bidon (le score de fraude).\n"
                + "2. Rédige la réponse parfaite à envoyer au client. Si c'est une fraude (score > 60%), refuse poliment mais fermement la demande. Si c'est une vraie demande, sois aidant et compréhensif.\n"
                + "Tu DOIS répondre STRICTEMENT dans ce format exact avec le séparateur '|||' au milieu :\n"
                + "[SCORE]% - [Explication très courte du mensonge ou de la vérité]\n"
                + "|||\n"
                + "Bonjour,\n[Ta réponse au client]\n\nCordialement,\nL'équipe Support Gambatta";

        String resultat = executerRequeteGenerique(prompt);
        if (resultat.equals("QUOTA_ATTEINT")) return "⚠️ Surcharge API|||L'IA a trop travaillé cette minute. Faites une pause de 60 secondes pour recharger les quotas gratuits.";
        if (resultat.startsWith("ERREUR")) return "❌ Erreur IA|||Une erreur s'est produite lors de la connexion à Gemini.";
        return resultat;
    }

    public String detecterDoublon(String descriptionActuelle, List<reclamation> historiqueTickets, int currentTicketId) {
        if (historiqueTickets == null || historiqueTickets.isEmpty()) return "Aucun historique pour comparer.";
        StringBuilder contexte = new StringBuilder();
        int compte = 0;
        for (reclamation r : historiqueTickets) {
            if (r.getIdrec() != currentTicketId && compte < 50) {
                contexte.append("ID:[").append(r.getIdrec()).append("] | Desc: ").append(r.getDescrirec()).append("\n");
                compte++;
            }
        }

        String prompt = "Tu es un assistant technique. Voici un NOUVEAU TICKET :\n"
                + "DESCRIPTION : [" + descriptionActuelle + "]\n\n"
                + "Voici la base de données des tickets récents :\n"
                + contexte.toString() + "\n\n"
                + "Ce nouveau ticket parle-t-il EXACTEMENT du même problème qu'un ou plusieurs tickets de la base ? "
                + "Si OUI, tu DOIS répondre STRICTEMENT au format suivant en incluant le séparateur '|||' :\n"
                + "IDs:[ID1, ID2] ||| ⚠️ DOUBLON DÉTECTÉ : Semblable aux tickets précédents - [Courte explication]\n"
                + "Si NON, réponds EXACTEMENT : '✅ Aucun doublon détecté.'";

        String resultat = executerRequeteGenerique(prompt);
        if (resultat.equals("QUOTA_ATTEINT")) return "⚠️ Quota API dépassé. Veuillez patienter 1 min.";
        if (resultat.startsWith("ERREUR")) return "❌ Erreur lors de la recherche de doublons.";
        return resultat;
    }

    public String analyserImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return "Aucune URL d'image valide.";
        try {
            URL url = new URL(imageUrl);
            try (InputStream in = url.openStream()) {
                byte[] imageBytes = in.readAllBytes();
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);

                String prompt = "Analyse cette capture d'écran fournie par un joueur dans le cadre d'un ticket de support technique. "
                        + "Dis-moi précisément ce que tu vois : est-ce une preuve valide d'un bug, d'une erreur de paiement, ou une image non pertinente (ex: un paysage, un mème, un selfie) ? "
                        + "Sois très direct et concis (2 phrases maximum).";

                JSONObject requestBody = new JSONObject();
                JSONArray contents = new JSONArray();
                JSONObject partsObj = new JSONObject();
                JSONArray partsArr = new JSONArray();

                JSONObject textObj = new JSONObject();
                textObj.put("text", prompt);
                partsArr.put(textObj);

                JSONObject imageObj = new JSONObject();
                JSONObject inlineData = new JSONObject();
                inlineData.put("mimeType", "image/jpeg");
                inlineData.put("data", base64Image);
                imageObj.put("inlineData", inlineData);
                partsArr.put(imageObj);

                partsObj.put("parts", partsArr);
                contents.put(partsObj);
                requestBody.put("contents", contents);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return new JSONObject(response.body()).getJSONArray("candidates")
                            .getJSONObject(0).getJSONObject("content").getJSONArray("parts")
                            .getJSONObject(0).getString("text").trim();
                } else if (response.statusCode() == 429) {
                    System.err.println("⚠️ ALERTE QUOTA IA VISION : Attendez 60 secondes.");
                    return "⚠️ Limite d'API atteinte. Attendez 1 minute.";
                } else {
                    return "Erreur Serveur IA.";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Erreur lors de l'analyse visuelle. L'image est peut-être inaccessible.";
    }

    // --- LE COPILOT DE TRAITEMENT (TICKET INDIVIDUEL) ---
    public String executerCopilot(String commandeAdmin) {
        if (commandeAdmin == null || commandeAdmin.trim().isEmpty()) return "{}";
        String prompt = "Tu es le 'Copilot' d'une interface d'administration e-sport.\n"
                + "L'administrateur te donne cet ordre en langage naturel : [" + commandeAdmin + "]\n"
                + "Tu dois analyser cet ordre et me renvoyer STRICTEMENT un objet JSON valide, sans aucun texte autour, ni balises Markdown (pas de ```json).\n"
                + "Les règles du JSON :\n"
                + "- \"statut\" : Mettre 'En attente', 'En cours', 'Résolu', ou 'Fermé' (si demandé, sinon null).\n"
                + "- \"assignation\" : Mettre 'Support Technique', 'Service Financier', 'Modération', ou 'Non assigné' (si demandé, sinon null).\n"
                + "- \"message\" : Rédiger une réponse polie au joueur si l'admin demande de lui dire quelque chose (sinon null).\n"
                + "- \"auto_envoyer\" : Mettre le booléen true si l'ordre sous-entend qu'il faut envoyer/sauvegarder immédiatement, sinon false.\n"
                + "Exemple de sortie attendue : {\"statut\": \"Résolu\", \"assignation\": \"Service Financier\", \"message\": \"Bonjour, le remboursement est effectué.\", \"auto_envoyer\": true}";

        String resultat = executerRequeteGenerique(prompt);
        if (resultat != null && resultat.startsWith("```json")) {
            resultat = resultat.replace("```json", "").replace("```", "").trim();
        }
        return resultat;
    }

    // --- NOUVEAUTÉ : LE COPILOT GLOBAL (TABLEAU DE BORD) ---
    public String executerCopilotGlobal(String commandeAdmin) {
        if (commandeAdmin == null || commandeAdmin.trim().isEmpty()) return "{}";
        String prompt = "Tu es le 'Copilot Global' d'un tableau de bord de réclamations e-sport.\n"
                + "L'administrateur te donne cet ordre : [" + commandeAdmin + "]\n"
                + "Renvoie STRICTEMENT un objet JSON valide, sans balises Markdown.\n"
                + "Règles pour le JSON :\n"
                + "- \"action\" : Doit être l'une de ces valeurs : ['AUTO_TRIAGE' (pour assigner/ranger les tickets), 'EXPORT_PDF' (pour générer un rapport), 'FILTER' (pour filtrer/chercher), 'UNKNOWN'].\n"
                + "- \"filtre_statut\" : Si action=FILTER, extraire le statut demandé ('EN ATTENTE', 'RÉSOLU', 'TOUS'), sinon null.\n"
                + "- \"filtre_module\" : Si action=FILTER, extraire le module demandé (ex: 'Service Technique') ou 'TOUS LES MODULES', sinon null.\n"
                + "Exemple 1 : {\"action\": \"AUTO_TRIAGE\"}\n"
                + "Exemple 2 : {\"action\": \"EXPORT_PDF\"}\n"
                + "Exemple 3 : {\"action\": \"FILTER\", \"filtre_statut\": \"EN ATTENTE\", \"filtre_module\": \"Facturation & Paiement\"}";

        String resultat = executerRequeteGenerique(prompt);
        if (resultat != null && resultat.startsWith("```json")) {
            resultat = resultat.replace("```json", "").replace("```", "").trim();
        }
        return resultat;
    }
}