package at.technikum.application.util;

import at.technikum.http.RequestContext;
import at.technikum.http.exceptions.BadRequestException;
import at.technikum.http.exceptions.UnauthorizedException;
import org.junit.jupiter.api.Assertions;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AuthorizationTest {

    private final Authorization authorization = new Authorization();

    @Test
    public void isAuthorizedTest_doNotContainKey(){
        // given
        RequestContext requestContext = new RequestContext();

        // then
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            // when
            authorization.isAuthorized(requestContext);
        });
        assertEquals("Access token is missing or invalid", exception.getMessage());
    }

    @Test
    public void isAuthorizedTest_noBasicAttribute(){
        // given
        RequestContext requestContext = new RequestContext();
        requestContext.setHeaders(Collections.singletonMap(Authorization.AUTHORIZATION, "Token"));
        // then
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            // when
            authorization.isAuthorized(requestContext);
        });
        assertEquals("Access token is missing or invalid", exception.getMessage());
    }

    @Test
    public void isAuthorizedTest_falseToken(){
        // given
        RequestContext requestContext = new RequestContext();
        requestContext.setHeaders(Collections.singletonMap(Authorization.AUTHORIZATION, "Basic -mtcg"));
        // then
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            // when
            authorization.isAuthorized(requestContext);
        });
        assertEquals("Access token is missing or invalid", exception.getMessage());
    }

    @Test
    public void isAuthorizedTest_happyPath(){
        // Arrange
        RequestContext requestContext = new RequestContext();
        requestContext.setHeaders(Collections.singletonMap(Authorization.AUTHORIZATION, "Basic admin-mtcgToken"));
        // Act/Assert
        assertEquals("admin", authorization.isAuthorized(requestContext));
    }

    @Test
    public void areAuthorizedTest_equalsAdmin(){
        // Arrange
        RequestContext requestContext = new RequestContext();
        requestContext.setHeaders(Collections.singletonMap(Authorization.AUTHORIZATION, "Basic admin-mtcgToken"));
        // Act/Assert
        Assertions.assertDoesNotThrow(() -> authorization.areAuthorized(requestContext));
    }

    @Test
    public void areAuthorizedTest_equalsUsernameFromPathVariable(){
        // Arrange
        RequestContext requestContext = new RequestContext();
        requestContext.setHeaders(Collections.singletonMap(Authorization.AUTHORIZATION, "Basic test-mtcgToken"));
        requestContext.setPathVariable("test");
        // Act/Assert
        Assertions.assertDoesNotThrow(() -> authorization.areAuthorized(requestContext));
    }

    @Test
    public void areAuthorizedTest_equalsUsernameFromBody(){
        // Arrange
        RequestContext requestContext = new RequestContext();
        requestContext.setHeaders(Collections.singletonMap(Authorization.AUTHORIZATION, "Basic test-mtcgToken"));
        requestContext.setBody("{\"Username\":\"test\",\"Password\":\"pwd\"}");
        // Act/Assert
        Assertions.assertDoesNotThrow(() -> authorization.areAuthorized(requestContext));
    }

    @Test
    public void areAuthorizedTest_usernameInPathAdminToken(){
        // Arrange
        RequestContext requestContext = new RequestContext();
        requestContext.setHeaders(Collections.singletonMap(Authorization.AUTHORIZATION, "Basic admin-mtcgToken"));
        requestContext.setPathVariable("test");
        // Act/Assert
        Assertions.assertDoesNotThrow(() -> authorization.areAuthorized(requestContext));
    }

    @Test
    public void areAuthorizedTest_noHeader(){
        // given
        RequestContext requestContext = new RequestContext();
        // then
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            //when
            authorization.areAuthorized(requestContext);
        });
        assertEquals("Access token is missing or invalid", exception.getMessage());
    }

    @Test
    public void areAuthorizedTest_usernameNotEqual(){
        // given
        RequestContext requestContext = new RequestContext();
        requestContext.setHeaders(Collections.singletonMap(Authorization.AUTHORIZATION, "Basic tester-mtcgToken"));
        requestContext.setPathVariable("test");
        // then
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            //when
            authorization.areAuthorized(requestContext);
        });
        assertEquals("Access token is missing or invalid", exception.getMessage());
    }

    @Test
    public void adminAuthorizationTest_noHeaders(){
        // given
        RequestContext requestContext = new RequestContext();
        // then
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            //when
            authorization.adminAuthorization(requestContext);
        });
        assertEquals("Access token is missing or invalid", exception.getMessage());
    }

    @Test
    public void adminAuthorizationTest_notAdmin(){
        // given
        RequestContext requestContext = new RequestContext();
        requestContext.setHeaders(Collections.singletonMap(Authorization.AUTHORIZATION, "Basic test-mtcgToken"));
        // then
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            //when
            authorization.adminAuthorization(requestContext);
        });
        assertEquals("Access token is missing or invalid", exception.getMessage());
    }

    @Test
    public void adminAuthorizationTest_happyPath(){
        // Arrange
        RequestContext requestContext = new RequestContext();
        requestContext.setHeaders(Collections.singletonMap(Authorization.AUTHORIZATION, "Basic admin-mtcgToken"));
        // Act/Assert
        Assertions.assertDoesNotThrow(() -> authorization.adminAuthorization(requestContext));
    }

    @Test
    public void noBodyTest(){
        // given
        RequestContext requestContext = new RequestContext();
        // then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            //when
            authorization.noBody(requestContext);
        });
        assertEquals("No Body!", exception.getMessage());
    }

    @Test
    public void noBodyTest_emptyBody(){
        // given
        RequestContext requestContext = new RequestContext();
        requestContext.setBody("");
        // then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            //when
            authorization.noBody(requestContext);
        });
        assertEquals("No Body!", exception.getMessage());
    }

    @Test
    public void noBodyTest_noHeader(){
        // given
        RequestContext requestContext = new RequestContext();
        requestContext.setBody("test");
        // then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            //when
            authorization.noBody(requestContext);
        });
        assertEquals("Content-Type is not declared", exception.getMessage());
    }

    @Test
    public void noBodyTest_noContentType(){
        // given
        RequestContext requestContext = new RequestContext();
        requestContext.setBody("test");
        requestContext.setHeaders(Collections.singletonMap("Content-Type", "text/plain"));
        // then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            //when
            authorization.noBody(requestContext);
        });
        assertEquals("Content-Type is not JSON", exception.getMessage());
    }




}
