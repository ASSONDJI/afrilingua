package cm.afrilingua.auth.exception;

public class InvalidCredentialsException extends AuthServiceException {
    public InvalidCredentialsException() {
        // Intentionally generic: never reveal whether the email exists or the password is wrong.
        super("Invalid email or password");
    }
}
