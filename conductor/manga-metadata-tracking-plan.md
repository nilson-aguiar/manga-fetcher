# Manga Metadata and Download Tracking Plan

## Objective
Enhance the `manga-fetcher` to produce additional output files matching the structure of the provided `downloads-sample/` directory:
1.  `cover.jpg`: The manga cover image.
2.  `manga_info.csv`: A CSV file containing metadata (title, authors, artists, description, tags).
3.  `download.db`: A SQLite database tracking downloaded chapters.

## Key Files & Context
- `build.gradle.kts`: Needs a new SQLite JDBC dependency.
- `src/main/resources/META-INF/native-image/reflect-config.json`: May need updates for GraalVM compatibility with SQLite.
- `src/main/kotlin/com/mangafetcher/downloader/MangaLivreScraper.kt`: Needs new methods to extract manga metadata and the cover image URL.
- `src/main/kotlin/com/mangafetcher/downloader/DownloaderApplication.kt`: Needs updates to the `DownloadCommand` to orchestrate the generation of these new files and interact with the database.
- `src/main/kotlin/com/mangafetcher/downloader/DatabaseTracker.kt` (New File): To handle SQLite interactions.

## Implementation Steps

### 1. Dependencies
- Add `implementation("org.xerial:sqlite-jdbc:<latest-version>")` to `build.gradle.kts`.
- Ensure GraalVM native image compilation is configured to include the SQLite JDBC driver (this may require adding reflection configuration to `reflect-config.json` if issues arise during native-image build).

### 2. Scraper Enhancements (`MangaLivreScraper.kt`)
- Create a `MangaDetails` data class containing `title`, `authors`, `artists`, `description`, `tags`, and `coverUrl`.
- Implement a `fetchMangaDetails(mangaId: String): MangaDetails` method.
  - Use `getPageContent` and Jsoup to parse the manga's main page.
  - Extract the required metadata strings (handling missing elements gracefully).
- Add a utility function to download the cover image from the `coverUrl` and save it to a specific `File`.

### 3. CSV and Cover Generation (`DownloaderApplication.kt`)
- In `DownloadCommand.call()`, immediately after initializing the `outputDir` and before looping through chapters:
  - Call `scraper.fetchMangaDetails(mangaId)`.
  - Check if `cover.jpg` exists in the output directory. If not, download the cover image.
  - Check if `manga_info.csv` exists. If not, generate it.
    - Since we are using a manual builder approach, construct the CSV string manually. Ensure that fields containing commas or quotes are properly escaped (enclosed in double quotes, and internal double quotes doubled).
    - Write the constructed string to `manga_info.csv` with the header: `title,authors,artists,description,tags`.

### 4. Database Tracking (`DatabaseTracker.kt`)
- Create a new class `DatabaseTracker(private val dbFile: File)`.
- Use standard JDBC: `DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}")`.
- In its initialization (or a `setup()` method), execute:
  `CREATE TABLE IF NOT EXISTS downloads (manga_id TEXT, chapter_id TEXT, chapter_number TEXT, downloaded_at DATETIME DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY(manga_id, chapter_id))`
- Implement methods:
  - `isDownloaded(mangaId: String, chapterId: String): Boolean`
  - `markDownloaded(mangaId: String, chapterId: String, chapterNumber: String)`
- Integrate `DatabaseTracker` into `DownloadCommand`:
  - Initialize it pointing to `download.db` in the output directory.
  - During the chapter loop, check both the filesystem (`ChapterNamingUtils.findExistingFile`) and `DatabaseTracker.isDownloaded` to skip already processed chapters.
  - After a successful download and `CbzConverter.convert`, call `markDownloaded`.

## Verification & Testing
- **Local Integration:** Run the `download` command locally against a test manga ID. Verify that `cover.jpg`, `manga_info.csv`, and `download.db` are generated correctly in the output directory.
- **SQLite Inspection:** Open `download.db` using a SQLite viewer to ensure the table schema and records are accurate.
- **CSV Format:** Verify the generated `manga_info.csv` parses correctly (e.g., in Excel or Numbers) and that complex descriptions with quotes are handled properly.
- **Native Image:** Build the GraalVM native image (`./gradlew nativeCompile`) and execute the resulting binary to ensure the SQLite JDBC driver functions correctly within the native executable without runtime reflection errors.
