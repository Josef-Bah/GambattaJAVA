package gambatta.tn.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private static Connection cnx;

    private static final String URL = "jdbc:mysql://localhost:3306/gambatta_db?connectTimeout=3000";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getInstance() {
        try {
            if (cnx == null || cnx.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                cnx = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Connected to DB");
            }
            return cnx;
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("❌ Erreur connexion base de données : " + e.getMessage());
            return null;
        }
    }
}
