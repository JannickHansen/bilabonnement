package dk.kea.bilabonnement.repository;

import dk.kea.bilabonnement.model.Bruger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class BrugerRepo {
    private static DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    public void setDataSource(DataSource dataSource) {
        BrugerRepo.dataSource = dataSource;
    }

    public void create(Bruger bruger) {
        final String INSERT_SQL = "INSERT INTO bruger (Login, Password, Type) VALUES (?, ?, ?)";
        jdbcTemplate.update(INSERT_SQL, bruger.getUsername(), bruger.getPassword(), bruger.getRole());
    }

    public Bruger getBruger(String login, String password){
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT * FROM bruger WHERE Login = ? AND Password = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, login);
            statement.setString(2, password);
            statement.setMaxRows(1);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Bruger brugerResponse = new Bruger();
                    brugerResponse.setMedarbejderId(resultSet.getInt("Medarbejder_id"));
                    brugerResponse.setUsername(resultSet.getString("Login"));
                    brugerResponse.setPassword(resultSet.getString("Password"));
                    brugerResponse.setRole(resultSet.getString("Type"));
                    return brugerResponse;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}