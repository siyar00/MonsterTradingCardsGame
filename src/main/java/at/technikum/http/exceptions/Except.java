package at.technikum.http.exceptions;

import at.technikum.http.HttpStatus;

public abstract class Except extends RuntimeException {
    public Except(String message){
        super(message);
    }
    public abstract HttpStatus getHttpStatus();
}
