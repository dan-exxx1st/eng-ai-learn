package com.devlingo.web.controller;

import com.devlingo.domain.entity.User;
import com.devlingo.domain.enums.Specialization;
import com.devlingo.repository.UserRepository;
import com.devlingo.web.dto.ProfileResponse;
import com.devlingo.web.dto.UpdateProfileRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(@AuthenticationPrincipal UUID userId,
                                                          @RequestBody UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName() != null) user.setLastName(request.lastName());
        if (request.specialization() != null) {
            user.setSpecialization(Specialization.valueOf(request.specialization()));
        }

        user = userRepository.save(user);
        return ResponseEntity.ok(toResponse(user));
    }

    private ProfileResponse toResponse(User user) {
        return new ProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                user.getLanguageLevel() != null ? user.getLanguageLevel().name() : null,
                user.getSpecialization() != null ? user.getSpecialization().name() : null
        );
    }
}
