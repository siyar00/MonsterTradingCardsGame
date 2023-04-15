package at.technikum.application.repository.market;

import at.technikum.application.model.CardSell;

public interface MarketRepository {
    String manaCoinStats(String username);

    String changeManaToCoins(String username);

    String sellCard(String username, CardSell card);

    String buyCard(String username, String cardId);

    String deleteSale(String username, String cardId);

    String showMarket(String username);
}
