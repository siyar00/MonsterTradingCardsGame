package at.technikum.application.repository.battle;

import at.technikum.http.Response;

public interface BattleRepository {
    Response startBattle(String username);
}