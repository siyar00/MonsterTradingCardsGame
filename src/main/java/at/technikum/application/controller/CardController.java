package at.technikum.application.controller;

import at.technikum.application.config.DataSource;
import at.technikum.application.repository.cards.CardsRepositoryImpl;
import at.technikum.application.router.Controller;
import at.technikum.application.router.Route;
import at.technikum.application.router.RouteIdentifier;
import at.technikum.application.service.CardService;
import at.technikum.application.util.Authorization;
import at.technikum.application.util.Headers;
import at.technikum.application.util.Pair;
import at.technikum.http.HttpStatus;
import at.technikum.http.RequestContext;
import at.technikum.http.Response;
import at.technikum.http.exceptions.BadRequestException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardController implements Controller {

    private final CardService cardService;

    public CardController(DataSource dataSource) {
        this.cardService = new CardService(new CardsRepositoryImpl(dataSource));
    }

    private Response readUserCards(RequestContext requestContext) {
        String username = new Authorization().isAuthorized(requestContext);
        return cardService.showAllUserCards(username);
    }

    private Response readUserDeck(RequestContext requestContext) {
        String username = new Authorization().isAuthorized(requestContext);
        Response response = cardService.showCurrentUserDeck(username);
        if (requestContext.getFormat() != null)
            if (requestContext.getFormat().equals("plain"))
                response.setHeaders(Collections.singletonList(Headers.CONTENT_TYPE_TEXT));
        return response;
    }

    private Response updateUserDeck(RequestContext requestContext) {
        String username = new Authorization().isAuthorized(requestContext);
        new Authorization().noBody(requestContext);
        String[] cardIds = requestContext.getBodyAs(String[].class);
        if (cardIds.length != 4)
            throw new BadRequestException("The provided deck did not include the required amount of cards");
        return new Response(HttpStatus.OK, cardService.configureUserDeckWith4Cards(username, List.of(cardIds)), Headers.CONTENT_TYPE_TEXT);
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
