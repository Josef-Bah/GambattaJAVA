package gambatta.tn.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private static Connection cnx;

    private static final String URL = "jdbc:mysql://localhost:3306/gambatta_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getInstance() {

        try {
            if (cnx == null) {
                cnx = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Connected to DB");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return cnx; // ✅ VERY IMPORTANT
    }
}