package com.devlingo.repository;

import com.devlingo.domain.entity.UserProgress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, UUID> {

    Optional<UserProgress> findByUserIdAndLessonId(UUID userId, UUID lessonId);

    @Query("SELECT COUNT(p) FROM UserProgress p WHERE p.user.id = :userId AND p.completed = true")
    long countCompletedByUserId(UUID userId);

    @Query("SELECT COUNT(l) FROM Lesson l JOIN l.module m JOIN m.program p WHERE p.user.id = :userId")
    long countTotalLessonsByUserId(UUID userId);

    Page<UserProgress> findByUserIdAndCompletedTrueOrderByCompletedAtDesc(UUID userId, Pageable pageable);
}
