package at.technikum.application.repository;

import at.technikum.application.config.DbConnector;
import at.technikum.application.model.Card;
import at.technikum.application.util.Headers;
import at.technikum.http.HttpStatus;
import at.technikum.http.Response;
import at.technikum.http.exceptions.BadRequestException;
import at.technikum.http.exceptions.ForbiddenException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CardsRepositoryImpl extends Repository implements CardsRepository {

    public CardsRepositoryImpl(@NotNull DbConnector connector) {
        super(connector);
    }

    @Override
    public Response showUserCards(String username) {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            try {
                int userId = authorizeUser(username).getInt(USER_ID);
                return getAllCardsFromUser(connection, userId);
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed: " + e);
        } catch (JsonProcessingException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private Response getAllCardsFromUser(Connection connection, int userId) throws SQLException, JsonProcessingException {
        PreparedStatement selectPackageStmt = connection.prepareStatement(SELECT_CARDS);
        selectPackageStmt.setInt(1, userId);
        ResultSet set = selectPackageStmt.executeQuery();
        if (!set.next())
            return new Response(HttpStatus.NO_CONTENT, "The request was fine, but the user doesn't have any cards");
        return convertResultToJSON(selectPackageStmt, set);
    }

    @Override
    public Response showUserDeck(String username) {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            try {
                int userId = authorizeUser(username).getInt(USER_ID);
                return getDeck(connection, userId);
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed: " + e);
        } catch (JsonProcessingException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private Response getDeck(Connection connection, int userId) throws SQLException, JsonProcessingException {
        PreparedStatement selectPackageStmt = connection.prepareStatement(READ_DECK);
        selectPackageStmt.setInt(1, userId);
        ResultSet set = selectPackageStmt.executeQuery();
        if (!set.next()) return new Response(HttpStatus.NO_CONTENT, "The request was fine, but the deck doesn't have any cards");
        return convertResultToJSON(selectPackageStmt, set);
    }

    private Response convertResultToJSON(PreparedStatement selectPackageStmt, ResultSet set) throws SQLException, JsonProcessingException {
        List<Card> cardList = new ArrayList<>();
        do {
            cardList.add(Card.builder()
                    .id(set.getString("card_id"))
                    .name(set.getString("name"))
                    .damage(set.getDouble("damage")).build());
        } while (set.next());
        selectPackageStmt.close();
        return new Response(HttpStatus.OK, new ObjectMapper().writeValueAsString(cardList), Headers.CONTENT_TYPE_JSON);
    }

    @Override
    public String configureUserDeck(String username, List<String> cardIds) {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            try {
                int userId = authorizeUser(username).getInt(USER_ID);
                cardsAvailable(connection, userId, cardIds);
                return updateDeck(connection, userId, cardIds);
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed: " + e);
        }
    }

    private void cardsAvailable(Connection connection, int userId, List<String> cardIds) throws SQLException {
        PreparedStatement selectStmt = connection.prepareStatement(CARDS_AVAILABILITY);
        int count = 0;
        selectStmt.setInt(++count, userId);
        for (String cardId : cardIds) selectStmt.setString(++count, cardId);
        ResultSet rs = selectStmt.executeQuery();
        if (!rs.next()) throw new SQLException("Could not find any results");
        if (rs.getInt("rowNr") != 4)
            throw new ForbiddenException("At least one of the provided cards does not belong to the user or is not available.");
    }

    private String updateDeck(Connection connection, int userId, List<String> cardIds) throws SQLException {
        PreparedStatement updateCardsStmt = connection.prepareStatement(UPDATE_DECK);
        int count = 0;
        for (String cardId : cardIds) updateCardsStmt.setString(++count, cardId);
        updateCardsStmt.setInt(++count, userId);
        updateCardsStmt.executeUpdate();
        updateCardsStmt.close();
        return "The deck has been successfully configured";
    }
}
