# sftp-downloader

This project contains a Spring Boot application that downloads files from an SFTP server based on the current date. Downloaded file names are stored in a PostgreSQL database so they are not retrieved twice.

## Configuration

Edit `src/main/resources/application.yml` to set the SFTP connection properties,
local download directory and database settings. The file contains an example
using the credentials provided:

```
sftp:
  host: 146.59.54.218
  port: 22
  username: ftpuser
  password: lolik228
  remote-dir: /
  local-dir: ./download  # or D:\app\Sends on Windows
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/sftpdb
    username: sftpuser
    password: sftppass
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
```

## Running

Build the project with Maven and run the application:

```
mvn package
java -jar target/sftp-downloader-1.0.0.jar
```

The service checks the SFTP server every minute and downloads all files whose modification date matches today's date. A progress bar is displayed for each file and information about downloaded files is saved in PostgreSQL. After every download the service logs a comparison of files on the server versus those already stored locally for the past week.

## Importing into IntelliJ IDEA

1. Clone this repository using `git clone`.
2. In IntelliJ IDEA, choose **File → Open** and select the `sftp-downloader` directory.
3. IDEA will detect the Maven project and download the required dependencies.
4. To run the application, use the Maven `spring-boot:run` goal or run the `SftpDownloaderApplication` class.

## Docker Compose

Use Docker Compose to run the application together with PostgreSQL. Execute the
command from the repository root so the Docker build context includes the
`sftp-downloader` module.

```bash
docker compose up --build
```

Downloaded files are stored in the `download` directory on your host machine and
database data persists in a named Docker volume.
