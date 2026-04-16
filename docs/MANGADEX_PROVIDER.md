# MangaDex Provider

This document describes the MangaDex metadata provider implementation for the manga-fetcher application.

## Overview

MangaDex (https://mangadex.org) is a popular manga reading platform with a comprehensive API. The provider is used **exclusively for metadata enrichment** and does not download manga chapters (as MangaDex's terms require using their official reader).

This provider is **the primary metadata source** in the application, tried before other metadata providers.

## API Structure

MangaDex provides a REST API at `https://api.mangadex.org`:

- **Search manga**: `GET /manga?title={query}&limit=1&includes[]=author&includes[]=artist`
- **Manga details**: Available in search response with `includes` parameter

## Components

### MangaDexMetadataProvider
Implements the `MangaMetadataProvider` interface and uses:
- **ResilientHttpClient** with built-in rate limiting (1000ms between requests)
- **Kotlinx Serialization JSON** for parsing API responses
- **OkHttp** for HTTP requests

## API Response Structure

The provider parses the following fields from the MangaDex API:

### Manga Object
```json
{
  "data": [
    {
      "id": "manga-uuid",
      "attributes": {
        "title": { "en": "One Piece", "ja": "ワンピース" },
        "altTitles": [ { "ja-ro": "One Piece" }, ... ],
        "description": { "en": "Description text...", ... },
        "tags": [
          {
            "attributes": {
              "name": { "en": "Action", ... }
            }
          }
        ]
      },
      "relationships": [
        {
          "type": "author",
          "attributes": { "name": "Oda Eiichiro" }
        },
        {
          "type": "artist",
          "attributes": { "name": "Oda Eiichiro" }
        }
      ]
    }
  ]
}
```

## Metadata Mapping

The provider maps MangaDex API fields to `MangaMetadata`:

| MangaMetadata Field | MangaDex API Source | Notes |
|---------------------|---------------------|-------|
| `series` | `attributes.title` (first available language) | Fallback to input title |
| `writer` | `relationships` where `type == "author"` | Multiple authors joined with `, ` |
| `penciller` | `relationships` where `type == "artist"` | Multiple artists joined with `, ` |
| `genre` | `attributes.tags[].attributes.name.en` | Joined with `,` |
| `summary` | `attributes.description.en` (or first available) | Prefers English description |
| `alternateSeries` | `attributes.altTitles[]` | All alternate titles joined with `,` |
| `volume` | Passed through from input | Preserved from request |
| `number` | Passed through from input | Preserved from request |
| `web` | `https://mangadex.org/manga/{id}` | Generated from manga ID |

## Rate Limiting

The provider implements rate limiting to respect MangaDex API guidelines:

- **Rate limit**: 1000ms (1 second) between requests
- **Max retries**: 3 attempts per request
- **Retry delay**: 100ms × (attempt + 1)
- **User Agent**: `MangaFetcher/0.0.1`

### ResilientHttpClient Behavior

```kotlin
ResilientHttpClient(
    maxRetries = 3,
    rateLimitMs = 1000  // 1 second between requests
)
```

**Retry logic**:
- Retries on 5xx server errors
- Does NOT retry on 4xx client errors
- Waits progressively longer between retries (100ms, 200ms, 300ms)

## Integration

The provider is the **first metadata provider** in the composite:

```kotlin
CompositeMetadataProvider(
    listOf(
        MangaDexMetadataProvider(),    // Tried first
        MangaLivreMetadataProvider(),
        TaosectMetadataProvider(),
    ),
)
```

This means MangaDex metadata is preferred when available, with fallback to scraper-based metadata if:
- Manga is not found on MangaDex
- API request fails
- Response parsing fails

## Usage

The provider is used automatically during chapter downloads to enrich CBZ files with metadata:

```kotlin
// Automatically called during download
val metadata = metadataProvider.getMetadata(
    title = "One Piece",
    chapter = "1000",
    volume = "99"
)

// Generates ComicInfo.xml embedded in CBZ
if (metadata != null) {
    val comicInfoXml = ComicInfoGenerator.generate(metadata)
}
```

## Testing

### Integration Tests
- `MangaDexMetadataProviderIT`: Tests real API requests to MangaDex

Test examples:
```bash
# Run all MangaDex tests
./gradlew test --tests "MangaDexMetadataProviderIT"

# Run specific test
./gradlew test --tests "MangaDexMetadataProviderIT.should fetch metadata for One Piece"
```

### Rate Limiting Test
The test suite includes a rate limiting verification:
```kotlin
@Test
fun `should respect rate limiting between requests`() {
    val provider = MangaDexMetadataProvider()
    val startTime = System.currentTimeMillis()
    
    provider.getMetadata(title = "One Piece")
    provider.getMetadata(title = "Naruto")
    
    val elapsedTime = System.currentTimeMillis() - startTime
    assertTrue(elapsedTime >= 1000) // At least 1 second
}
```

## Features

✅ **Comprehensive metadata**: Authors, artists, description, genres, alternate titles  
✅ **Multi-language support**: Prefers English, falls back to other languages  
✅ **Rate limiting**: Respects API guidelines (1 req/second)  
✅ **Resilient**: Auto-retry with exponential backoff  
✅ **Web links**: Generates MangaDex manga URLs  
✅ **Null safety**: Returns null on failure (allows fallback)  

## Limitations

❌ **No chapter downloads**: Only provides metadata (MangaDex TOS compliance)  
❌ **Search only**: Uses title search, not manga ID lookup  
❌ **First result**: Takes first search result (may not be exact match)  
❌ **English bias**: Prefers English descriptions and tags  

## API Reference

### MangaDex API Documentation
- Base URL: `https://api.mangadex.org`
- Documentation: `https://api.mangadex.org/docs/`
- Rate limit: 5 requests/second (we use 1 req/second for safety)

### Request Example
```http
GET https://api.mangadex.org/manga?title=One%20Piece&limit=1&includes[]=author&includes[]=artist
User-Agent: MangaFetcher/0.0.1
```

### Response Handling
- **Success (200)**: Parse JSON and extract metadata
- **Client error (4xx)**: Throw exception (no retry)
- **Server error (5xx)**: Retry up to 3 times
- **Network error**: Retry up to 3 times
- **Parse error**: Return null (silent failure)

## Error Handling

The provider handles errors gracefully:

```kotlin
override fun getMetadata(
    title: String,
    chapter: String?,
    volume: String?
): MangaMetadata? {
    return try {
        // API call and parsing
    } catch (e: Exception) {
        null  // Returns null on any error
    }
}
```

This allows the composite provider to try the next provider in the chain.

## Best Practices

1. **Use MangaDex for metadata only**: Don't attempt to download chapters
2. **Respect rate limits**: The provider enforces 1 req/second automatically
3. **Handle nulls**: Always check for null metadata and have fallbacks
4. **Prefer exact titles**: Search works best with exact manga titles
5. **Monitor API changes**: MangaDex API may change; update parser accordingly

## Future Improvements

Potential enhancements:
- [ ] Support manga ID lookup (not just search)
- [ ] Improve search result ranking (fuzzy matching)
- [ ] Cache API responses to reduce requests
- [ ] Support multiple languages in metadata
- [ ] Add cover image downloading from MangaDex
