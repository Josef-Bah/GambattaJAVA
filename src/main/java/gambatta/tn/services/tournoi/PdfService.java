package gambatta.tn.services.tournoi;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import gambatta.tn.entites.tournois.equipe;
import gambatta.tn.entites.tournois.inscriptiontournoi;
import gambatta.tn.entites.tournois.tournoi;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class PdfService {

    // Couleurs Gambatta
    private static final DeviceRgb NAVY = new DeviceRgb(0, 1, 51);
    private static final DeviceRgb GOLD = new DeviceRgb(197, 179, 88);

    /**
     * Génère un ticket PDF dans un fichier temporaire.
     */
    public File generateTicketFile(inscriptiontournoi inscription) throws IOException {
        File tempFile = File.createTempFile("Ticket_" + inscription.getId() + "_", ".pdf");
        tempFile.deleteOnExit();
        generateTicket(inscription, tempFile.getAbsolutePath());
        return tempFile;
    }

    /**
     * Génère un ticket PDF pour une inscription de tournoi vers un chemin précis.
     */
    public void generateTicket(inscriptiontournoi inscription, String destPath) throws FileNotFoundException {
        PdfWriter writer = new PdfWriter(destPath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        Table ticketTable = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
        ticketTable.setBorder(new SolidBorder(GOLD, 3));
        ticketTable.setPadding(20);

        Cell header = new Cell()
                .setBackgroundColor(NAVY)
                .add(new Paragraph("GAMBATTA E-SPORTS")
                        .setFontColor(GOLD)
                        .setBold()
                        .setFontSize(26)
                        .setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("TICKET OFFICIEL D'INSCRIPTION")
                        .setFontColor(DeviceRgb.WHITE)
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.CENTER))
                .setPadding(10);
        ticketTable.addCell(header);

        Cell body = new Cell().setPadding(30);
        body.add(new Paragraph("DÉTAILS DU TOURNOI").setBold().setFontColor(NAVY).setUnderline().setMarginBottom(10));
        body.add(new Paragraph("🏆 Tournoi : ").setBold().add(new Paragraph(inscription.getTournoi().getNomt())));
        body.add(new Paragraph("🛡️ Équipe : ").setBold().add(new Paragraph(inscription.getEquipe().getNom())));
        body.add(new Paragraph("📅 Date : ").setBold().add(new Paragraph(inscription.getDateInscrit() != null ? inscription.getDateInscrit().toString() : "-")));
        body.add(new Paragraph("🆔 ID : ").setBold().add(new Paragraph("#" + inscription.getId())));
        body.add(new Paragraph("📌 Statut : ").setBold().add(new Paragraph(inscription.getStatus()).setFontColor(GOLD)));

        ticketTable.addCell(body);

        Cell footer = new Cell()
                .add(new Paragraph("Veuillez présenter ce ticket le jour de l'événement.")
                        .setFontSize(9).setItalic().setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("© 2026 Gambatta Manager").setFontSize(8).setTextAlignment(TextAlignment.CENTER))
                .setBorderTop(new SolidBorder(NAVY, 1)).setPadding(10);
        ticketTable.addCell(footer);

        document.add(ticketTable);
        document.close();
    }

    /**
     * Génère une liste récapitulative dans un fichier temporaire.
     */
    public File generateTournamentListFile(List<tournoi> tournois) throws IOException {
        File tempFile = File.createTempFile("ListeTournois_", ".pdf");
        tempFile.deleteOnExit();
        generateTournamentList(tournois, tempFile.getAbsolutePath());
        return tempFile;
    }

    /**
     * Génère une liste récapitulative de tous les tournois.
     */
    public void generateTournamentList(List<tournoi> tournois, String destPath) throws FileNotFoundException {
        PdfWriter writer = new PdfWriter(destPath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("RAPPORT GLOBAL DES TOURNOIS")
                .setFontColor(NAVY).setBold().setFontSize(22).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("\n"));

        float[] columnWidths = {3, 4, 2, 1, 3};
        Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();

        table.addHeaderCell(new Cell().add(new Paragraph("Nom")).setBackgroundColor(NAVY).setFontColor(DeviceRgb.WHITE).setBold());
        table.addHeaderCell(new Cell().add(new Paragraph("Période")).setBackgroundColor(NAVY).setFontColor(DeviceRgb.WHITE).setBold());
        table.addHeaderCell(new Cell().add(new Paragraph("Statut")).setBackgroundColor(NAVY).setFontColor(DeviceRgb.WHITE).setBold());
        table.addHeaderCell(new Cell().add(new Paragraph("ID")).setBackgroundColor(NAVY).setFontColor(DeviceRgb.WHITE).setBold());
        table.addHeaderCell(new Cell().add(new Paragraph("Description")).setBackgroundColor(NAVY).setFontColor(DeviceRgb.WHITE).setBold());

        for (tournoi t : tournois) {
            table.addCell(new Cell().add(new Paragraph(t.getNomt())));
            table.addCell(new Cell().add(new Paragraph(t.getDatedebutt().toLocalDate() + " au " + t.getDatefint().toLocalDate())));
            table.addCell(new Cell().add(new Paragraph(t.getStatutt()).setFontColor(GOLD)));
            table.addCell(new Cell().add(new Paragraph("#" + t.getId())));
            table.addCell(new Cell().add(new Paragraph(t.getDescrit() != null ? t.getDescrit() : "-")));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Total de tournois : " + tournois.size()).setItalic().setFontColor(NAVY));

        document.close();
    }

    /**
     * Génère une liste d'équipes dans un fichier temporaire.
     */
    public File generateEquipeListFile(List<equipe> equipes) throws IOException {
        File tempFile = File.createTempFile("ListeEquipes_", ".pdf");
        tempFile.deleteOnExit();
        generateEquipeList(equipes, tempFile.getAbsolutePath());
        return tempFile;
    }

    /**
     * Génère une liste récapitulative de toutes les équipes.
     */
    public void generateEquipeList(List<equipe> equipes, String destPath) throws FileNotFoundException {
        PdfWriter writer = new PdfWriter(destPath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("LISTE OFFICIELLE DES ÉQUIPES")
                .setFontColor(NAVY).setBold().setFontSize(22).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("\n"));

        float[] columnWidths = {3, 3, 2, 1, 2};
        Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();

        table.addHeaderCell(new Cell().add(new Paragraph("Nom")).setBackgroundColor(NAVY).setFontColor(DeviceRgb.WHITE).setBold());
        table.addHeaderCell(new Cell().add(new Paragraph("Capitaine")).setBackgroundColor(NAVY).setFontColor(DeviceRgb.WHITE).setBold());
        table.addHeaderCell(new Cell().add(new Paragraph("Coach")).setBackgroundColor(NAVY).setFontColor(DeviceRgb.WHITE).setBold());
        table.addHeaderCell(new Cell().add(new Paragraph("ID")).setBackgroundColor(NAVY).setFontColor(DeviceRgb.WHITE).setBold());
        table.addHeaderCell(new Cell().add(new Paragraph("Statut")).setBackgroundColor(NAVY).setFontColor(DeviceRgb.WHITE).setBold());

        for (equipe e : equipes) {
            table.addCell(new Cell().add(new Paragraph(e.getNom())));
            table.addCell(new Cell().add(new Paragraph(e.getTeamLeader() != null ? e.getTeamLeader() : "-")));
            table.addCell(new Cell().add(new Paragraph(e.getCoach() != null ? e.getCoach() : "-")));
            table.addCell(new Cell().add(new Paragraph("#" + e.getId())));
            table.addCell(new Cell().add(new Paragraph(e.getStatus()).setFontColor(GOLD)));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Total d'équipes : " + equipes.size()).setItalic().setFontColor(NAVY));

        document.close();
    }
}
