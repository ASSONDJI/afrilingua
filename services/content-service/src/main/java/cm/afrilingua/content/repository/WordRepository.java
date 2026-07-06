package cm.afrilingua.content.repository;

import cm.afrilingua.content.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WordRepository extends JpaRepository<Word, UUID> {
    List<Word> findByLanguageId(UUID languageId);
}
