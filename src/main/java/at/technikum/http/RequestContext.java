package at.technikum.http;

import at.technikum.http.exceptions.BadRequestException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class RequestContext {

    private static final String CONTENT_LENGTH_HEADER_NAME = "Content-Length";
    private String httpVerb;
    private String path;
    private String pathVariable;
    private String body;
    private String format;
    private Map<String, String> headers = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RequestContext parseRequest(BufferedReader bufferedReader) throws IOException {
        RequestContext requestContext = new RequestContext();

        String[] splitVersionString = bufferedReader.readLine().split(" ");
        requestContext.setHttpVerb(splitVersionString[0]);
        requestContext.setPath(splitVersionString[1]);

        String input;
        while (!(input = bufferedReader.readLine()).equals("")) {
            String[] header = input.split(":", 2);
            headers.put(header[0], header[1].trim());
        }
        requestContext.setHeaders(headers);
        if (requestContext.getHeaders().containsKey(CONTENT_LENGTH_HEADER_NAME)) {
            int contentLength = Integer.parseInt(requestContext.getHeaders().get(CONTENT_LENGTH_HEADER_NAME));
            char[] buffer = new char[contentLength];
            bufferedReader.read(buffer, 0, contentLength);
            requestContext.setBody(new String(buffer));
        }
        return requestContext;
    }

    public void setPath(String path) {
        if (path.contains("?")) {
            this.format = path.substring(path.lastIndexOf("format=") + "format=".length());
            path = path.substring(0, path.indexOf("?"));
        }
        if (path.contains("users/")) {
            this.pathVariable = path.substring(path.lastIndexOf("/") + 1);
            this.path = path.substring(0, path.lastIndexOf("/") + 1);
        } else if (path.contains("tradings/")) {
            this.pathVariable = path.substring(path.lastIndexOf("/") + 1);
            this.path = path.substring(0, path.lastIndexOf("/") + 1);
        } else {
            this.path = path;
            pathVariable = null;
        }
    }

    public <T> T getBodyAs(Class<T> clazz) {
        try {
            return objectMapper.readValue(body, clazz);
        } catch (JsonProcessingException e) {
            System.err.println(e.getMessage());
            throw new BadRequestException(e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "HTTP-Verb: " + httpVerb + "\n" +
                "Path: " + path + "\n" +
                "Headers: " + headers + "\n" +
                "Body: " + body + "\n";
    }
}
