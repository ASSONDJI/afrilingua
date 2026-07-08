package cm.afrilingua.content.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "words")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Word {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(nullable = false)
    private String word;

    @Column(nullable = false)
    private String translation;

    @Column(name = "grammatical_category")
    private String grammaticalCategory;

    @Column(name = "phonetic_ipa")
    private String phoneticIpa;

    @Column(name = "audio_url")
    private String audioUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false)
    @Builder.Default
    private DifficultyLevel difficultyLevel = DifficultyLevel.BEGINNER;

    // Optional linguistic annotations (e.g. Yemba tonal data). Null for
    // languages/words without this data — see RecommendationClient for how
    // their absence triggers a fallback to the default difficulty level.
    @Column(name = "nb_syllabes")
    private Integer nbSyllabes;

    @Column(name = "tone1")
    private String tone1;

    @Column(name = "tone2")
    private String tone2;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    public enum DifficultyLevel {
        BEGINNER, INTERMEDIATE, ADVANCED
    }
}