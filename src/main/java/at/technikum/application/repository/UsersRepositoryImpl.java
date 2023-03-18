package at.technikum.application.repository;

import at.technikum.application.config.DbConnector;
import at.technikum.application.database.UsersDB;
import at.technikum.application.model.Credentials;
import at.technikum.application.model.UserDataRec;
import at.technikum.http.exceptions.ExistingException;
import at.technikum.http.exceptions.NotFoundException;
import at.technikum.http.exceptions.UnauthorizedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsersRepositoryImpl implements UsersRepository {

    private static final String SETUP_TABLE = """
                CREATE TABLE IF NOT EXISTS users(
                    user_id SERIAL PRIMARY KEY,
                    username TEXT NOT NULL,
                    password TEXT NOT NULL
                );
            """;

    private static final String SELECT_USER = """
            SELECT * FROM users WHERE username = ?
            """;
    private static final String QUERY_USER_PASS = """
            SELECT * FROM users WHERE username = ? AND password = ?
            """;
    private static final String INSERT_USER = """
            INSERT INTO users (username, password) VALUES (?,?)
            """;
    private static final String UPDATE_USER = """
            UPDATE users SET name = ?, bio = ?, image = ? WHERE username = ?
            """;

    private final DbConnector dataSource;

    public UsersRepositoryImpl(DbConnector dataSource) {
        this.dataSource = dataSource;
        try (PreparedStatement ps = dataSource.getConnection().prepareStatement(SETUP_TABLE)) {
            ps.execute();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to setup up table", e);
        }
    }

    @Override
    public String registerUser(Credentials credentials) {
        try (Connection connection = dataSource.getConnection()) {
            assert connection != null;
            try (PreparedStatement stmt = connection.prepareStatement(SELECT_USER)) {
                stmt.setString(1, credentials.getUsername());
                ResultSet rs = stmt.executeQuery();
                if (rs.next())
                    throw new ExistingException("User with username " + credentials.getUsername() + " already registered");
                stmt.close();
                PreparedStatement insertStmt = connection.prepareStatement(INSERT_USER);
                insertStmt.setString(1, credentials.getUsername());
                insertStmt.setString(2, credentials.getPassword());
                if (insertStmt.executeUpdate() == 0) throw new SQLException("Could not register user!");
                return "User successfully created";
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed", e);
        }
    }

    @Override
    public String getUserData(String username) {
        try (Connection connection = dataSource.getConnection()) {
            assert connection != null;
            ResultSet rs = findUser(connection, username);
            try {
                UserDataRec userDataRec = UserDataRec.builder()
                        .name(rs.getString(UsersDB.NAME.toString()))
                        .bio(rs.getString(UsersDB.BIO.toString()))
                        .image(rs.getString(UsersDB.IMAGE.toString())).build();
                return new ObjectMapper().writeValueAsString(userDataRec);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("JSON converting did not work!" + e);
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed", e);
        }
    }

    @Override
    public String updateUser(String username, UserDataRec userDataRec) {
        try (Connection connection = dataSource.getConnection()) {
            assert connection != null;
            findUser(connection, username);
            try (PreparedStatement updateStmt = connection.prepareStatement(UPDATE_USER)) {
                updateStmt.setString(1, userDataRec.getName());
                updateStmt.setString(2, userDataRec.getBio());
                updateStmt.setString(3, userDataRec.getImage());
                updateStmt.setString(4, username);
                if (updateStmt.executeUpdate() == 0) throw new SQLException("Could not register user!");
                return "User successfully updated.";
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed", e);
        }
    }

    @Override
    public String loginUser(Credentials credentials) {
        try (Connection connection = dataSource.getConnection()) {
            assert connection != null;
            try (PreparedStatement stmt = connection.prepareStatement(QUERY_USER_PASS)) {
                stmt.setString(1, credentials.getUsername());
                stmt.setString(2, credentials.getPassword());
                ResultSet rs = stmt.executeQuery();
                if(!rs.next()) throw new UnauthorizedException("Invalid username/password provided");
                return credentials.getUsername()+"-mtcgToken";
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed", e);
        }
    }

    private ResultSet findUser(Connection connection, String username) throws NotFoundException, SQLException {
        PreparedStatement stmt = connection.prepareStatement(SELECT_USER);
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();
        if (!rs.next()) throw new NotFoundException("User not found!");
        return rs;
    }

}
