package at.technikum.application.controller;

import at.technikum.application.config.DataSource;
import at.technikum.application.model.Credentials;
import at.technikum.application.model.UserData;
import at.technikum.application.repository.users.UsersRepositoryImpl;
import at.technikum.application.router.Controller;
import at.technikum.application.router.Route;
import at.technikum.application.router.RouteIdentifier;
import at.technikum.application.service.UserService;
import at.technikum.application.util.Authorization;
import at.technikum.http.Headers;
import at.technikum.application.util.Pair;
import at.technikum.http.HttpStatus;
import at.technikum.http.RequestContext;
import at.technikum.http.Response;

import java.util.ArrayList;
import java.util.List;

import static at.technikum.application.router.RouteIdentifier.routeIdentifier;

public class UserController implements Controller {

    private final UserService userService;

    public UserController(DataSource dataSource) {
        this.userService = new UserService(new UsersRepositoryImpl(dataSource));
    }

    Response registerUser(RequestContext requestContext) {
        new Authorization().noBody(requestContext);
        Credentials credentials = requestContext.getBodyAs(Credentials.class);
        return new Response(HttpStatus.CREATED, userService.registerUser(credentials), Headers.CONTENT_TYPE_TEXT);
    }

    Response readUser(RequestContext requestContext) {
        new Authorization().areAuthorized(requestContext);
        return new Response(HttpStatus.OK, userService.readUserData(requestContext.getPathVariable()), Headers.CONTENT_TYPE_JSON);
    }

    Response updateUser(RequestContext requestContext) {
        new Authorization().noBody(requestContext);
        new Authorization().areAuthorized(requestContext);
        UserData userData = requestContext.getBodyAs(UserData.class);
        return new Response(HttpStatus.OK, userService.updateUser(requestContext.getPathVariable(), userData), Headers.CONTENT_TYPE_TEXT);
    }

    Response loginUser(RequestContext requestContext) {
        new Authorization().noBody(requestContext);
        Credentials credentials = requestContext.getBodyAs(Credentials.class);
        return new Response(HttpStatus.OK, userService.loginUser(credentials), Headers.CONTENT_TYPE_JSON);
    }

    @Override
    public List<Pair<RouteIdentifier, Route>> listRoutes() {
        List<Pair<RouteIdentifier, Route>> userRoutes = new ArrayList<>();
        userRoutes.add(new Pair<>(routeIdentifier("/users", "POST"), this::registerUser));
        userRoutes.add(new Pair<>(routeIdentifier("/sessions", "POST"), this::loginUser));
        userRoutes.add(new Pair<>(routeIdentifier("/users/", "GET"), this::readUser));
        userRoutes.add(new Pair<>(routeIdentifier("/users/", "PUT"), this::updateUser));
        return userRoutes;
    }
}
