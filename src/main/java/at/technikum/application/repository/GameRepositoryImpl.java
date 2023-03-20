package at.technikum.application.repository;

import at.technikum.application.config.DbConnector;

import java.sql.Connection;
import java.sql.SQLException;

public class GameRepositoryImpl implements GameRepository{

    private final DbConnector connector;

    public GameRepositoryImpl(DbConnector connector) {
        this.connector = connector;
    }

    @Override
    public String readStats() {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            try {
                return "GAME";
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed: " + e);
        }
    }

    @Override
    public String readScoreboard() {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            try {
                return "GAME";
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed: " + e);
        }
    }

    @Override
    public String startBattle() {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            try {
                return "GAME";
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed: " + e);
        }
    }
}
