package at.technikum.application.controller;

import at.technikum.application.config.DataSource;
import at.technikum.application.model.Trading;
import at.technikum.application.repository.trading.TradingRepositoryImpl;
import at.technikum.application.router.Controller;
import at.technikum.application.router.Route;
import at.technikum.application.router.RouteIdentifier;
import at.technikum.application.service.TradingService;
import at.technikum.application.util.Authorization;
import at.technikum.application.util.Pair;
import at.technikum.http.HttpStatus;
import at.technikum.http.RequestContext;
import at.technikum.http.Response;

import java.util.ArrayList;
import java.util.List;

public class TradingController implements Controller {

    private final TradingService tradingService;

    public TradingController(DataSource dataSource){
        this.tradingService = new TradingService(new TradingRepositoryImpl(dataSource));
    }

    private Response readTradingDeals(RequestContext requestContext) {
        String username = new Authorization().isAuthorized(requestContext);
        return tradingService.showCurrentlyAvailableTradings(username);
    }

    private Response createTradingDeal(RequestContext requestContext) {
        String username = new Authorization().isAuthorized(requestContext);
        new Authorization().noBody(requestContext);
        Trading trading = requestContext.getBodyAs(Trading.class);
        return new Response(HttpStatus.CREATED, tradingService.createsNewTradingDeal(trading, username));
    }

    private Response deleteTradingDeal(RequestContext requestContext) {
        String username = new Authorization().isAuthorized(requestContext);
        return new Response(HttpStatus.OK, tradingService.deletesExistingTradingDeal(username, requestContext.getPathVariable()));
    }

    private Response startTrading(RequestContext requestContext) {
        new Authorization().noBody(requestContext);
        String username = new Authorization().isAuthorized(requestContext);
        String cardId = requestContext.getBodyAs(String.class);
        return new Response(HttpStatus.OK, tradingService.startTrading(username, requestContext.getPathVariable(), cardId));
    }

    @Override
    public List<Pair<RouteIdentifier, Route>> listRoutes() {
        List<Pair<RouteIdentifier, Route>> routes = new ArrayList<>();
        routes.add(new Pair<>(new RouteIdentifier("/tradings", "GET"), this::readTradingDeals));
        routes.add(new Pair<>(new RouteIdentifier("/tradings", "POST"), this::createTradingDeal));
        routes.add(new Pair<>(new RouteIdentifier("/tradings/", "DELETE"), this::deleteTradingDeal));
        routes.add(new Pair<>(new RouteIdentifier("/tradings/", "POST"), this::startTrading));
        return routes;
    }

}
