package at.technikum.http.exceptions;

import at.technikum.http.HttpStatus;

public class ExistingException extends Except {
    public ExistingException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus(){
        return HttpStatus.EXISTING;
    }
}
