package gambatta.tn.ui.buvette;

import gambatta.tn.services.buvette.ProduitService;
import gambatta.tn.tools.MyDataBase;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

import java.sql.ResultSet;

public class DashboardController {

    @FXML private TableView<String> ventesTable;
    @FXML private Label revenueLabel;
    @FXML private Label topProductLabel;

    private final ProduitService ps = new ProduitService();

    @FXML
    public void initialize() {
        loadVentes();
        revenueLabel.setText(ps.getTotalRevenue() + " DT");
        topProductLabel.setText(ps.getTopProduct());
    }

    public void loadVentes() {
        try {
            ResultSet rs = MyDataBase.getInstance()
                    .createStatement()
                    .executeQuery("SELECT * FROM vente");

            while (rs.next()) {
                ventesTable.getItems().add(
                        "ID:" + rs.getInt("id") +
                                " | Total:" + rs.getDouble("montantv")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}