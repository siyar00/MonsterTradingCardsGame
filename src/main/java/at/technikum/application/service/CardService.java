package at.technikum.application.service;

import at.technikum.application.repository.CardsRepository;
import at.technikum.http.Response;

import java.util.List;

public record CardService(CardsRepository cardsRepository) {
    public Response showAllUserCards(String username) {
        return cardsRepository.showUserCards(username);
    }

    public Response showCurrentUserDeck(String username) {
        return cardsRepository.showUserDeck(username);
    }

    public String configureUserDeckWith4Cards(String username, List<String> cardIds) {
        return cardsRepository.configureUserDeck(username, cardIds);
    }
}
