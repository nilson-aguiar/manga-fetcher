# Track Specification: Build core manga downloader and .cbz converter

## Overview
This track focuses on the core functionality of the Manga Fetcher CLI: searching for manga on MangaLivre, downloading chapters, and converting the images into .cbz files.

## User Stories
- **Download Manga by Title:** As a user, I want to search for and download a manga series by its title from MangaLivre.
- **Auto-CBZ Conversion:** As a collector, I want my downloads to be automatically converted into a standardized .cbz format.
- **Custom Output Directory:** As a power user, I want to specify where my downloaded manga files are saved.

## Functional Requirements
- **MangaLivre Search:** Implement a scrapper to search for manga by title on `mangalivre.to`.
- **Chapter Fetching:** Retrieve the list of available chapters for a selected manga.
- **Image Downloading:** Download all images for a specific chapter, handling network retries.
- **CBZ Conversion:** Bundle the downloaded images into a `.cbz` file (ZIP archive).
- **CLI Interface:** Provide commands for searching and downloading (e.g., `manga-fetcher search <title>`, `manga-fetcher download <id> <chapter>`).
- **Metadata Support:** Embed basic metadata like title and chapter number into the .cbz file.

## Technical Constraints
- **Language:** Kotlin (JVM)
- **Build Tool:** Gradle (Kotlin DSL)
- **CLI Framework:** Picocli
- **HTTP Client:** OkHttp
- **Native Image:** Must be compatible with GraalVM Native Image.
- **Rate Limiting:** Implement basic delays between requests to MangaLivre.

## Acceptance Criteria
- [ ] User can search for a manga by title and see a list of results.
- [ ] User can download a specific chapter by ID/URL.
- [ ] Downloaded images are automatically converted to a `.cbz` file in the specified output directory.
- [ ] The application can be compiled into a native binary using GraalVM.
- [ ] Test coverage for the new modules is >80%.
