package dk.kea.bilabonnement.repository;

import dk.kea.bilabonnement.model.Skaderapport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public class SkadeRepo {
    //lavet af Thomas
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void save(Skaderapport skaderapport, int lejeaftaleId) {
        String sql = "INSERT INTO Skaderapport (Lejeaftale_id, Skade, Skade_Pris, Kunde_id) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, lejeaftaleId, skaderapport.getSkade(), skaderapport.getSkadePris(), skaderapport.getKundeId());
    }

}
