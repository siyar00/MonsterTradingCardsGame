package at.technikum.application.repository;

import at.technikum.application.model.Card;

import java.util.List;

public interface PackagesRepository {
    String createPackages(List<Card> cardList);
    String acquirePackages(String username);
}
