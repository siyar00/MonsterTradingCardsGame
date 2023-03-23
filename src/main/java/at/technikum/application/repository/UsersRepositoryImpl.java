package at.technikum.application.repository;

import at.technikum.application.config.DbConnector;
import at.technikum.application.database.UsersDB;
import at.technikum.application.model.Credentials;
import at.technikum.application.model.UserData;
import at.technikum.http.exceptions.BadRequestException;
import at.technikum.http.exceptions.ExistingException;
import at.technikum.http.exceptions.NotFoundException;
import at.technikum.http.exceptions.UnauthorizedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsersRepositoryImpl implements UsersRepository {

    private static final String SETUP_TABLE = """
                CREATE TABLE IF NOT EXISTS users(
                    user_id SERIAL PRIMARY KEY,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    coins INTEGER NOT NULL DEFAULT 22 CHECK (coins >= 0),
                    name TEXT,
                    bio TEXT,
                    image TEXT,
                    elo INTEGER DEFAULT 100 NOT NULL,
                    wins INTEGER DEFAULT 0 NOT NULL,
                    losses INTEGER DEFAULT 0 NOT NULL,
                    draws INTEGER DEFAULT 0 NOT NULL,
                    played INTEGER DEFAULT 0
                )
            """;

    private static final String SELECT_USER = """
            SELECT * FROM users WHERE username = ?
            """;
    private static final String QUERY_USER_PASS = """
            SELECT * FROM users WHERE username = ? AND password = ?
            """;
    private static final String INSERT_USER = """
            INSERT INTO users (username, password) VALUES (?,?) RETURNING *
            """;
    private static final String UPDATE_USER = """
            UPDATE users SET name = ?, bio = ?, image = ? WHERE username = ?
            """;

    private final DbConnector connector;

    public UsersRepositoryImpl(@NotNull DbConnector connector) {
        this.connector = connector;
        try (PreparedStatement ps = connector.getConnection().prepareStatement(SETUP_TABLE)) {
            ps.execute();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to setup up table: " + e);
        }
    }

    @Override
    public String registerUser(Credentials credentials) {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            try {
                existingUser(connection, credentials.getUsername());
                insertUser(connection, credentials);
                return "User successfully created";
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed: " + e);
        }
    }

    private void existingUser(Connection connection, String username) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(SELECT_USER);
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();
        if (rs.next())
            throw new ExistingException("User with username " + username + " already registered");
        stmt.close();
    }

    private void insertUser(Connection connection, Credentials credentials) throws SQLException {
        PreparedStatement insertStmt = connection.prepareStatement(INSERT_USER);
        insertStmt.setString(1, credentials.getUsername());
        insertStmt.setString(2, credentials.getPassword());
        ResultSet rs = insertStmt.executeQuery();
        if (!rs.next()) throw new SQLException("Insert did not work!");
        createDeckForUser(connection, rs.getInt("user_id"));
        insertStmt.close();
    }

    private void createDeckForUser(Connection connection, int user_id) throws SQLException {
        PreparedStatement createDeckStmt = connection.prepareStatement("INSERT INTO deck(user_id_fk) VALUES (?)");
        createDeckStmt.setInt(1, user_id);
        createDeckStmt.executeUpdate();
        createDeckStmt.close();
    }

    @Override
    public String getUserData(String username) {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            ResultSet rs = findUser(connection, username);
            try {
                UserData userData = UserData.builder()
                        .name(rs.getString(UsersDB.NAME.toString()))
                        .bio(rs.getString(UsersDB.BIO.toString()))
                        .image(rs.getString(UsersDB.IMAGE.toString())).build();
                return new ObjectMapper().writeValueAsString(userData);
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed: " + e);
        } catch (JsonProcessingException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @Override
    public String updateUser(String username, UserData userData) {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            findUser(connection, username);
            try (PreparedStatement updateStmt = connection.prepareStatement(UPDATE_USER)) {
                updateStmt.setString(1, userData.getName());
                updateStmt.setString(2, userData.getBio());
                updateStmt.setString(3, userData.getImage());
                updateStmt.setString(4, username);
                if (updateStmt.executeUpdate() == 0) throw new SQLException("Could not register user!");
                return "User successfully updated.";
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed: " + e);
        }
    }

    @Override
    public String loginUser(Credentials credentials) {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            try (PreparedStatement stmt = connection.prepareStatement(QUERY_USER_PASS)) {
                stmt.setString(1, credentials.getUsername());
                stmt.setString(2, credentials.getPassword());
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) throw new UnauthorizedException("Invalid username/password provided");
                return credentials.getUsername() + "-mtcgToken";
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed: " + e);
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
