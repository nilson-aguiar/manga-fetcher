package com.mangafetcher.downloader.infrastructure.metadata

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MangaDexMetadataProviderIT {
    @Test
    fun `should fetch metadata for One Piece with complete information`() {
        val provider = MangaDexMetadataProvider()

        val metadata = provider.getMetadata(title = "One Piece", chapter = "1", volume = "1")

        assertNotNull(metadata, "Metadata should not be null")
        assertTrue(metadata.series.contains("One Piece", ignoreCase = true), "Series should contain 'One Piece'")
        assertNotNull(metadata.writer, "Writer should not be null")
        assertTrue(metadata.writer.contains("Oda", ignoreCase = true), "Writer should contain 'Oda'")
        assertNotNull(metadata.summary, "Summary should not be null")
        assertNotNull(metadata.genre, "Genre should not be null")
        assertNotNull(metadata.web, "Web URL should not be null")
        assertTrue(metadata.web.contains("mangadex.org"), "Web URL should be from MangaDex")
        assertEquals("1", metadata.volume, "Volume should be preserved")
        assertEquals("1", metadata.number, "Chapter number should be preserved")
    }

    @Test
    fun `should fetch metadata for Berserk`() {
        val provider = MangaDexMetadataProvider()

        val metadata = provider.getMetadata(title = "Berserk")

        assertNotNull(metadata, "Metadata should not be null")
        assertNotNull(metadata.series, "Series should not be null")
        assertTrue(metadata.series.isNotBlank(), "Series should not be blank")
        assertNotNull(metadata.summary, "Summary should not be null")
        assertNotNull(metadata.web, "Web URL should not be null")
        assertTrue(metadata.web.contains("mangadex.org"), "Web URL should be from MangaDex")
    }

    @Test
    fun `should preserve chapter and volume information`() {
        val provider = MangaDexMetadataProvider()

        val metadata = provider.getMetadata(title = "Naruto", chapter = "42", volume = "5")

        assertNotNull(metadata, "Metadata should not be null")
        assertEquals("5", metadata.volume, "Volume should be preserved")
        assertEquals("42", metadata.number, "Chapter number should be preserved")
    }

    @Test
    fun `should handle non-existent manga gracefully`() {
        val provider = MangaDexMetadataProvider()

        val metadata =
            provider.getMetadata(
                title = "This Manga Does Not Exist XYZ123 Very Unlikely Title",
            )

        // The provider should return null for non-existent manga or return valid metadata
        assertTrue(metadata == null || metadata.series.isNotBlank(), "Should either return null or valid metadata")
    }

    @Test
    fun `should respect rate limiting between requests`() {
        val provider = MangaDexMetadataProvider()

        val startTime = System.currentTimeMillis()

        // Make two consecutive requests
        provider.getMetadata(title = "One Piece")
        provider.getMetadata(title = "Naruto")

        val elapsedTime = System.currentTimeMillis() - startTime

        // Should take at least 1 second due to rate limiting (1000ms between requests)
        assertTrue(elapsedTime >= 1000, "Should respect rate limiting, elapsed time: ${elapsedTime}ms")
    }
}
