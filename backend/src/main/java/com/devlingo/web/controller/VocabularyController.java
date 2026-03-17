package com.devlingo.web.controller;

import com.devlingo.service.VocabularyService;
import com.devlingo.web.dto.AddWordRequest;
import com.devlingo.web.dto.ReviewWordRequest;
import com.devlingo.web.dto.ReviewWordResponse;
import com.devlingo.web.dto.VocabularyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/vocabulary")
@RequiredArgsConstructor
public class VocabularyController {

    private final VocabularyService vocabularyService;

    @PostMapping
    public ResponseEntity<VocabularyResponse> addWord(@AuthenticationPrincipal UUID userId,
                                                       @Valid @RequestBody AddWordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vocabularyService.addWord(userId, request));
    }

    @GetMapping
    public ResponseEntity<Page<VocabularyResponse>> getWords(@AuthenticationPrincipal UUID userId,
                                                              @RequestParam(required = false) Boolean learned,
                                                              Pageable pageable) {
        return ResponseEntity.ok(vocabularyService.getWords(userId, learned, pageable));
    }

    @PostMapping("/{wordId}/toggle-learned")
    public ResponseEntity<VocabularyResponse> toggleLearned(@AuthenticationPrincipal UUID userId,
                                                             @PathVariable UUID wordId) {
        return ResponseEntity.ok(vocabularyService.toggleLearned(userId, wordId));
    }

    @DeleteMapping("/{wordId}")
    public ResponseEntity<Void> deleteWord(@AuthenticationPrincipal UUID userId,
                                            @PathVariable UUID wordId) {
        vocabularyService.deleteWord(userId, wordId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/review")
    public ResponseEntity<Page<VocabularyResponse>> getWordsForReview(@AuthenticationPrincipal UUID userId,
                                                                       Pageable pageable) {
        return ResponseEntity.ok(vocabularyService.getWordsForReview(userId, pageable));
    }

    @PostMapping("/review")
    public ResponseEntity<ReviewWordResponse> reviewWord(@AuthenticationPrincipal UUID userId,
                                                          @Valid @RequestBody ReviewWordRequest request) {
        return ResponseEntity.ok(vocabularyService.reviewWord(userId, request));
    }

    @GetMapping("/review/count")
    public ResponseEntity<Long> getReviewCount(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(vocabularyService.getReviewCount(userId));
    }

    @PostMapping("/translate")
    public ResponseEntity<java.util.Map<String, String>> translate(@RequestBody java.util.Map<String, String> body) {
        String text = body.get("text");
        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        String translation = vocabularyService.translateText(text);
        return ResponseEntity.ok(java.util.Map.of("text", text, "translation", translation != null ? translation : ""));
    }
}
