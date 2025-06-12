# Build stage
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /build
COPY sftp-downloader/pom.xml sftp-downloader/pom.xml
COPY sftp-downloader/src sftp-downloader/src
RUN mvn -f sftp-downloader/pom.xml package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/sftp-downloader/target/sftp-downloader-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
