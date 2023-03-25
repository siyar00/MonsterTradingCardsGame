package at.technikum.application.repository.trading;

import at.technikum.application.config.DbConnector;
import at.technikum.application.model.Trading;
import at.technikum.application.repository.Repository;
import at.technikum.http.Headers;
import at.technikum.http.HttpStatus;
import at.technikum.http.Response;
import at.technikum.http.exceptions.BadRequestException;
import at.technikum.http.exceptions.ExistingException;
import at.technikum.http.exceptions.ForbiddenException;
import at.technikum.http.exceptions.NotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TradingRepositoryImpl extends Repository implements TradingRepository {

    public TradingRepositoryImpl(@NotNull DbConnector connector) {
        super(connector);
    }

    @Override
    public Response showAllTradings(String username) {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            try {
                authorizeUser(username);
                PreparedStatement selectStmt = connection.prepareStatement(SELECT_TRADINGS);
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
                int userId = authorizeUser(username);
                checkDeal(connection, userId, trading);
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
        PreparedStatement statement = connection.prepareStatement(SELECT_WITH_TRADING_ID);
        statement.setString(1, trading.getId());
        ResultSet set = statement.executeQuery();
        if (set.next()) throw new ExistingException("A deal with this deal ID already exists.");
        statement.close();
    }

    private void createDeal(Connection connection, Trading trading) throws SQLException {
        PreparedStatement insertStmt = connection.prepareStatement(INSERT_TRADING_DEAL);
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
                int userId = authorizeUser(username);
                checkDelete(connection, userId, checkTradeId(connection, tradingId));
                deleteTrading(connection, tradingId);
                return "Trading deal successfully deleted";
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed: " + e);
        }
    }

    private String checkTradeId(Connection connection, String tradingId) throws SQLException {
        PreparedStatement selectStmt = connection.prepareStatement(SELECT_WITH_TRADING_ID);
        selectStmt.setString(1, tradingId);
        ResultSet rs = selectStmt.executeQuery();
        if (!rs.next()) throw new NotFoundException("The provided deal ID was not found.");
        return rs.getString("card_to_trade");
    }

    private void checkDelete(Connection connection, int userId, String card_id) throws SQLException {
        PreparedStatement selectStmt = connection.prepareStatement(SELECT_TRADING_CARD_OWNER);
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
                int userId = authorizeUser(username);
                checkTradeId(connection, tradingId);
                selfTrade(connection, tradingId, userId);
                ownedByUser(connection, tradingId, cardId, userId);
                lockedCard(connection, cardId);
                trading(connection, tradingId, cardId, userId);
                return "Trading deal successfully executed.";
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed: " + e);
        }
    }

    private void trading(Connection connection, String tradingId, String cardId, int user_id) throws SQLException {
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
        PreparedStatement deleteStmt = connection.prepareStatement(DELETE_TRADING);
        deleteStmt.setString(1, tradingId);
        deleteStmt.executeUpdate();
        deleteStmt.close();
    }

    private void ownedByUser(Connection connection, String tradingId, String cardId, int userId) throws SQLException {
        System.out.println(cardId + " " + userId);
        PreparedStatement selectStmt = connection.prepareStatement(SELECT_TRADING_CARD_OWNER);
        selectStmt.setString(1, cardId);
        selectStmt.setInt(2, userId);
        ResultSet rs = selectStmt.executeQuery();
        if (!rs.next()) throw new ForbiddenException("The offered card is not owned by the user!");
        minRequirements(connection, tradingId, rs);
    }

    private void minRequirements(Connection connection, String tradingId, ResultSet cardResult) throws SQLException {
        PreparedStatement selectStmt = connection.prepareStatement(JOIN_SELECT_MINIMUM_REQ);
        selectStmt.setString(1, tradingId);
        ResultSet tradingResult = selectStmt.executeQuery();
        tradingResult.next();
        if (!tradingResult.getString("card_type").equals(cardResult.getString("card_type")))
            throw new ForbiddenException("The requirements are not met");
        else if (tradingResult.getInt("minimum_damage") > cardResult.getInt("damage"))
            throw new ForbiddenException("The requirements are not met");
    }

    private void lockedCard(Connection connection, String cardId) throws SQLException {
        PreparedStatement selectStmt = connection.prepareStatement(TRADING_LOCKED_CARD);
        selectStmt.setString(1, cardId);
        ResultSet rs = selectStmt.executeQuery();
        if (rs.next()) throw new ForbiddenException("The offered card is locked in a deck!");
    }

    private void selfTrade(Connection connection, String tradingId, int userId) throws SQLException {
        PreparedStatement selectStmt = connection.prepareStatement(TRADING_SELF_TRADE);
        selectStmt.setString(1, tradingId);
        ResultSet rs = selectStmt.executeQuery();
        rs.next();
        if (rs.getInt("user_id_fk") == userId) throw new ForbiddenException("The offered card to trade is owned by the user!");
    }
}
