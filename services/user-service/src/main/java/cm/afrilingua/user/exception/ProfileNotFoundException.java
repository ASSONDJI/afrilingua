package cm.afrilingua.user.exception;

import java.util.UUID;

public class ProfileNotFoundException extends UserServiceException {
    public ProfileNotFoundException(UUID accountId) {
        super("No profile found for account: " + accountId);
    }
}