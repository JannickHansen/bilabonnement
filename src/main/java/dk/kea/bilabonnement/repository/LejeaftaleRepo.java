package dk.kea.bilabonnement.repository;

import dk.kea.bilabonnement.model.Lejeaftale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@Repository
public class LejeaftaleRepo {
    //lavet af Jannick
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static DataSource dataSource;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        LejeaftaleRepo.dataSource = dataSource;
    }

    public List<Lejeaftale> findRentedCars() {
        String sql = "SELECT Lejeaftale.*, Bil.brand, Bil.carModel, Bil.licensePlate " +
                "FROM Lejeaftale " +
                "JOIN Bil ON Lejeaftale.chassisNumber = Bil.chassisNumber " +
                "WHERE Lejeaftale.status = 'Udlejet'";
        RowMapper<Lejeaftale> rowMapper = new BeanPropertyRowMapper<>(Lejeaftale.class);
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<Lejeaftale> findWaitingCars() {
        String sql = "SELECT Lejeaftale.*, Bil.brand, Bil.carModel, Bil.licensePlate " +
                "FROM Lejeaftale " +
                "JOIN Bil ON Lejeaftale.chassisNumber = Bil.chassisNumber " +
                "WHERE Lejeaftale.status = 'Afventende'";
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
    public List<Integer> findCustomerNumber (int lejeaftale_id) {
        String sql = "SELECT Kunde_id FROM Lejeaftale WHERE lejeaftale_id = ?";
        RowMapper<Integer> rowMapper = new BeanPropertyRowMapper<>(Integer.class);
        return jdbcTemplate.query(sql, rowMapper, lejeaftale_id);
    }

    public List<Integer> findUdlejningsPeriodeByChassisNumber(String chassisNumber) {
        String sql = "SELECT udlejningsperiode FROM lejeaftale WHERE chassisNumber = ?";
        RowMapper<Integer> rowMapper = new RowMapper<>() {
            @Override
            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getInt("udlejningsperiode");
            }
        };
        return jdbcTemplate.query(sql, rowMapper, chassisNumber);
    }

    // metode er delvis lavet af chatgpt, specifikt kontakten til databasen i String sql linjen.
    public List<Integer> findSkadePrisByChassisNumber(String chassisNumber) {
        String sql = "SELECT sr.Skade_Pris " +
                "FROM skaderapport sr " +
                "JOIN lejeaftale la ON sr.lejeaftale_id = la.lejeaftale_id " +
                "JOIN bil b ON la.chassisNumber = b.chassisNumber " +
                "WHERE b.chassisNumber = ?";

        RowMapper<Integer> rowMapper = new RowMapper<>() {
            @Override
            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getInt("Skade_Pris");
            }
        };
        return jdbcTemplate.query(sql, rowMapper, chassisNumber);
    }
    public List<Lejeaftale> findLejePrisByChassisNumber(String chassisNumber) {
        String sql = "SELECT udlejningsperiode, Udlejnings_Type FROM lejeaftale WHERE chassisNumber = ?";
        RowMapper<Lejeaftale> rowMapper = new RowMapper<>() {
            @Override
            public Lejeaftale mapRow(ResultSet rs, int rowNum) throws SQLException {
                Lejeaftale lejeaftale = new Lejeaftale();
                lejeaftale.setUdlejningsperiode(rs.getInt("udlejningsperiode"));
                lejeaftale.setUdlejnings_Type(rs.getString("Udlejnings_Type"));
                return lejeaftale;
            }
        };
        return jdbcTemplate.query(sql, rowMapper, chassisNumber);
    }

    // Metoden modtager kunde informationer som indsættes ved oprettelse af en ny lejeaftale. Denne metode er så ansvarlig for, at
    // tjekke om kunden allerede er oprettet i databasen. Hvis kunden ikke er oprettet, opretter den en ny kunde med de indsatte informationer,
    // og hvis kunden er oprettet, tilbage sendes kundens kunde_id.
    public int customerCheck(String Kunde_Navn, int Telefon_nummer, String Email, String Adresse) {
        // SQL koden for begge situationer opstilles som Strings
        String selectSql = "SELECT Kunde_id FROM kunde WHERE Kunde_Navn = ? AND Telefon_nummer = ? AND Email = ? AND Adresse = ?";
        String insertSql = "INSERT INTO kunde (Kunde_Navn, Telefon_nummer, Email, Adresse) VALUES (?, ?, ?, ?)";

        try (
                Connection connection = dataSource.getConnection();
                // PreparedStatements oprettes med SQL Strings
                PreparedStatement select = connection.prepareStatement(selectSql);
                PreparedStatement insert = connection.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS);
        ) {
            select.setString(1, Kunde_Navn);
            select.setInt(2, Telefon_nummer);
            select.setString(3, Email);
            select.setString(4, Adresse);
            // Vi kalder executeQuery på vores PreparedStatement select.
            ResultSet resultSet = select.executeQuery();
            // Hvis executeQuery finder en række i databasen som stemmer overens med alle søge kriterier indsendt, returnere den kundens kunde_id.
            // Ellers opretter den en ny kunde med de indsatte informationer med PreparedStatement insert, og derefter returnere den nyoprettede
            // kundes kunde_id.
            if (resultSet.next()) {
                return resultSet.getInt("Kunde_id");
            } else {
                insert.setString(1, Kunde_Navn);
                insert.setInt(2, Telefon_nummer);
                insert.setString(3, Email);
                insert.setString(4, Adresse);
                insert.executeUpdate();

                ResultSet generatedKunde_id = insert.getGeneratedKeys();
                if (generatedKunde_id.next()) {
                    return generatedKunde_id.getInt(1);
                } else {
                    throw new SQLException("Fandt intet Kunde_id for eftersøgte række.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void create(Lejeaftale lejeaftale) {
        final String INSERT_SQL = "INSERT INTO lejeaftale (chassisNumber, dato, Udlejnings_Type, Afhentningstidspunkt, Afhentningssted, Kunde_id, status, Udlejningsperiode) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(INSERT_SQL, lejeaftale.getChassisNumber(), lejeaftale.getDato(), lejeaftale.getUdlejnings_Type(), lejeaftale.getAfhentningstidspunkt(), lejeaftale.getAfhentningssted(), lejeaftale.getKunde_id(), lejeaftale.getStatus(), lejeaftale.getUdlejningsperiode());
    }

    public List<Lejeaftale> findAllAfventende() {
        String sql = "SELECT lejeaftale.*, bil.LicensePlate " +
                "FROM lejeaftale " +
                "JOIN bil ON lejeaftale.chassisNumber = bil.chassisNumber " +
                "WHERE lejeaftale.status = 'Afventende'";

        RowMapper<Lejeaftale> rowMapper = new BeanPropertyRowMapper<>(Lejeaftale.class);

        //trycatch til at fange hvis databasen ikke har nogle lejeaftaler som kvalificere til "Afventende"
        try {
            return jdbcTemplate.query(sql, rowMapper);
        } catch (Exception e) {
            System.err.println("SQL query execution failed: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public void statusUpdate(String status, int lejeaftale_id){
        final String UPDATE_STATUS_BY_LEJEAFTALE_SQL = "UPDATE lejeaftale SET status = ? WHERE lejeaftale_id = ?";
        jdbcTemplate.update(UPDATE_STATUS_BY_LEJEAFTALE_SQL, status, lejeaftale_id);

    }
}
