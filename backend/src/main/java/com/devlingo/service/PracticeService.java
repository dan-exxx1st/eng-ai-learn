package com.devlingo.service;

import com.devlingo.domain.entity.PracticeSession;
import com.devlingo.domain.entity.User;
import com.devlingo.domain.enums.ScenarioType;
import com.devlingo.repository.PracticeSessionRepository;
import com.devlingo.repository.UserRepository;
import com.devlingo.web.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PracticeService {

    private final PracticeSessionRepository practiceSessionRepository;
    private final UserRepository userRepository;
    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("classpath:prompts/practice-chat.st")
    private Resource chatPromptTemplate;

    @Value("classpath:prompts/practice-feedback.st")
    private Resource feedbackPromptTemplate;

    private static final Map<ScenarioType, String> SCENARIO_DESCRIPTIONS = Map.of(
            ScenarioType.CODE_REVIEW,
            "You are a senior developer reviewing the student's pull request. Ask about code decisions, suggest improvements, discuss best practices.",
            ScenarioType.JOB_INTERVIEW,
            "You are a technical interviewer at a FAANG company. Ask about experience, technical concepts, and problem-solving approach. Mix behavioral and technical questions.",
            ScenarioType.DAILY_STANDUP,
            "You are a team lead running a daily standup meeting. Ask what the student worked on yesterday, plans for today, and any blockers. Follow up on their answers."
    );

    @Transactional
    public PracticeSessionResponse startSession(UUID userId, String scenarioType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        ScenarioType type = ScenarioType.valueOf(scenarioType.toUpperCase());
        String description = SCENARIO_DESCRIPTIONS.get(type);

        // Generate opening message from the character
        String openingMessage = generateAiResponse(type, description, "[]", "Hello, let's start.");

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "assistant", "content", openingMessage));

        String messagesJson = serializeMessages(messages);

        PracticeSession session = PracticeSession.builder()
                .user(user)
                .scenarioType(type.name())
                .messages(messagesJson)
                .build();

        session = practiceSessionRepository.save(session);

        return toResponse(session, messages);
    }

    @Transactional
    public PracticeMessageResponse sendMessage(UUID userId, PracticeMessageRequest request) {
        PracticeSession session = practiceSessionRepository.findByIdAndUserId(request.sessionId(), userId)
                .orElseThrow(() -> new EntityNotFoundException("Practice session not found"));

        if (session.isCompleted()) {
            throw new IllegalStateException("Session is already completed");
        }

        ScenarioType type = ScenarioType.valueOf(session.getScenarioType());
        String description = SCENARIO_DESCRIPTIONS.get(type);

        List<Map<String, String>> messages = deserializeMessages(session.getMessages());

        // Add user message
        messages.add(Map.of("role", "user", "content", request.message()));

        // Generate AI response
        String history = formatHistory(messages);
        String aiResponse = generateAiResponse(type, description, history, request.message());

        // Add assistant message
        messages.add(Map.of("role", "assistant", "content", aiResponse));

        session.setMessages(serializeMessages(messages));
        practiceSessionRepository.save(session);

        return new PracticeMessageResponse("assistant", aiResponse);
    }

    @Transactional
    public PracticeSessionResponse endSession(UUID userId, UUID sessionId) {
        PracticeSession session = practiceSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Practice session not found"));

        if (session.isCompleted()) {
            throw new IllegalStateException("Session is already completed");
        }

        User user = session.getUser();
        String level = user.getLanguageLevel() != null ? user.getLanguageLevel().name() : "UNKNOWN";

        List<Map<String, String>> messages = deserializeMessages(session.getMessages());
        String conversation = formatHistory(messages);

        // Generate feedback
        String feedbackJson = generateFeedback(session.getScenarioType(), level, conversation);

        // Parse score from feedback JSON
        Integer score = parseScoreFromFeedback(feedbackJson);

        session.setFeedback(feedbackJson);
        session.setScore(score);
        session.setCompleted(true);
        session.setCompletedAt(LocalDateTime.now());

        session = practiceSessionRepository.save(session);

        return toResponse(session, messages);
    }

    public Page<PracticeSessionResponse> getSessions(UUID userId, Pageable pageable) {
        return practiceSessionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(session -> toResponse(session, deserializeMessages(session.getMessages())));
    }

    public PracticeSessionResponse getSession(UUID userId, UUID sessionId) {
        PracticeSession session = practiceSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Practice session not found"));
        return toResponse(session, deserializeMessages(session.getMessages()));
    }

    private String generateAiResponse(ScenarioType type, String description, String history, String message) {
        try {
            String prompt = chatPromptTemplate.getContentAsString(StandardCharsets.UTF_8)
                    .replace("{scenario}", type.name())
                    .replace("{scenarioDescription}", description)
                    .replace("{history}", history)
                    .replace("{message}", message);

            return chatClientBuilder.build()
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read chat prompt template", e);
        }
    }

    private String generateFeedback(String scenario, String level, String conversation) {
        try {
            String prompt = feedbackPromptTemplate.getContentAsString(StandardCharsets.UTF_8)
                    .replace("{scenario}", scenario)
                    .replace("{level}", level)
                    .replace("{conversation}", conversation);

            return chatClientBuilder.build()
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read feedback prompt template", e);
        }
    }

    private Integer parseScoreFromFeedback(String feedbackJson) {
        try {
            JsonNode node = objectMapper.readTree(feedbackJson);
            if (node.has("score")) {
                return node.get("score").asInt();
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse score from feedback JSON", e);
        }
        return null;
    }

    private String formatHistory(List<Map<String, String>> messages) {
        StringBuilder sb = new StringBuilder();
        for (Map<String, String> msg : messages) {
            String role = msg.get("role");
            String content = msg.get("content");
            sb.append(role.equals("assistant") ? "Character" : "Student")
                    .append(": ")
                    .append(content)
                    .append("\n");
        }
        return sb.toString();
    }

    private List<Map<String, String>> deserializeMessages(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize messages", e);
            return new ArrayList<>();
        }
    }

    private String serializeMessages(List<Map<String, String>> messages) {
        try {
            return objectMapper.writeValueAsString(messages);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize messages", e);
        }
    }

    private PracticeSessionResponse toResponse(PracticeSession session, List<Map<String, String>> messages) {
        List<PracticeMessageResponse> messageResponses = messages.stream()
                .map(m -> new PracticeMessageResponse(m.get("role"), m.get("content")))
                .toList();

        return new PracticeSessionResponse(
                session.getId(),
                session.getScenarioType(),
                messageResponses,
                session.getFeedback(),
                session.getScore(),
                session.isCompleted()
        );
    }
}
