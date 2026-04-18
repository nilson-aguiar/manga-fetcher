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

            // Verify chapters are sorted (check some known sequences)
            val chapter72Index = chapters.indexOfFirst { it.number.startsWith("72") && !it.number.contains(".") }
            val chapter72_5Index = chapters.indexOfFirst { it.number == "72.5" }
            if (chapter72Index >= 0 && chapter72_5Index >= 0) {
                assertTrue(
                    chapter72_5Index > chapter72Index,
                    "Chapter 72.5 should come after chapter 72",
                )
            }

            // Check that chapter 128 comes before chapter 195
            val chapter128Index = chapters.indexOfFirst { it.number == "128" }
            val chapter195Index = chapters.indexOfFirst { it.number.startsWith("195") }
            if (chapter128Index >= 0 && chapter195Index >= 0) {
                assertTrue(
                    chapter128Index < chapter195Index,
                    "Chapter 128 should come before chapter 195",
                )
            }

            println(chapters)
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
            assertEquals(metadata.writer?.contains("ONE"), true)
            assertEquals(metadata.penciller?.contains("Murata Yuusuke"), true)
            assertEquals(metadata.summary?.isNotEmpty(), true)
            assertEquals(metadata.genre?.isNotEmpty(), true)
            assertEquals(metadata.alternateSeries?.isNotEmpty(), true)

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

                println("Downloaded ${images.size} images to ${tempDir.absolutePath}")
                println("First image: ${images.first().name} (${images.first().length()} bytes)")
            } finally {
                tempDir.deleteRecursively()
            }
        }
    }

    @Test
    fun `should not hang when fetching details and chapters sequentially`() {
        // This test reproduces the production issue where fetchMangaDetails()
        // followed by fetchChapters() causes the second page.content() call to hang
        TaosectScraper().use { scraper ->
            val mangaId = "one-punch-man"

            println("Starting sequential fetch test...")
            val startTime = System.currentTimeMillis()

            // First call - fetchMangaDetails
            println("Fetching manga details...")
            val details = scraper.fetchMangaDetails(mangaId)
            assertNotNull(details)
            assertEquals("One Punch Man", details.title)
            val detailsTime = System.currentTimeMillis() - startTime
            println("Details fetched in ${detailsTime}ms")

            // Second call - fetchChapters (this used to hang)
            println("Fetching chapters...")
            val chaptersStartTime = System.currentTimeMillis()
            val chapters = scraper.fetchChapters(mangaId)
            assertTrue(chapters.isNotEmpty())
            assertTrue(chapters.size > 200)
            val chaptersTime = System.currentTimeMillis() - chaptersStartTime
            println("Chapters fetched in ${chaptersTime}ms")

            val totalTime = System.currentTimeMillis() - startTime
            println("Total time: ${totalTime}ms")

            // If this test completes in under 30 seconds, it's not hanging
            assertTrue(totalTime < 30000, "Test took too long ($totalTime ms), possible hanging issue")
        }
    }

    @Test
    fun `should handle multiple sequential page fetches without hanging`() {
        // Test fetching the same page multiple times to ensure no memory/resource leaks
        TaosectScraper().use { scraper ->
            val mangaId = "one-punch-man"

            repeat(3) { iteration ->
                println("Iteration ${iteration + 1}: Fetching chapters...")
                val startTime = System.currentTimeMillis()

                val chapters = scraper.fetchChapters(mangaId)
                assertTrue(chapters.isNotEmpty())

                val elapsed = System.currentTimeMillis() - startTime
                println("Iteration ${iteration + 1}: Fetched ${chapters.size} chapters in ${elapsed}ms")

                // Each iteration should complete in reasonable time
                assertTrue(elapsed < 15000, "Iteration ${iteration + 1} took too long ($elapsed ms)")
            }
        }
    }
}
