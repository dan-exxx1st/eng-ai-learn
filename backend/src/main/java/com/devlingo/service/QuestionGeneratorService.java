package com.devlingo.service;

import com.devlingo.ai.PlacementTestAiService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionGeneratorService {

    private final PlacementTestAiService aiService;
    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, List<Map<String, Object>>>> prefetchCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> generatingBatches = new ConcurrentHashMap<>();

    private static final String[] LEVELS = {"A1", "A2", "B1", "B2", "C1"};

    public void initCache(UUID testId) {
        prefetchCache.put(testId, new ConcurrentHashMap<>());
    }

    public void cleanupCache(UUID testId) {
        prefetchCache.remove(testId);
    }

    @Async
    public void generateBatchAsync(UUID testId, int batchIndex) {
        String key = testId + "-" + batchIndex;
        if (generatingBatches.putIfAbsent(key, true) != null) {
            return;
        }
        try {
            String level = LEVELS[batchIndex];
            log.info("Async generating questions for level {} (batch {}) testId={}", level, batchIndex, testId);
            String batchJson = aiService.generateQuestionsForLevel(level);
            List<Map<String, Object>> batch = objectMapper.readValue(batchJson, new TypeReference<>() {});

            var testCache = prefetchCache.get(testId);
            if (testCache != null) {
                testCache.put(batchIndex, batch);
            }
            log.info("Batch {} (level {}) ready for testId={}", batchIndex, level, testId);
        } catch (Exception e) {
            log.error("Failed to generate batch {} for testId={}", batchIndex, testId, e);
        } finally {
            generatingBatches.remove(key);
        }
    }

    public List<Map<String, Object>> waitForBatch(UUID testId, int batchIndex) {
        var testCache = prefetchCache.get(testId);
        if (testCache == null) {
            log.warn("No cache for testId={}, generating synchronously", testId);
            return generateSync(batchIndex);
        }

        for (int i = 0; i < 240; i++) {
            List<Map<String, Object>> batch = testCache.get(batchIndex);
            if (batch != null) return batch;
            try { Thread.sleep(500); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted", e);
            }
        }
        log.warn("Timeout waiting for batch {}, generating synchronously", batchIndex);
        return generateSync(batchIndex);
    }

    private List<Map<String, Object>> generateSync(int batchIndex) {
        try {
            String batchJson = aiService.generateQuestionsForLevel(LEVELS[batchIndex]);
            return objectMapper.readValue(batchJson, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate questions", e);
        }
    }
}
