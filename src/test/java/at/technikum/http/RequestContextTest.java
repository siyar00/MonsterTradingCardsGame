package at.technikum.http;

import at.technikum.http.exceptions.BadRequestException;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Collections;

public class RequestContextTest {

    @Test
    public void testParseRequest() throws Exception {
        // Arrange
        BufferedReader reader = new BufferedReader(new StringReader(getRequest()));

        // Act
        RequestContext result = new RequestContext().parseRequest(reader);

        // Assert
        Assertions.assertEquals("GET", result.getHttpVerb());
        Assertions.assertEquals("/users", result.getPath());
        Assertions.assertNull(result.getPathVariable());
        Assertions.assertEquals("application/json", result.getHeaders().get("Content-Type"));
        Assertions.assertEquals("{\"username\":\"foo\"}", result.getBody());
    }
    private String getRequest(){
        return """
                GET /users HTTP/1.1
                Host: example.com
                User-Agent: Mozilla/5.0
                Content-Type: application/json
                Content-Length: 18
                
                {"username":"foo"}""";
    }

    @Test
    public void testSetPathWithFormat() {
        // Arrange
        RequestContext requestContext = new RequestContext();
        String path = "/users?format=json";

        // Act
        requestContext.setPath(path);

        // Assert
        Assertions.assertEquals("/users", requestContext.getPath());
        Assertions.assertEquals("json", requestContext.getFormat());
    }

    @Test
    public void testSetPathWithUsersPathVariable() {
        // Arrange
        RequestContext requestContext = new RequestContext();
        String path = "/users/42";

        // Act
        requestContext.setPath(path);

        // Assert
        Assertions.assertEquals("/users/", requestContext.getPath());
        Assertions.assertEquals("42", requestContext.getPathVariable());
    }

    @Test
    public void testSetPathWithTradingsPathVariable() {
        // Arrange
        RequestContext requestContext = new RequestContext();
        String path = "/tradings/123";

        // Act
        requestContext.setPath(path);

        // Assert
        Assertions.assertEquals("/tradings/", requestContext.getPath());
        Assertions.assertEquals("123", requestContext.getPathVariable());
    }

    @Test
    public void testSetPathWithoutPathVariable() {
        // Arrange
        RequestContext requestContext = new RequestContext();
        String path = "/about";

        // Act
        requestContext.setPath(path);

        // Assert
        Assertions.assertEquals("/about", requestContext.getPath());
        Assertions.assertNull(requestContext.getPathVariable());
    }

    @Test
    public void testGetBodyAs() {
        // Arrange
        RequestContext requestContext = new RequestContext();
        String json = "{\"username\":\"foo\"}";
        requestContext.setBody(json);

        // Act
        User user = requestContext.getBodyAs(User.class);

        // Assert
        Assertions.assertEquals("foo", user.getUsername());
    }
    @Data
    static class User {
        @JsonProperty("username")
        private String username;
    }

    @Test
    public void testToString(){
        // Arrange
        RequestContext requestContext = new RequestContext();
        requestContext.setHttpVerb("GET");
        requestContext.setPath("/endpoint");
        requestContext.setBody("json");
        requestContext.setHeaders(Collections.singletonMap("Te","st"));
        String input = """
                HTTP-Verb: GET
                Path: /endpoint
                Headers: {Te=st}
                Body: json
                """;

        // Act/Assert
        Assertions.assertEquals(input, requestContext.toString());
    }

    @Test (expectedExceptions = BadRequestException.class)
    public void testGetBodyAsWithInvalidJson() {
        // Arrange
        RequestContext requestContext = new RequestContext();
        String json = "{\"username\":}";
        requestContext.setBody(json);

        // Act/Assert
        requestContext.getBodyAs(User.class);
    }
}

