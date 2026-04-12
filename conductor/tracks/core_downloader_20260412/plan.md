# Implementation Plan: Build core manga downloader and .cbz converter

## Phase 1: Project Scaffolding and Core Utilities
Initialize the Kotlin/Gradle project and implement basic utility classes for network requests and file management.

- [x] **Task: Setup Gradle Project** (b51120c)
    - [ ] Initialize a new Gradle project with Kotlin DSL.
    - [ ] Configure dependencies (Picocli, OkHttp, Kotlin-Stdlib, JUnit).
    - [ ] Set up the basic project structure (src/main/kotlin, src/test/kotlin).
- [ ] **Task: Implement Core HTTP Client**
    - [ ] Write tests for a simple HTTP client wrapper using OkHttp.
    - [ ] Implement the HTTP client with basic retry logic and rate limiting.
- [ ] **Task: Implement CBZ Conversion Utility**
    - [ ] Write tests for zipping a list of images into a .cbz file.
    - [ ] Implement the `CbzConverter` using standard JVM zip libraries.
- [ ] **Task: Conductor - User Manual Verification 'Phase 1' (Protocol in workflow.md)**

## Phase 2: MangaLivre Scrapper Implementation
Develop the logic to search for manga and fetch chapter data from MangaLivre.

- [ ] **Task: Implement MangaLivre Search**
    - [ ] Write tests for searching manga on MangaLivre.
    - [ ] Implement `MangaLivreScraper.search(title)` to return a list of manga results.
- [ ] **Task: Implement Chapter Fetching**
    - [ ] Write tests for fetching chapter URLs for a given manga.
    - [ ] Implement `MangaLivreScraper.fetchChapters(mangaId)`.
- [ ] **Task: Implement Image Downloading**
    - [ ] Write tests for downloading images from a chapter page.
    - [ ] Implement `MangaLivreScraper.downloadImages(chapterId)`.
- [ ] **Task: Conductor - User Manual Verification 'Phase 2' (Protocol in workflow.md)**

## Phase 3: CLI Interface and Integration
Combine the scrapper and conversion logic into a cohesive CLI application.

- [ ] **Task: Implement Core CLI Commands**
    - [ ] Write tests for Picocli command parsing.
    - [ ] Implement `search` and `download` commands.
- [ ] **Task: Integrate Scrapper with Converter**
    - [ ] Write tests for the full search-download-convert flow.
    - [ ] Implement the integration logic in the CLI commands.
- [ ] **Task: GraalVM Native Image Configuration**
    - [ ] Add the GraalVM Gradle plugin.
    - [ ] Configure reflections and other native image settings.
    - [ ] Verify the native image build process.
- [ ] **Task: Conductor - User Manual Verification 'Phase 3' (Protocol in workflow.md)**
