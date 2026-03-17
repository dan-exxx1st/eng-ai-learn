package com.devlingo.service;

import com.devlingo.domain.entity.User;
import com.devlingo.domain.entity.UserVocabulary;
import com.devlingo.repository.UserRepository;
import com.devlingo.repository.UserVocabularyRepository;
import com.devlingo.web.dto.AddWordRequest;
import com.devlingo.web.dto.ReviewWordRequest;
import com.devlingo.web.dto.ReviewWordResponse;
import com.devlingo.web.dto.VocabularyResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VocabularyService {

    private final UserVocabularyRepository vocabularyRepository;
    private final UserRepository userRepository;
    private final ChatClient.Builder chatClientBuilder;

    @Transactional
    public VocabularyResponse addWord(UUID userId, AddWordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // If word already exists, just return it
        var existing = vocabularyRepository.findByUserIdAndWord(userId, request.word().trim().toLowerCase());
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        String translation = request.translation();
        if (translation == null || translation.isBlank()) {
            translation = autoTranslate(request.word().trim(), request.context());
        }

        UserVocabulary vocab = UserVocabulary.builder()
                .user(user)
                .word(request.word().trim().toLowerCase())
                .translation(translation)
                .context(request.context())
                .source(request.source())
                .build();

        return toResponse(vocabularyRepository.save(vocab));
    }

    public Page<VocabularyResponse> getWords(UUID userId, Boolean learned, Pageable pageable) {
        if (learned != null) {
            return vocabularyRepository.findByUserIdAndLearnedOrderByCreatedAtDesc(userId, learned, pageable)
                    .map(this::toResponse);
        }
        return vocabularyRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public VocabularyResponse toggleLearned(UUID userId, UUID wordId) {
        UserVocabulary vocab = vocabularyRepository.findById(wordId)
                .orElseThrow(() -> new EntityNotFoundException("Word not found"));
        if (!vocab.getUser().getId().equals(userId)) {
            throw new EntityNotFoundException("Word not found");
        }
        vocab.setLearned(!vocab.isLearned());
        return toResponse(vocabularyRepository.save(vocab));
    }

    @Transactional
    public void deleteWord(UUID userId, UUID wordId) {
        UserVocabulary vocab = vocabularyRepository.findById(wordId)
                .orElseThrow(() -> new EntityNotFoundException("Word not found"));
        if (!vocab.getUser().getId().equals(userId)) {
            throw new EntityNotFoundException("Word not found");
        }
        vocabularyRepository.delete(vocab);
    }

    public Page<VocabularyResponse> getWordsForReview(UUID userId, Pageable pageable) {
        return vocabularyRepository.findByUserIdAndNextReviewAtBeforeOrderByNextReviewAtAsc(
                userId, LocalDateTime.now(), pageable
        ).map(this::toResponse);
    }

    public long getReviewCount(UUID userId) {
        return vocabularyRepository.countByUserIdAndNextReviewAtBefore(userId, LocalDateTime.now());
    }

    @Transactional
    public ReviewWordResponse reviewWord(UUID userId, ReviewWordRequest request) {
        UserVocabulary vocab = vocabularyRepository.findById(request.wordId())
                .orElseThrow(() -> new EntityNotFoundException("Word not found"));
        if (!vocab.getUser().getId().equals(userId)) {
            throw new EntityNotFoundException("Word not found");
        }

        int quality = request.quality();
        int repetitions = vocab.getRepetitions();
        int intervalDays = vocab.getIntervalDays();
        double easeFactor = vocab.getEaseFactor();

        // SM-2 Algorithm
        if (quality < 3) {
            repetitions = 0;
            intervalDays = 1;
        } else {
            if (repetitions == 0) {
                intervalDays = 1;
            } else if (repetitions == 1) {
                intervalDays = 6;
            } else {
                intervalDays = (int) (intervalDays * easeFactor);
            }
            repetitions++;
        }

        easeFactor = easeFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        if (easeFactor < 1.3) {
            easeFactor = 1.3;
        }

        LocalDateTime nextReviewAt = LocalDateTime.now().plusDays(intervalDays);
        boolean learned = repetitions >= 3 && quality >= 3;

        vocab.setRepetitions(repetitions);
        vocab.setIntervalDays(intervalDays);
        vocab.setEaseFactor(easeFactor);
        vocab.setNextReviewAt(nextReviewAt);
        vocab.setLearned(learned);

        vocabularyRepository.save(vocab);

        return new ReviewWordResponse(
                vocab.getId(),
                vocab.getWord(),
                vocab.getEaseFactor(),
                vocab.getIntervalDays(),
                vocab.getNextReviewAt()
        );
    }

    public String translateText(String text) {
        return autoTranslate(text, null);
    }

    private String autoTranslate(String word, String context) {
        try {
            String contextHint = (context != null && !context.isBlank())
                    ? "\nContext sentence: \"" + context + "\"" : "";
            String prompt = "You are an English-Russian dictionary. " +
                    "Translate the English word to Russian. " +
                    "Reply with ONLY the Russian translation word(s), nothing else. " +
                    "No explanations, no transliteration, no quotes, no punctuation.\n\n" +
                    "English: hello\nRussian: привет\n\n" +
                    "English: simultaneously\nRussian: одновременно\n\n" +
                    "English: " + word + contextHint + "\nRussian:";
            String result = chatClientBuilder.build()
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();
            if (result == null) return null;
            // Clean up: take only first line, remove quotes/punctuation
            String cleaned = result.trim().split("\n")[0]
                    .replaceAll("^\"|\"$", "")
                    .replaceAll("^\\*+|\\*+$", "")
                    .trim();
            return cleaned.isEmpty() ? null : cleaned;
        } catch (Exception e) {
            log.warn("Failed to auto-translate '{}': {}", word, e.getMessage());
            return null;
        }
    }

    private VocabularyResponse toResponse(UserVocabulary v) {
        return new VocabularyResponse(v.getId(), v.getWord(), v.getTranslation(),
                v.getContext(), v.getSource(), v.isLearned(),
                v.getEaseFactor(), v.getIntervalDays(), v.getNextReviewAt(),
                v.getCreatedAt());
    }
}
