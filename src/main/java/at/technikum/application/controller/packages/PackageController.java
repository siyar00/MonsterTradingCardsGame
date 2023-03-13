package at.technikum.application.controller.packages;

import at.technikum.application.router.Controller;
import at.technikum.application.router.Route;
import at.technikum.application.router.RouteIdentifier;
import at.technikum.application.util.Pair;
import at.technikum.http.RequestContext;
import at.technikum.http.Response;

import java.util.ArrayList;
import java.util.List;

public class PackageController implements Controller {

    private Response acquireCardPackages(RequestContext requestContext) {
        return null;
    }

    private Response createCardPackages(RequestContext requestContext) {
        return null;
    }

    @Override
    public List<Pair<RouteIdentifier, Route>> listRoutes() {
        List<Pair<RouteIdentifier, Route>> routes = new ArrayList<>();
        routes.add(new Pair<>(new RouteIdentifier("/packages", "POST"), this::createCardPackages));
        routes.add(new Pair<>(new RouteIdentifier("/transactions/packages", "POST"), this::acquireCardPackages));
        return routes;
    }

}
