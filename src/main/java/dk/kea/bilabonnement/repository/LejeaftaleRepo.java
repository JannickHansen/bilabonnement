package dk.kea.bilabonnement.repository;

import dk.kea.bilabonnement.model.BilModel;
import dk.kea.bilabonnement.model.Lejeaftale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Repository
public class LejeaftaleRepo {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static DataSource dataSource;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        LejeaftaleRepo.dataSource = dataSource;
    }

    public List<Lejeaftale> findAll(){
        String sql = "SELECT Lejeaftale.*, bil.brand, bil.carModel, bil.licensePlate FROM Lejeaftale, bil WHERE Lejeaftale.chassisNumber = bil.chassisNumber";
        RowMapper<Lejeaftale> rowMapper = new BeanPropertyRowMapper<>(Lejeaftale.class);
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<Lejeaftale> findRentedCars() {
        String sql = "SELECT Lejeaftale.*, Bil.brand, Bil.carModel, Bil.licensePlate " +
                "FROM Lejeaftale " +
                "JOIN Bil ON Lejeaftale.chassisNumber = Bil.chassisNumber " +
                "WHERE Bil.status = 'udlejet'";
        RowMapper<Lejeaftale> rowMapper = new BeanPropertyRowMapper<>(Lejeaftale.class);
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<Lejeaftale> findWaitingCars() {
        String sql = "SELECT Lejeaftale.*, Bil.brand, Bil.carModel, Bil.licensePlate " +
                "FROM Lejeaftale " +
                "JOIN Bil ON Lejeaftale.chassisNumber = Bil.chassisNumber " +
                "WHERE Bil.status = 'afventer'";
        RowMapper<Lejeaftale> rowMapper = new BeanPropertyRowMapper<>(Lejeaftale.class);
        return jdbcTemplate.query(sql, rowMapper);
    }

    public String findLicensePlate(String chassisNumber) {
        return "SELECT LicensePlate FROM bil WHERE chassisNumber = ?";
    }

    public List<String> findChassisNumberInDatabase(String chassisNumber) {
        String sql = "SELECT chassisNumber FROM bil WHERE chassisNumber = ?";
        RowMapper<String> rowMapper = new BeanPropertyRowMapper<>(String.class);
        return jdbcTemplate.query(sql, rowMapper, chassisNumber);
    }

    public int customerCheck(String Kunde_Navn, int Telefon_nummer, String Email, String Adresse) {
        String selectSql = "SELECT Kunde_id FROM kunde WHERE Kunde_Navn = ? AND Telefon_nummer = ? AND Email = ? AND Adresse = ?";
        String insertSql = "INSERT INTO kunde (Kunde_Navn, Telefon_nummer, Email, Adresse) VALUES (?, ?, ?, ?)";

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement selectStmt = connection.prepareStatement(selectSql);
                PreparedStatement insertStmt = connection.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS);
        ) {

            selectStmt.setString(1, Kunde_Navn);
            selectStmt.setInt(2, Telefon_nummer);
            selectStmt.setString(3, Email);
            selectStmt.setString(4, Adresse);

            ResultSet resultSet = selectStmt.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("Kunde_id");
            } else {
                // Insert new row
                insertStmt.setString(1, Kunde_Navn);
                insertStmt.setInt(2, Telefon_nummer);
                insertStmt.setString(3, Email);
                insertStmt.setString(4, Adresse);
                insertStmt.executeUpdate();

                ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Failed to get Kunde_id for inserted row");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void create(Lejeaftale lejeaftale) {
        final String INSERT_SQL = "INSERT INTO lejeaftale (chassisNumber, dato, Udlejnings_Type, Afhentningstidspunkt, Afhentningssted, Medarbejder_id, Kunde_id, status, Udlejningsperiode) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(INSERT_SQL, lejeaftale.getChassisNumber(), lejeaftale.getDato(), lejeaftale.getUdlejnings_Type(), lejeaftale.getAfhentningstidspunkt(), lejeaftale.getAfhentningssted(), lejeaftale.getMedarbejder_id(), lejeaftale.getKunde_id(), lejeaftale.getStatus(), lejeaftale.getUdlejningsperiode());
    }

    public List<Lejeaftale> findAllAfventende(){
        String sql = "SELECT lejeaftale.*, bil.LicensePlate FROM lejeaftale, bil WHERE lejeaftale.chassisNumber = bil.chassisNumber AND lejeaftale.status = 'Afventende'";
        RowMapper<Lejeaftale> rowMapper = new BeanPropertyRowMapper<>(Lejeaftale.class);
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<Lejeaftale> lejeaftalePeriodeFind() {
        String sql = "SELECT Udlejnings_Type, chassisNumber FROM lejeaftale WHERE chassisNumber = :chassisNumber";
        RowMapper<Lejeaftale> rowMapper = new BeanPropertyRowMapper<>(Lejeaftale.class);
        return jdbcTemplate.query(sql, rowMapper);
    }

    public void statusUpdate(String status, int lejeaftale_id){
        final String UPDATE_STATUS_BY_LEJEAFTALE_SQL = "UPDATE lejeaftale SET status = ? WHERE lejeaftale_id = ?";
        jdbcTemplate.update(UPDATE_STATUS_BY_LEJEAFTALE_SQL, status, lejeaftale_id);

    }
}
