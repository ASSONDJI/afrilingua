package cm.afrilingua.content.controller;

import cm.afrilingua.content.api.LanguagesApi;
import cm.afrilingua.content.dto.CreateLanguageRequest;
import cm.afrilingua.content.dto.Language;
import cm.afrilingua.content.service.LanguageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class LanguageController implements LanguagesApi {

    private final LanguageService languageService;

    @Override
    public ResponseEntity<Language> createLanguage(CreateLanguageRequest createLanguageRequest) {
        Language response = languageService.create(createLanguageRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<Language> getLanguage(UUID languageId) {
        return ResponseEntity.ok(languageService.getById(languageId));
    }

    @Override
    public ResponseEntity<List<Language>> listLanguages() {
        return ResponseEntity.ok(languageService.listAll());
    }
}
