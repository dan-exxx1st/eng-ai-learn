package com.devlingo.service;

import com.devlingo.domain.entity.User;
import com.devlingo.domain.enums.ProgramStatus;
import com.devlingo.repository.LearningProgramRepository;
import com.devlingo.repository.UserProgressRepository;
import com.devlingo.repository.UserRepository;
import com.devlingo.web.dto.DashboardSummary;
import com.devlingo.web.dto.LessonHistoryItem;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final UserProgressRepository progressRepository;
    private final LearningProgramRepository programRepository;

    public DashboardSummary getSummary(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        long completed = progressRepository.countCompletedByUserId(userId);
        long total = progressRepository.countTotalLessonsByUserId(userId);
        int percentage = total > 0 ? (int) (completed * 100 / total) : 0;

        int activePrograms = (int) programRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(p -> p.getStatus() == ProgramStatus.ACTIVE)
                .count();

        return new DashboardSummary(
                user.getLanguageLevel() != null ? user.getLanguageLevel().name() : null,
                completed,
                total,
                percentage,
                activePrograms
        );
    }

    public Page<LessonHistoryItem> getLessonHistory(UUID userId, Pageable pageable) {
        return progressRepository.findByUserIdAndCompletedTrueOrderByCompletedAtDesc(userId, pageable)
                .map(p -> new LessonHistoryItem(
                        p.getLesson().getId(),
                        p.getLesson().getTitle(),
                        p.getLesson().getLessonType().name(),
                        p.getScore(),
                        p.getCompletedAt()
                ));
    }
}
