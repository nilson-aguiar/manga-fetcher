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
}
