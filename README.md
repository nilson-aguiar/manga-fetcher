# Manga Fetcher CLI

A command-line tool for searching and downloading manga chapters, converting them into optimized `.cbz` (Comic Book ZIP) archives with embedded `ComicInfo.xml` metadata—tailor-made for self-hosted readers like **Komga** and **Kavita**, or mobile/tablet comic viewers.

---

## Features

- **Automated `.cbz` Generation:** Converts downloaded page images directly into CBZ archives ready for reading.
- **Rich Metadata (`ComicInfo.xml`):** Embeds standardized metadata inside each CBZ archive for seamless library indexing (series name, chapter number, volume, etc.).
- **Multiple Providers Supported:**
  - `mangalivre`
  - `taosect`
  - `composite` (default for downloads; tries providers sequentially to locate chapters)
- **Catch-up Downloads:** Download individual chapters or fetch all chapters starting from a specific chapter index (`--from`).
- **Volume Awareness:** Support for formatting chapter files with volume numbers (`--with-volume`) or retroactively updating existing libraries (`rename`).
- **Duplicate Prevention:** Tracks downloaded chapters in a portable SQLite database (`download.db`) inside your output folder to prevent redundant downloads.
- **Integrity Verification:** Audit tool (`check`) to inspect and clean up mismatched or deleted database entries.
- **Docker-Ready:** Isolated execution with bundled headless Playwright Chromium and tuned memory limits.

---

## Quick Start

### Option 1: Docker (Recommended)

Running with Docker avoids having to install Java or browser drivers on your host machine.

#### 1. Build the Image
```bash
./build-docker.sh
```

#### 2. Search for a Manga
```bash
docker run --rm manga-fetcher:latest search "Solo Leveling"
```

#### 3. Download Chapters
Mount your local downloads directory into `/app/downloads`:
```bash
# Download a single chapter
docker run --rm -v $(pwd)/downloads:/app/downloads manga-fetcher:latest \
  download solo-leveling -c 1 -o /app/downloads

# Download all chapters starting from chapter 10
docker run --rm -v $(pwd)/downloads:/app/downloads manga-fetcher:latest \
  download solo-leveling --from 10 -o /app/downloads
```

#### Or Using Docker Compose
```bash
# Search
docker-compose run --rm manga-fetcher search "Solo Leveling"

# Download
docker-compose run --rm manga-fetcher download solo-leveling -c 1 -o /app/downloads
```

---

### Option 2: Local CLI (Native)

#### Prerequisites
- **JDK 25+**

#### 1. Build Application
```bash
./gradlew installDist
```
The executable distribution will be generated in `build/install/downloader/bin/downloader`.

#### 2. Run via Gradle or Script
```bash
# Via Gradle
./gradlew run --args="search 'Solo Leveling'"

# Via compiled binary
./build/install/downloader/bin/downloader search "Solo Leveling"
```

---

## Command Reference

### 1. `search`
Search for manga titles across supported providers to retrieve their unique `mangaId`.

```bash
manga-fetcher search "<title>" [-p <provider>]
```

- `<title>`: Search query.
- `-p, --provider`: Provider to search on (`mangalivre`, `taosect`). Defaults to `mangalivre`.

**Example:**
```bash
manga-fetcher search "One Punch Man" -p mangalivre
```

---

### 2. `download`
Download chapters and package them into `.cbz` archives.

```bash
manga-fetcher download <mangaId> (-c <chapter> | --from <chapter>) [options]
```

#### Required Arguments
- `<mangaId>`: The ID of the manga retrieved from `search`.
- Chapter Selection (choose one):
  - `-c, --chapter <number>`: Download a single specific chapter (e.g. `-c 1` or `-c 05.5`).
  - `--from <number>`: Download all available chapters starting from this chapter number up to the latest.

#### Options
- `-o, --output, --output-dir <path>`: Directory to save `.cbz` files and `download.db`. Defaults to current directory (`.`).
- `--with-volume`: Include volume prefix in filenames when available (e.g., `Vol. 1 Ch. 1.cbz`).
- `-p, --provider <provider>`: Provider to use (`composite`, `mangalivre`, `taosect`). Defaults to `composite`.

**Examples:**
```bash
# Download a single chapter using the composite provider
manga-fetcher download one-punch-man -c 1 -o ./downloads

# Download everything starting from chapter 50 with volume names
manga-fetcher download one-punch-man --from 50 --with-volume -o ./downloads

# Force a specific provider
manga-fetcher download one-punch-man -c 1 -p taosect -o ./downloads
```

---

### 3. `rename`
Retroactively scan an output directory and rename existing `.cbz` files to include volume numbers retrieved from metadata providers.

```bash
manga-fetcher rename <mangaId> [-o <outputDir>]
```

- `<mangaId>`: Target manga identifier.
- `-o, --output-dir <path>`: Folder containing the existing `.cbz` files.

**Example:**
```bash
manga-fetcher rename one-punch-man -o ./downloads
```

---

### 4. `check`
Audit the SQLite tracking database (`download.db`) against the filesystem to ensure every recorded chapter exists on disk.

```bash
manga-fetcher check [-o <outputDir>] [-d]
```

- `-o, --output-dir <path>`: Folder containing `download.db` and CBZ files.
- `-d, --delete-missing`: Automatically delete database entries for missing files without an interactive confirmation prompt.

**Example:**
```bash
# Interactive check
manga-fetcher check -o ./downloads

# Non-interactive automated cleanup
manga-fetcher check -o ./downloads -d
```

---

### Global Options

- `--log-level <level>`: Set logging verbosity. Supported levels: `trace`, `debug`, `info`, `warn`, `error`. Default: `info`.
  ```bash
  manga-fetcher --log-level debug download one-punch-man -c 1
  ```
- `-h, --help`: Display usage and options.
- `-V, --version`: Display application version.

---

## Output Structure & Reader Compatibility

Each download folder functions as an independent, portable manga library:

```
downloads/
├── download.db                 # SQLite database tracking downloaded chapters
├── One Punch Man - Ch. 001.cbz
├── One Punch Man - Ch. 002.cbz
└── One Punch Man - Vol. 01 Ch. 003.cbz (when --with-volume is used)
```

### ComicInfo.xml
Inside each `.cbz` archive, a `ComicInfo.xml` file is generated with standard schema tags:
- `<Title>`: Chapter title or number
- `<Series>`: Manga name
- `<Number>`: Chapter number
- `<Volume>`: Volume number (if available)
- `<PageCount>`: Total number of pages

Compatible out-of-the-box with **Komga**, **Kavita**, **Chunky Comic Reader**, and **Tachiyomi/Mihon local source**.

---

## System Requirements & Memory Management

When downloading, the application utilizes headless **Playwright (Chromium)** to bypass dynamic client-side rendering and scrape image assets.

- **Recommended Memory:** `1.5 GB` to `2.0 GB` container memory limit.
- **JVM Configuration:**
  - By default, the JVM heap is capped at **33%** of container memory (`-XX:MaxRAMPercentage=33.0`).
  - This ensures sufficient unmanaged RAM is preserved for headless Chromium and native SQLite bindings, preventing container OOM kills (exit code 137).
- If running under Docker, assign memory with:
  ```bash
  docker run --rm -m 2g -v $(pwd)/downloads:/app/downloads manga-fetcher:latest ...
  ```

---

## Troubleshooting

### Scraper Rate Limiting / Cloudflare
Some providers employ anti-bot protections or aggressive rate limiting.
- If requests fail, try specifying an alternative provider with `-p <provider>`.
- Use `--log-level debug` to view detailed request workflows.

### Permissions on Downloaded Files (Docker)
Ensure your host download directory is writable by Docker:
```bash
mkdir -p downloads
chmod 777 downloads
```

---

## License

This project is licensed under the Apache 2.0 License.
