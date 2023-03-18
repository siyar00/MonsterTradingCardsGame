package at.technikum.http.exceptions;

import at.technikum.http.HttpStatus;

public class UnauthorizedException extends Except {
    public UnauthorizedException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.UNAUTHORIZED;
    }
}
