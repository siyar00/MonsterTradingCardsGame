package at.technikum.application.service;

import at.technikum.application.model.Credentials;
import at.technikum.application.model.UserData;
import at.technikum.application.repository.users.UsersRepository;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

public class UserServiceTest {

    @Mock
    UsersRepository usersRepository;

    @InjectMocks
    UserService userService;

    @BeforeTest
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void registerUserTest() {
        //Arrange
        Credentials credentials = Credentials.builder().username("max").password("pwd").build();
        when(usersRepository.registerUser(credentials)).thenReturn("test");
        //Act
        String result = userService.registerUser(credentials);
        //Assert
        Assertions.assertEquals("test", result);
    }

    @Test
    public void readUserDataTest() {
        //Arrange
        when(usersRepository.getUserData("username")).thenReturn("test");
        //Act
        String result = userService.readUserData("username");
        //Assert
        Assertions.assertEquals("test", result);
    }

    @Test
    public void updateUserTest() {
        //Arrange
        UserData userData = UserData.builder().build();
        when(usersRepository.updateUser("username", userData)).thenReturn("test");
        //Act
        String result = userService.updateUser("username", userData);
        //Assert
        Assertions.assertEquals("test", result);
    }

    @Test
    public void loginUserTest() {
        //Arrange
        Credentials credentials = Credentials.builder().username("max").password("pwd").build();
        when(usersRepository.loginUser(credentials)).thenReturn("test");
        //Act
        String result = userService.loginUser(credentials);
        //Assert
        Assertions.assertEquals("test", result);
    }
}
