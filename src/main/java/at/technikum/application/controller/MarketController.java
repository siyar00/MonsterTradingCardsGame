package at.technikum.application.controller;

import at.technikum.application.config.DataSource;
import at.technikum.application.model.CardSell;
import at.technikum.application.repository.market.MarketRepositoryImpl;
import at.technikum.application.router.Controller;
import at.technikum.application.router.Route;
import at.technikum.application.router.RouteIdentifier;
import at.technikum.application.service.MarketService;
import at.technikum.application.util.Authorization;
import at.technikum.application.util.Pair;
import at.technikum.http.Headers;
import at.technikum.http.HttpStatus;
import at.technikum.http.RequestContext;
import at.technikum.http.Response;
import at.technikum.http.exceptions.BadRequestException;

import java.util.ArrayList;
import java.util.List;

public class MarketController implements Controller {

    private final MarketService marketService;

    public MarketController(DataSource dataSource) {
        marketService = new MarketService(new MarketRepositoryImpl(dataSource));
    }

    private Response currentCoinMana(RequestContext requestContext){
        String username = new Authorization().isAuthorized(requestContext);
        return new Response(HttpStatus.OK, marketService.currentCoinMana(username), Headers.CONTENT_TYPE_JSON);
    }

    private Response changeManaToCoin(RequestContext requestContext) {
        String username = new Authorization().isAuthorized(requestContext);
        return new Response(HttpStatus.OK, marketService.changeMana(username), Headers.CONTENT_TYPE_JSON);
    }

    private Response sellCard(RequestContext requestContext) {
        new Authorization().noBody(requestContext);
        String username = new Authorization().isAuthorized(requestContext);
        CardSell card = requestContext.getBodyAs(CardSell.class);
        if(card.getPrice() > 5) throw new BadRequestException("Cards can not be sold for more than 5 coins!");
        if(card.getPrice() < 1) throw new BadRequestException("Cards can not be sold for less than 1 coins!");
        return new Response(HttpStatus.CREATED, marketService.sellCard(username, card));
    }

    private Response buyCard(RequestContext requestContext) {
        new Authorization().noPathVariable(requestContext);
        String username = new Authorization().isAuthorized(requestContext);
        return new Response(HttpStatus.OK, marketService.buyCard(username, requestContext.getPathVariable()));
    }

    private Response deleteSale(RequestContext requestContext) {
        new Authorization().noPathVariable(requestContext);
        String username = new Authorization().isAuthorized(requestContext);
        return new Response(HttpStatus.OK, marketService.deleteSale(username, requestContext.getPathVariable()));
    }

    private Response showMarket(RequestContext requestContext) {
        String username = new Authorization().isAuthorized(requestContext);
        return new Response(HttpStatus.OK, marketService.showMarket(username), Headers.CONTENT_TYPE_JSON);
    }

    @Override
    public List<Pair<RouteIdentifier, Route>> listRoutes() {
        List<Pair<RouteIdentifier, Route>> routes = new ArrayList<>();
        routes.add(new Pair<>(new RouteIdentifier("/balance", "GET"), this::currentCoinMana));
        routes.add(new Pair<>(new RouteIdentifier("/manaExchange", "PUT"), this::changeManaToCoin));
        routes.add(new Pair<>(new RouteIdentifier("/sellCard", "POST"), this::sellCard));
        routes.add(new Pair<>(new RouteIdentifier("/buyCard/", "PUT"), this::buyCard));
        routes.add(new Pair<>(new RouteIdentifier("/deleteSale/", "DELETE"), this::deleteSale));
        routes.add(new Pair<>(new RouteIdentifier("/showMarket", "GET"), this::showMarket));
        return routes;
    }
}
