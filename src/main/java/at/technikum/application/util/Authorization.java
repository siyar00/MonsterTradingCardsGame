package at.technikum.application.util;

import at.technikum.application.model.Credentials;
import at.technikum.http.RequestContext;
import at.technikum.http.exceptions.BadRequestException;
import at.technikum.http.exceptions.UnauthorizedException;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public record Authorization() {
    private static final String AUTHORIZATION = "Authorization";

    public String isAuthorized(RequestContext requestContext) {
        if (!requestContext.getHeaders().containsKey(AUTHORIZATION))
            throw new UnauthorizedException("Access token is missing or invalid");
        String token = requestContext.getHeaders().get(AUTHORIZATION);
        if (!token.contains("Basic") && !token.contains("-mtcgToken"))
            throw new UnauthorizedException("Access token is missing or invalid");
        return token.substring(token.indexOf(" ") + 1, token.indexOf("-mtcgToken"));
    }

    public void areAuthorized(@NotNull RequestContext requestContext) {
        String username = "";
        String authorization = requestContext.getHeaders().get(AUTHORIZATION);

        if (requestContext.getHttpVerb() != null)
            username = requestContext.getPathVariable();
        else if (requestContext.getBody() != null)
            username = requestContext.getBodyAs(Credentials.class).getUsername();

        if (authorization == null || (!authorization.equals("Basic " + username + "-mtcgToken") && !authorization.equals("Basic admin-mtcgToken") &&
                !authorization.equals("Bearer " + username + "-mtcgToken") && !authorization.equals("Bearer admin-mtcgToken")))
            throw new UnauthorizedException("Access token is missing or invalid");
    }

    public void adminAuthorization(@NotNull RequestContext requestContext) {
        String s = requestContext.getHeaders().get(AUTHORIZATION);
        if (s == null || (!s.equals("Basic admin-mtcgToken") && !s.equals("Bearer admin-mtcgToken")))
            throw new UnauthorizedException("Access token is missing or invalid");
    }

    public void noBody(@NotNull RequestContext requestContext) {
        if (requestContext.getBody().equals("")) throw new BadRequestException("No Body!");
        if (!requestContext.getHeaders().get("Content-Type").equals("application/json"))
            throw new BadRequestException("Content-Type is not declared");
    }

    public @NotNull ResultSet authorizeUser(String username, @NotNull Connection connection) throws SQLException {
        PreparedStatement selectUser = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
        selectUser.setString(1, username);
        ResultSet rs = selectUser.executeQuery();
        if (!rs.next()) throw new UnauthorizedException("Access token is missing or invalid");
        return rs;
    }

}