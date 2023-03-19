package at.technikum.application.repository;

import at.technikum.application.config.DbConnector;
import at.technikum.application.model.Card;
import at.technikum.http.exceptions.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
                	soldTo INTEGER REFERENCES users(id) ON DELETE SET NULL
                );
                CREATE TABLE IF NOT EXISTS cards (
                    card_id TEXT NOT NULL,
                    name TEXT NOT NULL,
                    damage NUMERIC NOT NULL CHECK (damage >= 0),
                    package_id INTEGER REFERENCES packages(package_id) ON DELETE CASCADE
                );
            """;

    private static final String INSERT_PACKAGES = """
                    INSERT INTO packages (card1, card2, card3, card4, card5) VALUES (?,?,?,?,?)
            """;
    private static final String UPDATE_PACKAGES = """
                UPDATE packages SET sold_to = ? WHERE package_id =
                (SELECT package_id FROM packages WHERE sold_to IS NULL ORDER BY RANDOM() LIMIT 1)
                RETURNING *
            """;

    private final DbConnector dataSource;
    private int packageId;
    private int userId;

    public PackagesRepositoryImpl(DbConnector dataSource) {
        this.dataSource = dataSource;
        try (PreparedStatement ps = dataSource.getConnection().prepareStatement(SETUP_TABLE)) {
            ps.execute();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to setup up table", e);
        }
    }

    @Override
    public String createPackages(List<Card> cardList) {
        try (Connection connection = dataSource.getConnection()) {
            assert connection != null;
            try {
                existingCards(connection, cardList);
                updatePackagesDB(connection, cardList);
                return updateCardsDB(connection, cardList);
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed", e);
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

        PreparedStatement selectPackageIdStmt = connection.prepareStatement("SELECT package_id FROM packages ORDER BY package_id DESC LIMIT 1");
        ResultSet rs = selectPackageIdStmt.executeQuery();
        if (!rs.next()) throw new NotFoundException("DB could not found package!");
        packageId = rs.getInt("package_id");
        selectPackageIdStmt.close();
    }

    private String updateCardsDB(Connection connection, List<Card> cardList) throws SQLException {
        StringBuilder stmt = new StringBuilder("INSERT INTO cards (card_id, name, damage, package_id) VALUES ");
        for (Card ignored : cardList) stmt.append("(?,?,?,?), ");
        String insertStmt = stmt.substring(0, stmt.toString().lastIndexOf(","));
        PreparedStatement updateCardsStmt = connection.prepareStatement(insertStmt);
        int count = 0;
        for (Card card : cardList) {
            updateCardsStmt.setString(++count, card.getId());
            updateCardsStmt.setString(++count, card.getName());
            updateCardsStmt.setDouble(++count, card.getDamage());
            updateCardsStmt.setInt(++count, packageId);
        }
        updateCardsStmt.executeUpdate();
        updateCardsStmt.close();
        return "Package and cards successfully created";
    }

    @Override
    public String acquirePackages(String username) {
        try (Connection connection = dataSource.getConnection()) {
            assert connection != null;
            try {
                existingUser(connection, username);
                String result = sellPackage(connection);
                updatingUser(connection);
                return result;
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DB query failed", e);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("");
        }
    }

    private void existingUser(Connection connection, String username) throws SQLException {
        PreparedStatement selectUser = connection.prepareStatement("SELECT coins, id FROM users WHERE username = ?");
        selectUser.setString(1, username);
        ResultSet rs = selectUser.executeQuery();
        if (!rs.next()) throw new UnauthorizedException("Access token is missing or invalid");
        if (rs.getInt("coins") < 5) throw new ForbiddenException("Not enough money for buying a card package");
        userId = rs.getInt("id");
        selectUser.close();
    }

    private String sellPackage(Connection connection) throws SQLException, JsonProcessingException {
        PreparedStatement updatePackageStmt = connection.prepareStatement(UPDATE_PACKAGES);
        updatePackageStmt.setInt(1, userId);
        ResultSet rs = updatePackageStmt.executeQuery();
        if (!rs.next()) throw new NotFoundException("No card package available for buying");
        int package_id = rs.getInt("package_id");
        updatePackageStmt.close();

        PreparedStatement selectPackageStmt = connection.prepareStatement("SELECT * FROM cards WHERE package_id = ?");
        selectPackageStmt.setInt(1, package_id);
        ResultSet set = selectPackageStmt.executeQuery();
        List<Card> cardList = new ArrayList<>();
        while(set.next()){
            cardList.add(Card.builder()
                    .id(set.getString("card_id"))
                    .name(set.getString("name"))
                    .damage(set.getDouble("damage")).build());
        }
        selectPackageStmt.close();
        return new ObjectMapper().writeValueAsString(cardList);
    }

    private void updatingUser(Connection connection) throws SQLException {
        PreparedStatement updateUser = connection.prepareStatement("UPDATE users SET coins = (SELECT coins FROM USERS WHERE id = ?) - 5 WHERE id = ?");
        updateUser.setInt(1, userId);
        updateUser.setInt(2, userId);
        updateUser.executeUpdate();
        updateUser.close();
    }

}
