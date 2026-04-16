# Metadata Generation and Volume-Aware Renaming Plan

## Background & Motivation
The application currently downloads manga chapters as standard CBZ files without rich metadata (`ComicInfo.xml`), and the filenames default to `Ch. [Number].cbz`. We want to introduce the ability to embed standard metadata into the CBZ files to improve compatibility with comic readers. The primary source for this metadata will be MangaLivre (scraped), with MangaDex as a robust fallback. Additionally, we want to allow users to rename files to include volume numbers (`Vol. [Number] Ch. [Number].cbz`), both when downloading new chapters and retroactively for existing downloads.

## Scope & Impact
- **MangaLivreScraper:** Will be updated to scrape metadata (Author, Artist, Summary, Genres) from the manga's main page.
- **MangaMetadataProvider:** A new interface will be introduced, with a `MangaDexMetadataProvider` implementation to serve as a fallback source when MangaLivre's data is incomplete.
- **CbzConverter:** Will be modified to accept a `ComicInfo.xml` string and inject it into the generated `.cbz` archive.
- **CLI Commands:**
  - A new `--with-volume` flag will be added to the `download` command.
  - A new `rename` subcommand will be added to retroactively rename existing CBZ files in a directory based on their volume numbers.
- **ChapterNamingUtils:** Will be updated to support the new volume-aware naming conventions natively.

## Implementation Steps

### Phase 1: Metadata Extraction & Fallback
1. **Define Data Models:** Create a `MangaMetadata` data class containing properties for Title, Writer, Penciller, Summary, Genres, Volume, and Chapter number.
2. **Update Scraper:** Modify `MangaLivreScraper.kt` to extract metadata from the manga's main page HTML.
3. **Implement Fallback:** 
   - Create a `MangaMetadataProvider` interface.
   - Implement `MangaDexMetadataProvider` to query the MangaDex API via title search to fill in missing metadata.

### Phase 2: CBZ Metadata Injection
1. **XML Generation:** Create a utility to serialize `MangaMetadata` into the standard `ComicInfo.xml` format.
2. **Update Converter:** Modify `CbzConverter.convert()` to accept an optional `metadataXml: String` parameter. If provided, create a `ZipEntry` for `ComicInfo.xml` and write the string content into the root of the archive alongside the images.

### Phase 3: Volume-Aware Renaming Feature
1. **Update Naming Logic:** Ensure `ChapterNamingUtils` can conditionally generate filenames formatted as `Vol. X Ch. Y.cbz` when volume information is available and requested.
2. **Download Command Flag:** Add a `--with-volume` boolean flag to `DownloadCommand`. When true, pass the parsed volume down to the naming utility.
3. **New Rename Command:** Create a `RenameCommand` in `DownloaderApplication.kt` that accepts a `mangaId` and a `--dir` flag. It will fetch the chapter list, map local files to chapters, and rename them to include the volume if known.

## Verification & Testing
1. **Unit Tests:** 
   - Test `MangaLivreScraper` metadata extraction logic against mock HTML.
   - Test the `ComicInfo.xml` generation logic to ensure the schema is correct.
   - Test `ChapterNamingUtils` for correct filename generation with and without volume flags.
2. **Integration Tests:** 
   - Verify that `CbzConverter` successfully creates a valid ZIP archive containing both images and the `ComicInfo.xml` file.
   - Run the `download` command with the new flag to verify end-to-end extraction, injection, and naming.
   - Run the new `rename` command on a mock directory of older files to verify correct renaming behavior.