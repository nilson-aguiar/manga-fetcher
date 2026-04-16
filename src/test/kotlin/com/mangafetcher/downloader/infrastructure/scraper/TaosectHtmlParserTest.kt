package com.mangafetcher.downloader.infrastructure.scraper

import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for TaosectHtmlParser.
 * Uses saved HTML fixtures to test parsing logic.
 */
class TaosectHtmlParserTest {
    private val parser = TaosectHtmlParser()

    @Test
    fun `should parse manga details from HTML`() {
        val html = File("/tmp/taosect-manga.html").takeIf { it.exists() }?.readText()
            ?: return // Skip if fixture doesn't exist

        val details = parser.parseMangaDetails(html)

        assertEquals("One Punch Man", details.title)
        assertTrue(details.authors.contains("ONE"))
        assertTrue(details.artists.contains("Murata Yuusuke"))
        assertTrue(details.description.contains("herói"))
        assertTrue(details.tags.contains("Ação"))
        assertTrue(details.coverUrl.isNotEmpty())
    }

    @Test
    fun `should parse chapters from HTML`() {
        val html = File("/tmp/taosect-manga.html").takeIf { it.exists() }?.readText()
            ?: return // Skip if fixture doesn't exist

        val chapters = parser.parseChapters(html)

        assertTrue(chapters.isNotEmpty())
        assertTrue(chapters.size > 200)

        // Check that chapter IDs follow expected pattern
        val firstChapter = chapters.first()
        assertTrue(firstChapter.id.matches(Regex("cap-tulo-\\d+.*")))
        assertTrue(firstChapter.number.isNotEmpty())
    }

    @Test
    fun `should parse manga metadata from HTML`() {
        val html = File("/tmp/taosect-manga.html").takeIf { it.exists() }?.readText()
            ?: return // Skip if fixture doesn't exist

        val metadata = parser.parseMangaMetadata(html, "https://taosect.com", "one-punch-man")

        assertNotNull(metadata)
        assertEquals("One Punch Man", metadata.series)
        assertTrue(metadata.writer?.contains("ONE") == true)
        assertTrue(metadata.penciller?.contains("Murata Yuusuke") == true)
        assertTrue(metadata.summary?.contains("herói") == true)
        assertTrue(metadata.genre?.contains("Ação") == true)
        assertEquals("ワンパンマン", metadata.alternateSeries)
        assertEquals("https://taosect.com/manga/one-punch-man/", metadata.web)
    }

    @Test
    fun `should extract image URLs from chapter HTML`() {
        val html = File("/tmp/taosect-chapter.html").takeIf { it.exists() }?.readText()
            ?: return // Skip if fixture doesn't exist

        val imageUrls = parser.extractImageUrls(html)

        assertTrue(imageUrls.isNotEmpty())
        assertTrue(imageUrls.size > 10)

        // Taosect images are hosted on Google Drive
        imageUrls.forEach { url ->
            assertTrue(url.contains("drive.google.com"))
        }
    }
}
