package com.devlingo.service;

import com.devlingo.domain.entity.UploadedMaterial;
import com.devlingo.domain.entity.User;
import com.devlingo.domain.enums.MaterialStatus;
import com.devlingo.file.FileStorageService;
import com.devlingo.file.TextExtractor;
import com.devlingo.repository.UploadedMaterialRepository;
import com.devlingo.repository.UserRepository;
import com.devlingo.web.dto.MaterialResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaterialService {

    private final UploadedMaterialRepository materialRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final List<TextExtractor> textExtractors;

    @Value("${app.upload.allowed-types}")
    private String allowedTypes;

    @Value("${app.upload.max-size}")
    private long maxSize;

    @Transactional
    public MaterialResponse upload(MultipartFile file, UUID uploadedBy) {
        validateFile(file);

        User user = userRepository.findById(uploadedBy)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String storedName = fileStorageService.store(file);

        UploadedMaterial material = UploadedMaterial.builder()
                .fileName(storedName)
                .originalName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .status(MaterialStatus.PROCESSING)
                .uploadedBy(user)
                .build();

        material = materialRepository.save(material);

        try {
            String extractedText = extractText(file);
            material.setExtractedText(extractedText);
            material.setStatus(MaterialStatus.PROCESSED);
        } catch (Exception e) {
            log.error("Failed to extract text from {}", file.getOriginalFilename(), e);
            material.setStatus(MaterialStatus.FAILED);
        }

        material = materialRepository.save(material);
        return toResponse(material);
    }

    public Page<MaterialResponse> list(Pageable pageable) {
        return materialRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toResponse);
    }

    @Transactional
    public void delete(UUID materialId) {
        UploadedMaterial material = materialRepository.findById(materialId)
                .orElseThrow(() -> new EntityNotFoundException("Material not found"));
        fileStorageService.delete(material.getFileName());
        materialRepository.delete(material);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File too large");
        }
        if (!allowedTypes.contains(file.getContentType())) {
            throw new IllegalArgumentException("File type not allowed: " + file.getContentType());
        }
    }

    private String extractText(MultipartFile file) {
        try {
            byte[] content = file.getBytes();
            return textExtractors.stream()
                    .filter(e -> e.supports(file.getContentType()))
                    .findFirst()
                    .map(e -> e.extract(content))
                    .orElseThrow(() -> new IllegalArgumentException("No extractor for type: " + file.getContentType()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file", e);
        }
    }

    private MaterialResponse toResponse(UploadedMaterial m) {
        return new MaterialResponse(m.getId(), m.getOriginalName(), m.getContentType(),
                m.getFileSize(), m.getStatus().name(), m.getCreatedAt());
    }
}
