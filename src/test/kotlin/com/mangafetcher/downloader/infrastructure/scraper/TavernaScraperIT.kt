package com.mangafetcher.downloader.infrastructure.scraper

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Tag("integration")
class TavernaScraperIT {
    @Test
    fun `should search and fetch Claymore chapters from Taverna de Quadrinhos`() {
        TavernaScraper(enablePlaywrightFallback = false).use { scraper ->
            val searchResults = scraper.search("claymore")
            assertTrue(searchResults.any { it.id == "claymore" }, "Expected 'claymore' in search results")

            val details = scraper.fetchMangaDetails("claymore")
            assertEquals("Claymore", details.title)
            assertTrue(details.authors.contains("Norihiro Yagi"))

            val chapters = scraper.fetchChapterLinks("claymore")
            assertTrue(chapters.size >= 27, "Expected at least 27 Claymore volumes/chapters")
            assertTrue(chapters.first().downloadUrl.contains("mediafire.com"))

            val metadata = scraper.getMetadata("claymore", "1", null)
            assertNotNull(metadata)
            assertEquals("pt-br", metadata.languageIso)
        }
    }
}
