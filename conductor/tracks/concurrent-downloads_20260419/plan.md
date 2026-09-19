# Implementation Plan: Parallel Image Downloads

## Phase 1: Dependencies & Configuration Updates
- [ ] Task: Add Kotlin Coroutines dependency
    - [ ] Update `build.gradle.kts` to include `kotlinx-coroutines-core`.
- [ ] Task: Add Configuration Options
    - [ ] Update `DownloaderApplication.kt` CLI arguments to support `--concurrency` (default 5) and `--timeout` for images.
    - [ ] Update the `DownloadRequest` model to accept concurrency and timeout parameters.
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Dependencies & Configuration Updates' (Protocol in workflow.md)

## Phase 2: PlaywrightClient Updates
- [ ] Task: Implement `APIRequestContext` for raw file downloads
    - [ ] Write Tests: Create/update tests in `PlaywrightClientTest` (or similar) mocking network responses to verify files are downloaded via `request().get()`.
    - [ ] Implement Feature: Update `PlaywrightClient.downloadImage` and `downloadFile` to use `context.request().get()` instead of `newPage()`.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: PlaywrightClient Updates' (Protocol in workflow.md)

## Phase 3: ImageDownloader Coroutine Integration
- [ ] Task: Implement parallel image downloads with Semaphores
    - [ ] Write Tests: Add unit tests to `ImageDownloaderTest` verifying concurrent behavior (e.g., using `TestCoroutineDispatcher` or `runTest` to verify that no more than N downloads occur simultaneously).
    - [ ] Write Tests: Add tests to verify retry logic on single image failure, and overall chapter failure if retries are exhausted.
    - [ ] Implement Feature: Refactor `ImageDownloader.downloadChapterImages` to a suspend function utilizing `coroutineScope`, `async`, and a `Semaphore(concurrencyLimit)`.
    - [ ] Implement Feature: Integrate retry and timeout logic with debug logging.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: ImageDownloader Coroutine Integration' (Protocol in workflow.md)

## Phase 4: Integration with Core Services
- [ ] Task: Propagate `suspend` and handle execution in `MangaDownloadService`
    - [ ] Write Tests: Update existing `MangaDownloadServiceTest` to run within `runTest` and pass concurrency parameters.
    - [ ] Implement Feature: Update `ImageDownloaderPort`, `MangaDownloadProvider` and concrete implementations (`MangaLivreScraper`, etc.) to mark `downloadChapterImages` as `suspend`.
    - [ ] Implement Feature: In `MangaDownloadService.downloadManga`, wrap the chapter loop or the `downloadChapterImages` call in `runBlocking` (since the CLI entrypoint is synchronous) and pass the configuration parameters.
- [ ] Task: Conductor - User Manual Verification 'Phase 4: Integration with Core Services' (Protocol in workflow.md)