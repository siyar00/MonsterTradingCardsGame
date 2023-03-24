package at.technikum.application.repository;

public interface GameRepository {
    String readStats(String username);

    String readScoreboard(String username);
}
