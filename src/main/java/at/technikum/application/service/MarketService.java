package at.technikum.application.service;

import at.technikum.application.model.CardSell;
import at.technikum.application.repository.market.MarketRepository;

public record MarketService(MarketRepository marketRepository) {
    public String currentCoinMana(String username) {
        return marketRepository.manaCoinStats(username);
    }

    public String changeMana(String username) {
        return marketRepository.changeManaToCoins(username);
    }

    public String sellCard(String username, CardSell card) {
        return marketRepository.sellCard(username, card);
    }

    public String buyCard(String username, String cardId) {
        return marketRepository.buyCard(username, cardId);
    }

    public String deleteSale(String username, String cardId) {
        return marketRepository.deleteSale(username, cardId);
    }

    public String showMarket(String username) {
        return marketRepository.showMarket(username);
    }
}
