# Pleyada Data Downloader

This application downloads `.zip` and `.rar` files from an SFTP server every 30 minutes.
Metadata about all files is stored in PostgreSQL, and downloaded files are tracked in a CSV file.

## Usage

Place `docker-compose.yml`, the built `jar` file and your `.env` file in the same directory. The `.env` file defines database, SFTP and local storage settings (the `HOST_STORAGE` variable controls where files are saved on the host). Then run:

```bash
docker-compose up --build
```

The `.env` file should define database and SFTP credentials as well as the `HOST_STORAGE`
path on the host system. The container uses `LOCAL_DIR` and `CSV_PATH` to know
where to place files internally. See `.env.example` for an example.
