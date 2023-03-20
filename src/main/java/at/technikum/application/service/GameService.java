package at.technikum.application.service;

import at.technikum.application.repository.GameRepository;

public record GameService(GameRepository gameRepository) {
    public String readStats() {
        return gameRepository.readStats();
    }

    public String readScoreboard() {
        return gameRepository.readScoreboard();
    }

    public String startBattle() {
        return gameRepository.startBattle();
    }
}
