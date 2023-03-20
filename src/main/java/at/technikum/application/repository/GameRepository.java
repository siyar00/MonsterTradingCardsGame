package at.technikum.application.repository;

public interface GameRepository {
    String readStats();

    String readScoreboard();

    String startBattle();
}
