package com.devlingo.service;

import com.devlingo.ai.LessonAiService;
import com.devlingo.domain.entity.Exercise;
import com.devlingo.domain.entity.Lesson;
import com.devlingo.domain.entity.UserProgress;
import com.devlingo.domain.enums.Difficulty;
import com.devlingo.domain.enums.ExerciseType;
import com.devlingo.repository.LessonRepository;
import com.devlingo.repository.UserProgressRepository;
import com.devlingo.web.dto.LessonResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonService {

    private final LessonAiService aiService;
    private final LessonRepository lessonRepository;
    private final UserProgressRepository progressRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public LessonResponse getLesson(UUID lessonId, UUID userId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found"));

        if (!lesson.isGenerated()) {
            generateContent(lesson);
        }

        return new LessonResponse(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getLessonType().name(),
                lesson.getContent(),
                lesson.isGenerated()
        );
    }

    @Transactional
    public void completeLesson(UUID lessonId, UUID userId, Integer score) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found"));

        UserProgress progress = progressRepository.findByUserIdAndLessonId(userId, lessonId)
                .orElse(UserProgress.builder()
                        .user(lesson.getModule().getProgram().getUser())
                        .lesson(lesson)
                        .build());

        progress.setCompleted(true);
        progress.setScore(score);
        progress.setCompletedAt(LocalDateTime.now());
        progressRepository.save(progress);
    }

    private void generateContent(Lesson lesson) {
        var module = lesson.getModule();
        var program = module.getProgram();

        String contentJson = aiService.generateLessonContent(
                lesson.getTitle(),
                lesson.getLessonType().name(),
                program.getLanguageLevel().name(),
                program.getSpecialization().name()
        );

        try {
            Map<String, Object> content = objectMapper.readValue(contentJson, new TypeReference<>() {});

            lesson.setContent(contentJson);
            lesson.setGenerated(true);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> exercisesData = (List<Map<String, Object>>) content.get("exercises");
            if (exercisesData != null) {
                for (int i = 0; i < exercisesData.size(); i++) {
                    Map<String, Object> ex = exercisesData.get(i);
                    Exercise exercise = Exercise.builder()
                            .lesson(lesson)
                            .exerciseType(ExerciseType.valueOf((String) ex.get("exerciseType")))
                            .difficulty(ex.containsKey("difficulty") ?
                                    Difficulty.valueOf((String) ex.get("difficulty")) : Difficulty.MEDIUM)
                            .question((String) ex.get("question"))
                            .options(ex.containsKey("options") ?
                                    objectMapper.writeValueAsString(ex.get("options")) : null)
                            .correctAnswer((String) ex.get("correctAnswer"))
                            .explanation((String) ex.get("explanation"))
                            .orderIndex(i)
                            .build();
                    lesson.getExercises().add(exercise);
                }
            }

            lessonRepository.save(lesson);
        } catch (Exception e) {
            log.error("Failed to generate lesson content", e);
            throw new RuntimeException("Failed to generate lesson content", e);
        }
    }
}
