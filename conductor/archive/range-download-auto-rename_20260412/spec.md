# Specification: Chapter Download Range and Volume Auto-Rename

## Overview
Enhance the Manga Fetcher CLI to support downloading a range of chapters starting from a specific chapter, while automatically managing file names based on volume information.

## Functional Requirements
- **Chapter Range Selection:** The CLI shall provide a `--from <chapter_number>` option (e.g., `manga-fetcher download --from 204`) to download all available chapters starting from the specified number.
- **Single Chapter Selection:** The CLI shall continue to support downloading a single chapter (e.g., `manga-fetcher download --chapter 204`).
- **Configurable Output Directory:** Users shall be able to specify the destination folder using a `--output-dir <path>` flag.
- **Existing File Detection:** Before downloading a chapter, the application shall check the specified output directory for an existing `.cbz` file corresponding to that chapter (e.g., `Ch. 204.cbz` or `Vol. 34 Ch. 204.cbz`).
- **Conflict Handling:** If a file for a chapter already exists in the output directory, the download for that chapter shall be skipped.
- **Volume Auto-Rename:** 
    - During each download session, the application shall scan the output directory for files named in the format `Ch. <number>.cbz`.
    - If volume information for a chapter becomes available (e.g., from the source metadata), the application shall rename the existing file to `Vol. <volume_number> Ch. <chapter_number>.cbz`.
    - This renaming logic applies to both newly downloaded chapters and previously downloaded chapters present in the folder.

## Acceptance Criteria
- Running `manga-fetcher download --from 204 --output-dir ./downloads` downloads all chapters from 204 onwards that are not already present in `./downloads`.
- If `Ch. 204.cbz` exists in the folder and the source now reports it as `Vol. 34`, the file is renamed to `Vol. 34 Ch. 204.cbz`.
- If `Vol. 34 Ch. 204.cbz` already exists, no new download or rename action is taken for that chapter.

## Out of Scope
- Downloading multiple specific chapters (e.g., `--chapters 200,205,210`).
- Downloading a fixed range (e.g., `--range 200-210`).
- Automatic deletion of corrupted or incomplete downloads (outside of basic file existence checks).
