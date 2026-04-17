package com.mangafetcher.downloader.infrastructure.metadata

import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CompositeMetadataProviderIT {
    @Test
    fun `should use first successful provider from the list`() {
        val mangaDexProvider = MangaDexMetadataProvider()
        val mangaLivreProvider = MangaLivreMetadataProvider()

        CompositeMetadataProvider(listOf(mangaDexProvider, mangaLivreProvider)).use { composite ->
            // Test with a well-known manga that should be available on MangaDex
            val metadata = composite.getMetadata(title = "One Piece")
            assertNotNull(metadata, "Should find One Piece metadata from first provider")
            assertTrue(metadata.series.isNotBlank(), "Series should not be blank")
            assertNotNull(metadata.web, "Web URL should not be null")
            assertTrue(
                metadata.web.contains("mangadex.org"),
                "Should use MangaDex as primary source, got: ${metadata.web}",
            )
        }
    }

    @Test
    fun `should fallback to second provider if first returns null`() {
        // Create a mock provider that always returns null
        val alwaysNullProvider =
            object : com.mangafetcher.downloader.domain.model.MangaMetadataProvider {
                override fun getMetadata(
                    title: String,
                    chapter: String?,
                    volume: String?,
                ) = null
            }

        val mangaDexProvider = MangaDexMetadataProvider()

        CompositeMetadataProvider(listOf(alwaysNullProvider, mangaDexProvider)).use { composite ->
            val metadata = composite.getMetadata(title = "One Piece")
            assertNotNull(metadata, "Should find metadata from fallback provider")
            assertNotNull(metadata.web, "Web URL should not be null")
            assertTrue(metadata.web.contains("mangadex.org"), "Should use second provider (MangaDex)")
        }
    }

    @Test
    fun `should fallback to second provider if first throws exception`() {
        // Create a mock provider that always throws
        val alwaysFailsProvider =
            object : com.mangafetcher.downloader.domain.model.MangaMetadataProvider {
                override fun getMetadata(
                    title: String,
                    chapter: String?,
                    volume: String?,
                ) = throw RuntimeException("Provider failed")
            }

        val mangaDexProvider = MangaDexMetadataProvider()

        CompositeMetadataProvider(listOf(alwaysFailsProvider, mangaDexProvider)).use { composite ->
            val metadata = composite.getMetadata(title = "One Piece")
            assertNotNull(metadata, "Should find metadata from fallback provider despite first provider failing")
            assertNotNull(metadata.web, "Web URL should not be null")
            assertTrue(metadata.web.contains("mangadex.org"), "Should use second provider (MangaDex)")
        }
    }

    @Test
    fun `should return null when all providers fail or return null`() {
        val alwaysNullProvider1 =
            object : com.mangafetcher.downloader.domain.model.MangaMetadataProvider {
                override fun getMetadata(
                    title: String,
                    chapter: String?,
                    volume: String?,
                ) = null
            }

        val alwaysNullProvider2 =
            object : com.mangafetcher.downloader.domain.model.MangaMetadataProvider {
                override fun getMetadata(
                    title: String,
                    chapter: String?,
                    volume: String?,
                ) = null
            }

        CompositeMetadataProvider(listOf(alwaysNullProvider1, alwaysNullProvider2)).use { composite ->
            val metadata = composite.getMetadata(title = "Non Existent Manga XYZ123")
            assertTrue(metadata == null, "Should return null when all providers return null")
        }
    }

    @Test
    fun `should close all providers when composite is closed`() {
        var provider1Closed = false
        var provider2Closed = false

        val trackableProvider1 =
            object :
                com.mangafetcher.downloader.domain.model.MangaMetadataProvider,
                AutoCloseable {
                override fun getMetadata(
                    title: String,
                    chapter: String?,
                    volume: String?,
                ) = null

                override fun close() {
                    provider1Closed = true
                }
            }

        val trackableProvider2 =
            object :
                com.mangafetcher.downloader.domain.model.MangaMetadataProvider,
                AutoCloseable {
                override fun getMetadata(
                    title: String,
                    chapter: String?,
                    volume: String?,
                ) = null

                override fun close() {
                    provider2Closed = true
                }
            }

        CompositeMetadataProvider(listOf(trackableProvider1, trackableProvider2)).use { composite ->
            composite.getMetadata(title = "Test")
        }

        assertTrue(provider1Closed, "First provider should be closed")
        assertTrue(provider2Closed, "Second provider should be closed")
    }

    @Test
    fun `should preserve chapter and volume through composite provider`() {
        val mangaDexProvider = MangaDexMetadataProvider()
        val mangaLivreProvider = MangaLivreMetadataProvider()

        CompositeMetadataProvider(listOf(mangaDexProvider, mangaLivreProvider)).use { composite ->
            val metadata = composite.getMetadata(title = "One Piece", chapter = "999", volume = "99")

            assertNotNull(metadata, "Metadata should not be null")
            assertTrue(metadata.number == "999", "Chapter should be preserved")
            assertTrue(metadata.volume == "99", "Volume should be preserved")
        }
    }
}
