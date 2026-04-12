package com.mangafetcher.downloader

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MangaLivreScraperTest {
    private lateinit var scraper: MangaLivreScraper

    @BeforeEach
    fun setUp() {
        scraper = MangaLivreScraper()
    }

    @AfterEach
    fun tearDown() {
        scraper.close()
    }

    @Test
    fun `should parse search results`() {
        val html =
            """
            <html>
                <body>
                    <div class="c-tabs-item__content">
                        <div class="tab-summary">
                            <div class="post-title h4">
                                <a href="https://site.com/manga/solo-leveling/">Solo Leveling</a>
                            </div>
                        </div>
                    </div>
                </body>
            </html>
            """.trimIndent()

        val results = scraper.parseSearchResults(html)

        assertEquals(1, results.size)
        assertEquals("Solo Leveling", results[0].title)
        assertEquals("solo-leveling", results[0].id)
    }

    @Test
    fun `should parse chapters`() {
        val html =
            """
            <html>
                <body>
                    <div class="chapter-box">
                        <a href="https://site.com/manga/solo-leveling/capitulo-200/">Capítulo 200</a>
                    </div>
                </body>
            </html>
            """.trimIndent()

        val results = scraper.parseChapters(html)

        assertEquals(1, results.size)
        assertEquals("Capítulo 200", results[0].number)
        assertEquals("capitulo-200", results[0].id)
    }
}
