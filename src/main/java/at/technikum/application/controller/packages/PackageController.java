package at.technikum.application.controller.packages;

import at.technikum.application.config.DataSource;
import at.technikum.application.model.Card;
import at.technikum.application.model.Credentials;
import at.technikum.application.repository.PackagesRepositoryImpl;
import at.technikum.application.router.Controller;
import at.technikum.application.router.Route;
import at.technikum.application.router.RouteIdentifier;
import at.technikum.application.service.PackageService;
import at.technikum.application.util.Headers;
import at.technikum.application.util.Pair;
import at.technikum.http.HttpStatus;
import at.technikum.http.RequestContext;
import at.technikum.http.Response;
import at.technikum.http.exceptions.BadRequestException;
import at.technikum.http.exceptions.UnauthorizedException;

import java.util.ArrayList;
import java.util.List;

public class PackageController implements Controller {

    PackageService packageService;

    public PackageController(DataSource dataSource) {
        packageService = new PackageService(new PackagesRepositoryImpl(dataSource));
    }

    private Response createCardPackages(RequestContext requestContext) {
        if (requestContext.getBody().equals("")) throw new BadRequestException("No Body!");
        adminAuthorization(requestContext.getHeaders().get("Authorization"));
        Card[] cards = requestContext.getBodyAs(Card[].class);

        return new Response(HttpStatus.CREATED, packageService.createPackages(List.of(cards)), Headers.CONTENT_TYPE_TEXT);
    }

    private Response acquireCardPackages(RequestContext requestContext) {
        if(!requestContext.getHeaders().containsKey("Authorization")) throw new UnauthorizedException("Access token is missing or invalid");
        String token = requestContext.getHeaders().get("Authorization");
        if(!token.contains("Basic") && !token.contains("-mtcgToken")) throw new UnauthorizedException("Access token is missing or invalid");
        String username = token.substring(token.indexOf(" ")+1, token.indexOf("-mtcgToken"));
        return new Response(HttpStatus.OK, packageService.acquirePackages(username), Headers.CONTENT_TYPE_JSON);
    }

    private void adminAuthorization(String authorization) {
        if (authorization == null || (!authorization.equals("Basic admin-mtcgToken") && !authorization.equals("Bearer admin-mtcgToken")))
            throw new UnauthorizedException("Access token is missing or invalid");
    }

    @Override
    public List<Pair<RouteIdentifier, Route>> listRoutes() {
        List<Pair<RouteIdentifier, Route>> routes = new ArrayList<>();
        routes.add(new Pair<>(new RouteIdentifier("/packages", "POST"), this::createCardPackages));
        routes.add(new Pair<>(new RouteIdentifier("/transactions/packages", "POST"), this::acquireCardPackages));
        return routes;
    }

}
