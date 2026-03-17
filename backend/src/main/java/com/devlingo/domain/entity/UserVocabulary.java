package com.devlingo.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_vocabulary", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "word"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVocabulary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String word;

    private String translation;

    @Column(columnDefinition = "TEXT")
    private String context;

    private String source;

    @Column(nullable = false)
    @Builder.Default
    private boolean learned = false;

    @Column(name = "ease_factor", nullable = false)
    @Builder.Default
    private double easeFactor = 2.5;

    @Column(name = "interval_days", nullable = false)
    @Builder.Default
    private int intervalDays = 0;

    @Column(nullable = false)
    @Builder.Default
    private int repetitions = 0;

    @Column(name = "next_review_at", nullable = false)
    @Builder.Default
    private LocalDateTime nextReviewAt = LocalDateTime.now();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
