package com.devlingo.repository;

import com.devlingo.domain.entity.UploadedMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UploadedMaterialRepository extends JpaRepository<UploadedMaterial, UUID> {

    Page<UploadedMaterial> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
