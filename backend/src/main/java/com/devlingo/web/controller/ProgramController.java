package com.devlingo.web.controller;

import com.devlingo.service.ProgramService;
import com.devlingo.web.dto.ProgramResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/programs")
@RequiredArgsConstructor
public class ProgramController {

    private final ProgramService programService;

    @PostMapping("/generate")
    public ResponseEntity<ProgramResponse> generateProgram(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(programService.generateProgram(userId));
    }

    @GetMapping
    public ResponseEntity<List<ProgramResponse>> getUserPrograms(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(programService.getUserPrograms(userId));
    }

    @GetMapping("/{programId}")
    public ResponseEntity<ProgramResponse> getProgram(@PathVariable UUID programId,
                                                       @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(programService.getProgram(programId, userId));
    }
}
