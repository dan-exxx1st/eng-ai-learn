package com.devlingo.web.controller;

import com.devlingo.service.DashboardService;
import com.devlingo.web.dto.DashboardSummary;
import com.devlingo.web.dto.LessonHistoryItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummary> getSummary(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(dashboardService.getSummary(userId));
    }

    @GetMapping("/history")
    public ResponseEntity<Page<LessonHistoryItem>> getLessonHistory(@AuthenticationPrincipal UUID userId,
                                                                     Pageable pageable) {
        return ResponseEntity.ok(dashboardService.getLessonHistory(userId, pageable));
    }
}
