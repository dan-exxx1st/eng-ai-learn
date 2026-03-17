package com.devlingo.service;

import com.devlingo.domain.entity.PlacementTestResult;
import com.devlingo.domain.entity.User;
import com.devlingo.domain.enums.LanguageLevel;
import com.devlingo.repository.PlacementTestResultRepository;
import com.devlingo.repository.UserRepository;
import com.devlingo.web.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlacementTestService {

    private static final String[] LEVELS = {"A1", "A2", "B1", "B2", "C1"};
    private static final int QUESTIONS_PER_LEVEL = 4;
    private static final int TOTAL_QUESTIONS = LEVELS.length * QUESTIONS_PER_LEVEL;

    private final QuestionGeneratorService generatorService;
    private final PlacementTestResultRepository testResultRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public TestResultResponse startTest(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        PlacementTestResult test = PlacementTestResult.builder()
                .user(user)
                .questions("[]")
                .answers("[]")
                .build();

        test = testResultRepository.save(test);
        UUID testId = test.getId();

        // Kick off generation of all batches in background via separate bean (@Async works)
        generatorService.initCache(testId);
        for (int i = 0; i < LEVELS.length; i++) {
            generatorService.generateBatchAsync(testId, i);
        }

        return new TestResultResponse(testId, null, 0, TOTAL_QUESTIONS, false, null);
    }

    @Transactional
    public QuestionDto getQuestion(UUID testId, UUID userId, int index) {
        PlacementTestResult test = testResultRepository.findByIdAndUserId(testId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Test not found"));

        if (index < 0 || index >= TOTAL_QUESTIONS) {
            throw new IllegalArgumentException("Invalid question index");
        }

        List<Map<String, Object>> questions = parseQuestions(test.getQuestions());

        int batchIndex = index / QUESTIONS_PER_LEVEL;
        if (index >= questions.size()) {
            // Wait for batches to be ready and append
            for (int b = questions.size() / QUESTIONS_PER_LEVEL; b <= batchIndex; b++) {
                List<Map<String, Object>> batch = generatorService.waitForBatch(testId, b);
                if (batch != null) {
                    questions.addAll(batch);
                }
            }

            try {
                test.setQuestions(objectMapper.writeValueAsString(questions));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize questions", e);
            }
            testResultRepository.save(test);
        }

        Map<String, Object> q = questions.get(index);
        @SuppressWarnings("unchecked")
        List<String> options = (List<String>) q.get("options");
        return new QuestionDto(index, (String) q.get("question"), options, (String) q.get("difficulty"));
    }

    @Transactional
    public AnswerResponse submitAnswer(UUID userId, AnswerRequest request) {
        PlacementTestResult test = testResultRepository.findByIdAndUserId(request.testId(), userId)
                .orElseThrow(() -> new EntityNotFoundException("Test not found"));

        if (test.isCompleted()) {
            throw new IllegalStateException("Test already completed");
        }

        List<Map<String, Object>> questions = parseQuestions(test.getQuestions());
        List<Map<String, String>> answers = parseAnswers(test.getAnswers());

        Map<String, Object> question = questions.get(request.questionIndex());
        String correctAnswer = (String) question.get("correctAnswer");
        boolean correct = correctAnswer.equalsIgnoreCase(request.answer());

        Map<String, String> answerEntry = new HashMap<>();
        answerEntry.put("questionIndex", String.valueOf(request.questionIndex()));
        answerEntry.put("answer", request.answer());
        answerEntry.put("correct", String.valueOf(correct));
        answers.add(answerEntry);

        try {
            test.setAnswers(objectMapper.writeValueAsString(answers));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize answers", e);
        }
        testResultRepository.save(test);

        return new AnswerResponse(
                correct,
                correctAnswer,
                (String) question.get("explanation"),
                answers.size(),
                TOTAL_QUESTIONS
        );
    }

    @Transactional
    public TestResultResponse getResult(UUID testId, UUID userId) {
        PlacementTestResult test = testResultRepository.findByIdAndUserId(testId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Test not found"));

        if (!test.isCompleted()) {
            List<Map<String, String>> answers = parseAnswers(test.getAnswers());

            if (answers.size() < TOTAL_QUESTIONS) {
                return new TestResultResponse(test.getId(), null, answers.size(), TOTAL_QUESTIONS, false, null);
            }

            int score = (int) answers.stream().filter(a -> "true".equals(a.get("correct"))).count();
            LanguageLevel level = determineLevel(score, TOTAL_QUESTIONS);

            test.setScore(score);
            test.setDeterminedLevel(level);
            test.setCompleted(true);
            test.setCompletedAt(LocalDateTime.now());
            testResultRepository.save(test);

            User user = test.getUser();
            user.setLanguageLevel(level);
            userRepository.save(user);
        }

        // Cleanup cache
        generatorService.cleanupCache(testId);

        // Build detailed results
        List<Map<String, Object>> questions = parseQuestions(test.getQuestions());
        List<Map<String, String>> allAnswers = parseAnswers(test.getAnswers());
        List<TestResultResponse.QuestionResult> details = new ArrayList<>();

        for (int i = 0; i < questions.size(); i++) {
            Map<String, Object> q = questions.get(i);
            String userAnswer = null;
            boolean correct = false;
            for (Map<String, String> a : allAnswers) {
                if (String.valueOf(i).equals(a.get("questionIndex"))) {
                    userAnswer = a.get("answer");
                    correct = "true".equals(a.get("correct"));
                    break;
                }
            }
            details.add(new TestResultResponse.QuestionResult(
                    i,
                    (String) q.get("question"),
                    (String) q.get("difficulty"),
                    userAnswer,
                    (String) q.get("correctAnswer"),
                    correct,
                    (String) q.get("explanation")
            ));
        }

        return new TestResultResponse(
                test.getId(),
                test.getDeterminedLevel().name(),
                test.getScore(),
                TOTAL_QUESTIONS,
                true,
                details
        );
    }

    private LanguageLevel determineLevel(int score, int total) {
        double percentage = (double) score / total * 100;
        if (percentage >= 85) return LanguageLevel.C1;
        if (percentage >= 70) return LanguageLevel.B2;
        if (percentage >= 55) return LanguageLevel.B1;
        if (percentage >= 35) return LanguageLevel.A2;
        return LanguageLevel.A1;
    }

    private List<Map<String, Object>> parseQuestions(String json) {
        try {
            return new ArrayList<>(objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {}));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse questions", e);
        }
    }

    private List<Map<String, String>> parseAnswers(String json) {
        try {
            return new ArrayList<>(objectMapper.readValue(json, new TypeReference<List<Map<String, String>>>() {}));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse answers", e);
        }
    }
}
