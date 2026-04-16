# Taosect Provider

This document describes the Taosect manga provider implementation for the manga-fetcher application.

## Overview

Taosect (https://taosect.com) is a Brazilian manga scanlation website that hosts manga chapters with Portuguese translations. The provider supports fetching manga details, chapters, metadata, and downloading chapter images.

## URL Structure

- **Manga page**: `https://taosect.com/manga/{manga-id}/`
- **Chapter reader**: `https://taosect.com/leitor-online/projeto/{manga-id}/{chapter-id}/`
- **Search**: `https://taosect.com/?s={query}`

## Components

### TaosectHtmlParser
Pure HTML parser with no I/O operations. Handles parsing of:
- Search results
- Chapter listings
- Manga details (title, authors, artists, description, tags, cover)
- Manga metadata (for ComicInfo.xml generation)
- Chapter images

### TaosectImageDownloader
Downloads images from Taosect chapter pages. Handles the specific Google Drive URLs used by Taosect for image hosting.

### TaosectScraper
Main coordinator that uses PlaywrightClient for browser operations, TaosectHtmlParser for parsing, and TaosectImageDownloader for downloads.

### TaosectDownloadProvider
Adapts TaosectScraper to the MangaDownloadProvider interface, allowing it to work with the composite provider pattern.

### TaosectMetadataProvider
Provides manga metadata for ComicInfo.xml generation, implementing the MangaMetadataProvider interface.

## HTML Selectors

The parser uses the following CSS selectors:

- **Title**: `h1.titulo-projeto`
- **Original title**: `h3.titulo-original`
- **Author (Roteiro)**: Table row with `<strong>Roteiro</strong>`
- **Artist (Arte)**: Table row with `<strong>Arte</strong>`
- **Description**: `table.tabela-projeto td[colspan='2'].tabela-projeto-conteudo p`
- **Genres**: `a.link_genero`
- **Cover image**: `img.imagem-volume-projeto` (first image)
- **Chapter links**: `a[href*='/leitor-online/projeto/']`
- **Chapter images**: `img.pagina_capitulo`

## Integration

The provider is automatically integrated into the application through the composite providers in `MangaDownloadService`:

```kotlin
CompositeDownloadProvider(
    listOf(
        MangaLivreDownloadProvider(),
        TaosectDownloadProvider(),
    ),
)

CompositeMetadataProvider(
    listOf(
        MangaDexMetadataProvider(),
        MangaLivreMetadataProvider(),
        TaosectMetadataProvider(),
    ),
)
```

## Usage Example

```bash
# Download a specific chapter
./manga-fetcher --manga-id one-punch-man --chapter 228 --output-dir ./downloads

# Download from a specific chapter onwards
./manga-fetcher --manga-id one-punch-man --from-chapter 220 --output-dir ./downloads
```

## Testing

### Unit Tests
- `TaosectHtmlParserTest`: Tests HTML parsing logic with saved fixtures

### Integration Tests
- `TaosectScraperIT`: Tests real HTTP requests to Taosect (disabled by default)

To run integration tests:
```bash
ENABLE_INTEGRATION_TESTS=true ./gradlew test --tests "TaosectScraperIT"
```

## Notes

- Chapter IDs follow the pattern `cap-tulo-{number}` (Portuguese for "chapter")
- Images are hosted on Google Drive (`drive.google.com/thumbnail`)
- The site uses WordPress-based manga theme
- Metadata includes both Portuguese and Japanese titles
