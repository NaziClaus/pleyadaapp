# sftp-downloader

This project contains a Spring Boot application that downloads files from an SFTP server based on the most recent modification date. Downloaded file names are stored in a PostgreSQL database so they are not retrieved twice.

## Configuration

Configuration is provided via environment variables so credentials are not kept
in source control. Copy `.env.example` to `.env` and adjust the values if
necessary. `application.yml` reads them automatically. Example values:

```
SFTP_HOST=146.59.54.218
SFTP_PORT=22
SFTP_USERNAME=ftpuser
SFTP_PASSWORD=lolik228
SFTP_REMOTE_DIR=/
SFTP_LOCAL_DIR=D:/app/Sends
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/sftpdb
SPRING_DATASOURCE_USERNAME=sftpuser
SPRING_DATASOURCE_PASSWORD=sftppass
```

## Running

Build the project with Maven and run the application:

```
mvn package
java -jar target/sftp-downloader-1.0.0.jar
```

The service checks the SFTP server every minute and downloads all files whose modification date matches today's date. It prints how many files were found and shows a progress bar for each download including speed and target path. If a download is interrupted the next run resumes from where it stopped. Downloaded file names are stored in PostgreSQL so the same file is not retrieved twice. After every download the service logs a comparison of files on the server versus those already stored locally for the past week.

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
The compose file maps the host directory `D:/app/Sends` to `/app/download` inside
the container. The `SFTP_LOCAL_DIR` variable is overridden to `/app/download` so
the application writes into the mounted folder. Adjust these paths if needed.
Database data persists in a named Docker volume.
