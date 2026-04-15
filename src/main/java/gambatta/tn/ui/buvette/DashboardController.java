package gambatta.tn.ui.buvette;

import gambatta.tn.tools.MyDataBase;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

import java.sql.ResultSet;

public class DashboardController {

    @FXML private TableView<String> ventesTable;

    @FXML
    public void initialize() {
        loadVentes();
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