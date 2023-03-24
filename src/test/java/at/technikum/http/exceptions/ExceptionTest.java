package at.technikum.http.exceptions;

import at.technikum.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ExceptionTest {

    @Test (expectedExceptions = BadRequestException.class)
    public void testBadRequestException() {
        try {
            throw new BadRequestException("BadRequestException");
        } catch (Except except){
            Assert.assertEquals(except.getClass(), BadRequestException.class);
            Assert.assertEquals(except.getMessage(), "BadRequestException");
            Assert.assertEquals(except.getHttpStatus(), HttpStatus.BAD_REQUEST);
        }
        throw new BadRequestException("BadRequestException");
    }

    @Test (expectedExceptions = ExistingException.class)
    public void testExistingException() {
        try {
            throw new ExistingException("ExistingException");
        } catch (Except except){
            Assert.assertEquals(except.getClass(), ExistingException.class);
            Assert.assertEquals(except.getMessage(), "ExistingException");
            Assert.assertEquals(except.getHttpStatus(), HttpStatus.EXISTING);
        }
        throw new ExistingException("ExistingException");
    }

    @Test (expectedExceptions = ForbiddenException.class)
    public void testForbiddenException() {
        try {
            throw new ForbiddenException("ForbiddenException");
        } catch (Except except){
            Assert.assertEquals(except.getClass(), ForbiddenException.class);
            Assert.assertEquals(except.getMessage(), "ForbiddenException");
            Assert.assertEquals(except.getHttpStatus(), HttpStatus.FORBIDDEN);
        }
        throw new ForbiddenException("ForbiddenException");
    }

    @Test (expectedExceptions = NotFoundException.class)
    public void testNotFoundException() {
        try {
            throw new NotFoundException("NotFoundException");
        } catch (Except except){
            Assert.assertEquals(except.getClass(), NotFoundException.class);
            Assert.assertEquals(except.getMessage(), "NotFoundException");
            Assert.assertEquals(except.getHttpStatus(), HttpStatus.NOT_FOUND);
        }
        throw new NotFoundException("NotFoundException");
    }

    @Test (expectedExceptions = UnauthorizedException.class)
    public void testUnauthorizedException() {
        try {
            throw new UnauthorizedException("UnauthorizedException");
        } catch (Except except){
            Assert.assertEquals(except.getClass(), UnauthorizedException.class);
            Assert.assertEquals(except.getMessage(), "UnauthorizedException");
            Assert.assertEquals(except.getHttpStatus(), HttpStatus.UNAUTHORIZED);
        }
        throw new UnauthorizedException("UnauthorizedException");
    }

    @Test (expectedExceptions = Except.class)
    public void testExcept() {
        throw new UnauthorizedException("UnauthorizedException");
    }

}
