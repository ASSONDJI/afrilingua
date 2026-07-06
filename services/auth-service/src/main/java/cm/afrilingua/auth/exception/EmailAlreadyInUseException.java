package cm.afrilingua.auth.exception;

public class EmailAlreadyInUseException extends AuthServiceException {
    public EmailAlreadyInUseException(String email) {
        super("An account already exists with email: " + email);
    }
}
