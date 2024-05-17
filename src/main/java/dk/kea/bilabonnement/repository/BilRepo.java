package dk.kea.bilabonnement.repository;

import dk.kea.bilabonnement.model.BilModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class BilRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static DataSource dataSource;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        BilRepo.dataSource = dataSource;
    }

    public void create(BilModel bil) {
        final String INSERT_SQL = "INSERT INTO bil (chassisNumber, licensePlate, brand, carModel, type, fuel, status, employeeID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(INSERT_SQL, bil.getChassisNumber(), bil.getLicensePlate(), bil.getBrand(), bil.getCarModel(), bil.getType(), bil.getFuel(), bil.getStatus(), bil.getEmployeeID());
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

}
