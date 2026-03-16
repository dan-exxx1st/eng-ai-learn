package com.devlingo.web.controller;

import com.devlingo.service.LessonService;
import com.devlingo.web.dto.LessonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @GetMapping("/{lessonId}")
    public ResponseEntity<LessonResponse> getLesson(@PathVariable UUID lessonId,
                                                     @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(lessonService.getLesson(lessonId, userId));
    }

    @PostMapping("/{lessonId}/complete")
    public ResponseEntity<Void> completeLesson(@PathVariable UUID lessonId,
                                                @AuthenticationPrincipal UUID userId,
                                                @RequestParam(required = false) Integer score) {
        lessonService.completeLesson(lessonId, userId, score);
        return ResponseEntity.ok().build();
    }
}
