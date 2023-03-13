package at.technikum.application.router;

import at.technikum.http.RequestContext;
import at.technikum.http.Response;

public interface Route {
    Response process(RequestContext requestContext);
}
