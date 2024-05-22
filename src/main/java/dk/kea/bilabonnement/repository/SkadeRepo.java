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

    public void addSkade(Skaderapport skadeRapport) {
        String sql = "INSERT INTO Skaderapport (Skade, Skade_Pris, Medarbejder_id, Kunde_id) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, skadeRapport.getSkade(), skadeRapport.getSkadePris(), 1, 1);
    }

    public void createSkadeRapport(String chassisNumber, List<Skaderapport> skadeList) {
        // Ensure skade_id is the same for all entries
        int skadeId = skadeList.get(0).getSkadeId();

        // Insert a row into SkadeRapport table with the same skade_id
        String skadeRapportSql = "INSERT INTO SkadeRapport (skade_id) VALUES (?)";
        jdbcTemplate.update(skadeRapportSql, skadeId);

        // Insert a row into Skade table for each entry in the skadeList
        String skadeSql = "INSERT INTO Skade (Skade, Skade_Pris, skade_id) VALUES (?, ?, ?)";
        for (Skaderapport skade : skadeList) {
            jdbcTemplate.update(skadeSql, skade.getSkade(), skade.getSkadePris(), skadeId);
        }

        // Update Bil status to "ledig"
        String updateSql = "UPDATE Bil SET status = 'ledig' WHERE chassisNumber = ?";
        jdbcTemplate.update(updateSql, chassisNumber);
    }

    public double getTotalPris() {
        String sql = "SELECT COALESCE(SUM(Skade_Pris), 0) FROM Skaderapport";
        return jdbcTemplate.queryForObject(sql, Double.class);
    }

    //mangler hvilken bil det er.
    public void submitTotalPrice() {
        String updateBilStatusSql = "UPDATE Bil SET status = 'ledig'";
        jdbcTemplate.update(updateBilStatusSql);
    }
}
