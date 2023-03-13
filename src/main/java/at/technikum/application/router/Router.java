package at.technikum.application.router;

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

    public Router(){
        new UserController(null).listRoutes().forEach(this::registerRoute);
        new CardController().listRoutes().forEach(this::registerRoute);
        new GameController().listRoutes().forEach(this::registerRoute);
        new PackageController().listRoutes().forEach(this::registerRoute);
        new TradingController().listRoutes().forEach(this::registerRoute);
    }

    public void registerRoute(Pair<RouteIdentifier, Route> routeIdentifierRoutePair) {
        routes.put(routeIdentifierRoutePair.left(), routeIdentifierRoutePair.right());
    }

    public Route findRoute(RouteIdentifier routeIdentifier) {
        return routes.get(routeIdentifier);
    }

}
