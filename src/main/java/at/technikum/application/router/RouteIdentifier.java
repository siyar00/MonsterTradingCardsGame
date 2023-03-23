package at.technikum.application.router;

import java.util.Objects;

public record RouteIdentifier(String path, String httpVerb) {

    public static RouteIdentifier routeIdentifier(String path, String httpVerb) {
        return new RouteIdentifier(path, httpVerb);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final RouteIdentifier routeIdentifier = (RouteIdentifier) o;
        return path.equals(routeIdentifier.path) &&
                httpVerb.equals(routeIdentifier.httpVerb);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, httpVerb);
    }
}
