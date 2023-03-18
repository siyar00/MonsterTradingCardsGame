package at.technikum.http;

import at.technikum.application.util.Headers;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Setter
@Getter
public class Response {
    private HttpStatus httpStatus;
    private String body;
    private List<String> headers;

    public Response(){
        this.httpStatus = HttpStatus.BAD_REQUEST;
        this.body = "";
        this.headers = new ArrayList<>();
    }

    public Response(HttpStatus status, String body){
        this.httpStatus = status;
        this.body = body;
        this.headers = new ArrayList<>();
    }

    public Response(HttpStatus status, String body, String header){
        this.httpStatus = status;
        this.body = body;
        this.headers = Collections.singletonList(header);
    }

    @Override
    public String toString(){
        StringBuilder response = new StringBuilder("HTTP/1.1 " + httpStatus.getStatusCode() + " " + httpStatus.getStatusMessage() + "\n");
        for (String header : this.headers){
            response.append(header).append("\n");
        }
        return response + "\n"+ body;
    }
}
