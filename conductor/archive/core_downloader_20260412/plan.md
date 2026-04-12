# Implementation Plan: Build core manga downloader and .cbz converter

## Phase 1: Project Scaffolding and Core Utilities [checkpoint: 7a8c947]
Initialize the Kotlin/Gradle project and implement basic utility classes for network requests and file management.

- [x] **Task: Setup Gradle Project** (b51120c)
    - [ ] Initialize a new Gradle project with Kotlin DSL.
    - [ ] Configure dependencies (Picocli, OkHttp, Kotlin-Stdlib, JUnit).
    - [ ] Set up the basic project structure (src/main/kotlin, src/test/kotlin).
- [x] **Task: Implement Core HTTP Client** (426105a)
    - [ ] Write tests for a simple HTTP client wrapper using OkHttp.
    - [ ] Implement the HTTP client with basic retry logic and rate limiting.
- [x] **Task: Implement CBZ Conversion Utility** (0d859ef)
    - [ ] Write tests for zipping a list of images into a .cbz file.
    - [ ] Implement the `CbzConverter` using standard JVM zip libraries.
- [x] **Task: Conductor - User Manual Verification 'Phase 1' (Protocol in workflow.md)**

## Phase 2: MangaLivre Scrapper Implementation [checkpoint: 6180716]
Develop the logic to search for manga and fetch chapter data from MangaLivre, utilizing Playwright for robust fetching.

- [x] **Task: Implement MangaLivre Search (Refactor to Playwright)** (d2f737f)
    - [ ] Update `MangaLivreScraper.search` to use Playwright.
    - [ ] Ensure tests pass with new stack.
- [x] **Task: Implement Chapter Fetching (Refactor to Playwright)** (d2f737f)
    - [ ] Update `MangaLivreScraper.fetchChapters` to use Playwright.
- [x] **Task: Implement Image Downloading (Refactor to Playwright)** (d2f737f)
    - [ ] Update `MangaLivreScraper.downloadImages` to use Playwright for reliable image extraction.
- [x] **Task: Conductor - User Manual Verification 'Phase 2' (Protocol in workflow.md)** (6180716)

## Phase 3: CLI Interface and Integration [checkpoint: e49d65a]
Combine the scrapper and conversion logic into a cohesive CLI application.

- [x] **Task: Implement Core CLI Commands** (82d33ff)
    - [ ] Write tests for Picocli command parsing.
    - [ ] Implement `search` and `download` commands.
- [x] **Task: Integrate Scrapper with Converter** (add4c61)
    - [ ] Write tests for the full search-download-convert flow.
    - [ ] Implement the integration logic in the CLI commands.
- [x] **Task: GraalVM Native Image Configuration** (17f0f1f)
    - [ ] Add the GraalVM Gradle plugin.
    - [ ] Configure reflections and other native image settings.
    - [ ] Verify the native image build process.
- [x] **Task: Conductor - User Manual Verification 'Phase 3' (Protocol in workflow.md)** (e49d65a)

