# Manga Fetcher CLI

A powerful command-line interface tool for downloading manga chapters from various providers and converting them into `.cbz` format, optimized for e-readers and tablet viewing.

<!-- CODEGRAPH_START -->
## CodeGraph

In repositories indexed by CodeGraph (a `.codegraph/` directory exists at the repo root), reach for it BEFORE grep/find or reading files when you need to understand or locate code:

- **MCP tool** (when available): `codegraph_explore` answers most code questions in one call — the relevant symbols' verbatim source plus the call paths between them, including dynamic-dispatch hops grep can't follow. Name a file or symbol in the query to read its current line-numbered source. If it's listed but deferred, load it by name via tool search.
- **Shell** (always works): `codegraph explore "<symbol names or question>"` prints the same output.

If there is no `.codegraph/` directory, skip CodeGraph entirely — indexing is the user's decision.
<!-- CODEGRAPH_END -->


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
- **Run tests:** `./gradlew test` (Use `-PexcludeTags=integration` to skip integration tests)
- **Code style:** Adheres to standard Kotlin conventions (standard Gradle check tasks apply).

### Key Commands (CLI)
- **Search:** `search <title> [-p provider]` (Providers: `mangalivre`, `taosect`)
- **Download:** `download <mangaId> (-c <chapter> | --from <chapter>) [-o outputDir] [--with-volume] [-p provider]` (Providers: `composite` (default), `mangalivre`, `taosect`)
- **Rename:** `rename <mangaId> [-o outputDir]` (Retroactively adds volume info to filenames)
- **Check:** `check [-o outputDir] [-d]` (Verifies database entries against local files; `-d`/`--delete-missing` deletes entries without prompt)

## Docker Support
The project includes a `Dockerfile` and `docker-compose.yml` for isolated execution.
- **Build Image:** `./build-docker.sh`
- **Run via Compose:** `docker-compose run --rm manga-fetcher <args>`

## Project Structure & Conventions

- **`src/main/kotlin/com/mangafetcher/downloader/`**
    - **`domain/`**: Core models (`model`), interface definitions (`port`), and domain utilities (`service`).
    - **`application/`**: Orchestration services that implement use cases.
    - **`infrastructure/`**: Concrete implementations of ports:
        - `scraper/`: Provider-specific scraping logic (Playwright/Jsoup).
        - `download/`: Adapters for downloading images.
        - `conversion/`: Logic for CBZ creation and ComicInfo.xml metadata generation.
        - `persistence/`: SQLite repository implementation.
        - `metadata/`: Metadata fetching (Composite, MangaDex, MangaLivre, Taosect).
        - `http/`: Resilient HTTP client with retry and rate-limiting support.
    - **`cli/`**: Picocli command definitions and application entry point.

### Development Conventions
- **Shared Playwright Instance:** To avoid resource leaks and browser crashes, a `sharedPlaywrightClient` should be passed through services where browser automation is required.
- **Persistence:** Each output directory contains its own `download.db` SQLite file, making downloads portable.
- **Metadata:** Downloads automatically include `ComicInfo.xml` inside the CBZ files for better compatibility with library managers like Komga or Kavita.
- **Logging:** Configurable via `--log-level`. Uses `slf4j-simple` with custom formatting defined in `DownloaderApplication`.
