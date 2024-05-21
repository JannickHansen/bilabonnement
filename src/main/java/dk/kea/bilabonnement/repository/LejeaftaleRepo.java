package dk.kea.bilabonnement.repository;

import dk.kea.bilabonnement.model.Lejeaftale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public class LejeaftaleRepo {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Lejeaftale> findAll(){
        String sql = "SELECT Lejeaftale.*, bil.licensePlate FROM Lejeaftale, bil WHERE Lejeaftale.chassisNumber = bil.chassisNumber";
        RowMapper<Lejeaftale> rowMapper = new BeanPropertyRowMapper<>(Lejeaftale.class);
        return jdbcTemplate.query(sql, rowMapper);
    }
}
