import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDB {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/skproject", "root", "root123");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT username, role FROM users");
            while (rs.next()) {
                System.out.println("User: " + rs.getString("username") + ", Role: " + rs.getString("role"));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
