# MangaLivre Provider

This document describes the MangaLivre manga provider implementation for the manga-fetcher application.

## Overview

MangaLivre (https://mangalivre.to) is a Brazilian manga website that hosts manga chapters with Portuguese translations. The provider supports fetching manga details, chapters, metadata, and downloading chapter images. It is the **primary download provider** in the application.

## URL Structure

- **Manga page**: `https://mangalivre.to/manga/{manga-id}/`
- **Chapter reader**: `https://mangalivre.to/manga/{manga-id}/{chapter-id}/`
- **Search**: `https://mangalivre.to/?s={query}&post_type=wp-manga`

## Components

### HtmlParser
Pure HTML parser with no I/O operations. Handles parsing of:
- Search results
- Chapter listings
- Manga details (title, authors, artists, description, tags, cover)
- Manga metadata (for ComicInfo.xml generation)
- Chapter images

The HtmlParser is shared across providers that use similar WordPress-based manga themes.

### ImageDownloader
Downloads images from MangaLivre chapter pages. Handles:
- Lazy-loaded images (scrolls page to trigger loading)
- Various image element selectors (`.reading-content img`, `.page-break img`, `img.wp-manga-chapter-img`)
- Multiple image attributes (`data-src`, `src`)

### MangaLivreScraper
Main coordinator that uses:
- **PlaywrightClient** for browser automation (handles JavaScript-heavy pages)
- **HtmlParser** for parsing
- **ImageDownloader** for downloads

Implements both `MangaScraperPort` and `MangaMetadataProvider` interfaces.

### MangaLivreDownloadProvider
Adapts MangaLivreScraper to the `MangaDownloadProvider` interface, allowing it to work with the composite provider pattern.

### MangaLivreMetadataProvider
Provides manga metadata for ComicInfo.xml generation by delegating to MangaLivreScraper.

## HTML Selectors

The parser uses the following CSS selectors:

### Search Results
- Post links: `.tab-summary .post-title a`, `.c-tabs-item__content .post-title a`

### Manga Details Page
- **Title**: `.post-title h1`, `.post-title h3`
- **Authors**: `.author-content a`
- **Artists**: `.artist-content a`
- **Description**: `.description-summary`, `.manga-excerpt`
- **Genres/Tags**: `.genres-content a`
- **Cover image**: `.summary_image img[data-src]` or `.summary_image img[src]`
- **Alternate title**: `.post-content_item:contains(Alternative) .summary-content`

### Chapter List
- Chapter links: `.chapter-box a`, `li.wp-manga-chapter a`
- Volume info: `.vol`, `.volume` (optional)

### Chapter Reader
- Images: `.reading-content img`, `.page-break img`, `img.wp-manga-chapter-img`
- Image URLs: `data-src` attribute (preferred) or `src` attribute

## Browser Automation

MangaLivre uses a JavaScript-heavy frontend that requires browser automation via Playwright:

```kotlin
val browser = playwright.chromium().launch(
    BrowserType.LaunchOptions()
        .setHeadless(true)
        .setArgs(listOf(
            "--no-sandbox",
            "--disable-setuid-sandbox",
            "--disable-dev-shm-usage",
            "--disable-gpu"
        ))
)
```

### User Agent
```
Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 
(KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36
```

### Image Loading Strategy
For chapter pages with lazy-loaded images:
1. Navigate to chapter URL
2. Wait for DOM content loaded
3. Wait for image selector (5s timeout)
4. Scroll to bottom to trigger lazy loading
5. Wait for network idle

## Integration

The provider is the **first provider** in the composite providers in `MangaDownloadService`:

```kotlin
CompositeDownloadProvider(
    listOf(
        MangaLivreDownloadProvider(),  // Tried first
        TaosectDownloadProvider(),
    ),
)

CompositeMetadataProvider(
    listOf(
        MangaDexMetadataProvider(),
        MangaLivreMetadataProvider(),  // Tried second (after MangaDex)
        TaosectMetadataProvider(),
    ),
)
```

## Usage Example

```bash
# Search for manga and download a specific chapter
./manga-fetcher --manga-id solo-leveling --chapter 100 --output-dir ./downloads

# Download from a specific chapter onwards
./manga-fetcher --manga-id one-piece --from-chapter 1000 --output-dir ./downloads

# Download with volume information
./manga-fetcher --manga-id naruto --chapter 1 --with-volume --output-dir ./downloads
```

## Testing

### Unit Tests
- `HtmlParserTest`: Tests HTML parsing logic with inline HTML fixtures

### Integration Tests
- `MangaLivreScraperIT`: Tests real HTTP requests to MangaLivre
- `MangaLivreMetadataProviderIT`: Tests metadata fetching

To run integration tests:
```bash
ENABLE_INTEGRATION_TESTS=true ./gradlew test --tests "MangaLivre*IT"
```

## Features

✅ **Search**: Find manga by title  
✅ **Chapter listing**: Get all available chapters with volume info  
✅ **Manga details**: Fetch title, authors, artists, description, tags, cover  
✅ **Image downloads**: Download all chapter images with lazy-loading support  
✅ **Metadata**: Generate ComicInfo.xml with complete manga information  
✅ **Browser automation**: Handles JavaScript-heavy pages  
✅ **Lazy loading**: Scrolls page to trigger image loading  

## Notes

- MangaLivre uses a WordPress-based manga theme (similar to many manga sites)
- Images are loaded via JavaScript and may require scrolling to trigger lazy loading
- The site uses both `data-src` and `src` attributes for images
- Chapter IDs are extracted from URL paths
- Supports volume information (when available)
- Portuguese language content (manga titles, chapter names, descriptions)
- Base URL: `https://mangalivre.to`

## Error Handling

The provider handles:
- Missing elements gracefully (returns empty strings or null)
- Failed image downloads (skips and continues)
- Lazy loading timeouts (continues with available images)
- Resource cleanup via `AutoCloseable` interface
