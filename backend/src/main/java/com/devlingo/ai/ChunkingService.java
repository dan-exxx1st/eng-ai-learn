package com.devlingo.ai;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChunkingService {

    private static final int DEFAULT_CHUNK_SIZE = 500;
    private static final int DEFAULT_OVERLAP = 100;
    private static final Pattern SENTENCE_BOUNDARY = Pattern.compile("(?<=[.!?])\\s+");

    public List<String> chunkText(String text) {
        return chunkText(text, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP);
    }

    public List<String> chunkText(String text, int chunkSize, int overlap) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<String> sentences = splitSentences(text);
        List<String> chunks = new ArrayList<>();

        StringBuilder currentChunk = new StringBuilder();
        List<String> currentSentences = new ArrayList<>();

        for (String sentence : sentences) {
            if (currentChunk.length() + sentence.length() > chunkSize && !currentChunk.isEmpty()) {
                chunks.add(currentChunk.toString().trim());

                // Calculate overlap: keep trailing sentences that fit within overlap size
                currentChunk = new StringBuilder();
                int overlapLength = 0;
                List<String> overlapSentences = new ArrayList<>();

                for (int i = currentSentences.size() - 1; i >= 0; i--) {
                    String s = currentSentences.get(i);
                    if (overlapLength + s.length() > overlap) {
                        break;
                    }
                    overlapSentences.addFirst(s);
                    overlapLength += s.length();
                }

                currentSentences = new ArrayList<>(overlapSentences);
                for (String s : overlapSentences) {
                    currentChunk.append(s).append(" ");
                }
            }

            currentChunk.append(sentence).append(" ");
            currentSentences.add(sentence);
        }

        if (!currentChunk.isEmpty()) {
            String lastChunk = currentChunk.toString().trim();
            if (!lastChunk.isEmpty()) {
                chunks.add(lastChunk);
            }
        }

        return chunks;
    }

    private List<String> splitSentences(String text) {
        List<String> sentences = new ArrayList<>();
        Matcher matcher = SENTENCE_BOUNDARY.matcher(text);
        int lastEnd = 0;

        while (matcher.find()) {
            String sentence = text.substring(lastEnd, matcher.start()).trim();
            if (!sentence.isEmpty()) {
                sentences.add(sentence);
            }
            lastEnd = matcher.end();
        }

        // Add remaining text
        if (lastEnd < text.length()) {
            String remaining = text.substring(lastEnd).trim();
            if (!remaining.isEmpty()) {
                sentences.add(remaining);
            }
        }

        return sentences;
    }
}
