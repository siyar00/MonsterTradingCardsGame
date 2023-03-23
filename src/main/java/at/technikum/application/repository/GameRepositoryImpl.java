package at.technikum.application.repository;

import at.technikum.application.config.DbConnector;
import at.technikum.application.model.CardRec;
import at.technikum.application.model.UserStats;
import at.technikum.application.util.Authorization;
import at.technikum.application.util.Headers;
import at.technikum.http.HttpStatus;
import at.technikum.http.Response;
import at.technikum.http.exceptions.BadRequestException;
import at.technikum.http.exceptions.NotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static at.technikum.application.repository.CardsRepositoryImpl.READ_DECK;

public class GameRepositoryImpl implements GameRepository {

    private final DbConnector connector;
    private static String battleLog = "";
    private static boolean battleStart = false;
    private static final List<String> waitingRoom = new ArrayList<>();
    private static final Map<String, List<CardRec>> userDeckLists = new HashMap<>();

    public GameRepositoryImpl(DbConnector connector) {
        this.connector = connector;
    }

    @Override
    public String readStats(String username) {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            try {
                new Authorization().authorizeUser(username, connection);
                PreparedStatement selectStmt = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
                selectStmt.setString(1, username);
                ResultSet rs = selectStmt.executeQuery();
                rs.next();
                UserStats result = UserStats.builder().name(rs.getString("name") == null ? "NO_NAME" : rs.getString("name"))
                        .elo(rs.getInt("elo"))
                        .wins(rs.getInt("wins"))
                        .losses(rs.getInt("losses")).build();
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
                new Authorization().authorizeUser(username, connection);
                PreparedStatement selectStmt = connection.prepareStatement("SELECT * FROM users ORDER BY elo DESC, wins DESC, losses DESC, played DESC, username DESC");
                ResultSet rs = selectStmt.executeQuery();
                List<UserStats> userStats = new ArrayList<>();
                while (rs.next())
                    userStats.add(UserStats.builder()
                            .name(rs.getString("name") == null ? "NO_NAME" : rs.getString("name"))
                            .elo(rs.getInt("elo"))
                            .wins(rs.getInt("wins"))
                            .losses(rs.getInt("losses")).build());
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

    @Override
    public Response startBattle(String username) {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            try {
                ResultSet rs = new Authorization().authorizeUser(username, connection);
                Response deck = getDeck(connection, rs.getInt("user_id"));
                if (deck.getHttpStatus().equals(HttpStatus.OK)) {
                    List<CardRec> temp = List.of(new ObjectMapper().readValue(deck.getBody(), CardRec[].class));
                    connection.close();
                    return battle(username, temp);
                }
                return deck;
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
        if (!set.next())
            return new Response(HttpStatus.NO_CONTENT, "The request was fine, but the deck doesn't have any cards");
        return convertResultToJSON(selectPackageStmt, set);
    }

    private Response convertResultToJSON(PreparedStatement selectPackageStmt, ResultSet rs) throws SQLException, JsonProcessingException {
        final List<CardRec> cardList = new ArrayList<>();
        do {
            cardList.add(new CardRec(rs.getString("name"), rs.getDouble("damage"), rs.getString("card_type"), rs.getString("element_type")));
        } while (rs.next());
        selectPackageStmt.close();
        return new Response(HttpStatus.OK, new ObjectMapper().writeValueAsString(cardList), Headers.CONTENT_TYPE_JSON);
    }

    private synchronized Response battle(String username, List<CardRec> userDeck) {
        String user1, user2;
        waitingRoom.add(username);
        if (waitingRoom.size() % 2 == 0) {
            user1 = waitingRoom.get(0);
            user2 = waitingRoom.get(1);
            if (user1.equals(user2)) {
                waitingRoom.remove(1);
                throw new BadRequestException("No fighting with yourself!");
            }
            waitingRoom.clear();
            userDeckLists.put(username, userDeck);
            battleLog = fight(user1, user2);
            battleStart = true;
            notify();
            return new Response(HttpStatus.OK, battleLog);
        } else {
            userDeckLists.put(username, userDeck);
            waiting();
            if (battleStart) {
                battleStart = false;
                return new Response(HttpStatus.OK, battleLog);
            } else {
                throw new NotFoundException("No opponent available!");
            }
        }
    }

    private void waiting() {
        System.out.println("Waiting...\n");
        try {
            wait();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println(e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    private String fight(String user1, String user2) {
        List<CardRec> firstPlayerDeck = GameRepositoryImpl.userDeckLists.get(user1);
        List<CardRec> secondPlayerDeck = GameRepositoryImpl.userDeckLists.get(user2);
        userDeckLists.clear();

        for (int i = 0; i < 100; i++) {
            if(firstPlayerDeck.get(0).cardType().equals(secondPlayerDeck.get(0).cardType())){
                monsterFight();
            }
        }

        return "FIGHT";
    }

    private void monsterFight() {
    }
}
