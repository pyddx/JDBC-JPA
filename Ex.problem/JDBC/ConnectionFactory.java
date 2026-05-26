package DOA;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    // ╔══════════════════════════════════════════╗
    // ║  🔧 แก้ตรงนี้เท่านั้น!                  ║
    // ║  url  → ชื่อ DB ท้าย url (sakila)        ║
    // ║  user → username ของ MySQL               ║
    // ║  pwd  → password ของ MySQL               ║
    // ╚══════════════════════════════════════════╝
    private static final String URL  = "jdbc:mysql://localhost:3306/sakila"; // ← แก้ชื่อ DB
    private static final String USER = "root";                               // ← แก้ user
    private static final String PWD  = "yourpassword";                      // ← แก้ password

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("❌ Driver not found: " + e.getMessage(), e);
        }
        return DriverManager.getConnection(URL, USER, PWD);
    }
}
