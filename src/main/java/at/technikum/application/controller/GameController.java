package at.technikum.application.controller;

import at.technikum.application.config.DataSource;
import at.technikum.application.repository.battle.BattleRepositoryImpl;
import at.technikum.application.repository.game.GameRepositoryImpl;
import at.technikum.application.router.Controller;
import at.technikum.application.router.Route;
import at.technikum.application.router.RouteIdentifier;
import at.technikum.application.service.GameService;
import at.technikum.application.util.Authorization;
import at.technikum.http.Headers;
import at.technikum.application.util.Pair;
import at.technikum.http.HttpStatus;
import at.technikum.http.RequestContext;
import at.technikum.http.Response;

import java.util.ArrayList;
import java.util.List;

public class GameController implements Controller {

    private final GameService gameService;

    public GameController(DataSource dataSource) {
        this.gameService = new GameService(new GameRepositoryImpl(dataSource), new BattleRepositoryImpl(dataSource));
    }

    private Response readStats(RequestContext requestContext) {
        String username = new Authorization().isAuthorized(requestContext);
        return new Response(HttpStatus.OK, gameService.readStats(username), Headers.CONTENT_TYPE_JSON);
    }

    private Response readScoreboard(RequestContext requestContext) {
        String username = new Authorization().isAuthorized(requestContext);
        return new Response(HttpStatus.OK, gameService.readScoreboard(username), Headers.CONTENT_TYPE_JSON);
    }

    private Response startBattle(RequestContext requestContext) {
        String username = new Authorization().isAuthorized(requestContext);
        return gameService.startBattle(username);
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
