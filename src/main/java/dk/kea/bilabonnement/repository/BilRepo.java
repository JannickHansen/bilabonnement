package dk.kea.bilabonnement.repository;

import dk.kea.bilabonnement.model.BilModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class BilRepo {
    //lavet af Jannick

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static DataSource dataSource;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        BilRepo.dataSource = dataSource;
    }

    public void create(BilModel bil) {
        final String INSERT_SQL = "INSERT INTO bil (chassisNumber, licensePlate, km, brand, carModel, type, fuel, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(INSERT_SQL, bil.getChassisNumber(), bil.getLicensePlate(), bil.getKm(), bil.getBrand(), bil.getCarModel(), bil.getType(), bil.getFuel(), bil.getStatus());
    }

    /*Methode taget fra tidligere gruppeprojekt, Wishlist, og redigeret for at løse opgaven.
    Metoden bliver her ændret til at tjekke om databasen indeholder et stelnummer eller en nummerplade
    som er identisk til det input som brugeren har sat ind ved oprettelse af ny bil. */
    public static int authenticateUniqueCar(String chassisNumber, String licensePlate) {
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT chassisNumber, licensePlate FROM bil WHERE chassisNumber = ? OR licensePlate = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, chassisNumber);
            statement.setString(2, licensePlate);

            try (ResultSet resultSet = statement.executeQuery()) {
                boolean chassisNumberExists = false;
                boolean licensePlateExists = false;
                while (resultSet.next()) {
                    if (chassisNumber.equals(resultSet.getString("chassisNumber"))) {
                        chassisNumberExists = true;
                    }
                    if (licensePlate.equals(resultSet.getString("licensePlate"))) {
                        licensePlateExists = true;
                    }
                }
                // returner 1 hvis stelnummer findes i databasen
                if (chassisNumberExists) {
                    return 1;
                }
                // returner 2 hvis nummerpladen findes i databasen
                if (licensePlateExists) {
                    return 2;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<BilModel> loadAllCars() {
        return loadCars("SELECT * FROM bil");
    }

    // Metoden modtager bruger-input fra controlleren for /KPIpageTables, og er ansvarlig for at sortere tabellen
    // for biler som lever op til brugerens ønskede input. Metoden bruger en String-builder til at sammensætte
    // en samlet String som bruges som vores SQL Query som sendes til en metode som henter alle biler som lever op til searchCriteria.
    // Det sættes op som (SELECT * FROM bil WHERE (searchCriteria)) efterfulgt af AND så længe at listen indeholder yderligere searchCriteria.
    // Hvis ingen searchCriteria findes, sendes dette også til metoden, som så returnere alle biler.
    public List<BilModel> searchByKPI(String status, String brand, String carModel, String type, String fuel) {
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM bil");

        // Vi laver en liste af criterier indsat fra controlleren, altså brugerens input
        List<String> searchCriteria = new ArrayList<>();

        // Søger efter specifikt status, ignorerer hvis intet input
        if (status != null && !status.isEmpty()) {
            searchCriteria.add("status = '" + status + "'");
        }
        // Søger efter specifikt brand, ignorerer hvis intet input
        if (brand != null && !brand.isEmpty()) {
            searchCriteria.add("brand = '" + brand + "'");
        }
        // Søger efter specifik bilmodel, ignorerer hvis intet input
        if (carModel != null && !carModel.isEmpty()) {
            searchCriteria.add("carModel = '" + carModel + "'");
        }
        // Søger efter specifik type, ignorerer hvis intet input
        if (type != null && !type.isEmpty()) {
            searchCriteria.add("type = '" + type + "'");
        }
        // Søger efter specifikt fuel, ignorerer hvis intet input
        if (fuel != null && !fuel.isEmpty()) {
            searchCriteria.add("fuel = '" + fuel + "'");
        }
        // Samler kriterierne til et SQL query
        if (!searchCriteria.isEmpty()) {
            queryBuilder.append(" WHERE ").append(String.join(" AND ", searchCriteria));
        }
        return loadCars(queryBuilder.toString());
    }

    private List<BilModel> loadCars(String sql) {
        List<BilModel> foundCars = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                BilModel bil = new BilModel();
                bil.setChassisNumber(resultSet.getString("chassisNumber"));
                bil.setLicensePlate(resultSet.getString("LicensePlate"));
                bil.setKm(resultSet.getInt("km"));
                bil.setBrand(resultSet.getString("brand"));
                bil.setCarModel(resultSet.getString("carModel"));
                bil.setType(resultSet.getString("type"));
                bil.setFuel(resultSet.getString("fuel"));
                bil.setStatus(resultSet.getString("status"));

                foundCars.add(bil);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return foundCars;
    }

    // Original jdbc kodeTemplate kode.
    /*public void deleteChassisNumber(String chassisNumber) {
        final String DISABLE_FK_CHECK = "SET FOREIGN_KEY_CHECKS = 0";
        final String ACTIVATE_FK_CHECK = "SET FOREIGN_KEY_CHECKS = 1";
        final String DELETE_BY_CHASSISNUMBER_SQL = "DELETE FROM bil WHERE chassisNumber = ?";

        jdbcTemplate.execute(DISABLE_FK_CHECK);
        jdbcTemplate.update(DELETE_BY_CHASSISNUMBER_SQL, chassisNumber);
        jdbcTemplate.execute(ACTIVATE_FK_CHECK);
    }*/

    // JDBC og mySQL driver har indbygget anti-SQL injection, som f.eks. forhindre
    // multi-Query input (så du kan f.eks. ikke SQL-inject DELETE i en linje som allerede har CREATE.
    // Derudover bruger JDBCtemplate automatisk preparedStatement, netop så man ikke kan SQL-inject.
    // Man kan dog stadig bruge preparedStatement med vulnerabilities som:
    // "DELETE FROM bil WHERE chassisNumber = '" + chassisNumber + "'";
    // i stedet for
    // "DELETE FROM bil WHERE chassisNumber = ?";
    // Det største problem ved statement er at det tillader multi-queries, men ud fra min testing kunne
    // JDBC og mySQL driveren fange dem alle. dog utroligt svag mod input som vist følgende,:

    // test til statement: ABC123' OR '1'='1
    // String bliver til:  DELETE FROM bil WHERE chassisNumber = 'ABC123'' OR ''1''=''1';;
    // enkeltanførelsestegnene ' bliver ændret til '' hvilket gør hele inputtet til en samlet streng.
    // Derfor kan logic som 'OR', '=' og lignende ikke påvirker logikken i queriet.

    // Hvis: 1 OR 1=1
    // Læses som '1 OR 1=1', altså et samlet stykke String data, men ikke udførbart SQL kode.
    // Det parameteriseret så det er en samlet værdi, og altså ikke læses som del af SQL query, men i stedet som
    // en string værdi.
    public void deleteChassisNumber(String chassisNumber) {
        final String DISABLE_FK_CHECK = "SET FOREIGN_KEY_CHECKS = 0";
        final String ACTIVATE_FK_CHECK = "SET FOREIGN_KEY_CHECKS = 1";
        final String DELETE_BY_CHASSISNUMBER_SQL = "DELETE FROM bil WHERE chassisNumber = ?";

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(DISABLE_FK_CHECK);

            try (PreparedStatement preparedStatement = connection.prepareStatement(DELETE_BY_CHASSISNUMBER_SQL)) {
                preparedStatement.setString(1, chassisNumber);
                preparedStatement.executeUpdate();
            }

            statement.execute(ACTIVATE_FK_CHECK);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    // test til statement: ABC123' OR '1'='1
    // String bliver til:  DELETE FROM bil WHERE chassisNumber = 'ABC123' OR '1'='1';
    // Den tilføjer input direkte ind i stringen og executer så hele stringen.

    // Hvis 1 OR 1=1, sker intet, da 'OR' ikke markeres som SQL med '.
    // Derfor læses det hele som én data-værdi i formen string.

    // Hvis 1' OR '1'='1
    // String bliver til: DELETE FROM bil WHERE chassisNumber = '1' OR '1'='1';
    // Derfor også executable
    /*public void deleteChassisNumber(String chassisNumber) {
        final String DISABLE_FK_CHECK = "SET FOREIGN_KEY_CHECKS = 0";
        final String ACTIVATE_FK_CHECK = "SET FOREIGN_KEY_CHECKS = 1";
        final String DELETE_BY_CHASSISNUMBER_SQL = "DELETE FROM bil WHERE chassisNumber = '" + chassisNumber + "'";

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(DISABLE_FK_CHECK);
            statement.executeUpdate(DELETE_BY_CHASSISNUMBER_SQL);
            statement.execute(ACTIVATE_FK_CHECK);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }*/

    // Metode som modtager chassisnummer og den nye km, og så opdatere km
    public void changeKmOnCar(String chassisNumber, int newKm) {
        final String UPDATE_STATUS_BY_CHASSISNUMBER_SQL = "UPDATE bil SET km = ? WHERE chassisNumber = ?";
        jdbcTemplate.update(UPDATE_STATUS_BY_CHASSISNUMBER_SQL, newKm, chassisNumber);
    }

    // Metode som modtager chassisnummer og den nye status, og så opdatere status
    public void changeStatusOnCar(String chassisNumber, String newStatus) {
        final String UPDATE_STATUS_BY_CHASSISNUMBER_SQL = "UPDATE bil SET status = ? WHERE chassisNumber = ?";
        jdbcTemplate.update(UPDATE_STATUS_BY_CHASSISNUMBER_SQL, newStatus, chassisNumber);
    }
    // Metode til at vise available cars i en liste fra databasen.
    public List<BilModel> showAvailableCars() {
        String sql = "SELECT * FROM Bil WHERE status = 'Ledig'";
        RowMapper<BilModel> rowMapper = new BeanPropertyRowMapper<>(BilModel.class);
        return jdbcTemplate.query(sql, rowMapper);
    }

    // Metode til at finde en bils km antal fra databasen ud fra chassisNumber
    public int getKmByChassisNumber(String chassisNumber) {
        String sql = "SELECT km FROM Bil WHERE chassisNumber = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{chassisNumber}, Integer.class);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
