package com.devlingo.web.controller;

import com.devlingo.service.ExerciseService;
import com.devlingo.web.dto.ExerciseResponse;
import com.devlingo.web.dto.ExerciseSubmitRequest;
import com.devlingo.web.dto.ExerciseSubmitResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<ExerciseResponse>> getExercises(@PathVariable UUID lessonId) {
        return ResponseEntity.ok(exerciseService.getExercises(lessonId));
    }

    @PostMapping("/submit")
    public ResponseEntity<ExerciseSubmitResponse> submitAnswer(@Valid @RequestBody ExerciseSubmitRequest request) {
        return ResponseEntity.ok(exerciseService.submitAnswer(request));
    }
}
