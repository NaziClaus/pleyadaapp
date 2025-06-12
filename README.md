# pleyadaapp

## Configuration

The application expects SFTP connection details to be provided via environment variables:

- `SFTP_HOST` - address of the SFTP server
- `SFTP_PORT` - port to connect (defaults to `22`)
- `SFTP_USERNAME` - username for login
- `SFTP_PASSWORD` - password for login
- `SFTP_REMOTE_PATH` - remote path for file operations (defaults to `/`)

When running with `docker-compose`, set these variables in an `.env` file or export
them in your shell before starting the stack:

```bash
SFTP_HOST=example.com
SFTP_USERNAME=user
SFTP_PASSWORD=secret
SFTP_REMOTE_PATH=/uploads
SFTP_PORT=2222 docker-compose up
```
