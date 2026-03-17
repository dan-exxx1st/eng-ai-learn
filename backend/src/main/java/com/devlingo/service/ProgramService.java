package com.devlingo.service;

import com.devlingo.ai.ProgramAiService;
import com.devlingo.domain.entity.LearningProgram;
import com.devlingo.domain.entity.Lesson;
import com.devlingo.domain.entity.Module;
import com.devlingo.domain.entity.User;
import com.devlingo.domain.enums.LessonType;
import com.devlingo.domain.enums.ModuleStatus;
import com.devlingo.repository.LearningProgramRepository;
import com.devlingo.repository.UserRepository;
import com.devlingo.web.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgramService {

    private final ProgramAiService aiService;
    private final LearningProgramRepository programRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ProgramResponse generateProgram(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (user.getLanguageLevel() == null) {
            throw new IllegalStateException("Please complete placement test first");
        }
        if (user.getSpecialization() == null) {
            throw new IllegalStateException("Please set your specialization in profile");
        }

        String structureJson = aiService.generateProgramStructure(
                user.getLanguageLevel().name(), user.getSpecialization().name());

        try {
            Map<String, Object> structure = objectMapper.readValue(structureJson, new TypeReference<>() {});

            LearningProgram program = LearningProgram.builder()
                    .user(user)
                    .title((String) structure.get("title"))
                    .description((String) structure.get("description"))
                    .languageLevel(user.getLanguageLevel())
                    .specialization(user.getSpecialization())
                    .build();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> modulesData = (List<Map<String, Object>>) structure.get("modules");

            for (int i = 0; i < modulesData.size(); i++) {
                Map<String, Object> moduleData = modulesData.get(i);
                Module module = Module.builder()
                        .program(program)
                        .title((String) moduleData.get("title"))
                        .description((String) moduleData.get("description"))
                        .orderIndex(i)
                        .status(i == 0 ? ModuleStatus.AVAILABLE : ModuleStatus.LOCKED)
                        .build();

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> lessonsData = (List<Map<String, Object>>) moduleData.get("lessons");

                for (int j = 0; j < lessonsData.size(); j++) {
                    Map<String, Object> lessonData = lessonsData.get(j);
                    Lesson lesson = Lesson.builder()
                            .module(module)
                            .title((String) lessonData.get("title"))
                            .lessonType(LessonType.valueOf((String) lessonData.get("lessonType")))
                            .orderIndex(j)
                            .build();
                    module.getLessons().add(lesson);
                }

                program.getModules().add(module);
            }

            program = programRepository.save(program);
            return toResponse(program);
        } catch (Exception e) {
            log.error("Failed to parse program structure", e);
            throw new RuntimeException("Failed to generate program", e);
        }
    }

    @Transactional(readOnly = true)
    public List<ProgramResponse> getUserPrograms(UUID userId) {
        return programRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProgramResponse getProgram(UUID programId, UUID userId) {
        LearningProgram program = programRepository.findByIdAndUserId(programId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Program not found"));
        return toResponse(program);
    }

    private ProgramResponse toResponse(LearningProgram program) {
        List<ModuleResponse> modules = program.getModules().stream()
                .map(m -> new ModuleResponse(
                        m.getId(),
                        m.getTitle(),
                        m.getDescription(),
                        m.getOrderIndex(),
                        m.getStatus().name(),
                        m.getLessons().stream()
                                .map(l -> new LessonSummary(l.getId(), l.getTitle(),
                                        l.getLessonType().name(), l.getOrderIndex(), l.isGenerated()))
                                .toList()
                ))
                .toList();

        return new ProgramResponse(
                program.getId(),
                program.getTitle(),
                program.getDescription(),
                program.getLanguageLevel().name(),
                program.getSpecialization().name(),
                program.getStatus().name(),
                modules
        );
    }
}
