package com.mangafetcher.downloader.infrastructure.scraper

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MangaLivreScraperTest {
    private lateinit var htmlParser: HtmlParser

    @BeforeEach
    fun setUp() {
        htmlParser = HtmlParser()
    }

    @AfterEach
    fun tearDown() {
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

        val results = htmlParser.parseSearchResults(html)

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

        val results = htmlParser.parseChapters(html)

        assertEquals(1, results.size)
        assertEquals("Capítulo 200", results[0].number)
        assertEquals("capitulo-200", results[0].id)
    }

    @Test
    fun `should parse chapters with volume information`() {
        val html =
            """
            <html>
                <body>
                    <li class="wp-manga-chapter">
                        <span class="vol">Vol. 34</span>
                        <a href="https://site.com/manga/solo-leveling/capitulo-200/">Capítulo 200</a>
                    </li>
                </body>
            </html>
            """.trimIndent()

        val results = htmlParser.parseChapters(html)

        assertEquals(1, results.size)
        assertEquals("Vol. 34", results[0].volume)
        assertEquals("Capítulo 200", results[0].number)
    }
}
