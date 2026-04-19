package com.mangafetcher.downloader.infrastructure.scraper

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

@Tag("integration")
class MangaLivreScraperIT {
    companion object {
        private lateinit var sharedClient: PlaywrightClient
        private lateinit var scraper: MangaLivreScraper

        @BeforeAll
        @JvmStatic
        fun setup() {
            sharedClient = PlaywrightClient()
            scraper = MangaLivreScraper(playwrightClient = sharedClient)
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            scraper.close()
            sharedClient.close()
        }
    }

    @Test
    fun `should search for solo leveling on real site`() {
        val results = scraper.search("solo leveling")

        assertTrue(results.isNotEmpty(), "Should find at least one result for 'solo leveling'")
        val soloLeveling =
            results.find { it.title.contains("Solo Leveling", ignoreCase = true) }
                ?: throw AssertionError("Could not find 'Solo Leveling' in results: ${results.map { it.title }}")

        val chapters = scraper.fetchChapters(soloLeveling.id)
        assertTrue(chapters.isNotEmpty(), "Should find at least one chapter for 'solo leveling'")

        val tempDir =
            java.nio.file.Files
                .createTempDirectory("scraper-it")
                .toFile()
        try {
            val firstChapter = chapters.last() // Usually last is the first chapter in Madara
            val images = scraper.downloadImages(soloLeveling.id, firstChapter.id, tempDir)
            assertTrue(images.isNotEmpty(), "Should download at least one image for first chapter")
            assertTrue(images[0].exists(), "Image file should exist")
            assertTrue(images[0].length() > 0, "Image file should not be empty")
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
