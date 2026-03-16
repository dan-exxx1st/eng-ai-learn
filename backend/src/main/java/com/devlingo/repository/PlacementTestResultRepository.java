package com.devlingo.repository;

import com.devlingo.domain.entity.PlacementTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlacementTestResultRepository extends JpaRepository<PlacementTestResult, UUID> {

    Optional<PlacementTestResult> findTopByUserIdOrderByStartedAtDesc(UUID userId);

    Optional<PlacementTestResult> findByIdAndUserId(UUID id, UUID userId);
}
