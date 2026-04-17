# Docker Usage Guide

## Quick Start

```bash
# Build the image
./build-docker.sh

# Test it
docker run --rm manga-fetcher:latest --help

# Search for manga
docker run --rm manga-fetcher:latest search "one punch man"

# Download manga example
docker run --rm -v $(pwd)/downloads:/app/downloads manga-fetcher:latest \
  download one-punch-man 1-3
```

## Using Docker Compose

```bash
# Build with docker-compose
docker-compose build

# Run commands
docker-compose run --rm manga-fetcher --help
docker-compose run --rm manga-fetcher search "one punch man"
docker-compose run --rm manga-fetcher download one-punch-man 1-3

# Check downloaded files
ls -la downloads/
```

## Available Commands

### Search for Manga
```bash
docker run --rm manga-fetcher:latest search "<title>"
```

Example:
```bash
docker run --rm manga-fetcher:latest search "one punch man"
```

### Download Manga
```bash
docker run --rm -v $(pwd)/downloads:/app/downloads manga-fetcher:latest \
  download <manga-id> <chapter-range>
```

Examples:
```bash
# Download chapters 1-10
docker run --rm -v $(pwd)/downloads:/app/downloads manga-fetcher:latest \
  download one-punch-man 1-10

# Download all chapters
docker run --rm -v $(pwd)/downloads:/app/downloads manga-fetcher:latest \
  download one-punch-man all

# Download specific chapters
docker run --rm -v $(pwd)/downloads:/app/downloads manga-fetcher:latest \
  download one-punch-man 1,5,10
```

### Rename Chapters
```bash
docker run --rm -v $(pwd)/downloads:/app/downloads manga-fetcher:latest \
  rename <input-directory>
```

Example:
```bash
docker run --rm -v $(pwd)/downloads:/app/downloads manga-fetcher:latest \
  rename /app/downloads/one-punch-man
```

## Volume Mounts

The `/app/downloads` directory in the container is where manga files are saved. Mount it to persist downloads:

```bash
-v $(pwd)/downloads:/app/downloads
```

On Windows (PowerShell):
```powershell
-v ${PWD}/downloads:/app/downloads
```

## Environment Variables

- `TZ` - Timezone (default: UTC)
- `PLAYWRIGHT_BROWSERS_PATH` - Browser cache location (pre-installed in image)

## Image Size

- **JVM Image**: ~1.5-2GB (includes JRE + Playwright browsers)

## Troubleshooting

### Downloads not persisting
Make sure you're mounting a volume: `-v $(pwd)/downloads:/app/downloads`

### Playwright browser not found
The image comes with pre-installed Playwright browsers. If you see this error, try rebuilding the image.

### Permission denied on downloads
The container runs as the default user. If you have permission issues, ensure the downloads directory is writable:
```bash
mkdir -p downloads
chmod 777 downloads
```
