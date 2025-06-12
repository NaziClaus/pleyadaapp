package com.example.sftpdownloader.repository;

import com.example.sftpdownloader.model.DownloadedFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DownloadedFileRepository extends JpaRepository<DownloadedFile, Long> {
    boolean existsByFilename(String filename);
    List<DownloadedFile> findByFileDateBetween(LocalDate start, LocalDate end);
}
