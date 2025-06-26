# Pleyada Data Downloader

This application downloads `.zip` and `.rar` files from an SFTP server every 30 minutes.
Metadata about all files is stored in PostgreSQL, and downloaded files are tracked in a CSV file.

## Usage

Place `docker-compose.yml`, the built `jar` file and your `.env` file in the same directory, then run:

```bash
docker-compose up --build
```

The `.env` file should contain database and SFTP credentials. See `.env.example` for an example.
