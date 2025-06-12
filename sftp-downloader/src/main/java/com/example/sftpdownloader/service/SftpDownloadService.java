package com.example.sftpdownloader.service;

import com.example.sftpdownloader.config.SftpProperties;
import com.example.sftpdownloader.model.DownloadedFile;
import com.example.sftpdownloader.repository.DownloadedFileRepository;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

@Service
public class SftpDownloadService {
    private static final Logger logger = LoggerFactory.getLogger(SftpDownloadService.class);
    private final SftpProperties properties;
    private final DownloadedFileRepository repository;

    public SftpDownloadService(SftpProperties properties, DownloadedFileRepository repository) {
        this.properties = properties;
        this.repository = repository;
        try {
            Files.createDirectories(Path.of(properties.getLocalDir()));
        } catch (Exception e) {
            logger.warn("Could not create local directory", e);
        }
    }

    @Scheduled(fixedDelayString = "60000")
    public void downloadForToday() {
        LocalDate today = LocalDate.now();
        JSch jsch = new JSch();
        Session session = null;
        Channel channel = null;
        try {
            session = jsch.getSession(properties.getUsername(), properties.getHost(), properties.getPort());
            session.setPassword(properties.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftp = (ChannelSftp) channel;

            @SuppressWarnings("unchecked")
            Vector<ChannelSftp.LsEntry> files = sftp.ls(properties.getRemoteDir());
            for (ChannelSftp.LsEntry entry : files) {
                if (entry.getAttrs().isDir()) continue;
                LocalDate fileDate = Instant.ofEpochSecond(entry.getAttrs().getMTime())
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                if (!fileDate.equals(today)) continue;
                String filename = entry.getFilename();
                if (repository.existsByFilename(filename)) continue;
                downloadFile(sftp, entry);
                repository.save(new DownloadedFile(filename, fileDate, LocalDateTime.now()));
                logWeeklyComparison(sftp);
            }
        } catch (Exception e) {
            logger.error("SFTP error", e);
        } finally {
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }

    private void downloadFile(ChannelSftp sftp, ChannelSftp.LsEntry entry) throws Exception {
        String remotePath = properties.getRemoteDir() + "/" + entry.getFilename();
        Path localPath = Path.of(properties.getLocalDir(), entry.getFilename());
        try (InputStream in = sftp.get(remotePath); FileOutputStream out = new FileOutputStream(localPath.toFile())) {
            long size = entry.getAttrs().getSize();
            byte[] buffer = new byte[8192];
            long transferred = 0;
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                transferred += read;
                printProgress(transferred, size);
            }
            System.out.println();
        }
        logger.info("Downloaded {}", entry.getFilename());
    }

    private void printProgress(long done, long total) {
        int width = 50;
        int progress = (int) (done * width / total);
        int percent = (int) (done * 100 / total);
        String bar = "[" + "#".repeat(progress) + " ".repeat(width - progress) + "] " + percent + "%\r";
        System.out.print(bar);
    }

    private void logWeeklyComparison(ChannelSftp sftp) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate weekAgo = today.minusDays(7);

            @SuppressWarnings("unchecked")
            Vector<ChannelSftp.LsEntry> files = sftp.ls(properties.getRemoteDir());
            Set<String> remote = files.stream()
                    .filter(f -> !f.getAttrs().isDir())
                    .filter(f -> {
                        LocalDate d = Instant.ofEpochSecond(f.getAttrs().getMTime())
                                .atZone(ZoneId.systemDefault()).toLocalDate();
                        return !d.isBefore(weekAgo) && !d.isAfter(today);
                    })
                    .map(ChannelSftp.LsEntry::getFilename)
                    .collect(Collectors.toSet());

            List<DownloadedFile> downloadedFiles = repository.findByFileDateBetween(weekAgo, today);
            Set<String> downloaded = downloadedFiles.stream().map(DownloadedFile::getFilename).collect(Collectors.toSet());

            Set<String> missing = new HashSet<>(remote);
            missing.removeAll(downloaded);
            logger.info("Weekly check - server files: {} downloaded: {} missing: {}", remote.size(), downloaded.size(), missing);
        } catch (Exception e) {
            logger.warn("Could not perform weekly comparison", e);
        }
    }
}
