package at.technikum.http.exceptions;

import at.technikum.http.HttpStatus;

public class ForbiddenException extends Except {
    public ForbiddenException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus(){
        return HttpStatus.FORBIDDEN;
    }
}
