package com.example.sftpdownloader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SftpDownloaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SftpDownloaderApplication.class, args);
    }
}
