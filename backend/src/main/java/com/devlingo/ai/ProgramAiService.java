package com.devlingo.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgramAiService {

    private final ChatClient.Builder chatClientBuilder;
    private final RagService ragService;

    @Value("classpath:prompts/program-structure.st")
    private Resource promptTemplate;

    public String generateProgramStructure(String level, String specialization) {
        return generateProgramStructure(level, specialization, true);
    }

    public String generateProgramStructure(String level, String specialization, boolean useRag) {
        try {
            String context = "";
            if (useRag) {
                try {
                    String query = "learning program " + specialization + " " + level + " English";
                    List<String> relevantChunks = ragService.searchRelevantChunks(query, 3);
                    if (!relevantChunks.isEmpty()) {
                        context = String.join("\n\n", relevantChunks);
                        log.debug("Found {} relevant RAG chunks for program structure", relevantChunks.size());
                    }
                } catch (Exception e) {
                    log.warn("Failed to retrieve RAG context for program structure, proceeding without it", e);
                }
            }

            String prompt = promptTemplate.getContentAsString(StandardCharsets.UTF_8)
                    .replace("{level}", level)
                    .replace("{specialization}", specialization)
                    .replace("{context}", context);

            String response = chatClientBuilder.build()
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();
            log.debug("Generated program structure for {} at {} level", specialization, level);
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read prompt template", e);
        }
    }
}
