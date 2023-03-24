package at.technikum.application.repository;

import at.technikum.application.config.DbConnector;
import at.technikum.http.exceptions.UnauthorizedException;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Repository {

    protected final DbConnector connector;

    public Repository(@NotNull DbConnector connector) {
        this.connector = connector;
        try (PreparedStatement ps = connector.getConnection().prepareStatement(SETUP_TABLE)) {
            ps.execute();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to setup up table: " + e);
        }
    }

    protected int authorizeUser(String username) {
        try (Connection connection = connector.getConnection()) {
            try (PreparedStatement selectUser = connection.prepareStatement(SELECT_USER)) {
                selectUser.setString(1, username);
                ResultSet rs = selectUser.executeQuery();
                if (!rs.next()) throw new UnauthorizedException("Access token is missing or invalid");
                return rs.getInt(USER_ID);
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed: " + e);
        }
    }

    /**
     * User
     */
    protected static final String USER_ID = "user_id";
    protected static final String SELECT_USER = """
            SELECT * FROM users WHERE username = ?
            """;
    protected static final String QUERY_USER_PASS = """
            SELECT * FROM users WHERE username = ? AND password = ?
            """;
    protected static final String INSERT_USER = """
            INSERT INTO users (username, password) VALUES (?,?) RETURNING *
            """;
    protected static final String UPDATE_USER = """
            UPDATE users SET name = ?, bio = ?, image = ? WHERE username = ?
            """;
    protected static final String INSERT_USERID_DECK = """
            INSERT INTO deck(user_id_fk) VALUES (?)
            """;

    /**
     * Cards
     */
    protected static final String SELECT_CARDS = """
            SELECT * FROM cards WHERE user_id_fk = ?
            """;
    protected static final String READ_DECK = """
            SELECT card_id, name, damage, card_type, element_type FROM cards c JOIN deck d ON c.card_id = d.card1
            OR c.card_id = d.card2 OR c.card_id = d.card3 OR c.card_id = d.card4 WHERE d.user_id_fk = ?;
            """;
    protected static final String CARDS_AVAILABILITY = """
            SELECT count(*) AS rowNr FROM cards WHERE user_id_fk = ? AND card_id IN (?,?,?,?)
            """;
    protected static final String UPDATE_DECK = """
            UPDATE deck SET card1 = ?, card2 = ?, card3 = ?, card4 = ? WHERE user_id_fk = ?
            """;
    protected static final String CHECK_NOT_IN_TRADE = """
            SELECT card_to_trade FROM tradings WHERE card_to_trade IN (?,?,?,?)
            """;

    /**
     * Packages
     */
    protected static final String INSERT_PACKAGES = """
            INSERT INTO packages (card1, card2, card3, card4, card5) VALUES (?,?,?,?,?)
            """;
    protected static final String UPDATE_PACKAGES = """
            UPDATE packages SET sold = true WHERE package_id =
            (SELECT package_id FROM packages WHERE sold = false ORDER BY package_id /*RANDOM()*/ LIMIT 1)
            RETURNING *
            """;
    protected static final String UPDATE_USER_COINS = """
            UPDATE users SET coins = coins - 5 WHERE user_id = ?
            """;
    protected static final String UPDATE_USER_CARDS = """
            UPDATE cards SET user_id_fk = ? WHERE package_id_fk = ? RETURNING *
            """;

    /**
     * Trading
     */
    protected static final String CHECK_CARDS = """
            SELECT * FROM cards c JOIN deck d ON c.user_id_fk = d.user_id_fk
            WHERE c.user_id_fk = ? AND card_id != card1 AND card_id != card2 AND card_id != card3 AND card_id != card4
            AND card_id = ?;
            """;
    protected static final String INSERT_TRADING_DEAL = """
            INSERT INTO tradings(trading_id, card_to_trade, card_type, minimum_damage) VALUES(?,?,?,?)
            """;
    protected static final String TRADE_FOR_SELLER = """
            UPDATE cards SET user_id_fk = (SELECT c.user_id_fk FROM tradings t
            JOIN cards c ON t.card_to_trade = c.card_id WHERE t.trading_id = ?)  WHERE card_id = ?
            """;
    protected static final String TRADE_FOR_BUYER = """
            UPDATE cards SET user_id_fk = ? WHERE card_id = (SELECT card_to_trade FROM tradings WHERE trading_id = ?)
            """;
    protected static final String SELECT_TRADINGS = """
            SELECT trading_id, card_to_trade, card_type, minimum_damage FROM tradings
            """;
    protected static final String SELECT_WITH_TRADING_ID = """
            SELECT * FROM tradings WHERE trading_id = ?
            """;
    protected static final String SELECT_TRADING_CARD_OWNER = """
            SELECT * FROM cards WHERE card_id = ? AND user_id_fk = ?
            """;
    protected static final String JOIN_SELECT_MINIMUM_REQ = """
            SELECT t.card_type, t.minimum_damage FROM tradings t JOIN cards c ON card_to_trade = card_id WHERE trading_id = ?
            """;
    protected static final String TRADING_LOCKED_CARD = """
            SELECT * FROM deck WHERE ? IN (card1, card2, card3, card4)
            """;
    protected static final String TRADING_SELF_TRADE = """
            SELECT c.user_id_fk FROM tradings t JOIN cards c ON t.card_to_trade = c.card_id WHERE t.trading_id = ?
            """;
    protected static final String COIN_CHANGE = """
            UPDATE users SET coins = coins - 1 WHERE user_id = ?;
            UPDATE users SET coins = coins + 1 WHERE user_id = (SELECT c.user_id_fk FROM tradings t JOIN cards c ON t.card_to_trade = c.card_id WHERE t.trading_id = ?);
            """;
    protected static final String DELETE_TRADING = """
            DELETE FROM tradings WHERE trading_id = ?
            """;

    /**
     * Game
     */
    protected static final String READ_SCOREBOARD = """
            SELECT * FROM users ORDER BY elo DESC, wins DESC, losses DESC, played DESC, draws DESC, username DESC
            """;
    protected static final String READ_STATS = """
            SELECT * FROM users WHERE username = ?
            """;
    private static final String SETUP_TABLE = """
                CREATE TABLE IF NOT EXISTS users (
                    user_id SERIAL PRIMARY KEY,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    coins INTEGER NOT NULL DEFAULT 22 CHECK (coins >= 0),
                    name TEXT,
                    bio TEXT,
                    image TEXT,
                    elo INTEGER DEFAULT 100 NOT NULL,
                    wins INTEGER DEFAULT 0 NOT NULL,
                    losses INTEGER DEFAULT 0 NOT NULL,
                    draws INTEGER DEFAULT 0 NOT NULL,
                    played INTEGER DEFAULT 0
                );
                CREATE TABLE IF NOT EXISTS packages (
                	package_id SERIAL PRIMARY KEY,
                	card1 TEXT NOT NULL,
                	card2 TEXT NOT NULL,
                	card3 TEXT NOT NULL,
                	card4 TEXT NOT NULL,
                	card5 TEXT NOT NULL,
                	sold BOOLEAN DEFAULT FALSE NOT NULL
                );
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
                CREATE TABLE IF NOT EXISTS tradings (
                    trading_id TEXT PRIMARY KEY,
                    card_to_trade TEXT REFERENCES cards(card_id) ON DELETE CASCADE,
                    card_type TEXT,
                    element TEXT,
                    minimum_damage NUMERIC
                );
            """;

}
