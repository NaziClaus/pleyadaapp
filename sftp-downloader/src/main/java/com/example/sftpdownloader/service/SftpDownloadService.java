package com.example.sftpdownloader.service;

import com.example.sftpdownloader.config.SftpProperties;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

@Service
public class SftpDownloadService {
    private static final Logger logger = LoggerFactory.getLogger(SftpDownloadService.class);
    private final SftpProperties properties;
    private long lastTimestamp = 0L;

    public SftpDownloadService(SftpProperties properties) {
        this.properties = properties;
        Path marker = Path.of(properties.getLocalDir(), "lastDownloadTime.txt");
        if (Files.exists(marker)) {
            try {
                String content = Files.readString(marker).trim();
                lastTimestamp = Long.parseLong(content);
            } catch (Exception e) {
                logger.warn("Could not read last download time", e);
            }
        }
    }

    @Scheduled(fixedDelayString = "60000") // every minute
    public void downloadLatest() {
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
            List<ChannelSftp.LsEntry> sorted = files.stream()
                    .filter(f -> !f.getAttrs().isDir())
                    .sorted(Comparator.comparingLong(e -> (long) e.getAttrs().getMTime() * 1000L))
                    .toList();

            Optional<ChannelSftp.LsEntry> newest = sorted.stream()
                    .filter(e -> (long) e.getAttrs().getMTime() * 1000L > lastTimestamp)
                    .reduce((first, second) -> second);

            if (newest.isPresent()) {
                ChannelSftp.LsEntry entry = newest.get();
                String filename = entry.getFilename();
                long mtime = (long) entry.getAttrs().getMTime() * 1000L;

                String remotePath = properties.getRemoteDir() + "/" + filename;
                Path localPath = Path.of(properties.getLocalDir(), filename);
                try (InputStream in = sftp.get(remotePath);
                     FileOutputStream out = new FileOutputStream(localPath.toFile())) {
                    in.transferTo(out);
                }
                Files.writeString(Path.of(properties.getLocalDir(), "lastDownloadTime.txt"), Long.toString(mtime));
                lastTimestamp = mtime;
                logger.info("Downloaded {}", filename);
            } else {
                logger.info("No new files to download");
            }

        } catch (Exception e) {
            logger.error("SFTP error", e);
        } finally {
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }
}
