package com.devlingo.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonAiService {

    private final ChatClient.Builder chatClientBuilder;

    @Value("classpath:prompts/lesson-content.st")
    private Resource promptTemplate;

    public String generateLessonContent(String title, String lessonType, String level, String specialization) {
        try {
            String prompt = promptTemplate.getContentAsString(StandardCharsets.UTF_8)
                    .replace("{title}", title)
                    .replace("{lessonType}", lessonType)
                    .replace("{level}", level)
                    .replace("{specialization}", specialization);

            String response = chatClientBuilder.build()
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();
            log.debug("Generated lesson content: {}", title);
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read prompt template", e);
        }
    }
}
