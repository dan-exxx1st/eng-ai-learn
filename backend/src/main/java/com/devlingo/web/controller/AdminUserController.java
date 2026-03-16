package com.devlingo.web.controller;

import com.devlingo.service.AdminUserService;
import com.devlingo.web.dto.AdminUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<Page<AdminUserResponse>> getUsers(
            @RequestParam(required = false) String search, Pageable pageable) {
        return ResponseEntity.ok(adminUserService.getUsers(search, pageable));
    }

    @PostMapping("/{userId}/block")
    public ResponseEntity<AdminUserResponse> blockUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(adminUserService.blockUser(userId));
    }

    @PostMapping("/{userId}/unblock")
    public ResponseEntity<AdminUserResponse> unblockUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(adminUserService.unblockUser(userId));
    }
}
