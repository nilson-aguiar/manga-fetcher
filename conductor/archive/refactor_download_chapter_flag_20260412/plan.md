# Implementation Plan: Refactor Download Command - Chapter Flag

## Objective
The goal is to refactor the `download` command to use a `-c` flag for specifying a chapter number instead of a positional argument for a chapter ID.

## Key Files & Context
- `src/main/kotlin/com/mangafetcher/downloader/DownloaderApplication.kt`: Contains the `DownloadCommand` definition.
- `src/test/kotlin/com/mangafetcher/downloader/DownloaderApplicationTest.kt`: Unit tests for the CLI commands.
- `src/test/kotlin/com/mangafetcher/downloader/CLIIntegrationIT.kt`: Integration tests for the CLI.

## Implementation Steps

### Phase 1: Preparation and Testing
- [x] Task: Create a new test case for the new `-c` flag in `DownloaderApplicationTest.kt`. 88c291c
    - [x] Add a test that verifies `DownloadCommand` correctly maps `-c` to a chapter selection.
    - [x] Add a test for mutual exclusivity between `-c` and `--from`.
    - [x] Add a test verifying that the positional chapter ID is no longer accepted.
- [x] Task: Update `CLIIntegrationIT.kt` if necessary to use the new flag format. 88c291c

### Phase 2: Refactor `DownloadCommand`
- [x] Task: Modify `DownloadCommand` in `DownloaderApplication.kt`. 88c291c
    - [x] Remove the positional `chapterId` parameter.
    - [x] Add the `@CommandLine.Option(names = ["-c", "--chapter"])` for `chapterNumber`.
    - [x] Update the `call()` method to use the `chapterNumber` for filtering.
    - [x] Implement mutual exclusivity check between `chapterNumber` and `fromChapter`.
- [x] Task: Verify all tests pass (TDD: Green Phase). 88c291c

### Phase 3: Finalization [checkpoint: 29ce7c9]
- [x] Task: Update the `workflow.md` examples and `README.md` (if any) to reflect the command changes. 2a78482
- [x] Task: Conductor - User Manual Verification 'Refactor Download Command' (Protocol in workflow.md). 29ce7c9

## Verification & Testing
- Run all unit tests: `./gradlew test`
- Run integration tests: `./gradlew test --tests "*IT"`
- Manual verification:
    - `manga-fetcher download <manga-id> -c <chapter-number>`
    - `manga-fetcher download <manga-id> --from <chapter-number>`
    - `manga-fetcher download <manga-id> -c <chapter-number> --from <chapter-number>` (should fail)