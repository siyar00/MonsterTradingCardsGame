package at.technikum.application.repository;

import at.technikum.http.Response;

public interface BattleRepository {
    Response startBattle(String username);
}