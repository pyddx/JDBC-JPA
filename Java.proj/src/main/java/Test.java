import DOA.connectionFactory;
import Repository.CityDAO;

import java.sql.Connection;
import java.sql.SQLException;

public class Test {
    static void main(String[] args) {
        CityDAO cityDAO = new CityDAO();
        cityDAO.find(5).ifPresent(city -> {
            System.out.println("ค้นพบเมือง: " + city.getCity());
            System.out.println("ข้อมูลทั้งหมด: " + city);
        });

        try (Connection con = connectionFactory.getConnection()) {
            System.out.println("connect success");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
