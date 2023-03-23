package at.technikum.http;

import at.technikum.application.util.Headers;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class Response {
    private HttpStatus httpStatus;
    private String body;
    private List<Headers> headers;

    public Response() {
        this.httpStatus = HttpStatus.BAD_REQUEST;
        this.body = "";
        this.headers = new ArrayList<>();
    }

    public Response(HttpStatus status, String body) {
        this.httpStatus = status;
        this.body = body;
        this.headers = Collections.singletonList(Headers.CONTENT_TYPE_TEXT);
    }

    public Response(HttpStatus status, String body, Headers header) {
        this.httpStatus = status;
        this.body = body;
        this.headers = Collections.singletonList(header);
    }

    @Override
    public String toString() {
        StringBuilder response = new StringBuilder("HTTP/1.1 " + httpStatus.getStatusCode() + " " + httpStatus.getStatusMessage() + "\n");
        for (Headers header : this.headers) {
            response.append(header.toString()).append("\n");
        }
        return response + "\n\n" + body;
    }
}
