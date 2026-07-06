package cm.afrilingua.content.exception;

import java.util.UUID;

public class LanguageNotFoundException extends ContentServiceException {
    public LanguageNotFoundException(UUID languageId) {
        super("Language not found with id: " + languageId);
    }
}
