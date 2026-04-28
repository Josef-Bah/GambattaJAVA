package gambatta.tn.services.tournoi;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Service Intelligent Local - Analyse vos données réelles (MySQL)
 */
public class GeminiService {

    // On ne crée plus les services ici de manière statique pour éviter de bloquer l'app si le SQL est éteint
    
    public static CompletableFuture<String> getCompletion(String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            String input = prompt.toLowerCase();
            
            // On crée les services uniquement au moment de l'analyse
            TournoiService tourService = new TournoiService();
            EquipeService equipeService = new EquipeService();
            InscritournoiService inscrService = new InscritournoiService();
            
            // Analyse Dynamique de la Base de Données
            if (input.contains("analyse") || input.contains("donnée") || input.contains("stat")) {
                return generateRealDatabaseAnalysis(tourService, equipeService, inscrService);
            }

            // Logique Chatbot (Réponses basées sur les données)
            if (input.contains("bonjour") || input.contains("salut")) {
                return "Bonjour ! Je suis l'assistant de Gambatta. Je vois que votre projet contient actuellement " + 
                       tourService.findAll().size() + " tournois. Comment puis-je vous aider ?";
            } else if (input.contains("tournoi")) {
                long enAttente = tourService.findAll().stream().filter(t -> "EN_ATTENTE".equals(t.getStatutt())).count();
                return "Vous avez " + tourService.findAll().size() + " tournois au total, dont " + enAttente + " en attente de validation.";
            } else if (input.contains("équipe") || input.contains("equipe")) {
                return "Il y a " + equipeService.findAll().size() + " équipes enregistrées dans Gambatta.";
            } else if (input.contains("inscript")) {
                return "Le système enregistre " + inscrService.findAll().size() + " inscriptions aux tournois actuellement.";
            }

            return "Je suis un assistant intelligent local. Je peux analyser vos données ou vous guider dans l'app !";
        });
    }

    private static String generateRealDatabaseAnalysis(TournoiService tourService, EquipeService equipeService, InscritournoiService inscrService) {
        int nbT = tourService.findAll().size();
        int nbE = equipeService.findAll().size();
        int nbI = inscrService.findAll().size();
        
        long tValides = tourService.findAll().stream().filter(t -> "VALIDE".equals(t.getStatutt())).count();
        long eValides = equipeService.findAll().stream().filter(e -> "VALIDE".equals(e.getStatus())).count();

        StringBuilder sb = new StringBuilder();
        sb.append("📊 RAPPORT D'ANALYSE GAMBATTA (LIVE)\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("🏆 TOURNOIS : ").append(nbT).append(" (").append(tValides).append(" confirmés)\n");
        sb.append("👥 ÉQUIPES  : ").append(nbE).append(" (").append(eValides).append(" validées)\n");
        sb.append("📝 INSCRIPTIONS : ").append(nbI).append(" total\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        sb.append("💡 CONSEIL IA :\n");

        if (nbT > 0 && nbI / nbT >= 8) {
            sb.append("🚀 PERFORMANCE : Vos tournois sont très populaires ! En moyenne, ").append(nbI / nbT).append(" équipes s'inscrivent par tournoi.");
        } else if (nbT > 2 && nbI < 5) {
            sb.append("⚠️ ATTENTION : Vous avez beaucoup de tournois mais peu d'inscrits. Pensez à faire plus de promotion !");
        } else {
            sb.append("✅ ÉTAT : Votre base de données est saine et bien structurée.");
        }

        return sb.toString();
    }
}

