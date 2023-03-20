package at.technikum.application.repository;

import at.technikum.application.config.DbConnector;
import at.technikum.application.model.Trading;
import at.technikum.application.util.Authorization;
import at.technikum.application.util.Headers;
import at.technikum.http.HttpStatus;
import at.technikum.http.Response;
import at.technikum.http.exceptions.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TradingRepositoryImpl implements TradingRepository {

    private static final String SETUP_TABLE = """
                    CREATE TABLE IF NOT EXISTS tradings (
                        trading_id TEXT PRIMARY KEY,
                        card_to_trade TEXT REFERENCES cards(card_id) ON DELETE CASCADE,
                        card_type TEXT,
                        element TEXT,
                        minimum_damage NUMERIC
                    )
            """;

    private static final String CHECK_CARDS = """
            SELECT * FROM cards c JOIN deck d ON c.user_id_fk = d.user_id_fk
            WHERE c.user_id_fk = ? AND card_id != card1 AND card_id != card2 AND card_id != card3 AND card_id != card4
            AND card_id = ?;
            """;

    private static final String INSERT_QUERY = """
            INSERT INTO tradings(trading_id, card_to_trade, card_type, minimum_damage) VALUES(?,?,?,?)
            """;

    private static final String TRADE_FOR_SELLER = """
            UPDATE cards SET user_id_fk = (SELECT c.user_id_fk FROM tradings t
            JOIN cards c ON t.card_to_trade = c.card_id WHERE t.trading_id = ?)  WHERE card_id = ?
            """;

    private static final String TRADE_FOR_BUYER = """
            UPDATE cards SET user_id_fk = ? WHERE card_id = (SELECT card_to_trade FROM tradings WHERE trading_id = ?)
            """;

    public TradingRepositoryImpl(@NotNull DbConnector connector) {
        this.connector = connector;
        new CardsRepositoryImpl(connector);
        try (PreparedStatement ps = connector.getConnection().prepareStatement(SETUP_TABLE)) {
            ps.execute();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to setup up table: " + e);
        }
    }

    private final DbConnector connector;

    @Override
    public Response showAllTradings(String username) {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            try {
                new Authorization().authorizeUser(username, connection);
                PreparedStatement selectStmt = connection.prepareStatement("SELECT trading_id, card_to_trade, card_type, minimum_damage FROM tradings");
                ResultSet rs = selectStmt.executeQuery();
                if (!rs.next())
                    return new Response(HttpStatus.NO_CONTENT, "The request was fine, but there are no trading deals available");
                List<Trading> tradings = new ArrayList<>();
                do {
                    tradings.add(Trading.builder()
                            .id(rs.getString("trading_id"))
                            .cardToTrade(rs.getString("card_to_trade"))
                            .type(rs.getString("card_type"))
                            .minimumDamage(rs.getDouble("minimum_damage")).build());
                } while (rs.next());
                return new Response(HttpStatus.OK, new ObjectMapper().writeValueAsString(tradings), Headers.CONTENT_TYPE_JSON);
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
    public String createNewTradingDeal(Trading trading, String username) {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            try {
                ResultSet rs = new Authorization().authorizeUser(username, connection);
                checkDeal(connection, rs.getInt("user_id"), trading);
                createDeal(connection, trading);
                return "Trading deal successfully created";
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed: " + e);
        }
    }

    private void checkDeal(Connection connection, int user_id, Trading trading) throws SQLException {
        PreparedStatement selectStmt = connection.prepareStatement(CHECK_CARDS);
        selectStmt.setInt(1, user_id);
        selectStmt.setString(2, trading.getCardToTrade());
        ResultSet rs = selectStmt.executeQuery();
        if (!rs.next())
            throw new ForbiddenException("The deal contains a card that is not owned by the user or locked in the deck.");
        selectStmt.close();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM tradings WHERE trading_id = ?");
        statement.setString(1, trading.getId());
        ResultSet set = statement.executeQuery();
        if (set.next())
            throw new ExistingException("A deal with this deal ID already exists.");
        statement.close();
    }

    private void createDeal(Connection connection, Trading trading) throws SQLException {
        PreparedStatement insertStmt = connection.prepareStatement(INSERT_QUERY);
        insertStmt.setString(1, trading.getId());
        insertStmt.setString(2, trading.getCardToTrade());
        insertStmt.setString(3, trading.getType());
        insertStmt.setDouble(4, trading.getMinimumDamage());
        insertStmt.executeUpdate();
        insertStmt.close();
    }

    @Override
    public String deleteTradingDeal(String username, String tradingId) {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            try {
                ResultSet rs = new Authorization().authorizeUser(username, connection);
                checkDelete(connection, rs.getInt("user_id"), checkTradeId(connection, tradingId));
                PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM tradings WHERE trading_id = ?");
                deleteStmt.setString(1, tradingId);
                deleteStmt.executeUpdate();
                return "Trading deal successfully deleted";
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed: " + e);
        }
    }

    private String checkTradeId(Connection connection, String tradingId) throws SQLException {
        PreparedStatement selectStmt = connection.prepareStatement("SELECT * FROM tradings WHERE trading_id = ?");
        selectStmt.setString(1, tradingId);
        ResultSet rs = selectStmt.executeQuery();
        if (!rs.next()) throw new NotFoundException("The provided deal ID was not found.");
        return rs.getString("card_to_trade");
    }

    private void checkDelete(Connection connection, int userId, String card_id) throws SQLException {
        PreparedStatement selectStmt = connection.prepareStatement("SELECT * FROM cards WHERE card_id = ? AND user_id_fk = ?");
        selectStmt.setString(1, card_id);
        selectStmt.setInt(2, userId);
        ResultSet set = selectStmt.executeQuery();
        if (!set.next()) throw new ForbiddenException("The deal contains a card that is not owned by the user.");
        selectStmt.close();
    }

    @Override
    public String trade(String username, String tradingId, String cardId) {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            try {
                ResultSet rs = new Authorization().authorizeUser(username, connection);
                checkTradeId(connection, tradingId);
                checkTrade(connection, tradingId, cardId, rs.getInt("user_id"));
                trading(connection, tradingId, cardId, rs.getInt("user_id"));
                return "Trading deal successfully executed.";
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed: " + e);
        }
    }

    private void trading(Connection connection, String tradingId, String cardId, int user_id) throws SQLException {
        coinChange(connection, tradingId, user_id);
        PreparedStatement updateFirstStmt = connection.prepareStatement(TRADE_FOR_SELLER);
        updateFirstStmt.setString(1, tradingId);
        updateFirstStmt.setString(2, cardId);
        updateFirstStmt.execute();

        PreparedStatement updateBuyerStmt = connection.prepareStatement(TRADE_FOR_BUYER);
        updateBuyerStmt.setInt(1, user_id);
        updateBuyerStmt.setString(2, tradingId);
        updateBuyerStmt.execute();

        deleteTrading(connection, tradingId);

        updateBuyerStmt.close();
        updateBuyerStmt.close();
    }

    private void deleteTrading(Connection connection, String tradingId) throws SQLException {
        PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM tradings WHERE trading_id = ?");
        deleteStmt.setString(1, tradingId);
        deleteStmt.execute();
        deleteStmt.close();
    }

    private void coinChange(Connection connection, String tradingId, int boughtCard) throws SQLException {
        try(PreparedStatement subCoin = connection.prepareStatement("UPDATE users SET coins = coins - 1 WHERE user_id = ?")){
            subCoin.setInt(1, boughtCard);
            subCoin.execute();
        } catch (SQLException e){
            throw new BadRequestException("Not enough coins to buy card.");
        }

        PreparedStatement addCoin = connection.prepareStatement("""
        UPDATE users SET coins = coins + 1 WHERE user_id = (SELECT c.user_id_fk FROM tradings t JOIN cards c ON t.card_to_trade = c.card_id WHERE t.trading_id = ?)
        """);
        addCoin.setString(1, tradingId);
        addCoin.execute();
    }

    private void checkTrade(Connection connection, String tradingId, String cardId, int userId) throws SQLException {
        ownedByUser(connection, cardId, tradingId, userId);
        lockedCard(connection, cardId);
        selfTrade(connection, tradingId, userId);
    }

    private void ownedByUser(Connection connection, String cardId, String tradingId, int userId) throws SQLException {
        PreparedStatement selectStmt = connection.prepareStatement("SELECT * FROM cards WHERE card_id = ? AND user_id_fk = ?");
        selectStmt.setString(1, cardId);
        selectStmt.setInt(2, userId);
        ResultSet rs = selectStmt.executeQuery();
        if (!rs.next()) throw new ForbiddenException("The offered card is not owned by the user!");
        minRequirements(connection, tradingId, rs);
    }

    private void minRequirements(Connection connection, String tradingId, ResultSet cardResult) throws SQLException {
        PreparedStatement selectStmt = connection.prepareStatement("SELECT t.card_type, t.minimum_damage FROM tradings t JOIN cards c ON card_to_trade = card_id WHERE trading_id = ?");
        selectStmt.setString(1, tradingId);
        ResultSet tradingResult = selectStmt.executeQuery();
        tradingResult.next();
        if (!tradingResult.getString("card_type").equals(cardResult.getString("card_type")))
            throw new ForbiddenException("The requirements are not met");
        else if (tradingResult.getInt("minimum_damage") > cardResult.getInt("damage"))
            throw new ForbiddenException("The requirements are not met");
    }

    private void lockedCard(Connection connection, String cardId) throws SQLException {
        PreparedStatement selectStmt = connection.prepareStatement("SELECT * FROM deck WHERE ? IN (card1, card2, card3, card4)");
        selectStmt.setString(1, cardId);
        ResultSet rs = selectStmt.executeQuery();
        if (rs.next()) throw new ForbiddenException("The offered card is locked in a deck!");
    }

    private void selfTrade(Connection connection, String tradingId, int userId) throws SQLException {
        PreparedStatement selectStmt = connection.prepareStatement("SELECT c.user_id_fk FROM tradings t JOIN cards c ON t.card_to_trade = c.card_id WHERE t.trading_id = ?");
        selectStmt.setString(1, tradingId);
        ResultSet rs = selectStmt.executeQuery();
        rs.next();
        if (rs.getInt("user_id_fk") == userId) throw new ForbiddenException("The offered card is owned by the user!");
    }
}
