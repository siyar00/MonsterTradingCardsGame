package at.technikum.application.repository.market;

import at.technikum.application.config.DbConnector;
import at.technikum.application.model.CardMarket;
import at.technikum.application.model.CardSell;
import at.technikum.application.model.ManaCoinStats;
import at.technikum.application.repository.Repository;
import at.technikum.http.exceptions.BadRequestException;
import at.technikum.http.exceptions.ForbiddenException;
import at.technikum.http.exceptions.UnauthorizedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class MarketRepositoryImpl extends Repository implements MarketRepository {
    public MarketRepositoryImpl(@NotNull DbConnector connector) {
        super(connector);
    }

    @Override
    public String manaCoinStats(String username) {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            int userId = authorizeUser(username);
            try (PreparedStatement statement = connection.prepareStatement(GET_COINS_MANA)) {
                statement.setInt(1, userId);
                ResultSet rs = statement.executeQuery();
                rs.next();
                return new ObjectMapper().writeValueAsString(ManaCoinStats.builder().mana(rs.getInt("mana")).coins(rs.getInt("coins")).build());
            } catch (JsonProcessingException e) {
                throw new BadRequestException(e.getMessage());
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed: " + e);
        }
    }

    @Override
    public String changeManaToCoins(String username) {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            int userId = authorizeUser(username);
            try (PreparedStatement updateCoinsStmt = connection.prepareStatement(UPDATE_COINS)) {
                updateCoinsStmt.setInt(1, userId);
                ResultSet rs = updateCoinsStmt.executeQuery();
                rs.next();
                PreparedStatement updateManaStmt = connection.prepareStatement(UPDATE_MANA);
                updateManaStmt.setInt(1, userId);
                ResultSet set = updateManaStmt.executeQuery();
                set.next();
                return new ObjectMapper().writeValueAsString(ManaCoinStats.builder().mana(set.getInt("mana")).coins(rs.getInt("coins")).build());
            } catch (JsonProcessingException e) {
                throw new BadRequestException(e.getMessage());
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed: " + e);
        }
    }

    @Override
    public String sellCard(String username, CardSell card) {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            int userId = authorizeUser(username);
            cardAuthorization(userId, card.getId(), connection);
            try (PreparedStatement updateStmt = connection.prepareStatement(SELL_CARD)) {
                updateStmt.setInt(1, card.getPrice());
                updateStmt.setString(2, card.getId());
                updateStmt.executeUpdate();
                return "Successfully created a sale";
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed: " + e);
        }
    }

    private void cardAuthorization(Integer userId, String cardId, Connection connection) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(CARD_OWNER);
        stmt.setString(1, cardId);
        stmt.setInt(2, userId);
        if (!stmt.executeQuery().next())
            throw new ForbiddenException("The card is not owned by the user or locked in the deck.");
    }

    @Override
    public String buyCard(String username, String cardId) {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            int userId = authorizeUser(username);
            notInMarket(cardId, connection);
            ownCard(userId, cardId, connection);
            enoughMoney(userId, cardId, connection);
            try (PreparedStatement stmt = connection.prepareStatement(BUY_CARD)) {
                stmt.setString(1, cardId);
                stmt.setInt(2, userId);
                stmt.setString(3, cardId);
                stmt.setString(4, cardId);
                stmt.setInt(5, userId);
                stmt.setString(6, cardId);
                stmt.executeUpdate();
                return "Successfully bought a card";
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed: " + e);
        }
    }

    private void notInMarket(String cardId, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(NOT_IN_MARKET);
        statement.setString(1, cardId);
        if (!statement.executeQuery().next()) throw new BadRequestException("Card is not available in the market!");
    }

    private void enoughMoney(int userId, String cardId, Connection connection) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(ENOUGH_MONEY);
        stmt.setString(1, cardId);
        stmt.setInt(2, userId);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        if (rs.getInt("coins") < rs.getInt("price"))
            throw new ForbiddenException("User has not enough money to buy card!");
    }

    private void ownCard(int userId, String cardId, Connection connection) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(OWN_CARD);
        stmt.setString(1, cardId);
        stmt.setInt(2, userId);
        if (stmt.executeQuery().next()) throw new ForbiddenException("The user tries to buy from himself");
    }

    @Override
    public String deleteSale(String username, String cardId) {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            int userId = authorizeUser(username);
            cardAuthorization(userId, cardId, connection);
            try (PreparedStatement updateStmt = connection.prepareStatement(DELETE_CARD_IN_MARKET)) {
                updateStmt.setString(1, cardId);
                updateStmt.executeUpdate();
                return "Successfully deleted sale";
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed: " + e);
        }
    }

    @Override
    public String showMarket(String username) {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            authorizeUser(username);
            try (PreparedStatement stmt = connection.prepareStatement(SHOW_MARKET)) {
                ResultSet rs = stmt.executeQuery();
                List<CardMarket> cardMarketList = new ArrayList<>();
                while (rs.next()) {
                    cardMarketList.add(CardMarket.builder()
                            .id(rs.getString("card_id"))
                            .name(rs.getString("name"))
                            .damage(rs.getDouble("damage"))
                            .cardType(rs.getString("card_type"))
                            .elementType(rs.getString("element_type"))
                            .price(rs.getInt("price")).build());
                }
                return new ObjectMapper().writeValueAsString(cardMarketList);
            } catch (JsonProcessingException e) {
                throw new BadRequestException(e.getMessage());
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed: " + e);
        }
    }
}
