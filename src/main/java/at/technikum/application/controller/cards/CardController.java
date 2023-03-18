package at.technikum.application.controller.cards;

import at.technikum.application.config.DataSource;
import at.technikum.application.router.Controller;
import at.technikum.application.router.Route;
import at.technikum.application.router.RouteIdentifier;
import at.technikum.application.service.CardService;
import at.technikum.application.util.Pair;
import at.technikum.http.RequestContext;
import at.technikum.http.Response;

import java.util.ArrayList;
import java.util.List;

public class CardController implements Controller {

    private CardService cardService;

    public CardController(DataSource dataSource) {
        //this.cardService = new CardService(new CardRepository(dataSource));
    }

    private Response readUserCards(RequestContext requestContext) {
        return null;
    }

    private Response readUserDeck(RequestContext requestContext) {
        return null;
    }

    private Response updateUserDeck(RequestContext requestContext) {
        return null;
    }

    @Override
    public List<Pair<RouteIdentifier, Route>> listRoutes() {
        List<Pair<RouteIdentifier, Route>> routes = new ArrayList<>();
        routes.add(new Pair<>(new RouteIdentifier("/cards", "GET"), this::readUserCards));
        routes.add(new Pair<>(new RouteIdentifier("/deck", "GET"), this::readUserDeck));
        routes.add(new Pair<>(new RouteIdentifier("/deck", "PUT"), this::updateUserDeck));
        return routes;
    }

}
