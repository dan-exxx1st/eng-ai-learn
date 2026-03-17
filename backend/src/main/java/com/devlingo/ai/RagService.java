package com.devlingo.ai;

import com.devlingo.domain.entity.DocumentChunk;
import com.devlingo.domain.entity.UploadedMaterial;
import com.devlingo.repository.DocumentChunkRepository;
import com.devlingo.repository.UploadedMaterialRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {

    private final ChunkingService chunkingService;
    private final EmbeddingService embeddingService;
    private final DocumentChunkRepository chunkRepository;
    private final UploadedMaterialRepository materialRepository;

    @Transactional
    public void indexMaterial(UUID materialId) {
        UploadedMaterial material = materialRepository.findById(materialId)
                .orElseThrow(() -> new EntityNotFoundException("Material not found: " + materialId));

        String text = material.getExtractedText();
        if (text == null || text.isBlank()) {
            log.warn("Material {} has no extracted text, skipping indexing", materialId);
            return;
        }

        // Remove old chunks if re-indexing
        chunkRepository.deleteByMaterialId(materialId);

        // Chunk the text
        List<String> chunks = chunkingService.chunkText(text);
        log.info("Split material {} into {} chunks", materialId, chunks.size());

        // Embed all chunks
        List<float[]> embeddings = embeddingService.embed(chunks);

        // Save chunks with embeddings
        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk chunk = DocumentChunk.builder()
                    .material(material)
                    .chunkText(chunks.get(i))
                    .chunkIndex(i)
                    .embedding(embeddingService.toVectorString(embeddings.get(i)))
                    .build();
            chunkRepository.save(chunk);
        }

        log.info("Indexed {} chunks for material {}", chunks.size(), materialId);
    }

    public List<String> searchRelevantChunks(String query, int limit) {
        float[] queryEmbedding = embeddingService.embed(query);
        String vectorString = embeddingService.toVectorString(queryEmbedding);

        List<DocumentChunk> similarChunks = chunkRepository.findSimilar(vectorString, limit);

        return similarChunks.stream()
                .map(DocumentChunk::getChunkText)
                .collect(Collectors.toList());
    }
}
