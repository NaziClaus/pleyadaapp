# sftp-downloader

This project contains a minimal Spring Boot application that periodically downloads the most recent file from an SFTP server.

## Configuration

Edit `src/main/resources/application.yml` to set the SFTP connection properties
and local download directory. The file contains an example using the credentials
provided:

```
sftp:
  host: 146.59.54.218
  port: 22
  username: ftpuser
  password: lolik228
  remote-dir: /
  local-dir: ./download  # or D:\app\Sends on Windows
```

## Running

Build the project with Maven and run the application:

```
mvn package
java -jar target/sftp-downloader-1.0.0.jar
```

The service checks the SFTP server every minute and downloads the newest file if its modification time is newer than the last downloaded file.

## Importing into IntelliJ IDEA

1. Clone this repository using `git clone`.
2. In IntelliJ IDEA, choose **File → Open** and select the `sftp-downloader` directory.
3. IDEA will detect the Maven project and download the required dependencies.
4. To run the application, use the Maven `spring-boot:run` goal or run the `SftpDownloaderApplication` class.
