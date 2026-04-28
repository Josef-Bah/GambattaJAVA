package gambatta.tn.services.reclamation;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import io.github.cdimascio.dotenv.Dotenv;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.AreaBreakType;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.renderer.DivRenderer;
import com.itextpdf.layout.renderer.DrawContext;
import gambatta.tn.entites.reclamation.reclamation;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class PdfService {

    private static final Color BG_APP = new DeviceRgb(2, 6, 23);       // #020617
    private static final Color BG_SURFACE = new DeviceRgb(15, 23, 42); // #0f172a
    private static final Color CYAN_NEON = new DeviceRgb(56, 189, 248);  // #38bdf8
    private static final Color GOLD_NEON = new DeviceRgb(251, 191, 36);  // #fbbf24
    private static final Color RED_NEON = new DeviceRgb(239, 68, 68);    // #ef4444
    private static final Color TEXT_WHITE = new DeviceRgb(248, 250, 252); // #f8fafc
    private static final Color TEXT_GRAY = new DeviceRgb(100, 116, 139);

    // ==========================================
    // SÉCURITÉ : CHARGEMENT DE LA CLÉ CLOUDINARY
    // ==========================================
    private static String recupererCloudinaryUrl() {
        Dotenv envStandard = Dotenv.configure().ignoreIfMissing().load();
        String url = envStandard.get("CLOUDINARY_URL");
        if (url != null && !url.isEmpty()) return url;

        Dotenv envMaven = Dotenv.configure().directory("../").ignoreIfMissing().load();
        return envMaven.get("CLOUDINARY_URL");
    }

    private static final String CLOUD_URL = recupererCloudinaryUrl();

    // =========================================================================
    // 1. PDF INDIVIDUEL (Utilisé pour un seul ticket)
    // =========================================================================
    public static String genererPdfLocal(reclamation r) {
        String outputDir = System.getProperty("user.home") + File.separator + "Downloads";
        String fileName = outputDir + File.separator + "Gambatta_Dossier_" + r.getIdrec() + ".pdf";
        return construireDocumentIntegral(List.of(r), fileName, "DOSSIER D'INSPECTION UNIQUE");
    }

    // =========================================================================
    // 2. PDF GLOBAL (Toutes les réclamations avec TOUS les détails)
    // =========================================================================
    public static String genererRapportComplet(List<reclamation> listeReclamations) {
        String outputDir = System.getProperty("user.home") + File.separator + "Downloads";
        String fileName = outputDir + File.separator + "Gambatta_ARCHIVE_TOTALE_" + System.currentTimeMillis() + ".pdf";
        return construireDocumentIntegral(listeReclamations, fileName, "ARCHIVE SYSTÈME TOTALE");
    }

    /**
     * CŒUR DU GÉNÉRATEUR : Construit le design pour une liste de réclamations
     */
    private static String construireDocumentIntegral(List<reclamation> liste, String fileName, String mainTitle) {
        try {
            // 1. CRÉATION DU PDF EN LOCAL
            PdfWriter writer = new PdfWriter(fileName);
            PdfDocument pdf = new PdfDocument(writer);
            pdf.addEventHandler(PdfDocumentEvent.INSERT_PAGE, new BackgroundEventHandler());

            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(40, 50, 40, 50);

            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fontMono = PdfFontFactory.createFont(StandardFonts.COURIER);

            for (int i = 0; i < liste.size(); i++) {
                reclamation r = liste.get(i);

                // --- EN-TÊTE DE PAGE ---
                document.add(new Paragraph("GAMBATTA_PROTOCOL // " + mainTitle).setFont(fontMono).setFontSize(9).setFontColor(CYAN_NEON));
                document.add(new Paragraph(r.getTitre() != null ? r.getTitre().toUpperCase() : "SANS TITRE")
                        .setFont(fontBold).setFontSize(22).setFontColor(TEXT_WHITE).setMarginBottom(5));
                document.add(new Paragraph("RÉFÉRENCE_LOG : 0x" + String.format("%06X", r.getIdrec()))
                        .setFont(fontMono).setFontSize(12).setFontColor(GOLD_NEON).setMarginBottom(25));

                // --- CARTE INFO (Bords arrondis) ---
                Div infoCard = new Div().setPadding(15).setMarginBottom(20);
                infoCard.setNextRenderer(new RoundedDivRenderer(infoCard, BG_SURFACE, CYAN_NEON, 12));

                Table grid = new Table(2).useAllAvailableWidth();
                String dateStr = r.getDaterec() != null ? r.getDaterec().toString().replace("T", " @ ") : "--";
                grid.addCell(createInvisibleCell("DATE_EXTRACTION", dateStr, fontMono, fontBold, TEXT_WHITE));
                grid.addCell(createInvisibleCell("MODULE_ORIGINE", r.getCategorierec() != null ? r.getCategorierec().toUpperCase() : "NC", fontMono, fontBold, TEXT_WHITE));

                Color statusColor = "RÉSOLU".equalsIgnoreCase(r.getStatutrec()) ? new DeviceRgb(16, 185, 129) : GOLD_NEON;
                grid.addCell(createInvisibleCell("STATUT_PROTOCOLE", r.getStatutrec() != null ? r.getStatutrec().toUpperCase() : "WAITING", fontMono, fontBold, statusColor));
                grid.addCell(createInvisibleCell("MENACE_LEVEL", r.isUrgent() ? "CRITIQUE" : "NOMINAL", fontMono, fontBold, r.isUrgent() ? RED_NEON : CYAN_NEON));

                infoCard.add(grid);
                document.add(infoCard);

                // --- CARTE PAYLOAD (Description) ---
                Div payloadCard = new Div().setPadding(20).setMarginBottom(20);
                payloadCard.setNextRenderer(new RoundedDivRenderer(payloadCard, BG_SURFACE, new DeviceRgb(51, 65, 85), 12));

                payloadCard.add(new Paragraph("> DÉTAILS DU LOG (INTEGRAL_DATA) :")
                        .setFont(fontMono).setFontSize(10).setFontColor(CYAN_NEON).setMarginBottom(10));

                payloadCard.add(new Paragraph(r.getDescrirec() != null ? r.getDescrirec() : "Empty payload.")
                        .setFont(fontBold).setFontSize(11).setFontColor(TEXT_WHITE).setMultipliedLeading(1.5f));

                document.add(payloadCard);

                // --- SÉPARATEUR OU SAUT DE PAGE ---
                if (i < liste.size() - 1) {
                    document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                } else {
                    document.add(new Paragraph("\n[ FIN DE L'ARCHIVE ]")
                            .setFont(fontMono).setFontSize(10).setFontColor(TEXT_GRAY).setTextAlignment(TextAlignment.CENTER));
                }
            }

            document.close();

            // ==========================================
            // 2. UPLOAD SUR CLOUDINARY
            // ==========================================
            if (CLOUD_URL == null || CLOUD_URL.isEmpty()) {
                System.err.println("🔴 ATTENTION : Pas de clé Cloudinary trouvée. Le PDF reste en local : " + fileName);
                return fileName; // On retourne le chemin local si le cloud n'est pas configuré
            }

            System.out.println("☁️ Upload du PDF sur Cloudinary en cours...");
            Cloudinary cloudinary = new Cloudinary(CLOUD_URL);
            File fileToUpload = new File(fileName);

            Map uploadResult = cloudinary.uploader().upload(fileToUpload, ObjectUtils.asMap(
                    "resource_type", "image", // Astuce : "image" permet à Cloudinary de générer un aperçu PDF si besoin
                    "folder", "gambatta_rapports"
            ));

            String urlCloudinary = (String) uploadResult.get("secure_url");
            System.out.println("✅ Upload réussi ! Lien : " + urlCloudinary);

            // 3. NETTOYAGE : SUPPRESSION DU PDF LOCAL POUR GAGNER DE LA PLACE
            try {
                Files.deleteIfExists(Paths.get(fileName));
            } catch (Exception ex) {
                System.err.println("Impossible de supprimer le fichier temporaire : " + ex.getMessage());
            }

            return urlCloudinary;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Cell createInvisibleCell(String label, String value, PdfFont fontLabel, PdfFont fontVal, Color valColor) {
        return new Cell().setBorder(Border.NO_BORDER).setPadding(5)
                .add(new Paragraph(label).setFont(fontLabel).setFontSize(8).setFontColor(TEXT_GRAY).setMarginBottom(0))
                .add(new Paragraph(value).setFont(fontVal).setFontSize(12).setFontColor(valColor));
    }

    private static class BackgroundEventHandler implements IEventHandler {
        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfPage page = docEvent.getPage();
            PdfCanvas canvas = new PdfCanvas(page);
            Rectangle pageSize = page.getPageSize();
            canvas.setFillColor(BG_APP);
            canvas.rectangle(pageSize.getLeft(), pageSize.getBottom(), pageSize.getWidth(), pageSize.getHeight());
            canvas.fill();
        }
    }

    private static class RoundedDivRenderer extends DivRenderer {
        private Color bgColor; private Color borderColor; private float borderRadius;
        public RoundedDivRenderer(Div modelElement, Color bgColor, Color borderColor, float borderRadius) {
            super(modelElement); this.bgColor = bgColor; this.borderColor = borderColor; this.borderRadius = borderRadius;
        }
        @Override
        public void drawBackground(DrawContext drawContext) {
            PdfCanvas canvas = drawContext.getCanvas();
            Rectangle rect = getOccupiedAreaBBox();
            canvas.saveState()
                    .roundRectangle(rect.getX(), rect.getBottom(), rect.getWidth(), rect.getHeight(), borderRadius)
                    .setFillColor(bgColor).fill()
                    .setStrokeColor(borderColor).setLineWidth(1.2f).stroke()
                    .restoreState();
        }
    }
}