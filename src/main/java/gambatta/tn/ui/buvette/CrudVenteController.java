package gambatta.tn.ui.buvette;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Element;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.Font;
import gambatta.tn.entites.buvette.vente;
import gambatta.tn.services.buvette.VenteService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import gambatta.tn.services.buvette.CloudinaryService;
import java.awt.Desktop;
import java.net.URI;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CrudVenteController {

    @FXML private Label statTotal;
    @FXML private Label statRevenue;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;

    @FXML private TableView<vente> venteTable;
    @FXML private TableColumn<vente, Integer> colId;
    @FXML private TableColumn<vente, Integer> colQuantv;
    @FXML private TableColumn<vente, LocalDateTime> colDatev;
    @FXML private TableColumn<vente, Double>  colMontantv;
    @FXML private TableColumn<vente, String>  colUserName;

    @FXML private Label statusLabel;

    private final VenteService vs = new VenteService();
    private ObservableList<vente> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colQuantv.setCellValueFactory(new PropertyValueFactory<>("quantv"));
        colDatev.setCellValueFactory(new PropertyValueFactory<>("datev"));
        colMontantv.setCellValueFactory(new PropertyValueFactory<>("montantv"));
        colUserName.setCellValueFactory(new PropertyValueFactory<>("userName"));

        sortCombo.getItems().addAll("Aucun", "Date (Récent)", "Date (Ancien)", "Montant (Croissant)", "Montant (Décroissant)");
        sortCombo.getSelectionModel().selectFirst();
        sortCombo.setOnAction(e -> applyFiltersAndSort());

        searchField.textProperty().addListener((obs, oldV, newV) -> applyFiltersAndSort());

        loadTable();
    }

    private void loadTable() {
        masterData.setAll(vs.getAll());
        applyFiltersAndSort();
        updateStats();
    }

    private void applyFiltersAndSort() {
        String keyword = searchField.getText().toLowerCase();
        
        List<vente> filteredList = masterData.stream()
                .filter(v -> String.valueOf(v.getMontantv()).contains(keyword) || 
                             (v.getDatev() != null && v.getDatev().toString().contains(keyword)))
                .collect(Collectors.toList());

        String sortType = sortCombo.getValue();
        if (sortType != null) {
            switch (sortType) {
                case "Date (Récent)": filteredList.sort(Comparator.comparing(vente::getDatev).reversed()); break;
                case "Date (Ancien)": filteredList.sort(Comparator.comparing(vente::getDatev)); break;
                case "Montant (Croissant)": filteredList.sort(Comparator.comparingDouble(vente::getMontantv)); break;
                case "Montant (Décroissant)": filteredList.sort(Comparator.comparingDouble(vente::getMontantv).reversed()); break;
            }
        }
        venteTable.getItems().setAll(filteredList);
    }

    private void updateStats() {
        int total = masterData.size();
        double revenue = masterData.stream().mapToDouble(vente::getMontantv).sum();
        statTotal.setText(String.valueOf(total));
        statRevenue.setText(String.format("%.2f DT", revenue));
    }

    @FXML
    public void exportPDF() {
        String fileName = "Rapport_Ventes_Gambatta.pdf";
        try {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // HEADER
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, new BaseColor(15, 23, 42));
            Paragraph title = new Paragraph("GAMBATTA BUVETTE\n", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.GRAY);
            Paragraph subtitle = new Paragraph("Rapport Officiel des Ventes\n Généré le " + LocalDateTime.now() + "\n\n", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);

            document.add(new Chunk(new com.itextpdf.text.pdf.draw.LineSeparator(2f, 100, new BaseColor(0, 122, 255), Element.ALIGN_CENTER, -2)));
            document.add(new Paragraph("\n"));

            // TABLE
            PdfPTable pdfTable = new PdfPTable(4); 
            pdfTable.setWidthPercentage(100);

            // Define Table Header Styling
            Font headFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
            BaseColor headerBg = new BaseColor(15, 23, 42);

            String[] headers = {"ID", "Quantité", "Date", "Montant (DT)"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headFont));
                cell.setBackgroundColor(headerBg);
                cell.setPadding(10);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfTable.addCell(cell);
            }

            // Define Table Content Styling
            com.itextpdf.text.Font contentFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.NORMAL);
            for (vente v : venteTable.getItems()) {
                pdfTable.addCell(createStyledCell(String.valueOf(v.getId()), contentFont));
                pdfTable.addCell(createStyledCell(String.valueOf(v.getQuantv()), contentFont));
                pdfTable.addCell(createStyledCell(v.getDatev() != null ? v.getDatev().toString() : "", contentFont));
                pdfTable.addCell(createStyledCell(String.valueOf(v.getMontantv()), contentFont));
            }

            document.add(pdfTable);
            document.close();

            // UPLOAD TO CLOUDINARY & OPEN
            statusLabel.setText("⏳ Finalisation du rapport...");
            String url = CloudinaryService.uploadFile(new java.io.File(fileName), "image");
            
            if (url != null) {
                statusLabel.setText("✅ Rapport en ligne !");
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI(url));
                }
            } else {
                statusLabel.setText("⚠️ PDF local créé, mais échec de l'upload.");
            }

        } catch (Exception e) {
            statusLabel.setText("❌ Erreur PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private PdfPCell createStyledCell(String text, com.itextpdf.text.Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    @FXML
    public void deleteVente() {
        vente selected = venteTable.getSelectionModel().getSelectedItem();
        if (selected == null) { statusLabel.setText("⚠️ Select a sale first."); return; }
        vs.delete(selected.getId());
        statusLabel.setText("✅ Sale deleted!");
        loadTable();
    }

    @FXML
    public void refresh() {
        searchField.clear();
        sortCombo.getSelectionModel().selectFirst();
        loadTable();
        statusLabel.setText(" Refreshed.");
    }

    private BuvetteMainController mainController;

    public void setMainController(BuvetteMainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void showStats() {
        if (mainController != null) {
            mainController.showVenteStats();
        }
    }
}
