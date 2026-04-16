package gambatta.tn.ui.buvette;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import gambatta.tn.entites.buvette.vente;
import gambatta.tn.services.buvette.VenteService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

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
    @FXML private TableColumn<vente, Integer> colUserId;

    @FXML private Label statusLabel;

    private final VenteService vs = new VenteService();
    private ObservableList<vente> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colQuantv.setCellValueFactory(new PropertyValueFactory<>("quantv"));
        colDatev.setCellValueFactory(new PropertyValueFactory<>("datev"));
        colMontantv.setCellValueFactory(new PropertyValueFactory<>("montantv"));
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));

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
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("Ventes_Report.pdf"));
            document.open();
            document.add(new Paragraph("Rapport des Ventes\n\n"));
            
            PdfPTable pdfTable = new PdfPTable(5);
            pdfTable.addCell("ID");
            pdfTable.addCell("Quantité");
            pdfTable.addCell("Date");
            pdfTable.addCell("Montant");
            pdfTable.addCell("User ID");

            for (vente v : venteTable.getItems()) {
                pdfTable.addCell(String.valueOf(v.getId()));
                pdfTable.addCell(String.valueOf(v.getQuantv()));
                pdfTable.addCell(v.getDatev() != null ? v.getDatev().toString() : "");
                pdfTable.addCell(String.valueOf(v.getMontantv()));
                pdfTable.addCell(String.valueOf(v.getUserId()));
            }

            document.add(pdfTable);
            document.close();
            statusLabel.setText("✅ Exporté en Ventes_Report.pdf");
        } catch (Exception e) {
            statusLabel.setText("❌ Erreur PDF: " + e.getMessage());
            e.printStackTrace();
        }
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
}
