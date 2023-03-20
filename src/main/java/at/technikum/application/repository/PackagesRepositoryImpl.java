package at.technikum.application.repository;

import at.technikum.application.config.DbConnector;
import at.technikum.application.model.Card;
import at.technikum.application.util.Authorization;
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

public class PackagesRepositoryImpl implements PackagesRepository {

    private static final String SETUP_TABLE = """
                CREATE TABLE IF NOT EXISTS packages(
                	package_id SERIAL PRIMARY KEY,
                	card1 TEXT NOT NULL,
                	card2 TEXT NOT NULL,
                	card3 TEXT NOT NULL,
                	card4 TEXT NOT NULL,
                	card5 TEXT NOT NULL,
                	sold BOOLEAN DEFAULT FALSE NOT NULL
                )
            """;

    private static final String INSERT_PACKAGES = """
                    INSERT INTO packages (card1, card2, card3, card4, card5) VALUES (?,?,?,?,?)
            """;
    private static final String UPDATE_PACKAGES = """
                UPDATE packages SET sold = true WHERE package_id =
                (SELECT package_id FROM packages WHERE sold = false ORDER BY RANDOM() LIMIT 1)
                RETURNING *
            """;

    private final DbConnector connector;

    public PackagesRepositoryImpl(@NotNull DbConnector connector) {
        this.connector = connector;
        try (PreparedStatement ps = connector.getConnection().prepareStatement(SETUP_TABLE)) {
            ps.execute();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to setup up table: " + e);
        }
    }

    @Override
    public String createPackages(List<Card> cardList) {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            try {
                existingCards(connection, cardList);
                updatePackagesDB(connection, cardList);
                return updateCardsDB(connection, cardList);
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed: " + e);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Damage should be a number!");
        }
    }

    private void existingCards(Connection connection, List<Card> cardList) throws SQLException {
        StringBuilder tmp = new StringBuilder("SELECT * FROM cards WHERE card_id IN (");
        for (Card ignored : cardList) tmp.append("?,");
        String stmt = tmp.substring(0, tmp.lastIndexOf(",")) + ")";
        PreparedStatement selectStmt = connection.prepareStatement(stmt);
        for (int i = 0; i < cardList.size(); i++)
            selectStmt.setString(i + 1, cardList.get(i).getId());
        ResultSet rs = selectStmt.executeQuery();
        if (rs.next()) throw new ExistingException("At least one card in the packages already exists");
        selectStmt.close();
    }

    private void updatePackagesDB(Connection connection, List<Card> cardList) throws SQLException {
        PreparedStatement updatePackagesStmt = connection.prepareStatement(INSERT_PACKAGES);
        for (int i = 0; i < cardList.size(); i++)
            updatePackagesStmt.setString(i + 1, cardList.get(i).getId());
        updatePackagesStmt.executeUpdate();
        updatePackagesStmt.close();
    }

    private String updateCardsDB(Connection connection, List<Card> cardList) throws SQLException {
        StringBuilder stmt = new StringBuilder("INSERT INTO cards (card_id, name, damage, card_type, element_type, package_id_fk) VALUES ");
        for (Card ignored : cardList) stmt.append("(?,?,?,?,?,(SELECT package_id FROM packages ORDER BY package_id DESC LIMIT 1)), ");
        String insertStmt = stmt.substring(0, stmt.toString().lastIndexOf(","));
        PreparedStatement updateCardsStmt = connection.prepareStatement(insertStmt);
        int count = 0;
        for (Card card : cardList) {
            updateCardsStmt.setString(++count, card.getId());
            updateCardsStmt.setString(++count, card.getName());
            updateCardsStmt.setDouble(++count, card.getDamage());
            updateCardsStmt.setString(++count, card.getName().contains("Spell") ? "spell" : "monster");
            updateCardsStmt.setString(++count, cardType(card.getName()));
        }
        updateCardsStmt.executeUpdate();
        updateCardsStmt.close();
        return "Package and cards successfully created";
    }

    private String cardType(String name) {
        if(name.contains("Water"))
            return "water";
        else if(name.contains("Fire"))
            return "fire";
        else if(name.contains("Regular"))
            return "normal";
        else
            return "normal";
    }

    @Override
    public String acquirePackages(String username) {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            try {
                int userId = existingUser(username, connection);
                String result = sellPackage(connection, userId);
                updatingUser(connection, userId);
                return result;
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed: " + e);
        } catch (JsonProcessingException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private int existingUser(String username, Connection connection) throws SQLException {
        ResultSet rs = new Authorization().authorizeUser(username, connection);
        if (rs.getInt("coins") < 5) throw new ForbiddenException("Not enough money for buying a card package");
        return rs.getInt("user_id");
    }

    private String sellPackage(Connection connection, int userId) throws SQLException, JsonProcessingException {
        PreparedStatement updatePackageStmt = connection.prepareStatement(UPDATE_PACKAGES);
        ResultSet rs = updatePackageStmt.executeQuery();
        if (!rs.next()) throw new NotFoundException("No card package available for buying");
        int package_id = rs.getInt("package_id");
        updatePackageStmt.close();

        PreparedStatement updateCards = connection.prepareStatement("UPDATE cards SET user_id_fk = ? WHERE package_id_fk = ? RETURNING *");
        updateCards.setInt(1, userId);
        updateCards.setInt(2, package_id);
        ResultSet set = updateCards.executeQuery();
        List<Card> cardList = new ArrayList<>();
        while(set.next()){
            cardList.add(Card.builder()
                    .id(set.getString("card_id"))
                    .name(set.getString("name"))
                    .damage(set.getDouble("damage")).build());
        }
        updateCards.close();
        return new ObjectMapper().writeValueAsString(cardList);
    }

    private void updatingUser(Connection connection, int userId) throws SQLException {
        PreparedStatement updateUser = connection.prepareStatement("UPDATE users SET coins = coins - 5 WHERE user_id = ?");
        updateUser.setInt(1, userId);
        updateUser.executeUpdate();
        updateUser.close();
    }

}
