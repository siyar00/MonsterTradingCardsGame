package at.technikum.application.service;

import at.technikum.application.repository.cards.CardsRepository;
import at.technikum.http.Headers;
import at.technikum.http.HttpStatus;
import at.technikum.http.Response;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Mockito.when;

public class CardServiceTest {

    @Mock
    CardsRepository cardsRepository;

    @InjectMocks
    CardService cardService;

    @BeforeTest
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void showAllUserCardsTest() {
        //Arrange
        when(cardsRepository.showUserCards("username")).thenReturn(new Response(HttpStatus.OK, "test", Headers.CONTENT_TYPE_JSON));
        //Act
        Response result = cardService.showAllUserCards("username");
        //Assert
        Assertions.assertEquals(HttpStatus.OK, result.getHttpStatus());
        Assertions.assertEquals("test", result.getBody());
        Assertions.assertEquals(Headers.CONTENT_TYPE_JSON, result.getHeaders().get(0));
    }

    @Test
    public void showCurrentUserDeckTest() {
        //Arrange
        when(cardsRepository.showUserDeck("username")).thenReturn(new Response(HttpStatus.OK, "test", Headers.CONTENT_TYPE_JSON));
        //Act
        Response result = cardService.showCurrentUserDeck("username");
        //Assert
        Assertions.assertEquals(HttpStatus.OK, result.getHttpStatus());
        Assertions.assertEquals("test", result.getBody());
        Assertions.assertEquals(Headers.CONTENT_TYPE_JSON, result.getHeaders().get(0));
    }

    @Test
    public void configureUserDeckWith4CardsTest() {
        //Arrange
        when(cardsRepository.configureUserDeck("username", Collections.singletonList("card"))).thenReturn("test");
        //Act
        String result = cardService.configureUserDeckWith4Cards("username", Collections.singletonList("card"));
        //Assert
        Assertions.assertEquals("test", result);
    }
}
