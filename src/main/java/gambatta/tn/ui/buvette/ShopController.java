package gambatta.tn.ui.buvette;

import io.github.cdimascio.dotenv.Dotenv;

import gambatta.tn.entites.buvette.CartItem;
import gambatta.tn.entites.buvette.produit;
import gambatta.tn.services.buvette.ProduitService;
import gambatta.tn.services.buvette.VenteService;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javafx.application.Platform;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

public class ShopController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;
    @FXML private ComboBox<String> deviseCombo;
    @FXML private FlowPane productsFlowPane;
    
    @FXML private VBox cartSidebar;
    @FXML private Region dimOverlay;
    @FXML private Label cartBadge; 
    @FXML private Label emptyCartLabel;
    @FXML private VBox cartItemsContainer;
    @FXML private Label totalLabel;
    @FXML private Button btnToggleCart;
    
    @FXML private VBox aiSuggestionsContainer;
    @FXML private HBox aiSuggestionsHBox;

    // Nutri-Score Modal
    @FXML private VBox nutriModal;
    @FXML private Label nutriTitle;
    @FXML private Label nutriScoreLabel;
    @FXML private Label nutriCal;
    @FXML private Label nutriSugar;
    @FXML private Label nutriSalt;
    @FXML private Label nutriBrand;

    // Smart Receipt Modal
    @FXML private VBox receiptModal;
    @FXML private ImageView qrCodeImage;
    @FXML private Label receiptTotalLabel;

    // Using the NEW Router URL format as the standard one is returning 404
    private final String HF_API_URL = "https://router.huggingface.co/hf-inference/models/sentence-transformers/all-MiniLM-L6-v2";
    
    // Load from .env securely
    private static final Dotenv dotenv = Dotenv.load();
    private final String HF_TOKEN = dotenv.get("HF_TOKEN");

    private boolean isCartOpen = false;

    private ProduitService ps = new ProduitService();
    private VenteService vs = new VenteService();
    
    private List<produit> masterProducts = new ArrayList<>();
    private List<CartItem> cart = new ArrayList<>();
    
    private double currentRate = 1.0;
    private String currentCurrency = "TND";
    private JSONObject exchangeRates = null;

    private final String EXCHANGE_API_URL = "https://open.er-api.com/v6/latest/TND";

    @FXML
    public void initialize() {
        sortCombo.getItems().addAll("Pertinence", "Prix Croissant", "Prix Décroissant", "Nom (A-Z)");
        sortCombo.getSelectionModel().selectFirst();
        sortCombo.setOnAction(e -> applyFilters());
        
        deviseCombo.getItems().addAll("Devise: TND", "Devise: EUR", "Devise: USD", "Devise: GBP");
        deviseCombo.getSelectionModel().selectFirst();
        deviseCombo.setOnAction(e -> handleDeviseChange());
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        
        masterProducts = ps.getAll();
        renderProducts(masterProducts);
        
        // Fetch exchange rates asynchronously
        fetchExchangeRates();
    }

    private void fetchExchangeRates() {
        new Thread(() -> {
            try {
                URL url = new URL(EXCHANGE_API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                
                if (conn.getResponseCode() == 200) {
                    StringBuilder response = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = br.readLine()) != null) response.append(line.trim());
                    }
                    JSONObject result = new JSONObject(response.toString());
                    if ("success".equals(result.getString("result"))) {
                        this.exchangeRates = result.getJSONObject("rates");
                        System.out.println("Currency: Exchange rates updated from API.");
                    }
                }
            } catch (Exception e) {
                System.err.println("Currency API Error: " + e.getMessage());
            }
        }).start();
    }

    private void handleDeviseChange() {
        String val = deviseCombo.getValue();
        currentCurrency = "TND";
        currentRate = 1.0;

        if (exchangeRates != null) {
            if (val.contains("EUR")) { currentCurrency = "EUR"; currentRate = exchangeRates.optDouble("EUR", 0.30); }
            else if (val.contains("USD")) { currentCurrency = "USD"; currentRate = exchangeRates.optDouble("USD", 0.32); }
            else if (val.contains("GBP")) { currentCurrency = "GBP"; currentRate = exchangeRates.optDouble("GBP", 0.25); }
        } else {
            // Fallback to hardcoded if API failed
            if (val.contains("EUR")) { currentRate = 0.30; currentCurrency = "EUR"; }
            else if (val.contains("USD")) { currentRate = 0.32; currentCurrency = "USD"; }
            else if (val.contains("GBP")) { currentRate = 0.25; currentCurrency = "GBP"; }
        }
        
        applyFilters(); 
        renderCartUI();
        updateAISuggestions(); // Update suggestions too as they show prices
    }

    private void applyFilters() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        List<produit> filtered = masterProducts.stream()
            .filter(p -> {
                String n = p.getNomp() == null ? "" : p.getNomp().toLowerCase();
                String d = p.getDescrip() == null ? "" : p.getDescrip().toLowerCase();
                return n.contains(keyword) || d.contains(keyword);
            })
            .collect(Collectors.toList());
            
        String sort = sortCombo.getValue();
        if (sort != null) {
            if (sort.equals("Prix Croissant")) filtered.sort(Comparator.comparingDouble(produit::getPrixp));
            else if (sort.equals("Prix Décroissant")) filtered.sort(Comparator.comparingDouble(produit::getPrixp).reversed());
            else if (sort.equals("Nom (A-Z)")) {
                filtered.sort((p1, p2) -> {
                    String n1 = p1.getNomp() == null ? "" : p1.getNomp();
                    String n2 = p2.getNomp() == null ? "" : p2.getNomp();
                    return n1.compareToIgnoreCase(n2);
                });
            }
        }
        
        renderProducts(filtered);
    }

    private void renderProducts(List<produit> list) {
        productsFlowPane.getChildren().clear();
        for (produit p : list) {
            productsFlowPane.getChildren().add(createProductCard(p));
        }
    }

    private VBox createProductCard(produit p) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color:#1e293b; -fx-background-radius:15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 8); -fx-border-color: rgba(255,255,255,0.05); -fx-border-radius:15;");
        card.setPrefWidth(220);
        card.setAlignment(Pos.CENTER_LEFT);

        // Image display logic pointing strictly to our new local path
        StackPane imgWrapper = new StackPane();
        imgWrapper.setPrefSize(180, 150);
        imgWrapper.setStyle("-fx-background-color:#0f172a; -fx-background-radius:10;");
        
        ImageView imgView = new ImageView();
        imgView.setFitWidth(180);
        imgView.setFitHeight(150);
        imgView.setPreserveRatio(true);
        imgWrapper.getChildren().add(imgView);

        // Restoring robust image loading logic with multiple fallbacks
        String imgName = p.getImagep();
        if (imgName != null && !imgName.isEmpty()) {
            try {
                if (imgName.startsWith("http")) {
                    // Cloudinary URL
                    imgView.setImage(new javafx.scene.image.Image(imgName, true));
                } else {
                    // Legacy Local file
                    java.io.File localFile = new java.io.File("uploads/produits/" + imgName);
                    if (localFile.exists()) {
                        imgView.setImage(new javafx.scene.image.Image(localFile.toURI().toString(), true));
                    }
                }
            } catch (Exception ex) {
                // Fail gracefully
            }
        }

        String pName = p.getNomp() == null ? "Produit sans nom" : p.getNomp();
        Label name = new Label(pName);
        name.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        name.setStyle("-fx-text-fill:#f8fafc;");

        double convertedPrice = p.getPrixp() * currentRate;
        Label price = new Label(String.format("%.2f %s", convertedPrice, currentCurrency));
        price.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        price.setStyle("-fx-text-fill:#FFD700;");

        Label kcal = new Label("Ref: " + p.getReferencep()); 
        kcal.setStyle("-fx-background-color:#334155; -fx-text-fill:#94a3b8; -fx-font-size:10px; -fx-padding: 2 6; -fx-background-radius:10;");
        
        Button btnInfo = new Button("🍏");
        btnInfo.setStyle("-fx-background-color:#334155; -fx-text-fill:white; -fx-font-size:14px; -fx-background-radius:10; -fx-padding:10 15; -fx-cursor:hand;");
        btnInfo.setOnAction(e -> fetchNutriScore(p.getNomp()));

        Button btnAdd = new Button("🛒 Ajouter");
        btnAdd.setMaxWidth(Double.MAX_VALUE);
        btnAdd.setStyle("-fx-background-color:#FFD700; -fx-text-fill:#020617; -fx-font-weight:bold; -fx-background-radius:10; -fx-padding:10; -fx-cursor:hand;");
        btnAdd.setOnAction(e -> addToCart(p));
        HBox.setHgrow(btnAdd, Priority.ALWAYS);

        HBox actionsBox = new HBox(8, btnInfo, btnAdd);

        card.getChildren().addAll(imgWrapper, name, price, kcal, actionsBox);
        return card;
    }

    private void fetchNutriScore(String productName) {
        if (productName == null || productName.isEmpty()) return;

        // Setup loading state
        nutriTitle.setText("Analyse pour : " + productName + "...");
        nutriScoreLabel.setText("⏳");
        nutriScoreLabel.setStyle("-fx-background-color:#334155; -fx-text-fill:white; -fx-font-size:40px; -fx-padding:5 35; -fx-background-radius:25;");
        nutriCal.setText("Recherche...");
        nutriSugar.setText("Recherche...");
        nutriSalt.setText("Recherche...");
        nutriBrand.setText("Recherche...");
        
        showNutriModal();

        new Thread(() -> {
            try {
                // Ensure correct encoding for the API
                String encodedName = java.net.URLEncoder.encode(productName, "UTF-8");
                String urlStr = "https://world.openfoodfacts.org/cgi/search.pl?search_terms=" + encodedName + "&search_simple=1&action=process&json=1&page_size=1";
                
                java.net.URL url = new java.net.URL(urlStr);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "GambattaBuvette/1.0");

                java.io.BufferedReader reader = new java.io.BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                org.json.JSONObject jsonResponse = new org.json.JSONObject(response.toString());
                org.json.JSONArray products = jsonResponse.optJSONArray("products");

                Platform.runLater(() -> {
                    if (products != null && products.length() > 0) {
                        org.json.JSONObject prod = products.getJSONObject(0);
                        String grade = prod.optString("nutriscore_grade", "inconnu").toUpperCase();
                        String brand = prod.optString("brands", "Non spécifiée");
                        
                        org.json.JSONObject nutriments = prod.optJSONObject("nutriments");
                        String energy = "N/A", sugar = "N/A", salt = "N/A";
                        if (nutriments != null) {
                            energy = nutriments.optString("energy-kcal_100g", "N/A") + " kcal / 100g";
                            sugar = nutriments.optString("sugars_100g", "N/A") + " g / 100g";
                            salt = nutriments.optString("salt_100g", "N/A") + " g / 100g";
                        }

                        nutriTitle.setText(prod.optString("product_name", productName));
                        nutriScoreLabel.setText(grade.equals("INCONNU") ? "?" : grade);
                        
                        String color = "#94a3b8"; // Default
                        switch (grade) {
                            case "A": color = "#15803d"; break;
                            case "B": color = "#84cc16"; break;
                            case "C": color = "#eab308"; break;
                            case "D": color = "#f97316"; break;
                            case "E": color = "#ef4444"; break;
                        }
                        nutriScoreLabel.setStyle("-fx-background-color:" + color + "; -fx-text-fill:white; -fx-font-size:50px; -fx-font-weight:900; -fx-padding:5 35; -fx-background-radius:25;");
                        
                        nutriBrand.setText(brand);
                        nutriCal.setText(energy);
                        nutriSugar.setText(sugar);
                        nutriSalt.setText(salt);
                    } else {
                        nutriTitle.setText("Produit non trouvé :(");
                        nutriScoreLabel.setText("?");
                        nutriScoreLabel.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-size:50px; -fx-padding:5 35; -fx-background-radius:25;");
                        nutriBrand.setText("Données indisponibles");
                        nutriCal.setText("-");
                        nutriSugar.setText("-");
                        nutriSalt.setText("-");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    nutriTitle.setText("Erreur réseau");
                    nutriScoreLabel.setText("!");
                    nutriScoreLabel.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-size:50px; -fx-padding:5 35; -fx-background-radius:25;");
                });
            }
        }).start();
    }

    private void showNutriModal() {
        dimOverlay.setVisible(true);
        nutriModal.setVisible(true);
        nutriModal.setManaged(true);
        
        FadeTransition ft = new FadeTransition(Duration.millis(300), nutriModal);
        ft.setFromValue(0);
        ft.setToValue(1);
        
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), nutriModal);
        tt.setFromY(50);
        tt.setToY(0);
        
        ft.play();
        tt.play();
    }

    @FXML
    public void closeNutriModal() {
        FadeTransition ft = new FadeTransition(Duration.millis(200), nutriModal);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            nutriModal.setVisible(false);
            nutriModal.setManaged(false);
            if (!isCartOpen) dimOverlay.setVisible(false);
        });
        ft.play();
    }

    @FXML
    public void closeAllModals() {
        if (isCartOpen) toggleCart();
        if (nutriModal.isVisible()) closeNutriModal();
        if (receiptModal != null && receiptModal.isVisible()) closeReceiptModal();
    }

    private void addToCart(produit p) {
        CartItem existing = cart.stream().filter(i -> i.getProduct().getId() == p.getId()).findFirst().orElse(null);
        if (existing != null) {
            existing.increment();
        } else {
            cart.add(new CartItem(p, 1));
        }
        renderCartUI();
        updateAISuggestions();
        
        // Open cart to show effect
        if (!isCartOpen) toggleCart();
    }

    private void updateAISuggestions() {
        if (cart.isEmpty()) {
            aiSuggestionsContainer.setVisible(false);
            aiSuggestionsContainer.setManaged(false);
            return;
        }

        String cartContext = cart.stream()
                .map(i -> i.getProduct().getNomp())
                .collect(Collectors.joining(", "));

        List<String> candidates = masterProducts.stream()
                .filter(p -> cart.stream().noneMatch(ci -> ci.getProduct().getId() == p.getId()))
                .map(produit::getNomp)
                .distinct()
                .limit(10) // Limit to 10 candidates for performance
                .collect(Collectors.toList());

        if (candidates.isEmpty()) return;

        new Thread(() -> {
            try {
                JSONObject payload = new JSONObject();
                JSONObject inputs = new JSONObject();
                inputs.put("source_sentence", "snack bar items similar to: " + cartContext);
                inputs.put("sentences", new JSONArray(candidates));
                payload.put("inputs", inputs);

                String urlWithParams = HF_API_URL + "?wait_for_model=true";
                URL url = new URL(urlWithParams);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + HF_TOKEN);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setUseCaches(false);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int code = conn.getResponseCode();
                System.out.println("AI: API Response Code: " + code);

                if (code == 200) {
                    StringBuilder response = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                    }
                    
                    System.out.println("AI: Response body: " + response.toString());
                    
                    // Similarity model returns an array of scores directly
                    JSONArray scores = new JSONArray(response.toString());

                    Platform.runLater(() -> {
                        aiSuggestionsHBox.getChildren().clear();
                        
                        // Sort candidates by score
                        List<Integer> indices = new ArrayList<>();
                        for (int i = 0; i < scores.length(); i++) indices.add(i);
                        indices.sort((a, b) -> Double.compare(scores.getDouble(b), scores.getDouble(a)));

                        int suggestedCount = 0;
                        for (int i : indices) {
                            if (suggestedCount >= 4) break;
                            double score = scores.getDouble(i);
                            String suggestedName = candidates.get(i);
                            System.out.println("AI: Similarity Score: " + suggestedName + " = " + score);
                            
                            masterProducts.stream()
                                .filter(p -> p.getNomp().equalsIgnoreCase(suggestedName))
                                .findFirst()
                                .ifPresent(p -> {
                                    aiSuggestionsHBox.getChildren().add(createMiniProductCard(p));
                                });
                            suggestedCount++;
                        }
                        boolean hasSuggestions = !aiSuggestionsHBox.getChildren().isEmpty();
                        aiSuggestionsContainer.setVisible(hasSuggestions);
                        aiSuggestionsContainer.setManaged(hasSuggestions);
                    });
                } else {
                    // Read error stream
                    StringBuilder errorResponse = new StringBuilder();
                    try (InputStream errorStream = conn.getErrorStream();
                         BufferedReader br = new BufferedReader(new InputStreamReader(errorStream != null ? errorStream : conn.getInputStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = br.readLine()) != null) errorResponse.append(line);
                    }
                    System.err.println("AI API Error Body: " + errorResponse.toString());
                }
            } catch (Exception e) {
                System.err.println("AI Suggestion Exception: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private VBox createMiniProductCard(produit p) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color:#1e293b; -fx-background-radius:12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 4); -fx-border-color: rgba(255,255,255,0.05); -fx-border-radius:12;");
        card.setPrefWidth(180);
        card.setAlignment(Pos.CENTER);

        StackPane imgContainer = new StackPane();
        imgContainer.setPrefSize(140, 90);
        imgContainer.setStyle("-fx-background-color:#0f172a; -fx-background-radius:8;");

        ImageView iv = new ImageView();
        iv.setFitHeight(80);
        iv.setFitWidth(130);
        iv.setPreserveRatio(true);
        
        String imgName = p.getImagep();
        if (imgName != null && !imgName.isEmpty()) {
            try {
                if (imgName.startsWith("http")) {
                    iv.setImage(new Image(imgName, true));
                } else {
                    java.io.File localFile = new java.io.File("uploads/produits/" + imgName);
                    if (localFile.exists()) {
                        iv.setImage(new Image(localFile.toURI().toString(), true));
                    }
                }
            } catch (Exception e) {}
        }
        imgContainer.getChildren().add(iv);

        Label name = new Label(p.getNomp());
        name.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        name.setStyle("-fx-text-fill:#f1f5f9;");
        name.setWrapText(false);

        Label price = new Label(String.format("%.2f %s", p.getPrixp() * currentRate, currentCurrency));
        price.setStyle("-fx-text-fill:#FFD700; -fx-font-weight:bold; -fx-font-size:12px;");
        
        Button btn = new Button("🛒 +");
        btn.setStyle("-fx-background-color:#FFD700; -fx-text-fill:#020617; -fx-background-radius:8; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding: 5 15;");
        btn.setOnAction(e -> addToCart(p));

        card.getChildren().addAll(imgContainer, name, price, btn);
        
        // Add hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color:#1e293b; -fx-background-radius:12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 12, 0, 0, 6); -fx-border-color:#FFD700; -fx-border-radius:12; -fx-cursor:hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color:#1e293b; -fx-background-radius:12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 4); -fx-border-color: rgba(255,255,255,0.05); -fx-border-radius:12;"));

        return card;
    }

    @FXML
    public void toggleCart() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), cartSidebar);
        if (isCartOpen) {
            tt.setToX(400); // Slide out
            dimOverlay.setVisible(false);
            isCartOpen = false;
        } else {
            tt.setToX(0); // Slide in
            dimOverlay.setVisible(true);
            isCartOpen = true;
        }
        tt.play();
    }

    private void renderCartUI() {
        cartItemsContainer.getChildren().clear();
        int totalItems = 0;
        double totalCost = 0;

        for (CartItem item : cart) {
            totalItems += item.getQuantity();
            totalCost += item.getProduct().getPrixp() * item.getQuantity();
            
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            
            VBox info = new VBox(2);
            String pName = item.getProduct().getNomp() == null ? "Produit" : item.getProduct().getNomp();
            Label name = new Label(pName);
            name.setStyle("-fx-font-weight:bold; -fx-text-fill:#f1f5f9;");
            
            double linePrice = item.getProduct().getPrixp() * item.getQuantity() * currentRate;
            Label price = new Label(String.format("%.2f %s", linePrice, currentCurrency));
            price.setStyle("-fx-text-fill:#FFD700; -fx-font-size:12px;");
            info.getChildren().addAll(name, price);
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            HBox controls = new HBox(5);
            controls.setAlignment(Pos.CENTER);
            Button btnMinus = new Button("-");
            btnMinus.setStyle("-fx-background-color:#334155; -fx-text-fill:white; -fx-background-radius:5;");
            btnMinus.setOnAction(e -> {
                item.decrement();
                if (item.getQuantity() == 0) cart.remove(item);
                renderCartUI();
                updateAISuggestions();
            });
            
            Label qty = new Label(String.valueOf(item.getQuantity()));
            qty.setPrefWidth(20);
            qty.setAlignment(Pos.CENTER);
            qty.setStyle("-fx-text-fill:white;");
            
            Button btnPlus = new Button("+");
            btnPlus.setStyle("-fx-background-color:#334155; -fx-text-fill:white; -fx-background-radius:5;");
            btnPlus.setOnAction(e -> {
                item.increment();
                renderCartUI();
            });
            
            controls.getChildren().addAll(btnMinus, qty, btnPlus);
            row.getChildren().addAll(info, spacer, controls);
            
            cartItemsContainer.getChildren().add(row);
        }

        emptyCartLabel.setManaged(cart.isEmpty());
        emptyCartLabel.setVisible(cart.isEmpty());
        
        cartBadge.setText(String.valueOf(totalItems));
        totalLabel.setText(String.format("%.2f %s", totalCost * currentRate, currentCurrency));
    }

    @FXML
    public void checkout() {
        if (cart.isEmpty()) return;
        
        try {
            // Calculate total before clearing cart for the receipt
            double totalCost = 0;
            StringBuilder orderSummary = new StringBuilder("Gambatta Buvette - Facture\n------------------------\n");
            for (CartItem item : cart) {
                double linePrice = item.getProduct().getPrixp() * item.getQuantity();
                totalCost += linePrice;
                orderSummary.append(item.getQuantity()).append("x ").append(item.getProduct().getNomp()).append("\n");
            }
            double convertedTotal = totalCost * currentRate;
            String totalText = String.format("%.2f %s", convertedTotal, currentCurrency);
            orderSummary.append("------------------------\nTotal: ").append(totalText).append("\n\nMerci de votre visite!");

            // Save to DB
            vs.createVente(cart);
            
            // Clear cart UI
            cart.clear();
            renderCartUI();
            toggleCart(); // Close sidebar
            
            // Show Smart Receipt
            showReceiptModal(totalText, orderSummary.toString());
            
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void showReceiptModal(String totalText, String orderData) {
        receiptTotalLabel.setText(totalText);
        
        try {
            String encodedData = java.net.URLEncoder.encode(orderData, "UTF-8");
            String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=" + encodedData;
            qrCodeImage.setImage(new Image(qrUrl, true));
        } catch (Exception e) {
            e.printStackTrace();
        }

        dimOverlay.setVisible(true);
        receiptModal.setVisible(true);
        receiptModal.setManaged(true);
        
        FadeTransition ft = new FadeTransition(Duration.millis(300), receiptModal);
        ft.setFromValue(0);
        ft.setToValue(1);
        
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), receiptModal);
        tt.setFromY(50);
        tt.setToY(0);
        
        ft.play();
        tt.play();
    }

    @FXML
    public void closeReceiptModal() {
        FadeTransition ft = new FadeTransition(Duration.millis(200), receiptModal);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            receiptModal.setVisible(false);
            receiptModal.setManaged(false);
            if (!isCartOpen) dimOverlay.setVisible(false);
        });
        ft.play();
    }
}
