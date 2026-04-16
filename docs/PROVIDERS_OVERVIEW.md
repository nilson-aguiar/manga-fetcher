# Providers Overview

This document provides a comprehensive overview of all manga providers in the manga-fetcher application and how they work together.

## Provider Types

The application uses two types of providers:

### 1. Download Providers
Download providers implement the `MangaDownloadProvider` interface and are responsible for:
- Fetching manga details (title, authors, artists, description, tags, cover)
- Fetching chapter lists
- Downloading chapter images
- Downloading cover images

**Available Download Providers**:
- **MangaLivreDownloadProvider** - Primary download source (Portuguese)
- **TaosectDownloadProvider** - Secondary download source (Portuguese)

### 2. Metadata Providers
Metadata providers implement the `MangaMetadataProvider` interface and are responsible for:
- Enriching chapter downloads with metadata
- Providing information for ComicInfo.xml generation
- Supporting metadata fields like writer, penciller, genre, summary

**Available Metadata Providers**:
- **MangaDexMetadataProvider** - Primary metadata source (API-based)
- **MangaLivreMetadataProvider** - Secondary metadata source (web scraping)
- **TaosectMetadataProvider** - Tertiary metadata source (web scraping)

## Composite Provider Pattern

The application uses a **composite pattern** to try multiple providers in sequence until one succeeds:

```kotlin
// Download Providers (tried in order)
CompositeDownloadProvider(
    listOf(
        MangaLivreDownloadProvider(),  // Try first
        TaosectDownloadProvider(),     // Fallback
    )
)

// Metadata Providers (tried in order)
CompositeMetadataProvider(
    listOf(
        MangaDexMetadataProvider(),      // Try first (best metadata)
        MangaLivreMetadataProvider(),    // Fallback
        TaosectMetadataProvider(),       // Last resort
    )
)
```

### How Composite Providers Work

#### CompositeDownloadProvider
```kotlin
override fun fetchMangaDetails(mangaId: String): MangaDetails {
    for (provider in providers) {
        try {
            return provider.fetchMangaDetails(mangaId)  // Success!
        } catch (e: Exception) {
            continue  // Try next provider
        }
    }
    throw Exception("All providers failed")
}
```

#### CompositeMetadataProvider
```kotlin
override fun getMetadata(title: String, chapter: String?, volume: String?): MangaMetadata? {
    for (provider in providers) {
        try {
            val metadata = provider.getMetadata(title, chapter, volume)
            if (metadata != null) {
                return metadata  // Success!
            }
        } catch (e: Exception) {
            continue  // Try next provider
        }
    }
    return null  // All providers failed or returned null
}
```

## Provider Comparison

| Feature | MangaLivre | Taosect | MangaDex |
|---------|-----------|---------|----------|
| **Type** | Download + Metadata | Download + Metadata | Metadata Only |
| **Language** | Portuguese | Portuguese | Multi-language (EN preferred) |
| **Technology** | Web Scraping (Playwright) | Web Scraping (Playwright) | REST API |
| **Downloads** | ✅ Chapters + Images | ✅ Chapters + Images | ❌ Metadata only |
| **Search** | ✅ By title | ✅ By title | ✅ By title (API) |
| **Metadata Quality** | Good | Good | Excellent |
| **Rate Limiting** | None | None | 1 req/second |
| **Cover Images** | ✅ Yes | ✅ Yes | ❌ Not implemented |
| **Chapter Volumes** | ✅ When available | ❌ No | Passed through |
| **Base URL** | mangalivre.to | taosect.com | api.mangadex.org |

## Architecture

### Layer Structure

```
┌─────────────────────────────────────────┐
│      MangaDownloadService (App)         │
└─────────────────────────────────────────┘
                  │
        ┌─────────┴─────────┐
        ▼                   ▼
┌──────────────────┐  ┌──────────────────┐
│ Download Provider│  │ Metadata Provider│
└──────────────────┘  └──────────────────┘
        │                   │
        ▼                   ▼
┌──────────────────┐  ┌──────────────────┐
│   Composite      │  │   Composite      │
│   Download       │  │   Metadata       │
│   Provider       │  │   Provider       │
└──────────────────┘  └──────────────────┘
        │                   │
    ┌───┴───┐         ┌─────┼─────┐
    ▼       ▼         ▼     ▼     ▼
┌────────┐ ┌───────┐ ┌──┐ ┌──┐ ┌──┐
│MangaLiv│ │Taosect│ │MD│ │ML│ │TS│
└────────┘ └───────┘ └──┘ └──┘ └──┘
```

### Technology Stack

```
┌─────────────────────────────────────┐
│         Playwright (Browser)         │  ← MangaLivre, Taosect
├─────────────────────────────────────┤
│       OkHttp (HTTP Client)           │  ← MangaDex
├─────────────────────────────────────┤
│      Jsoup (HTML Parser)             │  ← All scrapers
├─────────────────────────────────────┤
│   Kotlinx Serialization (JSON)      │  ← MangaDex API
└─────────────────────────────────────┘
```

## Usage Workflow

### 1. Download a Chapter

```bash
./manga-fetcher --manga-id one-piece --chapter 1000 --output-dir ./downloads
```

**What happens internally:**

1. **Fetch Manga Details** (CompositeDownloadProvider)
   - Try MangaLivreDownloadProvider.fetchMangaDetails("one-piece")
   - If fails, try TaosectDownloadProvider.fetchMangaDetails("one-piece")
   - Save to `manga_info.csv`

2. **Download Cover** (First successful provider)
   - Download cover image to `cover.jpg`

3. **Fetch Chapters** (First successful provider)
   - Get list of all chapters
   - Filter to chapter 1000

4. **Download Images** (First successful provider)
   - Download all images for chapter 1000
   - Save to temp directory

5. **Fetch Metadata** (CompositeMetadataProvider)
   - Try MangaDexMetadataProvider.getMetadata("one-piece", "1000")
   - If null, try MangaLivreMetadataProvider.getMetadata("one-piece", "1000")
   - If null, try TaosectMetadataProvider.getMetadata("one-piece", "1000")

6. **Generate CBZ**
   - Create ComicInfo.xml from metadata
   - Package images + ComicInfo.xml into CBZ
   - Save to output directory

### 2. Download Multiple Chapters

```bash
./manga-fetcher --manga-id naruto --from-chapter 500 --output-dir ./downloads
```

**What happens internally:**

Same as above, but:
- Fetches all chapters >= 500
- Iterates through each chapter
- Uses database to track downloaded chapters
- Skips already downloaded chapters

## Adding a New Provider

To add a new provider, follow these steps:

### 1. Create the Core Components

```kotlin
// 1. HTML Parser (for web scrapers)
class NewSiteHtmlParser {
    fun parseSearchResults(html: String): List<MangaResult>
    fun parseChapters(html: String): List<ChapterResult>
    fun parseMangaDetails(html: String): MangaDetails
    fun parseMangaMetadata(html: String, baseUrl: String, mangaId: String): MangaMetadata?
    fun extractImageUrls(html: String): List<String>
}

// 2. Image Downloader
class NewSiteImageDownloader(
    private val playwrightClient: PlaywrightClient,
    private val htmlParser: NewSiteHtmlParser
) : ImageDownloaderPort

// 3. Scraper
class NewSiteScraper(
    private val playwrightClient: PlaywrightClient,
    private val htmlParser: NewSiteHtmlParser,
    private val imageDownloader: NewSiteImageDownloader
) : MangaScraperPort, MangaMetadataProvider, AutoCloseable
```

### 2. Create the Providers

```kotlin
// Download Provider
class NewSiteDownloadProvider : MangaDownloadProvider {
    override fun fetchMangaDetails(mangaId: String): MangaDetails
    override fun fetchChapters(mangaId: String): List<ChapterResult>
    override fun downloadChapterImages(mangaId: String, chapterId: String, outputDir: File): List<File>
    override fun downloadFile(url: String, outputFile: File)
    override fun close()
}

// Metadata Provider
class NewSiteMetadataProvider : MangaMetadataProvider, AutoCloseable {
    override fun getMetadata(title: String, chapter: String?, volume: String?): MangaMetadata?
    override fun close()
}
```

### 3. Register in MangaDownloadService

```kotlin
class MangaDownloadService(
    private val downloadProvider: MangaDownloadProvider =
        CompositeDownloadProvider(
            listOf(
                MangaLivreDownloadProvider(),
                TaosectDownloadProvider(),
                NewSiteDownloadProvider(),  // Add here
            ),
        ),
    private val metadataProvider: MangaMetadataProvider =
        CompositeMetadataProvider(
            listOf(
                MangaDexMetadataProvider(),
                MangaLivreMetadataProvider(),
                TaosectMetadataProvider(),
                NewSiteMetadataProvider(),  // Add here
            ),
        ),
)
```

### 4. Create Tests

```kotlin
// Unit tests
class NewSiteHtmlParserTest {
    // Test parsing with HTML fixtures
}

// Integration tests
@EnabledIfEnvironmentVariable(named = "ENABLE_INTEGRATION_TESTS", matches = "true")
class NewSiteScraperIT {
    // Test real HTTP requests
}
```

### 5. Create Documentation

```markdown
# NewSite Provider

## Overview
...

## URL Structure
...

## Components
...
```

## Best Practices

### 1. Provider Order
- **Download providers**: Order by reliability and speed
- **Metadata providers**: Order by metadata quality

### 2. Error Handling
- Return `null` for metadata providers (allows fallback)
- Throw exceptions for download providers (retries on next provider)
- Always close resources (use `AutoCloseable`)

### 3. Resource Management
```kotlin
provider.use { p ->
    val data = p.fetchData()
}  // Automatically closed
```

### 4. Testing
- Unit tests with HTML fixtures (fast, no network)
- Integration tests behind environment variable (slow, real network)
- Test both success and failure cases

### 5. Rate Limiting
- Respect API rate limits (use `ResilientHttpClient`)
- Add delays between requests if needed
- Use exponential backoff for retries

## Troubleshooting

### Provider Not Working

1. **Check provider order**
   - Is it registered in `MangaDownloadService`?
   - Is it in the correct position in the list?

2. **Check error handling**
   - Are exceptions being swallowed?
   - Check logs for error messages

3. **Check selectors**
   - Has the website changed its HTML structure?
   - Use browser dev tools to verify selectors

4. **Check rate limiting**
   - Are requests being blocked?
   - Add delays or reduce request frequency

### All Providers Failing

1. **Check network connectivity**
2. **Check website availability**
3. **Check for website structure changes**
4. **Review provider logs**

## Related Documentation

- [MangaLivre Provider](MANGALIVRE_PROVIDER.md)
- [Taosect Provider](TAOSECT_PROVIDER.md)
- [MangaDex Provider](MANGADEX_PROVIDER.md)
- [Architecture](../README.md)
