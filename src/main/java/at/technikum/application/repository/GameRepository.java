package at.technikum.application.repository;

import at.technikum.http.Response;

public interface GameRepository {
    String readStats(String username);

    String readScoreboard(String username);

    Response startBattle(String username);
}
