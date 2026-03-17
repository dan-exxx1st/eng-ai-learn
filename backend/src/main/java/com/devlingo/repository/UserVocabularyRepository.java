package com.devlingo.repository;

import com.devlingo.domain.entity.UserVocabulary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserVocabularyRepository extends JpaRepository<UserVocabulary, UUID> {

    Page<UserVocabulary> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<UserVocabulary> findByUserIdAndLearnedOrderByCreatedAtDesc(UUID userId, boolean learned, Pageable pageable);

    Optional<UserVocabulary> findByUserIdAndWord(UUID userId, String word);

    long countByUserIdAndLearned(UUID userId, boolean learned);

    Page<UserVocabulary> findByUserIdAndNextReviewAtBeforeOrderByNextReviewAtAsc(UUID userId, LocalDateTime now, Pageable pageable);

    long countByUserIdAndNextReviewAtBefore(UUID userId, LocalDateTime now);
}
