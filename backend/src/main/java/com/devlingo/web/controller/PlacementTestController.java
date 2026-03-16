package com.devlingo.web.controller;

import com.devlingo.service.PlacementTestService;
import com.devlingo.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/placement-test")
@RequiredArgsConstructor
public class PlacementTestController {

    private final PlacementTestService testService;

    @PostMapping("/start")
    public ResponseEntity<TestResultResponse> startTest(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(testService.startTest(userId));
    }

    @GetMapping("/{testId}/question/{index}")
    public ResponseEntity<QuestionDto> getQuestion(@PathVariable UUID testId,
                                                    @PathVariable int index,
                                                    @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(testService.getQuestion(testId, userId, index));
    }

    @PostMapping("/answer")
    public ResponseEntity<AnswerResponse> submitAnswer(@AuthenticationPrincipal UUID userId,
                                                        @Valid @RequestBody AnswerRequest request) {
        return ResponseEntity.ok(testService.submitAnswer(userId, request));
    }

    @GetMapping("/{testId}/result")
    public ResponseEntity<TestResultResponse> getResult(@PathVariable UUID testId,
                                                         @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(testService.getResult(testId, userId));
    }
}
