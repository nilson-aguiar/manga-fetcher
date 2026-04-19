package com.mangafetcher.downloader.infrastructure.scraper

import com.mangafetcher.downloader.domain.model.MangaMetadata
import com.mangafetcher.downloader.domain.model.MangaMetadataProvider
import com.mangafetcher.downloader.domain.port.MangaScraperPort
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Taosect web scraper - coordinator for Taosect manga site scraping.
 * Uses PlaywrightClient for browser operations, TaosectHtmlParser for parsing,
 * and ImageDownloader for downloads.
 */
class TaosectScraper(
    private val baseUrl: String = "https://taosect.com",
    playwrightClient: PlaywrightClient? = null,
    private val htmlParser: TaosectHtmlParser = TaosectHtmlParser(),
) : MangaScraperPort,
    MangaMetadataProvider,
    AutoCloseable {
    private val logger = LoggerFactory.getLogger(TaosectScraper::class.java)
    private val ownClient = playwrightClient == null
    private val playwrightClient: PlaywrightClient = playwrightClient ?: PlaywrightClient()
    private val imageDownloader: TaosectImageDownloader = TaosectImageDownloader(this.playwrightClient, htmlParser)

    // Cache HTML for manga pages to avoid fetching twice
    private val htmlCache = mutableMapOf<String, String>()

    override fun getMetadata(
        title: String,
        chapter: String?,
        volume: String?,
    ): MangaMetadata? {
        logger.info("Searching Taosect for: {}", title)
        return try {
            val metadata = fetchMangaMetadata(title)?.copy(number = chapter, volume = volume)
            if (metadata != null) {
                logger.info("Taosect: Found match for '{}'", title)
            } else {
                logger.info("Taosect: No results found for '{}'", title)
            }
            metadata
        } catch (e: Exception) {
            logger.warn("Taosect: Error fetching metadata for '{}': {}", title, e.message)
            null
        }
    }

    override fun search(title: String): List<MangaResult> {
        logger.info("Searching for: {}", title)
        val html = playwrightClient.fetchPage("$baseUrl/?s=$title")
        val results = htmlParser.parseSearchResults(html)
        logger.info("Found {} search results", results.size)
        return results
    }

    override fun fetchChapters(mangaId: String): List<ChapterResult> {
        logger.info("Fetching chapters for manga: {}", mangaId)
        val url = "$baseUrl/manga/$mangaId/"

        val html =
            if (htmlCache.containsKey(url)) {
                logger.debug("Using cached HTML for: {}", url)
                htmlCache[url]!!
            } else {
                logger.debug("Loading page: {}", url)
                val fetchedHtml = playwrightClient.fetchPage(url)
                htmlCache[url] = fetchedHtml
                fetchedHtml
            }

        logger.debug("Page HTML received, length: {} bytes", html.length)
        logger.debug("Calling HTML parser to extract chapters...")
        val chapters =
            try {
                htmlParser.parseChapters(html)
            } catch (e: Exception) {
                logger.error("Error parsing chapters: {}", e.message, e)
                throw e
            }
        logger.info("Found {} chapters available", chapters.size)

        // Sort chapters by number
        val sortedChapters = chapters.sortedWith(compareBy({ parseChapterNumber(it.number) }, { it.number }))
        logger.debug("Chapters sorted by number")
        return sortedChapters
    }

    /**
     * Parses chapter number string to Double for sorting.
     */
    private fun parseChapterNumber(chapterNumber: String): Double {
        val numericPart =
            chapterNumber
                .replace(Regex("[vV]\\d+"), "")
                .replace(Regex("\\s+e\\s+.*"), "")
                .trim()

        return numericPart.toDoubleOrNull() ?: 0.0
    }

    fun fetchMangaMetadata(mangaId: String): MangaMetadata? {
        logger.info("Fetching metadata for: {}", mangaId)
        val url = "$baseUrl/manga/$mangaId/"

        val html =
            if (htmlCache.containsKey(url)) {
                logger.debug("Using cached HTML for: {}", url)
                htmlCache[url]!!
            } else {
                logger.debug("Loading page: {}", url)
                val fetchedHtml = playwrightClient.fetchPage(url)
                htmlCache[url] = fetchedHtml
                fetchedHtml
            }

        return htmlParser.parseMangaMetadata(html, baseUrl, mangaId)
    }

    override fun fetchMangaDetails(mangaId: String): MangaDetails {
        logger.info("Fetching manga details for: {}", mangaId)
        val url = "$baseUrl/manga/$mangaId/"

        val html =
            if (htmlCache.containsKey(url)) {
                logger.debug("Using cached HTML for: {}", url)
                htmlCache[url]!!
            } else {
                logger.debug("Loading page: {}", url)
                val fetchedHtml = playwrightClient.fetchPage(url)
                htmlCache[url] = fetchedHtml
                fetchedHtml
            }

        val details = htmlParser.parseMangaDetails(html)
        logger.info("Manga title: {}", details.title)
        return details
    }

    fun downloadFile(
        url: String,
        outputFile: File,
    ) {
        imageDownloader.downloadFile(url, outputFile)
    }

    fun downloadImages(
        mangaId: String,
        chapterId: String,
        outputDir: File,
    ): List<File> = imageDownloader.downloadChapterImages(baseUrl, mangaId, chapterId, outputDir)

    override fun close() {
        if (ownClient) {
            playwrightClient.close()
        }
    }
}
