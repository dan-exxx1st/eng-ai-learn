package com.devlingo.repository;

import com.devlingo.domain.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, UUID> {

    List<DocumentChunk> findByMaterialId(UUID materialId);

    void deleteByMaterialId(UUID materialId);

    @Query(value = "SELECT * FROM document_chunks WHERE embedding IS NOT NULL ORDER BY embedding <=> cast(:embedding as vector) LIMIT :limit", nativeQuery = true)
    List<DocumentChunk> findSimilar(@Param("embedding") String embedding, @Param("limit") int limit);
}
