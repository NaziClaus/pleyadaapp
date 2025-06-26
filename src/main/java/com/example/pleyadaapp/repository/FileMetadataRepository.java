package com.example.pleyadaapp.repository;

import com.example.pleyadaapp.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    Optional<FileMetadata> findByPath(String path);
}
