# Implementation Plan: Refactor Download Command - Chapter Flag

## Objective
The goal is to refactor the `download` command to use a `-c` flag for specifying a chapter number instead of a positional argument for a chapter ID.

## Key Files & Context
- `src/main/kotlin/com/mangafetcher/downloader/DownloaderApplication.kt`: Contains the `DownloadCommand` definition.
- `src/test/kotlin/com/mangafetcher/downloader/DownloaderApplicationTest.kt`: Unit tests for the CLI commands.
- `src/test/kotlin/com/mangafetcher/downloader/CLIIntegrationIT.kt`: Integration tests for the CLI.

## Implementation Steps

### Phase 1: Preparation and Testing
- [ ] Task: Create a new test case for the new `-c` flag in `DownloaderApplicationTest.kt`.
    - [ ] Add a test that verifies `DownloadCommand` correctly maps `-c` to a chapter selection.
    - [ ] Add a test for mutual exclusivity between `-c` and `--from`.
    - [ ] Add a test verifying that the positional chapter ID is no longer accepted.
- [ ] Task: Update `CLIIntegrationIT.kt` if necessary to use the new flag format.

### Phase 2: Refactor `DownloadCommand`
- [ ] Task: Modify `DownloadCommand` in `DownloaderApplication.kt`.
    - [ ] Remove the positional `chapterId` parameter.
    - [ ] Add the `@CommandLine.Option(names = ["-c", "--chapter"])` for `chapterNumber`.
    - [ ] Update the `call()` method to use the `chapterNumber` for filtering.
    - [ ] Implement mutual exclusivity check between `chapterNumber` and `fromChapter`.
- [ ] Task: Verify all tests pass (TDD: Green Phase).

### Phase 3: Finalization
- [ ] Task: Update the `workflow.md` examples and `README.md` (if any) to reflect the command changes.
- [ ] Task: Conductor - User Manual Verification 'Refactor Download Command' (Protocol in workflow.md).

## Verification & Testing
- Run all unit tests: `./gradlew test`
- Run integration tests: `./gradlew test --tests "*IT"`
- Manual verification:
    - `manga-fetcher download <manga-id> -c <chapter-number>`
    - `manga-fetcher download <manga-id> --from <chapter-number>`
    - `manga-fetcher download <manga-id> -c <chapter-number> --from <chapter-number>` (should fail)