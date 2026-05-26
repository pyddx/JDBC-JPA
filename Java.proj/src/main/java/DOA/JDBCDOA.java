package DOA;
import Model.City;

import java.util.List;
import java.util.Optional;

public interface JDBCDOA<T,I> {
    Optional<City> find(Integer id);

    Optional<T>find(I id);
    List<T> getAll();
    void save(T entity);
    void update(T entity);
    void delete(I id);
}

