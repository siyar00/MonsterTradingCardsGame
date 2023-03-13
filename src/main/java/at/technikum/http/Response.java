package at.technikum.http;

public class Response {
    private HttpStatus httpStatus;
    private String body;

    public Response(){
        this.httpStatus = HttpStatus.BAD_REQUEST;
        this.body = "";
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString(){
        return "HTTP/1.1 " + httpStatus.getStatusCode() + " " + httpStatus.getStatusMessage() + "\n\n"+ body;
    }
}
