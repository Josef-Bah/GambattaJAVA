package gambatta.tn.tools;
import java.sql.*;
import java.util.Date;
public class TestDB {
    public static void main(String[] args) {
        try {
            Connection cnx = DriverManager.getConnection("jdbc:mysql://localhost:3306/gambatta_db", "root", "");
            System.out.println("Connected!");
            String sql = "INSERT INTO reservationactivite (datedebut, heurer, statutr, ida_id, idu_id, creneau_id) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setDate(1, new java.sql.Date(new Date().getTime()));
            ps.setString(2, "10:00");
            ps.setString(3, "EN_ATTENTE");
            ps.setInt(4, 1); // Assuming activity 1 exists
            ps.setInt(5, 1); // Assuming user 1 exists
            ps.setNull(6, Types.INTEGER);
            ps.executeUpdate();
            System.out.println("Inserted!");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
