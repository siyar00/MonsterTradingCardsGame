package at.technikum.application.repository.game;

public interface GameRepository {
    String readStats(String username);

    String readScoreboard(String username);
}
