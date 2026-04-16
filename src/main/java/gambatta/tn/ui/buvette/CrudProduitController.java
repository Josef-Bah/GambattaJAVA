package gambatta.tn.ui.buvette;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import gambatta.tn.entites.buvette.produit;
import gambatta.tn.services.buvette.ProduitService;
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

public class CrudProduitController {

    @FXML private Label statTotal;
    @FXML private Label statStock;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;

    @FXML private TableView<produit> produitTable;
    @FXML private TableColumn<produit, Integer> colId;
    @FXML private TableColumn<produit, String>  colNom;
    @FXML private TableColumn<produit, String>  colDesc;
    @FXML private TableColumn<produit, Double>  colPrix;
    @FXML private TableColumn<produit, Integer> colStock;
    @FXML private TableColumn<produit, Integer> colRef;

    @FXML private TextField fieldNom;
    @FXML private TextField fieldDesc;
    @FXML private TextField fieldPrix;
    @FXML private TextField fieldStock;
    @FXML private TextField fieldRef;

    @FXML private Label statusLabel;

    private final ProduitService ps = new ProduitService();
    private ObservableList<produit> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomp"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("descrip"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prixp"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockp"));
        colRef.setCellValueFactory(new PropertyValueFactory<>("referencep"));

        // form fill
        produitTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                fieldNom.setText(selected.getNomp());
                fieldDesc.setText(selected.getDescrip());
                fieldPrix.setText(String.valueOf(selected.getPrixp()));
                fieldStock.setText(String.valueOf(selected.getStockp()));
                fieldRef.setText(String.valueOf(selected.getReferencep()));
            }
        });

        // Initialize sort combo
        sortCombo.getItems().addAll("Aucun", "Nom (A-Z)", "Nom (Z-A)", "Prix (Croissant)", "Prix (Décroissant)");
        sortCombo.getSelectionModel().selectFirst();
        sortCombo.setOnAction(e -> applyFiltersAndSort());

        // Initialize search
        searchField.textProperty().addListener((obs, oldV, newV) -> applyFiltersAndSort());

        loadTable();
    }

    private void loadTable() {
        masterData.setAll(ps.getAll());
        applyFiltersAndSort();
        updateStats();
    }

    private void applyFiltersAndSort() {
        String keyword = searchField.getText().toLowerCase();
        
        List<produit> filteredList = masterData.stream()
                .filter(p -> p.getNomp().toLowerCase().contains(keyword) || p.getDescrip().toLowerCase().contains(keyword))
                .collect(Collectors.toList());

        String sortType = sortCombo.getValue();
        if (sortType != null) {
            switch (sortType) {
                case "Nom (A-Z)": filteredList.sort(Comparator.comparing(produit::getNomp)); break;
                case "Nom (Z-A)": filteredList.sort(Comparator.comparing(produit::getNomp).reversed()); break;
                case "Prix (Croissant)": filteredList.sort(Comparator.comparingDouble(produit::getPrixp)); break;
                case "Prix (Décroissant)": filteredList.sort(Comparator.comparingDouble(produit::getPrixp).reversed()); break;
            }
        }
        produitTable.getItems().setAll(filteredList);
    }

    private void updateStats() {
        int total = masterData.size();
        int totalStock = masterData.stream().mapToInt(produit::getStockp).sum();
        statTotal.setText(String.valueOf(total));
        statStock.setText(String.valueOf(totalStock));
    }

    @FXML
    public void exportPDF() {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("Produits_Report.pdf"));
            document.open();
            document.add(new Paragraph("Rapport des Produits\n\n"));
            
            PdfPTable pdfTable = new PdfPTable(6);
            pdfTable.addCell("ID");
            pdfTable.addCell("Nom");
            pdfTable.addCell("Description");
            pdfTable.addCell("Prix");
            pdfTable.addCell("Stock");
            pdfTable.addCell("Référence");

            for (produit p : produitTable.getItems()) {
                pdfTable.addCell(String.valueOf(p.getId()));
                pdfTable.addCell(p.getNomp());
                pdfTable.addCell(p.getDescrip());
                pdfTable.addCell(String.valueOf(p.getPrixp()));
                pdfTable.addCell(String.valueOf(p.getStockp()));
                pdfTable.addCell(String.valueOf(p.getReferencep()));
            }

            document.add(pdfTable);
            document.close();
            statusLabel.setText("✅ Exporté en Produits_Report.pdf");
        } catch (Exception e) {
            statusLabel.setText("❌ Erreur PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void addProduit() {
        try {
            produit p = buildFromForm();
            ps.add(p);
            statusLabel.setText("✅ Product added!");
            clearForm();
            loadTable();
        } catch (Exception e) {
            statusLabel.setText("❌ Error: " + e.getMessage());
        }
    }

    @FXML
    public void updateProduit() {
        produit selected = produitTable.getSelectionModel().getSelectedItem();
        if (selected == null) { statusLabel.setText("⚠️ Select a product first."); return; }
        try {
            produit p = buildFromForm();
            p.setId(selected.getId());
            ps.update(p);
            statusLabel.setText("✅ Product updated!");
            clearForm();
            loadTable();
        } catch (Exception e) {
            statusLabel.setText("❌ Error: " + e.getMessage());
        }
    }

    @FXML
    public void deleteProduit() {
        produit selected = produitTable.getSelectionModel().getSelectedItem();
        if (selected == null) { statusLabel.setText("⚠️ Select a product first."); return; }
        ps.delete(selected.getId());
        statusLabel.setText("✅ Product deleted!");
        clearForm();
        loadTable();
    }

    @FXML
    public void refresh() {
        searchField.clear();
        sortCombo.getSelectionModel().selectFirst();
        loadTable();
        statusLabel.setText(" Refreshed.");
    }

    private produit buildFromForm() {
        return new produit(
            fieldNom.getText(),
            fieldDesc.getText(),
            Double.parseDouble(fieldPrix.getText()),
            Integer.parseInt(fieldStock.getText()),
            LocalDateTime.now(),
            "",
            Integer.parseInt(fieldRef.getText())
        );
    }

    private void clearForm() {
        fieldNom.clear(); fieldDesc.clear(); fieldPrix.clear();
        fieldStock.clear(); fieldRef.clear();
    }
}
