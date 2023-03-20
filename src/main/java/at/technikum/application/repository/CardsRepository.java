package at.technikum.application.repository;

import at.technikum.http.Response;

import java.util.List;

public interface CardsRepository {
    Response showUserCards(String username);
    Response showUserDeck(String username);
    String configureUserDeck(String username, List<String> cardIds);
}
