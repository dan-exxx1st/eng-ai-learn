package com.devlingo.web.controller;

import com.devlingo.service.PracticeService;
import com.devlingo.web.dto.PracticeMessageRequest;
import com.devlingo.web.dto.PracticeMessageResponse;
import com.devlingo.web.dto.PracticeSessionResponse;
import com.devlingo.web.dto.StartPracticeRequest;
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
@RequestMapping("/api/practice")
@RequiredArgsConstructor
public class PracticeController {

    private final PracticeService practiceService;

    @PostMapping("/start")
    public ResponseEntity<PracticeSessionResponse> startSession(@AuthenticationPrincipal UUID userId,
                                                                  @RequestBody StartPracticeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(practiceService.startSession(userId, request.scenarioType()));
    }

    @PostMapping("/message")
    public ResponseEntity<PracticeMessageResponse> sendMessage(@AuthenticationPrincipal UUID userId,
                                                                 @Valid @RequestBody PracticeMessageRequest request) {
        return ResponseEntity.ok(practiceService.sendMessage(userId, request));
    }

    @PostMapping("/{sessionId}/end")
    public ResponseEntity<PracticeSessionResponse> endSession(@AuthenticationPrincipal UUID userId,
                                                                @PathVariable UUID sessionId) {
        return ResponseEntity.ok(practiceService.endSession(userId, sessionId));
    }

    @GetMapping
    public ResponseEntity<Page<PracticeSessionResponse>> getSessions(@AuthenticationPrincipal UUID userId,
                                                                       Pageable pageable) {
        return ResponseEntity.ok(practiceService.getSessions(userId, pageable));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<PracticeSessionResponse> getSession(@AuthenticationPrincipal UUID userId,
                                                                @PathVariable UUID sessionId) {
        return ResponseEntity.ok(practiceService.getSession(userId, sessionId));
    }
}
