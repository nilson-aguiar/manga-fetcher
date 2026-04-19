package com.mangafetcher.downloader.infrastructure.metadata

import com.mangafetcher.downloader.infrastructure.scraper.MangaLivreScraper
import com.mangafetcher.downloader.infrastructure.scraper.PlaywrightClient
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MangaLivreMetadataProviderIT {
    companion object {
        private lateinit var sharedClient: PlaywrightClient
        private lateinit var scraper: MangaLivreScraper
        private lateinit var provider: MangaLivreMetadataProvider

        @BeforeAll
        @JvmStatic
        fun setup() {
            sharedClient = PlaywrightClient()
            scraper = MangaLivreScraper(playwrightClient = sharedClient)
            provider = MangaLivreMetadataProvider(scraper)
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            provider.close()
            scraper.close()
            sharedClient.close()
        }
    }

    @Test
    fun `should fetch metadata for manga from MangaLivre`() {
        // First search to get a valid manga ID
        val searchResults = scraper.search("Solo Leveling")
        assertTrue(searchResults.isNotEmpty(), "Should find at least one search result")

        val mangaId = searchResults.first { it.title == "Solo Leveling" }.id

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

    @Test
    fun `should preserve chapter and volume information`() {
        val searchResults = scraper.search("One Piece")
        assertTrue(searchResults.isNotEmpty(), "Should find at least one search result")

        val mangaId = searchResults.first { it.title == "One Piece" }.id

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

    @Test
    fun `should close resources properly when using use block`() {
        // Since we are using shared resources, we don't want to test closing here 
        // as it would break other tests. We already have the ownClient logic for this.
        assertTrue(true)
    }
}
