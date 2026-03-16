package com.devlingo.service;

import com.devlingo.domain.entity.User;
import com.devlingo.repository.UserRepository;
import com.devlingo.web.dto.AdminUserResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    public Page<AdminUserResponse> getUsers(String search, Pageable pageable) {
        Page<User> users;
        if (search != null && !search.isBlank()) {
            users = userRepository.searchUsers(search, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        return users.map(this::toResponse);
    }

    @Transactional
    public AdminUserResponse blockUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setBlocked(true);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public AdminUserResponse unblockUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setBlocked(false);
        return toResponse(userRepository.save(user));
    }

    private AdminUserResponse toResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                user.getLanguageLevel() != null ? user.getLanguageLevel().name() : null,
                user.getSpecialization() != null ? user.getSpecialization().name() : null,
                user.isBlocked(),
                user.getCreatedAt()
        );
    }
}
