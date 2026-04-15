package gambatta.tn.tools;

import java.util.concurrent.CompletableFuture;

/**
 * Service Intelligent Local - Fonctionne sans clé API
 */
public class GeminiService {

    public static CompletableFuture<String> getCompletion(String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            String input = prompt.toLowerCase();
            
            // Logique de prédiction si le prompt contient des statistiques
            if (input.contains("{") || input.contains("analyse")) {
                return generateLocalStatsAnalysis(prompt);
            }

            // Logique Chatbot (Mots-clés)
            if (input.contains("bonjour") || input.contains("salut")) {
                return "Bonjour ! Je suis l'assistant local de Gambatta. Comment puis-je vous aider ?";
            } else if (input.contains("tournoi")) {
                return "Vous pouvez gérer les tournois dans l'onglet dédié. N'oubliez pas de valider les inscriptions !";
            } else if (input.contains("pdf") || input.contains("ticket")) {
                return "Pour générer un ticket, sélectionnez une inscription dans la liste et cliquez sur 'EXPORTER PDF'.";
            } else if (input.contains("logo")) {
                return "Le générateur de logo utilise votre nom d'équipe pour créer une identité unique instantanément.";
            } else if (input.contains("aide")) {
                return "Je peux vous aider pour : les prédictions, les tickets PDF, ou la gestion des équipes.";
            }

            return "Je suis un assistant intelligent local. Je peux analyser vos données ou vous guider dans l'app !";
        });
    }

    private static String generateLocalStatsAnalysis(String data) {
        // Simple analyse logique sans IA
        if (data.contains("ACCEPTED")) {
            return "ANALYSE LOCALE : Vos tournois sont attractifs ! Le taux d'acceptation montre une dynamique positive. " +
                   "Conseil : Prévoyez plus de buvettes si le nombre d'inscriptions acceptées dépasse 10.";
        }
        return "ANALYSE LOCALE : Les données sont stables. Pensez à relancer les inscriptions en attente pour dynamiser vos compétitions.";
    }
}

