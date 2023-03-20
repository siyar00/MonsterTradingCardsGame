package at.technikum.application.repository;

import at.technikum.application.config.DbConnector;
import at.technikum.application.model.Card;
import at.technikum.application.util.Authorization;
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

public class CardsRepositoryImpl implements CardsRepository {

    private static final String SETUP_TABLE = """
                CREATE TABLE IF NOT EXISTS cards (
                    card_id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    damage NUMERIC NOT NULL CHECK (damage >= 0),
                    card_type TEXT,
                    element_type TEXT,
                    package_id_fk INTEGER DEFAULT NULL REFERENCES packages(package_id) ON DELETE CASCADE,
                    user_id_fk INTEGER DEFAULT NULL REFERENCES users(user_id) ON DELETE SET NULL
                );
                CREATE TABLE IF NOT EXISTS deck (
                    user_id_fk INTEGER REFERENCES users(user_id) ON DELETE CASCADE,
                    card1 TEXT REFERENCES cards(card_id) ON DELETE SET NULL,
                    card2 TEXT REFERENCES cards(card_id) ON DELETE SET NULL,
                    card3 TEXT REFERENCES cards(card_id) ON DELETE SET NULL,
                    card4 TEXT REFERENCES cards(card_id) ON DELETE SET NULL
                );
            """;

    private static final String READ_DECK = """
            SELECT card_id, name, damage FROM cards c JOIN deck d ON c.card_id = d.card1
            OR c.card_id = d.card2 OR c.card_id = d.card3 OR c.card_id = d.card4 WHERE d.user_id_fk = ?;
            """;

    private final DbConnector connector;

    public CardsRepositoryImpl(@NotNull DbConnector connector) {
        this.connector = connector;
        new PackagesRepositoryImpl(connector);
        new UsersRepositoryImpl(connector);
        try (PreparedStatement ps = connector.getConnection().prepareStatement(SETUP_TABLE)) {
            ps.execute();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to setup up table: " + e);
        }
    }

    @Override
    public Response showUserCards(String username) {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            try {
                ResultSet rs = new Authorization().authorizeUser(username, connection);
                return getAllCardsFromUser(connection, rs.getInt("user_id"));
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
        PreparedStatement selectPackageStmt = connection.prepareStatement("SELECT * FROM cards WHERE user_id_fk = ?");
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
                ResultSet rs = new Authorization().authorizeUser(username, connection);
                return getDeck(connection, rs.getInt("user_id"));
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
                ResultSet rs = new Authorization().authorizeUser(username, connection);
                cardsAvailable(connection, rs.getInt("user_id"), cardIds);
                return updateDeck(connection, rs.getInt("user_id"), cardIds);
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed: " + e);
        }
    }

    private void cardsAvailable(Connection connection, int userId, List<String> cardIds) throws SQLException {
        PreparedStatement selectStmt = connection.prepareStatement("SELECT count(*) AS rowNr FROM cards WHERE user_id_fk = ? AND card_id IN (?,?,?,?)");
        int count = 0;
        selectStmt.setInt(++count, userId);
        for (String cardId : cardIds) selectStmt.setString(++count, cardId);
        ResultSet rs = selectStmt.executeQuery();
        if (!rs.next()) throw new SQLException("Could not find any results");
        if (rs.getInt("rowNr") != 4)
            throw new ForbiddenException("At least one of the provided cards does not belong to the user or is not available.");
    }

    private String updateDeck(Connection connection, int userId, List<String> cardIds) throws SQLException {
        PreparedStatement updateCardsStmt = connection.prepareStatement("UPDATE deck SET card1 = ?, card2 = ?, card3 = ?, card4 = ? WHERE user_id_fk = ?");
        int count = 0;
        for (String cardId : cardIds) updateCardsStmt.setString(++count, cardId);
        updateCardsStmt.setInt(++count, userId);
        updateCardsStmt.executeUpdate();
        updateCardsStmt.close();
        return "The deck has been successfully configured";
    }
}
