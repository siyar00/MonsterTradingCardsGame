package at.technikum.application.router;

import at.technikum.application.config.DataSource;
import at.technikum.application.controller.cards.CardController;
import at.technikum.application.controller.game.GameController;
import at.technikum.application.controller.packages.PackageController;
import at.technikum.application.controller.trading.TradingController;
import at.technikum.application.controller.users.UserController;
import at.technikum.application.util.Pair;

import java.util.HashMap;
import java.util.Map;

public class Router {

    private final Map<RouteIdentifier, Route> routes = new HashMap<>();
    private final static DataSource ds = DataSource.getInstance();

    public Router(){
        new UserController(ds).listRoutes().forEach(this::registerRoute);
        new CardController(ds).listRoutes().forEach(this::registerRoute);
        new GameController(ds).listRoutes().forEach(this::registerRoute);
        new PackageController(ds).listRoutes().forEach(this::registerRoute);
        new TradingController(ds).listRoutes().forEach(this::registerRoute);
    }

    public void registerRoute(Pair<RouteIdentifier, Route> routeIdentifierRoutePair) {
        routes.put(routeIdentifierRoutePair.left(), routeIdentifierRoutePair.right());
    }

    public Route findRoute(RouteIdentifier routeIdentifier) {
        return routes.get(routeIdentifier);
    }

}
