package at.technikum.application.service;

import at.technikum.application.model.Trading;
import at.technikum.application.repository.trading.TradingRepository;
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

public class TradingServiceTest {

    @Mock
    TradingRepository tradingRepository;

    @InjectMocks
    TradingService tradingService;

    @BeforeTest
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void showCurrentlyAvailableTradingsTest() {
        //Arrange
        when(tradingRepository.showAllTradings("username")).thenReturn(new Response(HttpStatus.OK, "test", Headers.CONTENT_TYPE_JSON));
        //Act
        Response result = tradingService.showCurrentlyAvailableTradings("username");
        //Assert
        Assertions.assertEquals(HttpStatus.OK, result.getHttpStatus());
        Assertions.assertEquals("test", result.getBody());
        Assertions.assertEquals(Headers.CONTENT_TYPE_JSON, result.getHeaders().get(0));
    }

    @Test
    public void createsNewTradingDealTest() {
        //Arrange
        when(tradingRepository.createNewTradingDeal(Trading.builder().build(), "username")).thenReturn("test");
        //Act
        String result = tradingService.createsNewTradingDeal(Trading.builder().build(), "username");
        //Assert
        Assertions.assertEquals("test", result);
    }

    @Test
    public void deletesExistingTradingDealTest() {
        //Arrange
        when(tradingRepository.deleteTradingDeal("username", "tradingId")).thenReturn("test");
        //Act
        String result = tradingService.deletesExistingTradingDeal("username", "tradingId");
        //Assert
        Assertions.assertEquals("test", result);
    }


    @Test
    public void configureUserDeckWith4CardsTest() {
        //Arrange
        when(tradingRepository.trade("username", "tradingId", "cardId")).thenReturn("test");
        //Act
        String result = tradingService.startTrading("username", "tradingId", "cardId");
        //Assert
        Assertions.assertEquals("test", result);
    }
}
