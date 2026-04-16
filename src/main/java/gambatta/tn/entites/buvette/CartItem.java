package gambatta.tn.entites.buvette;

public class CartItem {
    private produit product;
    private int quantity;

    public CartItem(produit product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public produit getProduct() { return product; }
    public void setProduct(produit product) { this.product = product; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public void increment() { this.quantity++; }
    public void decrement() { if (this.quantity > 0) this.quantity--; }
}
