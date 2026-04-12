# Implementation Plan: Chapter Download Range and Volume Auto-Rename

## Objective
Implement a range-based download option for the Manga Fetcher CLI and add automatic file renaming based on volume information.

## Key Files & Context
- `DownloaderApplication.kt`: Contains the `DownloadCommand` which needs new flags and range logic.
- `MangaLivreScraper.kt`: Scraper logic needs to extract more metadata (volume) if available.
- `CbzConverter.kt`: Handles the creation of `.cbz` files.

## Phase 1: Preparation & Metadata Enhancement [checkpoint: d5cb813]
- [x] Task: Update `ChapterResult` and scraper logic to attempt volume extraction. [a58e4f2]
    - [x] Update `ChapterResult` to include `val volume: String? = null`.
    - [x] Update `parseChapters` in `MangaLivreScraper.kt` to extract volume information from the HTML if possible.
    - [x] Write tests to verify `parseChapters` correctly identifies volume when present.
- [x] Task: Conductor - User Manual Verification 'Phase 1' (Protocol in workflow.md)

## Phase 2: Range Download Logic & Output Management [checkpoint: f5d8491]
- [x] Task: Implement range selection and output directory in `DownloadCommand`. [11fd05e]
    - [x] Update `DownloadCommand` to include `--from <chapter>` and `--output-dir <path>` options.
    - [x] Implement logic to fetch all chapters and filter those `>=` specified chapter.
    - [x] Write unit tests for the filtering logic in `DownloadCommand`.
- [x] Task: Conductor - User Manual Verification 'Phase 2' (Protocol in workflow.md)

## Phase 3: File Existence & Renaming Logic
- [x] Task: Implement file existence check and auto-renaming. [ae4d5aa]
    - [ ] Create a utility or internal function to check if a chapter file already exists in both `Ch. X.cbz` and `Vol. Y Ch. X.cbz` formats.
    - [ ] Implement logic to rename existing `Ch. X.cbz` files to `Vol. Y Ch. X.cbz` when volume information is available.
    - [ ] Write unit tests for naming logic and renaming operations.
- [ ] Task: Conductor - User Manual Verification 'Phase 3' (Protocol in workflow.md)

## Phase 4: CLI Integration & Final Verification
- [ ] Task: Update `DownloadCommand.call()` to orchestrate the new flow.
    - [ ] Integrate filtering, checking, downloading, and renaming.
    - [ ] Ensure proper logging and error handling for skipped files.
- [ ] Task: Conductor - User Manual Verification 'Phase 4' (Protocol in workflow.md)

## Verification & Testing
- **Automated Tests:** Run `./gradlew test` to ensure all new tests pass and coverage is maintained.
- **Manual Verification:**
    1. Run `manga-fetcher download <mangaId> --from <chapter> --output-dir ./test-downloads`.
    2. Verify files are skipped if they already exist.
    3. Verify files are renamed if they were `Ch. X.cbz` and now have volume information.
    4. Verify the naming format `Vol. 34 Ch. 204.cbz` or `Ch. 204.cbz`.
