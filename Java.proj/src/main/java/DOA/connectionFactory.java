package DOA;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class connectionFactory {
    private static String url = "jdbc:mysql://localhost:3306/sakila";
    private static String user = "root";
    private static String pwd = "7200Grace";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver not found", e);
        }
        return DriverManager.getConnection(url, user, pwd);
    }
}
