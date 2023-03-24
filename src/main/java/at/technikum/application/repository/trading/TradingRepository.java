package at.technikum.application.repository.trading;

import at.technikum.application.model.Trading;
import at.technikum.http.Response;

public interface TradingRepository {
    Response showAllTradings(String username);

    String createNewTradingDeal(Trading trading, String username);

    String deleteTradingDeal(String username, String tradingId);

    String trade(String username, String tradingId, String cardId);
}
