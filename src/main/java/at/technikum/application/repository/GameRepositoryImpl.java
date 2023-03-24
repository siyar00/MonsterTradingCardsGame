package at.technikum.application.repository;

import at.technikum.application.config.DbConnector;
import at.technikum.application.model.UserStats;
import at.technikum.http.exceptions.BadRequestException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GameRepositoryImpl extends Repository implements GameRepository {
    public GameRepositoryImpl(DbConnector connector) {
        super(connector);
    }

    @Override
    public String readStats(String username) {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            try {
                authorizeUser(username);
                PreparedStatement selectStmt = connection.prepareStatement(READ_STATS);
                selectStmt.setString(1, username);
                ResultSet rs = selectStmt.executeQuery();
                rs.next();
                UserStats result = UserStats.builder().name(rs.getString("name") == null ? rs.getString("username") : rs.getString("name"))
                        .elo(rs.getInt("elo"))
                        .wins(rs.getInt("wins"))
                        .losses(rs.getInt("losses"))
                        .draws(rs.getInt("draws"))
                        .played(rs.getInt("played")).build();
                return new ObjectMapper().writeValueAsString(result);
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
    public String readScoreboard(String username) {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            try {
                authorizeUser(username);
                PreparedStatement selectStmt = connection.prepareStatement(READ_SCOREBOARD);
                ResultSet rs = selectStmt.executeQuery();
                List<UserStats> userStats = new ArrayList<>();
                while (rs.next())
                    userStats.add(UserStats.builder()
                            .name(rs.getString("name") == null ? "NO_NAME" : rs.getString("name"))
                            .elo(rs.getInt("elo"))
                            .wins(rs.getInt("wins"))
                            .losses(rs.getInt("losses"))
                            .draws(rs.getInt("draws"))
                            .played(rs.getInt("played")).build());
                return new ObjectMapper().writeValueAsString(userStats);
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed: " + e);
        } catch (JsonProcessingException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

}
