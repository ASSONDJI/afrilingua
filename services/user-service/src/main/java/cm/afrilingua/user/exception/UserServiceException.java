package cm.afrilingua.user.exception;

public abstract class UserServiceException extends RuntimeException {
    public UserServiceException(String message) {
        super(message);
    }
}