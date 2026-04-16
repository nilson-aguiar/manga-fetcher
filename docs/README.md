# Documentation Index

Welcome to the manga-fetcher documentation! This directory contains comprehensive documentation for all aspects of the project.

## Quick Links

### Provider Documentation

| Provider | Type | Language | Status | Documentation |
|----------|------|----------|--------|---------------|
| **MangaLivre** | Download + Metadata | Portuguese | ✅ Active | [MANGALIVRE_PROVIDER.md](MANGALIVRE_PROVIDER.md) |
| **Taosect** | Download + Metadata | Portuguese | ✅ Active | [TAOSECT_PROVIDER.md](TAOSECT_PROVIDER.md) |
| **MangaDex** | Metadata Only | Multi-language | ✅ Active | [MANGADEX_PROVIDER.md](MANGADEX_PROVIDER.md) |

### Architecture Documentation

- **[Providers Overview](PROVIDERS_OVERVIEW.md)** - Comprehensive guide to the provider system, composite pattern, and how to add new providers

## Getting Started

### For Users

1. **Installation** - See main [README.md](../README.md)
2. **Basic Usage** - Run `./manga-fetcher --help`
3. **Provider Selection** - Automatic (composite pattern)

### For Developers

1. **Architecture Overview** - Read [PROVIDERS_OVERVIEW.md](PROVIDERS_OVERVIEW.md)
2. **Adding a Provider** - Follow the guide in [PROVIDERS_OVERVIEW.md](PROVIDERS_OVERVIEW.md#adding-a-new-provider)
3. **Testing** - See individual provider docs for test instructions

## Documentation by Topic

### Provider System

- **[Providers Overview](PROVIDERS_OVERVIEW.md)** - Complete guide to the provider architecture
  - Provider types (Download vs Metadata)
  - Composite provider pattern
  - Provider comparison table
  - Architecture diagrams
  - Adding new providers
  - Best practices

### Individual Providers

#### MangaLivre Provider
- **File**: [MANGALIVRE_PROVIDER.md](MANGALIVRE_PROVIDER.md)
- **Type**: Download + Metadata
- **Technology**: Web scraping with Playwright
- **Features**:
  - Primary download source
  - Portuguese manga site
  - Browser automation for JavaScript-heavy pages
  - Lazy-loading image support

#### Taosect Provider
- **File**: [TAOSECT_PROVIDER.md](TAOSECT_PROVIDER.md)
- **Type**: Download + Metadata
- **Technology**: Web scraping with Playwright
- **Features**:
  - Secondary download source
  - Portuguese manga site
  - Google Drive hosted images
  - WordPress-based theme

#### MangaDex Provider
- **File**: [MANGADEX_PROVIDER.md](MANGADEX_PROVIDER.md)
- **Type**: Metadata Only
- **Technology**: REST API
- **Features**:
  - Primary metadata source
  - Comprehensive manga information
  - Rate-limited API access (1 req/sec)
  - Multi-language support

## Common Tasks

### Running the Application

```bash
# Download a specific chapter
./manga-fetcher --manga-id one-piece --chapter 1000 --output-dir ./downloads

# Download from a chapter onwards
./manga-fetcher --manga-id naruto --from-chapter 500 --output-dir ./downloads

# Download with volume information
./manga-fetcher --manga-id bleach --chapter 1 --with-volume --output-dir ./downloads
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run unit tests only (fast, no network)
./gradlew test --tests "*Test"

# Run integration tests (slow, requires network)
ENABLE_INTEGRATION_TESTS=true ./gradlew test --tests "*IT"

# Run tests for specific provider
./gradlew test --tests "MangaLivre*"
./gradlew test --tests "Taosect*"
./gradlew test --tests "MangaDex*"
```

### Building the Application

```bash
# Build
./gradlew build

# Clean build
./gradlew clean build

# Build without tests
./gradlew build -x test
```

## Key Concepts

### Composite Provider Pattern

The application uses a **composite pattern** to try multiple providers in sequence:

```
Download Request
       ↓
CompositeDownloadProvider
       ↓
   Try MangaLivre → Success? → Return
       ↓ Failed
   Try Taosect → Success? → Return
       ↓ Failed
   Throw Exception
```

**Benefits**:
- Automatic fallback on failure
- No manual provider selection needed
- Easy to add new providers
- Resilient to individual provider failures

### Provider Types

#### Download Providers
- Implement: `MangaDownloadProvider`
- Responsibility: Fetch chapters and download images
- Methods:
  - `fetchMangaDetails(mangaId: String): MangaDetails`
  - `fetchChapters(mangaId: String): List<ChapterResult>`
  - `downloadChapterImages(mangaId: String, chapterId: String, outputDir: File): List<File>`
  - `downloadFile(url: String, outputFile: File)`

#### Metadata Providers
- Implement: `MangaMetadataProvider`
- Responsibility: Enrich chapters with metadata
- Methods:
  - `getMetadata(title: String, chapter: String?, volume: String?): MangaMetadata?`

### Technology Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Browser Automation** | Playwright | JavaScript-heavy websites (MangaLivre, Taosect) |
| **HTTP Client** | OkHttp | REST API calls (MangaDex) |
| **HTML Parsing** | Jsoup | Extract data from HTML |
| **JSON Parsing** | Kotlinx Serialization | Parse API responses |
| **File Format** | CBZ (ZIP) | Package manga chapters |
| **Metadata** | ComicInfo.xml | Embed metadata in CBZ |

## File Structure

```
docs/
├── README.md                    # This file
├── PROVIDERS_OVERVIEW.md        # Provider architecture guide
├── MANGALIVRE_PROVIDER.md       # MangaLivre documentation
├── TAOSECT_PROVIDER.md          # Taosect documentation
└── MANGADEX_PROVIDER.md         # MangaDex documentation
```

## Contributing

### Adding Documentation

When adding a new provider:

1. Create `NEW_PROVIDER.md` in this directory
2. Follow the structure of existing provider docs
3. Include:
   - Overview
   - URL structure
   - Components
   - HTML selectors (for scrapers) or API endpoints
   - Integration instructions
   - Testing guide
   - Features and limitations
4. Update this README.md
5. Update PROVIDERS_OVERVIEW.md

### Documentation Style

- Use **clear headings** for easy navigation
- Include **code examples** for key concepts
- Add **tables** for comparisons
- Use **diagrams** for architecture (ASCII or Mermaid)
- Provide **runnable commands** for all examples
- Keep it **up-to-date** with code changes

## Support

For questions or issues:

1. Check relevant provider documentation
2. Review [PROVIDERS_OVERVIEW.md](PROVIDERS_OVERVIEW.md)
3. Check the main [README.md](../README.md)
4. Run tests to verify functionality
5. Check error logs for debugging

## Version History

- **v1.0** - Initial documentation
  - MangaLivre provider
  - Taosect provider
  - MangaDex provider
  - Provider overview
  - Architecture guide

## License

Same as main project license.
