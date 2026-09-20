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
  download one-punch-man -c 1 -o /app/downloads
```

## Using Docker Compose

```bash
# Build with docker-compose
docker-compose build

# Run commands
docker-compose run --rm manga-fetcher --help
docker-compose run --rm manga-fetcher search "one punch man"
docker-compose run --rm manga-fetcher download one-punch-man -c 1 -o /app/downloads

# Check downloaded files
ls -la downloads/
```

## Available Commands

### Search for Manga
```bash
docker run --rm manga-fetcher:latest search "<title>" [-p <provider>]
```

Example:
```bash
docker run --rm manga-fetcher:latest search "one punch man"
```

### Download Manga
```bash
docker run --rm -v $(pwd)/downloads:/app/downloads manga-fetcher:latest \
  download <manga-id> (-c <chapter> | --from <chapter>) [-o <output-dir>] [--with-volume] [-p <provider>]
```

Examples:
```bash
# Download a specific chapter
docker run --rm -v $(pwd)/downloads:/app/downloads manga-fetcher:latest \
  download one-punch-man -c 1 -o /app/downloads

# Download all chapters starting from chapter 10
docker run --rm -v $(pwd)/downloads:/app/downloads manga-fetcher:latest \
  download one-punch-man --from 10 -o /app/downloads

# Download with volume in filename
docker run --rm -v $(pwd)/downloads:/app/downloads manga-fetcher:latest \
  download one-punch-man -c 1 --with-volume -o /app/downloads
```

### Rename Chapters
```bash
docker run --rm -v $(pwd)/downloads:/app/downloads manga-fetcher:latest \
  rename <manga-id> [-o <output-dir>]
```

Example:
```bash
docker run --rm -v $(pwd)/downloads:/app/downloads manga-fetcher:latest \
  rename one-punch-man -o /app/downloads
```

### Check Database Integrity
```bash
docker run --rm -v $(pwd)/downloads:/app/downloads manga-fetcher:latest \
  check [-o <output-dir>] [-d]
```

Example:
```bash
docker run --rm -v $(pwd)/downloads:/app/downloads manga-fetcher:latest \
  check -o /app/downloads -d
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
- `JAVA_TOOL_OPTIONS` - Additional JVM options (default: `-XX:MaxRAMPercentage=33.0 -XX:+ExitOnOutOfMemoryError`)

## Memory Sizing & Resource Recommendations

- **Recommended Container Memory:** `1.5 GB` to `2.0 GB` (e.g. `docker run -m 2g ...` or `mem_limit: 2g`).
- **Memory Allocation:**
  - JVM Heap is set to **33%** of container memory (`-XX:MaxRAMPercentage=33.0`).
  - The remaining 67% of memory provides the necessary headroom for the out-of-process headless Chromium browser (Playwright), JVM Metaspace/native memory, and SQLite operations without triggering container OOM kills (exit code 137).

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
