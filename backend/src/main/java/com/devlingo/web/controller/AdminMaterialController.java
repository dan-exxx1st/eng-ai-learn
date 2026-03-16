package com.devlingo.web.controller;

import com.devlingo.service.MaterialService;
import com.devlingo.web.dto.MaterialResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/materials")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminMaterialController {

    private final MaterialService materialService;

    @PostMapping
    public ResponseEntity<MaterialResponse> upload(@RequestParam("file") MultipartFile file,
                                                    @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(materialService.upload(file, userId));
    }

    @GetMapping
    public ResponseEntity<Page<MaterialResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(materialService.list(pageable));
    }

    @DeleteMapping("/{materialId}")
    public ResponseEntity<Void> delete(@PathVariable UUID materialId) {
        materialService.delete(materialId);
        return ResponseEntity.noContent().build();
    }
}
