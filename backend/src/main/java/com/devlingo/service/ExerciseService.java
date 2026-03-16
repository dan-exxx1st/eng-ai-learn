package com.devlingo.service;

import com.devlingo.domain.entity.Exercise;
import com.devlingo.domain.enums.ExerciseType;
import com.devlingo.repository.ExerciseRepository;
import com.devlingo.web.dto.ExerciseResponse;
import com.devlingo.web.dto.ExerciseSubmitRequest;
import com.devlingo.web.dto.ExerciseSubmitResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ChatClient.Builder chatClientBuilder;

    public List<ExerciseResponse> getExercises(UUID lessonId) {
        return exerciseRepository.findByLessonIdOrderByOrderIndex(lessonId)
                .stream()
                .map(e -> new ExerciseResponse(
                        e.getId(),
                        e.getExerciseType().name(),
                        e.getDifficulty().name(),
                        e.getQuestion(),
                        e.getOptions(),
                        e.getOrderIndex()
                ))
                .toList();
    }

    public ExerciseSubmitResponse submitAnswer(ExerciseSubmitRequest request) {
        Exercise exercise = exerciseRepository.findById(request.exerciseId())
                .orElseThrow(() -> new EntityNotFoundException("Exercise not found"));

        if (exercise.getExerciseType() == ExerciseType.FREE_TEXT) {
            return evaluateFreeText(exercise, request.answer());
        }

        boolean correct = exercise.getCorrectAnswer() != null &&
                exercise.getCorrectAnswer().equalsIgnoreCase(request.answer().trim());

        return new ExerciseSubmitResponse(
                correct,
                exercise.getCorrectAnswer(),
                exercise.getExplanation(),
                null
        );
    }

    private ExerciseSubmitResponse evaluateFreeText(Exercise exercise, String answer) {
        String prompt = String.format(
                "Evaluate this English exercise answer. Question: '%s'. Student answer: '%s'. " +
                "Is it correct/acceptable? Provide brief feedback. " +
                "Return JSON: {\"correct\": true/false, \"feedback\": \"your feedback\"}",
                exercise.getQuestion(), answer);

        String response = chatClientBuilder.build()
                .prompt()
                .user(prompt)
                .call()
                .content();

        // Simple parsing — in production use proper JSON parsing
        boolean correct = response != null && response.toLowerCase().contains("\"correct\": true");
        return new ExerciseSubmitResponse(correct, null, exercise.getExplanation(), response);
    }
}
