# Pleyada Data Downloader

This application downloads `.zip` and `.rar` files from an SFTP server every 30 minutes.
Metadata about all files is stored in PostgreSQL, and downloaded files are tracked in a CSV file.

## Usage

Place `docker-compose.yml`, the built `jar` file and your `.env` file in the same directory. The `.env` file defines database, SFTP and local storage settings. Then run:

```bash
docker-compose up --build
```

`HOST_STORAGE` specifies the host directory for downloads. Inside the container the files are saved to `LOCAL_DIR` (default `/data`) and logged to `CSV_PATH`.
See `.env.example` for an example.
