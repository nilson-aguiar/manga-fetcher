package com.mangafetcher.downloader.infrastructure.scraper

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration test for TaosectScraper.
 * These tests make real HTTP requests to Taosect and are disabled by default.
 * Set ENABLE_INTEGRATION_TESTS=true to run them.
 */
@EnabledIfEnvironmentVariable(named = "ENABLE_INTEGRATION_TESTS", matches = "true")
class TaosectScraperIT {
    @Test
    fun `should fetch manga details for One Punch Man`() {
        TaosectScraper().use { scraper ->
            val details = scraper.fetchMangaDetails("one-punch-man")

            assertEquals("One Punch Man", details.title)
            assertTrue(details.authors.contains("ONE"))
            assertTrue(details.artists.contains("Murata Yuusuke"))
            assertTrue(details.description.isNotEmpty())
            assertTrue(details.tags.isNotEmpty())
            assertTrue(details.coverUrl.isNotEmpty())

            println("Title: ${details.title}")
            println("Authors: ${details.authors}")
            println("Artists: ${details.artists}")
            println("Tags: ${details.tags}")
            println("Cover URL: ${details.coverUrl}")
        }
    }

    @Test
    fun `should fetch chapters for One Punch Man`() {
        TaosectScraper().use { scraper ->
            val chapters = scraper.fetchChapters("one-punch-man")

            assertTrue(chapters.isNotEmpty())
            assertTrue(chapters.size > 200)

            val firstChapter = chapters.first()
            assertTrue(firstChapter.number.isNotEmpty())
            assertTrue(firstChapter.id.isNotEmpty())

            println("Found ${chapters.size} chapters")
            println("First chapter: ${firstChapter.number} (ID: ${firstChapter.id})")
            println("Last chapter: ${chapters.last().number} (ID: ${chapters.last().id})")
        }
    }

    @Test
    fun `should fetch manga metadata for One Punch Man`() {
        TaosectScraper().use { scraper ->
            val metadata = scraper.fetchMangaMetadata("one-punch-man")

            assertNotNull(metadata)
            assertEquals("One Punch Man", metadata.series)
            assertTrue(metadata.writer?.contains("ONE") == true)
            assertTrue(metadata.penciller?.contains("Murata Yuusuke") == true)
            assertTrue(metadata.summary?.isNotEmpty() == true)
            assertTrue(metadata.genre?.isNotEmpty() == true)
            assertTrue(metadata.alternateSeries?.isNotEmpty() == true)

            println("Series: ${metadata.series}")
            println("Writer: ${metadata.writer}")
            println("Penciller: ${metadata.penciller}")
            println("Genres: ${metadata.genre}")
            println("Alternate: ${metadata.alternateSeries}")
        }
    }

    @Test
    fun `should download chapter images for One Punch Man chapter 1`() {
        TaosectScraper().use { scraper ->
            val tempDir = kotlin.io.path.createTempDirectory("taosect-test").toFile()

            try {
                val images = scraper.downloadImages("one-punch-man", "cap-tulo-01", tempDir)

                assertTrue(images.isNotEmpty())
                assertTrue(images.size > 10)

                images.forEach { file ->
                    assertTrue(file.exists())
                    assertTrue(file.length() > 0)
                }

                println("Downloaded ${images.size} images to ${tempDir.absolutePath}")
                println("First image: ${images.first().name} (${images.first().length()} bytes)")
            } finally {
                tempDir.deleteRecursively()
            }
        }
    }
}
