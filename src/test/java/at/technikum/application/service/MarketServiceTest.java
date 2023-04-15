package at.technikum.application.service;

import at.technikum.application.model.CardSell;
import at.technikum.application.repository.market.MarketRepository;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

public class MarketServiceTest {

    @Mock
    MarketRepository marketRepository;

    @InjectMocks
    MarketService marketService;

    @BeforeTest
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void changeManaTest() {
        //Arrange
        when(marketRepository.changeManaToCoins("username")).thenReturn("test");
        //Act
        String result = marketService.changeMana("username");
        //Assert
        Assertions.assertEquals("test", result);
    }

    @Test
    public void currentCoinManaTest() {
        //Arrange
        when(marketRepository.manaCoinStats("username")).thenReturn("test");
        //Act
        String result = marketService.currentCoinMana("username");
        //Assert
        Assertions.assertEquals("test", result);
    }

    @Test
    public void sellCardTest() {
        //Arrange
        when(marketRepository.sellCard("username", CardSell.builder().build())).thenReturn("test");
        //Act
        String result = marketService.sellCard("username", CardSell.builder().build());
        //Assert
        Assertions.assertEquals("test", result);
    }

    @Test
    public void buyCardTest() {
        //Arrange
        when(marketRepository.buyCard("username", "card")).thenReturn("test");
        //Act
        String result = marketService.buyCard("username", "card");
        //Assert
        Assertions.assertEquals("test", result);
    }

    @Test
    public void deleteSaleTest() {
        //Arrange
        when(marketRepository.deleteSale("username", "card")).thenReturn("test");
        //Act
        String result = marketService.deleteSale("username", "card");
        //Assert
        Assertions.assertEquals("test", result);
    }

    @Test
    public void showMarketTest() {
        //Arrange
        when(marketRepository.showMarket("username")).thenReturn("test");
        //Act
        String result = marketService.showMarket("username");
        //Assert
        Assertions.assertEquals("test", result);
    }
}
