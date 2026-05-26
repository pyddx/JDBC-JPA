package Repository;

import DOA.JDBCDOA;
import DOA.connectionFactory;
import Model.City;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CityDAO implements JDBCDOA {
    @Override
    public Optional<City> find(Integer id) {String statement = "select * from city where city_id = ?";

        try (Connection con = connectionFactory.getConnection();
             PreparedStatement preState = con.prepareStatement(statement)) {
            preState.setString(1, String.valueOf(id));

            try (ResultSet rs = preState.executeQuery()) {
                if (rs.next()) {
                    City city = new City();
                    city.setIDCity(rs.getInt("city_id"));
                    city.setCity(rs.getString("city"));
                    city.setIDCountry(rs.getInt("country_id"));
                    city.setLastUpdate(rs.getObject("last_update", java.time.LocalDateTime.class));
                    return Optional.of(city);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public Optional find(Object id) {
        return Optional.empty();
    }

    @Override
    public List getAll() {
        return List.of();
    }

    @Override
    public void save(Object entity) {

    }

    @Override
    public void update(Object entity) {

    }

    @Override
    public void delete(Object id) {

    }
}
