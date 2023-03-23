package at.technikum.application.service;

import at.technikum.application.repository.GameRepository;
import at.technikum.http.Response;

public record GameService(GameRepository gameRepository) {
    public String readStats(String username) {
        return gameRepository.readStats(username);
    }

    public String readScoreboard(String username) {
        return gameRepository.readScoreboard(username);
    }

    public Response startBattle(String username) {
        return gameRepository.startBattle(username);
    }
}
