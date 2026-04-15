package gambatta.tn.ui.buvette;

import gambatta.tn.entites.buvette.produit;
import gambatta.tn.services.buvette.ProduitService;
import gambatta.tn.services.buvette.VenteService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;

public class buvController {

    @FXML private ListView<String> productList;
    @FXML private ListView<String> cartList;
    @FXML private TextField searchField;
    @FXML private Label totalLabel;

    private final ProduitService ps = new ProduitService();
    private final VenteService vs = new VenteService();

    private List<produit> produits = new ArrayList<>();
    private List<produit> cart = new ArrayList<>();

    @FXML
    public void initialize() {
        produits = ps.getAll();
        refreshProducts(produits);
    }

    private void refreshProducts(List<produit> list) {
        productList.getItems().clear();
        for (produit p : list) {
            productList.getItems().add(
                    p.getId() + " | " + p.getNomp() + " - " + p.getPrixp() + " DT"
            );
        }
    }

    @FXML
    public void search() {
        refreshProducts(ps.search(searchField.getText()));
    }

    @FXML
    public void addToCart() {
        int index = productList.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            produit p = produits.get(index);
            cart.add(p);
            cartList.getItems().add(p.getNomp());
            updateTotal();
        }
    }

    private void updateTotal() {
        double total = cart.stream().mapToDouble(produit::getPrixp).sum();
        totalLabel.setText("Total: " + total + " DT");
    }

    @FXML
    public void checkout() {
        if (cart.isEmpty()) return;

        vs.createVente(cart);

        cart.clear();
        cartList.getItems().clear();
        updateTotal();
    }
}