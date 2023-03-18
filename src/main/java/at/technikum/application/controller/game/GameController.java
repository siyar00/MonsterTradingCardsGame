package at.technikum.application.controller.game;

import at.technikum.application.config.DataSource;
import at.technikum.application.repository.UsersRepositoryImpl;
import at.technikum.application.router.Controller;
import at.technikum.application.router.Route;
import at.technikum.application.router.RouteIdentifier;
import at.technikum.application.service.GameService;
import at.technikum.application.util.Pair;
import at.technikum.http.RequestContext;
import at.technikum.http.Response;

import java.util.ArrayList;
import java.util.List;

public class GameController implements Controller {

    private GameService gameService;

    public GameController(DataSource dataSource) {
        //this.gameService = new GameService(new GameRepository(dataSource));
    }

    private Response readStats(RequestContext requestContext) {
        return null;
    }

    private Response readScoreboard(RequestContext requestContext) {
        return null;
    }

    private Response startBattle(RequestContext requestContext) {
        return null;
    }

    @Override
    public List<Pair<RouteIdentifier, Route>> listRoutes() {
        List<Pair<RouteIdentifier, Route>> routes = new ArrayList<>();
        routes.add(new Pair<>(new RouteIdentifier("/stats", "GET"), this::readStats));
        routes.add(new Pair<>(new RouteIdentifier("/scoreboard", "GET"), this::readScoreboard));
        routes.add(new Pair<>(new RouteIdentifier("/battles", "POST"), this::startBattle));
        return routes;
    }
}
