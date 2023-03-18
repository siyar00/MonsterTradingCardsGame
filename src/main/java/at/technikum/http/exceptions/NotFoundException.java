package at.technikum.http.exceptions;

import at.technikum.http.HttpStatus;

public class NotFoundException extends Except{
    public NotFoundException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus(){
        return HttpStatus.NOT_FOUND;
    }
}
