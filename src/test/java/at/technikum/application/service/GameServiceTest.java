package at.technikum.application.service;

import at.technikum.application.repository.battle.BattleRepository;
import at.technikum.application.repository.game.GameRepository;
import at.technikum.http.Headers;
import at.technikum.http.HttpStatus;
import at.technikum.http.Response;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

public class GameServiceTest {

    @Mock
    GameRepository gameRepository;

    @Mock
    BattleRepository battleRepository;

    @InjectMocks
    GameService gameService;

    @BeforeTest
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void readStatsTest() {
        //Arrange
        when(gameRepository.readStats("username")).thenReturn("test");
        //Act
        String result = gameService.readStats("username");
        //Assert
        Assertions.assertEquals("test", result);
    }

    @Test
    public void showCurrentUserDeckTest() {
        //Arrange
        when(gameRepository.readScoreboard("username")).thenReturn("test");
        //Act
        String result = gameService.readScoreboard("username");
        //Assert
        Assertions.assertEquals("test", result);
    }

    @Test
    public void configureUserDeckWith4CardsTest() {
        //Arrange
        when(battleRepository.startBattle("username")).thenReturn(new Response(HttpStatus.OK, "test", Headers.CONTENT_TYPE_JSON));
        //Act
        Response result = gameService.startBattle("username");
        //Assert
        Assertions.assertEquals(HttpStatus.OK, result.getHttpStatus());
        Assertions.assertEquals("test", result.getBody());
        Assertions.assertEquals(Headers.CONTENT_TYPE_JSON, result.getHeaders().get(0));
    }
}
