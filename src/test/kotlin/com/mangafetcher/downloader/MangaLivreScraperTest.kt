package com.mangafetcher.downloader

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MangaLivreScraperTest {

    private val server = MockWebServer()
    private lateinit var scraper: MangaLivreScraper

    @BeforeEach
    fun setUp() {
        server.start()
        val client = CoreHttpClient()
        scraper = MangaLivreScraper(client, server.url("/").toString())
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `should search manga and return list of results`() {
        val html = """
            <html>
                <body>
                    <ul class="seriesList">
                        <li>
                            <a href="/manga/solo-leveling/123" title="Solo Leveling">
                                <span class="series-title">Solo Leveling</span>
                            </a>
                        </li>
                        <li>
                            <a href="/manga/one-piece/456" title="One Piece">
                                <span class="series-title">One Piece</span>
                            </a>
                        </li>
                    </ul>
                </body>
            </html>
        """.trimIndent()

        server.enqueue(MockResponse().setBody(html))

        val results = scraper.search("solo")

        assertEquals(2, results.size)
        assertEquals("Solo Leveling", results[0].title)
        assertEquals("123", results[0].id)
        assertEquals("One Piece", results[1].title)
        assertEquals("456", results[1].id)
    }

    @Test
    fun `should fetch chapters and return list of results`() {
        val html = """
            <html>
                <body>
                    <ul class="chapterList">
                        <li>
                            <a href="/manga/solo-leveling/123/chapter-2" title="Chapter 2">
                                <span class="chapter-number">Chapter 2</span>
                            </a>
                        </li>
                        <li>
                            <a href="/manga/solo-leveling/123/chapter-1" title="Chapter 1">
                                <span class="chapter-number">Chapter 1</span>
                            </a>
                        </li>
                    </ul>
                </body>
            </html>
        """.trimIndent()

        server.enqueue(MockResponse().setBody(html))

        val results = scraper.fetchChapters("123")

        assertEquals(2, results.size)
        assertEquals("Chapter 2", results[0].number)
        assertEquals("chapter-2", results[0].id)
        assertEquals("Chapter 1", results[1].number)
        assertEquals("chapter-1", results[1].id)
    }

    @Test
    fun `should download images and return list of files`() {
        val html = """
            <html>
                <body>
                    <div class="manga-pages">
                        <img src="${server.url("/img1.jpg")}">
                        <img src="${server.url("/img2.jpg")}">
                    </div>
                </body>
            </html>
        """.trimIndent()

        server.enqueue(MockResponse().setBody(html))
        server.enqueue(MockResponse().setBody("image1 data"))
        server.enqueue(MockResponse().setBody("image2 data"))

        val tempDir = java.nio.file.Files.createTempDirectory("scraper-test").toFile()
        val files = scraper.downloadImages("manga123", "chapter1", tempDir)

        assertEquals(2, files.size)
        assertEquals("image1 data", files[0].readText())
        assertEquals("image2 data", files[1].readText())
        
        tempDir.deleteRecursively()
    }
}
