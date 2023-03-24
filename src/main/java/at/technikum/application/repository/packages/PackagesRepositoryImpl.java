package at.technikum.application.repository.packages;

import at.technikum.application.config.DbConnector;
import at.technikum.application.model.Card;
import at.technikum.application.repository.Repository;
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

public class PackagesRepositoryImpl extends Repository implements PackagesRepository {

    public PackagesRepositoryImpl(@NotNull DbConnector connector) {
        super(connector);
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
        for (Card ignored : cardList)
            stmt.append("(?,?,?,?,?,(SELECT package_id FROM packages ORDER BY package_id DESC LIMIT 1)), ");
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
        if (name.contains("Water")) return "water";
        else if (name.contains("Fire")) return "fire";
        else if (name.contains("Regular")) return "normal";
        else return "normal";
    }

    @Override
    public String acquirePackages(String username) {
        try (Connection connection = connector.getConnection()) {
            assert connection != null;
            try {
                int userId = existingUser(username);
                return sellPackage(connection, userId);
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed: " + e);
        } catch (JsonProcessingException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private int existingUser(String username) throws SQLException {
        ResultSet rs = authorizeUser(username);
        if (rs.getInt("coins") < 5) throw new ForbiddenException("Not enough money for buying a card package");
        return rs.getInt(USER_ID);
    }

    private String sellPackage(Connection connection, int userId) throws SQLException, JsonProcessingException {
        PreparedStatement updatePackageStmt = connection.prepareStatement(UPDATE_PACKAGES);
        ResultSet rs = updatePackageStmt.executeQuery();
        if (!rs.next()) throw new NotFoundException("No card package available for buying");
        int package_id = rs.getInt("package_id");
        updatePackageStmt.close();

        PreparedStatement updateUserCards = connection.prepareStatement(UPDATE_USER_CARDS);
        updateUserCards.setInt(1, userId);
        updateUserCards.setInt(2, userId);
        updateUserCards.setInt(3, package_id);
        ResultSet set = updateUserCards.executeQuery();
        List<Card> cardList = new ArrayList<>();
        while (set.next()) {
            cardList.add(Card.builder()
                    .id(set.getString("card_id"))
                    .name(set.getString("name"))
                    .damage(set.getDouble("damage")).build());
        }
        updateUserCards.close();
        return new ObjectMapper().writeValueAsString(cardList);
    }

}
