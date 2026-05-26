package Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class City {
    private int IDCity ;
    private String City ;
    private  int IDCountry;
    private java.time.LocalDateTime lastUpdate;
}
