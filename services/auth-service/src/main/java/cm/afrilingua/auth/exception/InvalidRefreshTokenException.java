package cm.afrilingua.auth.exception;

public class InvalidRefreshTokenException extends AuthServiceException {
    public InvalidRefreshTokenException() {
        super("Refresh token is invalid or has expired");
    }
}
