package cm.afrilingua.lesson.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "language_id", nullable = false)
    private UUID languageId;

    @Column(nullable = false)
    private String title;

    @Column(name = "lesson_order", nullable = false)
    private Integer order;

    @Column(nullable = false)
    private Integer level;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    // Simple join table: no extra columns, so an element collection of raw UUIDs
    // is more appropriate than a full entity relationship (content-service owns Word, not us).
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "lesson_words", joinColumns = @JoinColumn(name = "lesson_id"))
    @Column(name = "word_id")
    @Builder.Default
    private Set<UUID> wordIds = new HashSet<>();
}
