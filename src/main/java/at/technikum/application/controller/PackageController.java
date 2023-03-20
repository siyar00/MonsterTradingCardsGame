package at.technikum.application.controller;

import at.technikum.application.config.DataSource;
import at.technikum.application.model.Card;
import at.technikum.application.repository.PackagesRepositoryImpl;
import at.technikum.application.router.Controller;
import at.technikum.application.router.Route;
import at.technikum.application.router.RouteIdentifier;
import at.technikum.application.service.PackageService;
import at.technikum.application.util.Authorization;
import at.technikum.application.util.Headers;
import at.technikum.application.util.Pair;
import at.technikum.http.HttpStatus;
import at.technikum.http.RequestContext;
import at.technikum.http.Response;

import java.util.ArrayList;
import java.util.List;

public class PackageController implements Controller {

    private final PackageService packageService;

    public PackageController(DataSource dataSource) {
        packageService = new PackageService(new PackagesRepositoryImpl(dataSource));
    }

    private Response createCardPackages(RequestContext requestContext) {
        new Authorization().noBody(requestContext);
        new Authorization().adminAuthorization(requestContext);
        Card[] cards = requestContext.getBodyAs(Card[].class);
        return new Response(HttpStatus.CREATED, packageService.createPackages(List.of(cards)), Headers.CONTENT_TYPE_TEXT);
    }

    private Response acquireCardPackages(RequestContext requestContext) {
        String username = new Authorization().isAuthorized(requestContext);
        return new Response(HttpStatus.OK, packageService.acquirePackages(username), Headers.CONTENT_TYPE_JSON);
    }


    @Override
    public List<Pair<RouteIdentifier, Route>> listRoutes() {
        List<Pair<RouteIdentifier, Route>> routes = new ArrayList<>();
        routes.add(new Pair<>(new RouteIdentifier("/packages", "POST"), this::createCardPackages));
        routes.add(new Pair<>(new RouteIdentifier("/transactions/packages", "POST"), this::acquireCardPackages));
        return routes;
    }

}
