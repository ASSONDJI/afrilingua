package cm.afrilingua.content.exception;

public class LanguageCodeAlreadyExistsException extends ContentServiceException {
    public LanguageCodeAlreadyExistsException(String code) {
        super("A language already exists with code: " + code);
    }
}
