package com.example.pleyadaapp.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "file_metadata")
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String path;

    private String filename;

    private long size;

    private Instant lastModified;

    private Instant downloadedAt;

    public FileMetadata() {
    }

    public FileMetadata(String path, String filename, long size, Instant lastModified) {
        this.path = path;
        this.filename = filename;
        this.size = size;
        this.lastModified = lastModified;
    }

    public Long getId() { return id; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    public Instant getLastModified() { return lastModified; }
    public void setLastModified(Instant lastModified) { this.lastModified = lastModified; }
    public Instant getDownloadedAt() { return downloadedAt; }
    public void setDownloadedAt(Instant downloadedAt) { this.downloadedAt = downloadedAt; }
}
