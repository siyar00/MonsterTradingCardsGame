package at.technikum.http.exceptions;

import at.technikum.http.HttpStatus;

public class BadRequestException extends Except {

    public BadRequestException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus(){
        return HttpStatus.BAD_REQUEST;
    }

}
