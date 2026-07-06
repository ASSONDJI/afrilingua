package cm.afrilingua.content.exception;

public abstract class ContentServiceException extends RuntimeException {
    public ContentServiceException(String message) {
        super(message);
    }
}
