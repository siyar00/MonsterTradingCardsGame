package at.technikum.application.repository.market;

import at.technikum.application.config.DbConnector;
import at.technikum.application.repository.Repository;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class MarketRepositoryImpl extends Repository implements MarketRepository {
    public MarketRepositoryImpl(@NotNull DbConnector connector) {
        super(connector);
    }


    @Override
    public String changeManaToCoins(String username) {
        try(Connection connection = connector.getConnection()){
            assert connection != null;
            int userId = authorizeUser(username);
            try(PreparedStatement updateStmt = connection.prepareStatement("UPDATE users SET mana = mana / 5 WHERE user_id = ?")){
                updateStmt.setInt(1, userId);
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed: " + e);
        }
        return null;
    }

    @Override
    public String sellCard(String username) {
        return null;
    }

    @Override
    public String buyCard(String username) {
        return null;
    }

    @Override
    public String deleteSell(String username) {
        return null;
    }

    @Override
    public String showMarket(String username) {
        return null;
    }

    @Override
    public String showStats(String username) {
        return null;
    }
}
