package cm.afrilingua.auth.exception;

/**
 * Base class for all business exceptions raised by the auth-service.
 * Keeping a common base allows the GlobalExceptionHandler to catch
 * unexpected subclasses safely if a new one is added later.
 */
public abstract class AuthServiceException extends RuntimeException {
    public AuthServiceException(String message) {
        super(message);
    }
}
