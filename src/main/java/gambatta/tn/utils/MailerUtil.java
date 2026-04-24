package gambatta.tn.utils;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class MailerUtil {

    private static final String USERNAME = "piskakh@gmail.com";
    private static final String PASSWORD = "hrbyvhcwyuokhooh";
    private static final String HOST = "smtp.gmail.com";
    private static final String PORT = "587";

    public static void sendConfirmationEmail(String toEmail, String activityName, String date, String time, String status) {
        if (toEmail == null || toEmail.isEmpty()) return;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            
            String subject = status.equals("ACCEPTEE") ? "✅ Réservation Confirmée - Gambatta" : "⏳ Réservation en attente - Gambatta";
            message.setSubject(subject);
            
            String content = "<h2 style='color:#FFD700;'>GAMBATTA ESPORTS</h2>"
                    + "<p>Bonjour,</p>"
                    + "<p>Votre demande de réservation pour l'activité <b>" + activityName + "</b> "
                    + "le <b>" + date + "</b> à <b>" + time + "</b> est actuellement : <b>" + status + "</b>.</p>";
                    
            if (status.equals("ACCEPTEE")) {
                content += "<p>Nous avons hâte de vous voir ! Préparez-vous à jouer !</p>";
            } else if (status.equals("EN_ATTENTE")) {
                content += "<p>Un administrateur va bientôt traiter votre demande.</p>";
            }

            content += "<br><p>L'équipe Gambatta</p>";

            message.setContent(content, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("📧 E-mail envoyé avec succès à " + toEmail);

        } catch (MessagingException e) {
            System.err.println("❌ Erreur d'envoi d'e-mail: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
