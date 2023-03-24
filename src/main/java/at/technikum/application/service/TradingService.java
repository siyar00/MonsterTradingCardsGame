package at.technikum.application.service;

import at.technikum.application.model.Trading;
import at.technikum.application.repository.trading.TradingRepository;
import at.technikum.http.Response;

public record TradingService(TradingRepository tradingRepository) {
    public Response showCurrentlyAvailableTradings(String username) {
        return tradingRepository.showAllTradings(username);
    }

    public String createsNewTradingDeal(Trading trading, String username) {
        return tradingRepository.createNewTradingDeal(trading, username);
    }

    public String deletesExistingTradingDeal(String username, String tradingId) {
        return tradingRepository.deleteTradingDeal(username, tradingId);
    }

    public String startTrading(String username, String tradingId, String cardId) {
        return tradingRepository.trade(username, tradingId, cardId);
    }
}
