# sftp-downloader

This project contains a minimal Spring Boot application that periodically downloads the most recent file from an SFTP server.

## Configuration

Edit `src/main/resources/application.yml` to set the SFTP connection properties and local download directory.

```
sftp:
  host: your-host
  port: 22
  username: user
  password: pass
  remote-dir: /path/on/server
  local-dir: ./download
```

## Running

Build the project with Maven and run the application:

```
mvn package
java -jar target/sftp-downloader-1.0.0.jar
```

The service checks the SFTP server every minute and downloads the newest file if its modification time is newer than the last downloaded file.
