# Manga Fetcher CLI

A powerful command-line interface tool for downloading manga chapters from various providers and converting them into `.cbz` format, optimized for e-readers and tablet viewing.

## Project Overview

- **Purpose:** Automate the process of searching, downloading, and converting manga chapters to high-quality CBZ files.
- **Architecture:** Follows a Clean Architecture / Hexagonal (Ports & Adapters) pattern, separating core domain logic from infrastructure details (scrapers, persistence, conversion).
- **Main Technologies:**
    - **Language:** Kotlin (JVM 25+)
    - **Build System:** Gradle (Kotlin DSL)
    - **CLI Framework:** [Picocli](https://picocli.info/)
    - **Web Scraping:** [Microsoft Playwright](https://playwright.dev/java/) (for browser automation) and [Jsoup](https://jsoup.org/) (for HTML parsing)
    - **Networking:** [OkHttp](https://square.github.io/okhttp/)
    - **Persistence:** SQLite (via JDBC) to track downloads and prevent duplicates
    - **Serialization:** [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)
    - **Containerization:** Docker & Docker Compose

## Building and Running

### Development Commands
- **Build the project:** `./gradlew build`
- **Run the application:** `./gradlew run --args="<command> <options>"`
- **Run tests:** `./gradlew test` (Use `-PexcludeTags=it` to skip integration tests)
- **Code style:** Adheres to standard Kotlin conventions (standard Gradle check tasks apply).

### Key Commands (CLI)
- **Search:** `search <title> [-p provider]` (Providers: `mangalivre`, `taosect`)
- **Download:** `download <mangaId> [-c chapter] [--from chapter] [-o outputDir] [--with-volume]`
- **Rename:** `rename <mangaId> [-o outputDir]` (Retroactively adds volume info to filenames)
- **Check:** `check [-o outputDir] [-d]` (Verifies database entries against local files)

## Docker Support
The project includes a `Dockerfile` and `docker-compose.yml` for isolated execution.
- **Build Image:** `./build-docker.sh`
- **Run via Compose:** `docker-compose run --rm manga-fetcher <args>`

## Project Structure & Conventions

- **`src/main/kotlin/com/mangafetcher/downloader/`**
    - **`domain/`**: Core models (`model`) and interface definitions (`port`).
    - **`application/`**: Orchestration services that implement use cases.
    - **`infrastructure/`**: Concrete implementations of ports:
        - `scraper/`: Provider-specific scraping logic (Playwright/Jsoup).
        - `download/`: Adapters for downloading images.
        - `conversion/`: Logic for CBZ creation and ComicInfo.xml metadata generation.
        - `persistence/`: SQLite repository implementation.
        - `metadata/`: External metadata fetching (e.g., MangaDex).
    - **`cli/`**: Picocli command definitions and application entry point.

### Development Conventions
- **Shared Playwright Instance:** To avoid resource leaks and browser crashes, a `sharedPlaywrightClient` should be passed through services where browser automation is required.
- **Persistence:** Each output directory contains its own `download.db` SQLite file, making downloads portable.
- **Metadata:** Downloads automatically include `ComicInfo.xml` inside the CBZ files for better compatibility with library managers like Komga or Kavita.
- **Logging:** Configurable via `--log-level`. Uses `slf4j-simple` with custom formatting defined in `DownloaderApplication`.
