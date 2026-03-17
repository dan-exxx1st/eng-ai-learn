package com.devlingo.repository;

import com.devlingo.domain.entity.PracticeSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PracticeSessionRepository extends JpaRepository<PracticeSession, UUID> {

    Page<PracticeSession> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Optional<PracticeSession> findByIdAndUserId(UUID id, UUID userId);
}
