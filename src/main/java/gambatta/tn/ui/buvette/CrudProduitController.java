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
    @FXML private Label imageNameLabel;

    @FXML private Label statusLabel;
    
    private java.io.File selectedImageFile = null;

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
                selectedImageFile = null;
                String iName = selected.getImagep();
                imageNameLabel.setText((iName != null && !iName.isEmpty()) ? iName : "Aucune image sélectionnée");
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
    public void chooseImage() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Choisir l'image du produit");
        fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.webp")
        );
        java.io.File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedImageFile = file;
            imageNameLabel.setText(file.getName());
        }
    }

    private String processImageUpload() throws Exception {
        if (selectedImageFile == null) {
            return imageNameLabel.getText().equals("Aucune image sélectionnée") ? "" : imageNameLabel.getText();
        }
        String fileName = System.currentTimeMillis() + "_" + selectedImageFile.getName();
        java.nio.file.Path dest = java.nio.file.Paths.get("uploads/produits/", fileName);
        java.nio.file.Files.copy(selectedImageFile.toPath(), dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    @FXML
    public void addProduit() {
        try {
            String imgName = processImageUpload();
            produit p = buildFromForm(imgName);
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
            String imgName = processImageUpload();
            if (imgName.isEmpty()) imgName = selected.getImagep() != null ? selected.getImagep() : "";
            
            produit p = buildFromForm(imgName);
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

    private produit buildFromForm(String imageName) {
        return new produit(
            fieldNom.getText(),
            fieldDesc.getText(),
            Double.parseDouble(fieldPrix.getText()),
            Integer.parseInt(fieldStock.getText()),
            LocalDateTime.now(),
            imageName,
            Integer.parseInt(fieldRef.getText())
        );
    }

    private void clearForm() {
        fieldNom.clear(); fieldDesc.clear(); fieldPrix.clear();
        fieldStock.clear(); fieldRef.clear();
        selectedImageFile = null;
        imageNameLabel.setText("Aucune image sélectionnée");
    }
}
