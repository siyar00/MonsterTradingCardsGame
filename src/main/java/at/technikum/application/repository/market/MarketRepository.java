package at.technikum.application.repository.market;

public interface MarketRepository {
    String changeManaToCoins(String username);
    String sellCard(String username);
    String buyCard(String username);
    String deleteSell(String username);
    String showMarket(String username);
    String showStats(String username);
}
