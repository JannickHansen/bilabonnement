package dk.kea.bilabonnement.repository;

import dk.kea.bilabonnement.model.Lejeaftale;
import dk.kea.bilabonnement.model.Skaderapport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public class SkadeRepo {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void save(Skaderapport skaderapport, int lejeaftaleId) {
        String sql = "INSERT INTO Skaderapport (Skade, Skade_Pris, Medarbejder_id, Kunde_id, Lejeaftale_id) VALUES (?, ?, 1, 1, ?)";
        jdbcTemplate.update(sql, skaderapport.getSkade(), skaderapport.getSkadePris(), lejeaftaleId);
    }

}
