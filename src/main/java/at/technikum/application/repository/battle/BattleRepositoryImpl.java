package at.technikum.application.repository.battle;

import at.technikum.application.config.DbConnector;
import at.technikum.application.model.CardRec;
import at.technikum.application.repository.Repository;
import at.technikum.application.util.Headers;
import at.technikum.http.HttpStatus;
import at.technikum.http.Response;
import at.technikum.http.exceptions.BadRequestException;
import at.technikum.http.exceptions.ForbiddenException;
import at.technikum.http.exceptions.NotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class BattleRepositoryImpl extends Repository implements BattleRepository {

    private static final String UPDATE_USER_STATS = """
            UPDATE users SET wins = wins + 1, played = played + 1, elo = elo + 5 WHERE username = ?;
            UPDATE users SET losses = losses + 1, played = played + 1, elo = elo - 3 WHERE username = ?;
            """;
    private static final String UPDATE_USER_STATS_DRAW = """
            UPDATE users SET draws = draws + 1, played = played + 1 WHERE username IN (?,?)
            """;

    private static final StringBuilder battleLog = new StringBuilder();
    private static boolean BATTLE_START = false;
    private static final List<String> WAITING_ROOM = new ArrayList<>();
    private static final Map<String, List<CardRec>> USER_DECK_LISTS = new HashMap<>();

    public BattleRepositoryImpl(DbConnector connector) {
        super(connector);
    }

    private static final Map<String, String> SPECIALITIES = new HashMap<>() {{
        put("Dragon", "Goblin");
        put("Wizard", "Ork");
        put("WaterSpell", "Knight");
        put("Kraken", "Spell");
    }};

    private static final Map<String, String> SPECIAL_LOG = new HashMap<>() {{
        put("Dragon", "The Goblin is too afraid to attack the Dragon.\n");
        put("Wizard", "Because the Wizard is controlling the Ork, the Ork can't deal any damage.\n");
        put("WaterSpell", "The heavy armor of the Knight made him drown in the water.\n");
        put("Kraken", "The Kraken is immune to every Spell\n");
        put("FireElf", "Because the FireElves have known the Dragons since they were little, the FireElf easily evades all attacks\n");
    }};

    private enum GAME {
        WIN_PLAYER1, WIN_PLAYER2, DRAW
    }

    @Override
    public Response startBattle(String username) {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            try {
                int userId = authorizeUser(username).getInt(USER_ID);
                Response deck = getDeck(connection, userId, username);
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

    private Response getDeck(Connection connection, int userId, String username) throws SQLException, JsonProcessingException {
        PreparedStatement selectPackageStmt = connection.prepareStatement(READ_DECK);
        selectPackageStmt.setInt(1, userId);
        ResultSet set = selectPackageStmt.executeQuery();
        if (!set.next())
            return new Response(HttpStatus.NO_CONTENT, "The request was fine, but the deck doesn't have any cards");
        return convertResultToJSON(selectPackageStmt, set, username);
    }

    private Response convertResultToJSON(PreparedStatement selectPackageStmt, ResultSet rs, String username) throws SQLException, JsonProcessingException {
        final List<CardRec> cardList = new ArrayList<>();
        do {
            cardList.add(new CardRec(username, rs.getString("name"), rs.getDouble("damage"), rs.getString("card_type"), rs.getString("element_type")));
        } while (rs.next());
        selectPackageStmt.close();
        return new Response(HttpStatus.OK, new ObjectMapper().writeValueAsString(cardList), Headers.CONTENT_TYPE_JSON);
    }

    private void changeStats(String user1, String user2, GAME result) {
        if (result == null) throw new IllegalStateException("Could not find a winner!");
        if (GAME.DRAW == result) {
            try (Connection connection = connector.getConnection()) {
                assert connection != null;
                try (PreparedStatement updateStmt = connection.prepareStatement(UPDATE_USER_STATS_DRAW)) {
                    updateStmt.setString(1, user1);
                    updateStmt.setString(2, user2);
                    updateStmt.executeUpdate();
                } finally {
                    connection.close();
                }
            } catch (SQLException e) {
                throw new IllegalStateException("DB query failed: " + e);
            }
        } else {
            try (Connection connection = connector.getConnection()) {
                assert connection != null;
                try (PreparedStatement updateStmt = connection.prepareStatement(UPDATE_USER_STATS)) {
                    if (GAME.WIN_PLAYER1 == result) {
                        updateStmt.setString(1, user1);
                        updateStmt.setString(2, user2);
                    } else {
                        updateStmt.setString(1, user2);
                        updateStmt.setString(2, user1);
                    }
                    updateStmt.executeUpdate();
                } finally {
                    connection.close();
                }
            } catch (SQLException e) {
                throw new IllegalStateException("DB query failed: " + e);
            }
        }
    }

    private synchronized Response battle(String username, List<CardRec> userDeck) {
        String user1, user2;
        WAITING_ROOM.add(username);
        if (WAITING_ROOM.size() % 2 == 0) {
            user1 = WAITING_ROOM.get(0);
            user2 = WAITING_ROOM.get(1);
            if (user1.equals(user2)) {
                WAITING_ROOM.remove(1);
                throw new BadRequestException("No fighting with yourself!");
            }
            WAITING_ROOM.remove(1);
            USER_DECK_LISTS.put(username, userDeck);
            fight(user1, user2);
            BATTLE_START = true;
            notify();
            return new Response(HttpStatus.OK, battleLog.toString());
        } else {
            USER_DECK_LISTS.put(username, userDeck);
            waiting();
            WAITING_ROOM.clear();
            if (BATTLE_START) {
                BATTLE_START = false;
                return new Response(HttpStatus.OK, battleLog.toString());
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

    private void fight(String user1, String user2) {
        List<CardRec> firstPlayerDeck = new ArrayList<>(USER_DECK_LISTS.get(user1));
        List<CardRec> secondPlayerDeck = new ArrayList<>(USER_DECK_LISTS.get(user2));
        GAME result = null;
        USER_DECK_LISTS.clear();
        battleLog.setLength(0);
        for (int i = 0; i < 100; i++) {
            CardRec firstPlayerCard = firstPlayerDeck.get(ThreadLocalRandom.current().nextInt(firstPlayerDeck.size()));
            CardRec secondPlayerCard = secondPlayerDeck.get(ThreadLocalRandom.current().nextInt(secondPlayerDeck.size()));
            if (firstPlayerCard.name().equals("Dragon") && secondPlayerCard.name().equals("FireElf")) {
                logger(firstPlayerCard, secondPlayerCard, SPECIAL_LOG.get("FireElf"));
            } else if (SPECIALITIES.containsKey(firstPlayerCard.name()) && secondPlayerCard.name().contains(SPECIALITIES.get(firstPlayerCard.name()))) {
                result = GAME.WIN_PLAYER1;
                logger(firstPlayerCard, secondPlayerCard, SPECIAL_LOG.get(firstPlayerCard.name()));
            } else if (SPECIALITIES.containsKey(secondPlayerCard.name()) && firstPlayerCard.name().contains(SPECIALITIES.get(secondPlayerCard.name()))) {
                result = GAME.WIN_PLAYER2;
                logger(firstPlayerCard, secondPlayerCard, SPECIAL_LOG.get(secondPlayerCard.name()));
            } else if (firstPlayerCard.monster() && secondPlayerCard.monster()) {
                result = normalFight(firstPlayerCard, secondPlayerCard);
            } else {
                result = elementFight(firstPlayerCard, secondPlayerCard);
            }

            if (result == GAME.WIN_PLAYER1) {
                firstPlayerDeck.add(secondPlayerCard);
                secondPlayerDeck.remove(secondPlayerCard);
            } else if (result == GAME.WIN_PLAYER2) {
                secondPlayerDeck.add(firstPlayerCard);
                firstPlayerDeck.remove(firstPlayerCard);
            }

            if (firstPlayerDeck.isEmpty()) {
                result = GAME.WIN_PLAYER2;
                break;
            } else if (secondPlayerDeck.isEmpty()) {
                result = GAME.WIN_PLAYER1;
                break;
            } else {
                result = GAME.DRAW;
            }
        }
        changeStats(user1, user2, result);
        if (firstPlayerDeck.isEmpty()) battleLog.append(user2.toUpperCase()).append(" WON!\n");
        else if (secondPlayerDeck.isEmpty()) battleLog.append(user1.toUpperCase()).append(" WON!\n");
        else battleLog.append("\nSorry it is a DRAW! Maybe play again.");
    }

    private void logger(CardRec firstPlayerCard, CardRec secondPlayerCard, String specialLog) {
        battleLog.append(firstPlayerCard.USER()).append(": ").append(firstPlayerCard.name()).append(" (")
                .append(firstPlayerCard.damage()).append(" Damage) VS ").append(secondPlayerCard.USER()).append(": ")
                .append(secondPlayerCard.name()).append(" (").append(secondPlayerCard.damage()).append(" Damage) => ")
                .append(specialLog);
    }

    private void logger(CardRec firstPlayerCard, CardRec secondPlayerCard, GAME game) {
        battleLog.append(firstPlayerCard.USER()).append(": ").append(firstPlayerCard.name()).append(" (")
                .append(firstPlayerCard.damage()).append(" Damage) VS ").append(secondPlayerCard.USER()).append(": ")
                .append(secondPlayerCard.name()).append(" (").append(secondPlayerCard.damage()).append(" Damage) => ");
        if (GAME.WIN_PLAYER1 == game)
            battleLog.append(firstPlayerCard.name()).append(" defeats ").append(secondPlayerCard.name()).append("\n");
        else if (GAME.WIN_PLAYER2 == game)
            battleLog.append(secondPlayerCard.name()).append(" defeats ").append(firstPlayerCard.name()).append("\n");
        else battleLog.append("Draw (no action)\n");
    }

    private void logger(CardRec firstPlayerCard, CardRec secondPlayerCard, Double damage, Double damage1, GAME game) {
        battleLog.append(firstPlayerCard.USER()).append(": ").append(firstPlayerCard.name()).append(" (")
                .append(firstPlayerCard.damage()).append(" Damage) VS ").append(secondPlayerCard.USER()).append(": ")
                .append(secondPlayerCard.name()).append(" (").append(secondPlayerCard.damage()).append(" Damage) => ")
                .append(damage).append(" VS ").append(damage1).append(" => ");
        if (GAME.WIN_PLAYER1 == game) battleLog.append(firstPlayerCard.name()).append(" wins\n");
        else if (GAME.WIN_PLAYER2 == game)
            battleLog.append(secondPlayerCard.name()).append(" wins\n");
        else battleLog.append("Draw (no action)\n");
    }

    private GAME elementFight(CardRec firsPlayerCard, CardRec secondPlayerCard) {
        if (firsPlayerCard.elementType().equals(secondPlayerCard.elementType())) {
            return normalFight(firsPlayerCard, secondPlayerCard);
        }
        return switch (firsPlayerCard.elementType()) {
            case "water" -> switch (secondPlayerCard.elementType()) {
                case "fire" -> proFirstPlayer(firsPlayerCard, secondPlayerCard);
                case "normal" -> proSecondPlayer(firsPlayerCard, secondPlayerCard);
                default -> throw new ForbiddenException("This card is forbidden!");
            };
            case "fire" -> switch (secondPlayerCard.elementType()) {
                case "water" -> proFirstPlayer(firsPlayerCard, secondPlayerCard);
                case "normal" -> proSecondPlayer(firsPlayerCard, secondPlayerCard);
                default -> throw new ForbiddenException("This card is forbidden!");
            };
            case "normal" -> switch (secondPlayerCard.elementType()) {
                case "water" -> proFirstPlayer(firsPlayerCard, secondPlayerCard);
                case "fire" -> proSecondPlayer(firsPlayerCard, secondPlayerCard);
                default -> throw new ForbiddenException("This card is forbidden!");
            };
            default -> throw new ForbiddenException("This card is forbidden!");
        };
    }

    private GAME proFirstPlayer(CardRec firstPlayerCard, CardRec secondPlayerCard) {
        double damage = firstPlayerCard.damage() * 2;
        double damage1 = secondPlayerCard.damage() / 2;
        GAME game = GAME.DRAW;
        if (damage > damage1) game = GAME.WIN_PLAYER1;
        else if (damage < damage1) game = GAME.WIN_PLAYER2;
        logger(firstPlayerCard, secondPlayerCard, damage, damage1, game);
        return game;
    }

    private GAME proSecondPlayer(CardRec firstPlayerCard, CardRec secondPlayerCard) {
        double damage = firstPlayerCard.damage() / 2;
        double damage1 = secondPlayerCard.damage() * 2;
        GAME game = GAME.DRAW;
        if (damage > damage1) game = GAME.WIN_PLAYER1;
        else if (damage < damage1) game = GAME.WIN_PLAYER2;
        logger(firstPlayerCard, secondPlayerCard, damage, damage1, game);
        return game;
    }

    private GAME normalFight(CardRec firstPlayerCard, CardRec secondPlayerCard) {
        GAME game = GAME.DRAW;
        if (firstPlayerCard.damage() > secondPlayerCard.damage()) game = GAME.WIN_PLAYER1;
        else if (firstPlayerCard.damage() < secondPlayerCard.damage()) game = GAME.WIN_PLAYER2;
        logger(firstPlayerCard, secondPlayerCard, game);
        return game;
    }
}