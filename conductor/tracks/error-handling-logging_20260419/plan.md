# Implementation Plan

## Phase 1: Robust Error Recovery & Retry Strategy
- [ ] Task: Implement exponential backoff and retry mechanism for network operations
    - [ ] Write Tests: Add unit tests to `ResilientHttpClientTest` asserting that failed requests are retried up to 3 times before failing
    - [ ] Implement Feature: Update `ResilientHttpClient` to catch IOExceptions and retry HTTP requests
- [ ] Task: Ensure bulk download continuity upon chapter failure
    - [ ] Write Tests: Add integration test mimicking a network failure on one chapter and verifying the next chapter still downloads
    - [ ] Implement Feature: Update `MangaDownloadService.kt` to catch unrecoverable exceptions per chapter, log the error, update SQLite status, and continue
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Robust Error Recovery & Retry Strategy' (Protocol in workflow.md)

## Phase 2: JSON Structured Logging Configuration
- [ ] Task: Implement JSON log formatting
    - [ ] Write Tests: Add tests to verify JSON formatting structure matches the required schema
    - [ ] Implement Feature: Add a custom SLF4J format or logging interceptor to structure log output into JSON format
- [ ] Task: Add CLI argument for JSON logging
    - [ ] Write Tests: Add test in `DownloaderApplicationTest` to ensure the `--json-log` flag is recognized and sets the internal logging configuration appropriately
    - [ ] Implement Feature: Update `DownloaderApplication.kt` Picocli configuration with a `--json-log` boolean parameter
- [ ] Task: Conductor - User Manual Verification 'Phase 2: JSON Structured Logging Configuration' (Protocol in workflow.md)