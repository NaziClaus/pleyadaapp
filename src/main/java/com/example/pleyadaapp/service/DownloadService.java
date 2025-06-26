package com.example.pleyadaapp.service;

import com.example.pleyadaapp.model.FileMetadata;
import com.example.pleyadaapp.repository.FileMetadataRepository;
import com.example.pleyadaapp.sftp.ConsoleProgressMonitor;
import com.example.pleyadaapp.sftp.SftpClient;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.opencsv.CSVWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;

@Service
public class DownloadService {

    private final SftpClient sftpClient;
    private final FileMetadataRepository repository;

    @Value("${SFTP_DIR}")
    private String remoteDir;

    @Value("${LOCAL_DIR}")
    private String localDir;

    @Value("${CSV_PATH}")
    private String csvPath;

    public DownloadService(SftpClient sftpClient, FileMetadataRepository repository) {
        this.sftpClient = sftpClient;
        this.repository = repository;
    }

    @Scheduled(fixedDelay = 1800000)
    public void scanAndDownload() {
        try {
            Files.createDirectories(Path.of(localDir));
            Files.createDirectories(Path.of(csvPath).getParent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            Vector<ChannelSftp.LsEntry> files = sftpClient.list(remoteDir);
            List<ChannelSftp.LsEntry> filtered = files.stream()
                    .filter(e -> !e.getAttrs().isDir())
                    .filter(e -> {
                        String name = e.getFilename().toLowerCase(Locale.ROOT);
                        return name.endsWith(".zip") || name.endsWith(".rar");
                    })
                    .collect(Collectors.toList());

            for (ChannelSftp.LsEntry entry : filtered) {
                String remotePath = remoteDir + "/" + entry.getFilename();
                Optional<FileMetadata> existing = repository.findByPath(remotePath);
                if (existing.isEmpty()) {
                    FileMetadata meta = new FileMetadata(remotePath, entry.getFilename(), entry.getAttrs().getSize(),
                            Instant.ofEpochSecond(entry.getAttrs().getMTime()));
                    repository.save(meta);
                }
            }

            List<FileMetadata> toDownload = repository.findAll().stream()
                    .filter(m -> m.getDownloadedAt() == null)
                    .collect(Collectors.toList());

            for (FileMetadata meta : toDownload) {
                downloadFile(meta);
            }
        } catch (JSchException | SftpException e) {
            System.err.println("SFTP error: " + e.getMessage());
        }
    }

    private void downloadFile(FileMetadata meta) {
        String remotePath = meta.getPath();
        Path localPath = Path.of(localDir, meta.getFilename());
        ConsoleProgressMonitor monitor = new ConsoleProgressMonitor();
        try {
            sftpClient.execute(channel -> {
                try (OutputStream os = Files.newOutputStream(localPath)) {
                    channel.get(remotePath, os, monitor);
                }
                return null;
            });
            meta.setDownloadedAt(Instant.now());
            repository.save(meta);
            appendCsv(meta);
        } catch (IOException | JSchException | SftpException e) {
            System.err.println("Download failed for " + meta.getFilename() + ": " + e.getMessage());
        }
    }

    private synchronized void appendCsv(FileMetadata meta) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(csvPath, true))) {
            long total = repository.findAll().stream()
                    .filter(m -> m.getDownloadedAt() != null)
                    .mapToLong(FileMetadata::getSize)
                    .sum();
            String time = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    .withZone(ZoneId.systemDefault())
                    .format(meta.getDownloadedAt());
            writer.writeNext(new String[]{meta.getFilename(), time, String.valueOf(meta.getSize()), String.valueOf(total)});
        } catch (IOException e) {
            System.err.println("Failed to write CSV: " + e.getMessage());
        }
    }
}
