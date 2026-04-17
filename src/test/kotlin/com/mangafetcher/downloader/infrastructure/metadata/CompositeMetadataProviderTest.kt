package com.mangafetcher.downloader.infrastructure.metadata

import com.mangafetcher.downloader.domain.model.MangaMetadata
import com.mangafetcher.downloader.domain.model.MangaMetadataProvider
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CompositeMetadataProviderTest {
    @Test
    fun `should return metadata from first provider when available`() {
        val metadata1 =
            MangaMetadata(
                series = "Test Manga",
                writer = "Author 1",
            )
        val provider1 = TestMetadataProvider(metadata1)
        val provider2 = TestMetadataProvider(null)

        val composite = CompositeMetadataProvider(listOf(provider1, provider2))

        val result = composite.getMetadata("test", "1", null)
        assertEquals(metadata1, result)
        assertEquals(1, provider1.callCount)
        assertEquals(0, provider2.callCount)
    }

    @Test
    fun `should fallback to second provider when first returns null`() {
        val metadata2 =
            MangaMetadata(
                series = "Test Manga",
                writer = "Author 2",
            )
        val provider1 = TestMetadataProvider(null)
        val provider2 = TestMetadataProvider(metadata2)

        val composite = CompositeMetadataProvider(listOf(provider1, provider2))

        val result = composite.getMetadata("test", "1", null)
        assertEquals(metadata2, result)
        assertEquals(1, provider1.callCount)
        assertEquals(1, provider2.callCount)
    }

    @Test
    fun `should fallback to second provider when first throws exception`() {
        val metadata2 =
            MangaMetadata(
                series = "Test Manga",
                writer = "Author 2",
            )
        val provider1 = TestMetadataProvider(exception = RuntimeException("Failed"))
        val provider2 = TestMetadataProvider(metadata2)

        val composite = CompositeMetadataProvider(listOf(provider1, provider2))

        val result = composite.getMetadata("test", "1", null)
        assertEquals(metadata2, result)
        assertEquals(1, provider1.callCount)
        assertEquals(1, provider2.callCount)
    }

    @Test
    fun `should return null when all providers fail`() {
        val provider1 = TestMetadataProvider(null)
        val provider2 = TestMetadataProvider(exception = RuntimeException("Failed"))

        val composite = CompositeMetadataProvider(listOf(provider1, provider2))

        val result = composite.getMetadata("test", "1", null)
        assertNull(result)
    }

    @Test
    fun `should close all providers on close`() {
        val provider1 = CloseableTestProvider()
        val provider2 = CloseableTestProvider()

        val composite = CompositeMetadataProvider(listOf(provider1, provider2))
        composite.close()

        assertEquals(true, provider1.closed)
        assertEquals(true, provider2.closed)
    }

    private class TestMetadataProvider(
        private val metadata: MangaMetadata? = null,
        private val exception: Exception? = null,
    ) : MangaMetadataProvider {
        var callCount = 0

        override fun getMetadata(
            title: String,
            chapter: String?,
            volume: String?,
        ): MangaMetadata? {
            callCount++
            if (exception != null) throw exception
            return metadata
        }
    }

    private class CloseableTestProvider :
        MangaMetadataProvider,
        AutoCloseable {
        var closed = false

        override fun getMetadata(
            title: String,
            chapter: String?,
            volume: String?,
        ): MangaMetadata? = null

        override fun close() {
            closed = true
        }
    }
}
