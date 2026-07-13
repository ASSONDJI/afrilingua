package cm.afrilingua.content.controller;

import cm.afrilingua.content.api.WordsApi;
import cm.afrilingua.content.dto.CreateWordRequest;
import cm.afrilingua.content.dto.Word;
import cm.afrilingua.content.service.WordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class WordController implements WordsApi {

    private final WordService wordService;

    @Override
    public ResponseEntity<Word> createWord(UUID languageId, CreateWordRequest createWordRequest) {
        Word response = wordService.create(languageId, createWordRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<Word> getWord(UUID wordId) {
        return ResponseEntity.ok(wordService.getById(wordId));
    }

    @Override
    public ResponseEntity<List<Word>> listWordsByLanguage(UUID languageId) {
        return ResponseEntity.ok(wordService.listByLanguage(languageId));
    }

    @Override
    public ResponseEntity<Word> uploadWordAudio(UUID wordId, MultipartFile file) {
        return ResponseEntity.ok(wordService.uploadAudio(wordId, file));
    }

    @Override
    public ResponseEntity<Word> uploadWordImage(UUID wordId, MultipartFile file) {
        return ResponseEntity.ok(wordService.uploadImage(wordId, file));
    }
}
