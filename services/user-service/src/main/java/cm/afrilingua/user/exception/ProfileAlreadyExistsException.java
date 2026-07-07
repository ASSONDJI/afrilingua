package cm.afrilingua.user.exception;

import java.util.UUID;

public class ProfileAlreadyExistsException extends UserServiceException {
    public ProfileAlreadyExistsException(UUID accountId) {
        super("A profile already exists for account: " + accountId);
    }
}