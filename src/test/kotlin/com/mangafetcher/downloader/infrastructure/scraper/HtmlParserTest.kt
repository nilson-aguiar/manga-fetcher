package com.mangafetcher.downloader.infrastructure.scraper

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Tests for HtmlParser - demonstrates how pure parsing logic is now easily testable
 * without requiring browser automation or network calls.
 */
class HtmlParserTest {
    private val parser = HtmlParser()

    @Test
    fun `should parse search results from HTML`() {
        val html =
            """
            <div class="tab-summary">
                <div class="post-title">
                    <a href="https://mangalivre.to/manga/one-piece/">One Piece</a>
                </div>
                <div class="post-title">
                    <a href="https://mangalivre.to/manga/naruto/">Naruto</a>
                </div>
            </div>
            """.trimIndent()

        val results = parser.parseSearchResults(html)

        assertEquals(2, results.size)
        assertEquals("One Piece", results[0].title)
        assertEquals("one-piece", results[0].id)
        assertEquals("Naruto", results[1].title)
        assertEquals("naruto", results[1].id)
    }

    @Test
    fun `should parse chapters from HTML`() {
        val html =
            """
            <li class="wp-manga-chapter">
                <a href="https://mangalivre.to/manga/one-piece/chapter-1/">Chapter 1</a>
            </li>
            <li class="wp-manga-chapter">
                <a href="https://mangalivre.to/manga/one-piece/chapter-2/">Chapter 2</a>
            </li>
            """.trimIndent()

        val chapters = parser.parseChapters(html)

        assertEquals(2, chapters.size)
        assertEquals("Chapter 1", chapters[0].number)
        assertEquals("chapter-1", chapters[0].id)
        assertEquals("Chapter 2", chapters[1].number)
        assertEquals("chapter-2", chapters[1].id)
    }

    @Test
    fun `should parse manga details from HTML`() {
        val html =
            """
            <div class="post-title"><h1>One Piece</h1></div>
            <div class="author-content"><a>Oda Eiichiro</a></div>
            <div class="artist-content"><a>Oda Eiichiro</a></div>
            <div class="description-summary">A great adventure manga</div>
            <div class="genres-content">
                <a>Action</a>
                <a>Adventure</a>
            </div>
            <div class="summary_image"><img src="https://example.com/cover.jpg" /></div>
            """.trimIndent()

        val details = parser.parseMangaDetails(html)

        assertEquals("One Piece", details.title)
        assertEquals("Oda Eiichiro", details.authors)
        assertEquals("Oda Eiichiro", details.artists)
        assertEquals("A great adventure manga", details.description)
        assertEquals("Action, Adventure", details.tags)
        assertEquals("https://example.com/cover.jpg", details.coverUrl)
    }

    @Test
    fun `should extract image URLs from chapter HTML`() {
        val html =
            """
            <div class="reading-content">
                <img data-src="https://example.com/page1.jpg" />
                <img data-src="https://example.com/page2.jpg" />
                <img src="https://example.com/page3.jpg" />
            </div>
            """.trimIndent()

        val imageUrls = parser.extractImageUrls(html)

        assertEquals(3, imageUrls.size)
        assertEquals("https://example.com/page1.jpg", imageUrls[0])
        assertEquals("https://example.com/page2.jpg", imageUrls[1])
        assertEquals("https://example.com/page3.jpg", imageUrls[2])
    }
}
