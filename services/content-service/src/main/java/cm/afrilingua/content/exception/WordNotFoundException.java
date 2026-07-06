package cm.afrilingua.content.exception;

import java.util.UUID;

public class WordNotFoundException extends ContentServiceException {
    public WordNotFoundException(UUID wordId) {
        super("Word not found with id: " + wordId);
    }
}
