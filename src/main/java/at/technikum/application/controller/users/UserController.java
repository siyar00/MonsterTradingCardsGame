package at.technikum.application.controller.users;

import at.technikum.application.model.Credentials;
import at.technikum.application.model.User;
import at.technikum.application.router.Controller;
import at.technikum.application.router.Route;
import at.technikum.application.router.RouteIdentifier;
import at.technikum.application.service.UserService;
import at.technikum.application.util.Pair;
import at.technikum.http.BadRequestException;
import at.technikum.http.HttpStatus;
import at.technikum.http.RequestContext;
import at.technikum.http.Response;

import java.util.ArrayList;
import java.util.List;

import static at.technikum.application.router.RouteIdentifier.routeIdentifier;

public class UserController implements Controller {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public Response registerUser(RequestContext requestContext) {
        System.out.println("Body of register: " + requestContext.getBody());
        Credentials credentials = requestContext.getBodyAs(Credentials.class);
        return register(credentials);
    }

    public Response register(Credentials credentials) {
        User user = userService.findUserByUsername(credentials.getUsername());
        if (user != null) {
            throw new BadRequestException("User with username " + credentials.getUsername() + " already exists");
        }
        Response response = new Response();
        response.setHttpStatus(HttpStatus.CREATED);
        return response;
    }

    public Response loginUser(RequestContext requestContext) {
        Credentials credentials = requestContext.getBodyAs(Credentials.class);
        return login(credentials);
    }

    public Response login(Credentials credentials) {
        Response response = new Response();
        response.setBody("Response-Body: {\"username\":\""+credentials.getUsername()+"\",\"password\":\""+credentials.getPassword()+"\"}");
        response.setHttpStatus(HttpStatus.OK);
        return response;
    }

    private Response updateUser(RequestContext requestContext) {
        Response response = new Response();
        response.setHttpStatus(HttpStatus.OK);
        return response;
    }

    private Response update(String username) {
        Response response = new Response();
        response.setHttpStatus(HttpStatus.OK);
        return response;
    }

    private Response readUser(RequestContext requestContext) {
        Response response = new Response();
        response.setHttpStatus(HttpStatus.OK);
        return response;
    }

    private Response read(String username) {
        Response response = new Response();
        response.setHttpStatus(HttpStatus.OK);
        return response;
    }

    @Override
    public List<Pair<RouteIdentifier, Route>> listRoutes() {
        List<Pair<RouteIdentifier, Route>> userRoutes = new ArrayList<>();
        userRoutes.add(new Pair<>(routeIdentifier("/users", "POST"), this::registerUser));
        userRoutes.add(new Pair<>(routeIdentifier("/sessions", "POST"), this::loginUser));
        userRoutes.add(new Pair<>(routeIdentifier("/users/{username}", "GET"), this::readUser));
        userRoutes.add(new Pair<>(routeIdentifier("/users/{username}", "PUT"), this::updateUser));
        return userRoutes;
    }
}
