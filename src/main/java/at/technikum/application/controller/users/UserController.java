package at.technikum.application.controller.users;

import at.technikum.application.config.DataSource;
import at.technikum.application.model.Credentials;
import at.technikum.application.model.User;
import at.technikum.application.model.UserDataRec;
import at.technikum.application.repository.UsersRepositoryImpl;
import at.technikum.application.router.Controller;
import at.technikum.application.router.Route;
import at.technikum.application.router.RouteIdentifier;
import at.technikum.application.service.UsersService;
import at.technikum.application.util.Headers;
import at.technikum.application.util.Pair;
import at.technikum.http.RequestHandler;
import at.technikum.http.exceptions.BadRequestException;
import at.technikum.http.HttpStatus;
import at.technikum.http.RequestContext;
import at.technikum.http.Response;
import at.technikum.http.exceptions.UnauthorizedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static at.technikum.application.router.RouteIdentifier.routeIdentifier;

public class UserController implements Controller {

    private final UsersService usersService;

    public UserController(DataSource dataSource) {
        this.usersService = new UsersService(new UsersRepositoryImpl(dataSource));
    }

    private Response registerUser(RequestContext requestContext) {
        if(requestContext.getBody().equals("")) throw new BadRequestException("No Body!");
        Credentials credentials = requestContext.getBodyAs(Credentials.class);
        return new Response(HttpStatus.CREATED, usersService.registerUser(credentials), Headers.CONTENT_TYPE_TEXT);
    }

    private Response readUser(RequestContext requestContext) {
        isAuthorized(requestContext);
        return new Response(HttpStatus.OK, usersService.readUserData(requestContext.getPathVariable()), Headers.CONTENT_TYPE_JSON);
    }

    private Response updateUser(RequestContext requestContext) {
        if(requestContext.getBody().equals("")) throw new BadRequestException("No Body!");
        isAuthorized(requestContext);
        UserDataRec userData = requestContext.getBodyAs(UserDataRec.class);
        return new Response(HttpStatus.OK, usersService.updateUser(requestContext.getPathVariable(), userData), Headers.CONTENT_TYPE_TEXT);
    }

    private Response loginUser(RequestContext requestContext) {
        if(requestContext.getBody().equals("")) throw new BadRequestException("No Body!");
        Credentials credentials = requestContext.getBodyAs(Credentials.class);
        return new Response(HttpStatus.OK, usersService.loginUser(credentials), Headers.CONTENT_TYPE_JSON);
    }

    private void isAuthorized(RequestContext requestContext){
        String username = "";
        String authorization = requestContext.getHeaders().get("Authorization");

        if(requestContext.getHttpVerb() != null)
            username = requestContext.getPathVariable();
        else if(requestContext.getBody() != null)
            username = requestContext.getBodyAs(Credentials.class).getUsername();

        if(authorization == null || (!authorization.equals("Basic "+username+"-mtcgToken") && !authorization.equals("Basic admin-mtcgToken") &&
                !authorization.equals("Bearer "+username+"-mtcgToken") && !authorization.equals("Bearer admin-mtcgToken")))
            throw new UnauthorizedException("Access token is missing or invalid");
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
