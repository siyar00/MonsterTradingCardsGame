package at.technikum.application.controller.trading;

import at.technikum.application.config.DataSource;
import at.technikum.application.router.Controller;
import at.technikum.application.router.Route;
import at.technikum.application.router.RouteIdentifier;
import at.technikum.application.service.TradingService;
import at.technikum.application.util.Pair;
import at.technikum.http.RequestContext;
import at.technikum.http.Response;

import java.util.ArrayList;
import java.util.List;

public class TradingController implements Controller {

    TradingService tradingService;

    public TradingController(DataSource dataSource){}

    private Response readTradingDeals(RequestContext requestContext) {
        return null;
    }

    private Response createTradingDeal(RequestContext requestContext) {
        return null;
    }

    private Response deleteTradingDeal(RequestContext requestContext) {
        return null;
    }

    private Response startTrading(RequestContext requestContext) {
        return null;
    }

    @Override
    public List<Pair<RouteIdentifier, Route>> listRoutes() {
        List<Pair<RouteIdentifier, Route>> routes = new ArrayList<>();
        routes.add(new Pair<>(new RouteIdentifier("/trading", "GET"), this::readTradingDeals));
        routes.add(new Pair<>(new RouteIdentifier("/tradings", "POST"), this::createTradingDeal));
        routes.add(new Pair<>(new RouteIdentifier("/tradings/", "DELETE"), this::deleteTradingDeal));
        routes.add(new Pair<>(new RouteIdentifier("/tradings/", "POST"), this::startTrading));
        return routes;
    }

}
