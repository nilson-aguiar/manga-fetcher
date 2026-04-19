package com.mangafetcher.downloader.infrastructure.scraper

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration test for TaosectScraper.
 * These tests make real HTTP requests to Taosect and are disabled by default.
 */
class TaosectScraperIT {
    companion object {
        private lateinit var sharedClient: PlaywrightClient
        private lateinit var scraper: TaosectScraper

        @BeforeAll
        @JvmStatic
        fun setup() {
            sharedClient = PlaywrightClient()
            scraper = TaosectScraper(playwrightClient = sharedClient)
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            scraper.close()
            sharedClient.close()
        }
    }

    @Test
    fun `should fetch manga details for One Punch Man`() {
        val details = scraper.fetchMangaDetails("one-punch-man")

        assertEquals("One Punch Man", details.title)
        assertTrue(details.authors.contains("ONE"))
        assertTrue(details.artists.contains("Murata Yuusuke"))
        assertTrue(details.description.isNotEmpty())
        assertTrue(details.tags.isNotEmpty())
        assertTrue(details.coverUrl.isNotEmpty())
    }

    @Test
    fun `should fetch chapters for One Punch Man`() {
        val chapters = scraper.fetchChapters("one-punch-man")

        assertTrue(chapters.isNotEmpty())
        assertTrue(chapters.size > 200)

        val firstChapter = chapters.first()
        assertTrue(firstChapter.number.isNotEmpty())
        assertTrue(firstChapter.id.isNotEmpty())

        // Verify no chapter has a URL as its number or null/empty values
        chapters.forEach { chapter ->
            assertTrue(
                !chapter.number.contains("://"),
                "Chapter number should not be a URL: ${chapter.number}",
            )
            assertTrue(
                chapter.number.isNotBlank(),
                "Chapter number should not be blank",
            )
            assertTrue(
                chapter.id.isNotBlank(),
                "Chapter ID should not be blank",
            )
        }
    }

    @Test
    fun `should fetch manga metadata for One Punch Man`() {
        val metadata = scraper.fetchMangaMetadata("one-punch-man")

        assertNotNull(metadata)
        assertEquals("One Punch Man", metadata.series)
        assertEquals(metadata.writer?.contains("ONE"), true)
        assertEquals(metadata.penciller?.contains("Murata Yuusuke"), true)
        assertEquals(metadata.summary?.isNotEmpty(), true)
        assertEquals(metadata.genre?.isNotEmpty(), true)
        assertEquals(metadata.alternateSeries?.isNotEmpty(), true)
    }

    @Test
    fun `should download chapter images for One Punch Man chapter 1`() {
        val tempDir =
            kotlin.io.path
                .createTempDirectory("taosect-test")
                .toFile()

        try {
            val images = scraper.downloadImages("one-punch-man", "cap-tulo-01", tempDir)

            assertTrue(images.isNotEmpty())
            assertTrue(images.size > 10)

            images.forEach { file ->
                assertTrue(file.exists())
                assertTrue(file.length() > 0)
            }
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun `should not hang when fetching details and chapters sequentially`() {
        val mangaId = "one-punch-man"
        val startTime = System.currentTimeMillis()

        val details = scraper.fetchMangaDetails(mangaId)
        assertNotNull(details)
        assertEquals("One Punch Man", details.title)

        val chapters = scraper.fetchChapters(mangaId)
        assertTrue(chapters.isNotEmpty())
        assertTrue(chapters.size > 200)

        val totalTime = System.currentTimeMillis() - startTime
        assertTrue(totalTime < 30000, "Test took too long ($totalTime ms), possible hanging issue")
    }

    @Test
    fun `should handle multiple sequential page fetches without hanging`() {
        val mangaId = "one-punch-man"

        repeat(3) { iteration ->
            val startTime = System.currentTimeMillis()
            val chapters = scraper.fetchChapters(mangaId)
            assertTrue(chapters.isNotEmpty())
            val elapsed = System.currentTimeMillis() - startTime
            assertTrue(elapsed < 15000, "Iteration ${iteration + 1} took too long ($elapsed ms)")
        }
    }
}
