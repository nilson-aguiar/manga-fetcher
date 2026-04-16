package com.mangafetcher.downloader.infrastructure.metadata

import com.mangafetcher.downloader.infrastructure.scraper.MangaLivreScraper
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MangaLivreMetadataProviderIT {
    @Test
    fun `should fetch metadata for manga from MangaLivre`() {
        MangaLivreMetadataProvider().use { provider ->
            // First search to get a valid manga ID
            val scraper = MangaLivreScraper()
            scraper.use {
                val searchResults = it.search("Solo Leveling")
                assertTrue(searchResults.isNotEmpty(), "Should find at least one search result")

                val mangaId = searchResults.first().id

                // Now test the metadata provider using the manga ID
                val metadata = provider.getMetadata(title = mangaId)

                assertNotNull(metadata, "Metadata should not be null")
                assertNotNull(metadata.series, "Series should not be null")
                assertTrue(metadata.series.isNotBlank(), "Series should not be blank")
                assertNotNull(metadata.web, "Web URL should not be null")
                assertTrue(
                    metadata.web.contains("mangalivre", ignoreCase = true),
                    "Web URL should be from MangaLivre",
                )
            }
        }
    }

    @Test
    fun `should preserve chapter and volume information`() {
        MangaLivreMetadataProvider().use { provider ->
            val scraper = MangaLivreScraper()
            scraper.use {
                val searchResults = it.search("One Piece")
                assertTrue(searchResults.isNotEmpty(), "Should find at least one search result")

                val mangaId = searchResults.first().id

                // Test with chapter and volume
                val metadata = provider.getMetadata(title = mangaId, chapter = "100", volume = "10")

                assertNotNull(metadata, "Metadata should not be null")
                assertTrue(
                    metadata.number == "100",
                    "Chapter number should be preserved, got: ${metadata.number}",
                )
                assertTrue(
                    metadata.volume == "10",
                    "Volume should be preserved, got: ${metadata.volume}",
                )
            }
        }
    }

    @Test
    fun `should close resources properly when using use block`() {
        var closedSuccessfully = false

        try {
            MangaLivreMetadataProvider().use { provider ->
                val scraper = MangaLivreScraper()
                scraper.use {
                    val searchResults = it.search("Naruto")
                    if (searchResults.isNotEmpty()) {
                        provider.getMetadata(title = searchResults.first().id)
                    }
                }
            }
            closedSuccessfully = true
        } catch (e: Exception) {
            // If we get here, resources weren't closed properly
        }

        assertTrue(closedSuccessfully, "Resources should close properly with use block")
    }
}
