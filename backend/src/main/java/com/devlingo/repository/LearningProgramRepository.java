package com.devlingo.repository;

import com.devlingo.domain.entity.LearningProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearningProgramRepository extends JpaRepository<LearningProgram, UUID> {

    List<LearningProgram> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<LearningProgram> findByIdAndUserId(UUID id, UUID userId);
}
