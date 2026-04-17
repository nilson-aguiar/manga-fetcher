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
 *
 * URL Structure:
 * - Manga page: https://taosect.com/manga/{manga-id}/
 * - Chapter reader: https://taosect.com/leitor-online/projeto/{manga-id}/{chapter-id}/
 * - Search: https://taosect.com/?s={query}
 */
class TaosectScraper(
    private val baseUrl: String = "https://taosect.com",
    private val playwrightClient: PlaywrightClient = PlaywrightClient(),
    private val htmlParser: TaosectHtmlParser = TaosectHtmlParser(),
    private val imageDownloader: TaosectImageDownloader = TaosectImageDownloader(playwrightClient, htmlParser),
) : MangaScraperPort,
    MangaMetadataProvider,
    AutoCloseable {

    private val logger = LoggerFactory.getLogger(TaosectScraper::class.java)

    // Cache HTML for manga pages to avoid fetching twice
    private val htmlCache = mutableMapOf<String, String>()
    override fun getMetadata(
        title: String,
        chapter: String?,
        volume: String?,
    ): MangaMetadata? = fetchMangaMetadata(title)?.copy(number = chapter, volume = volume)

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

        val html = if (htmlCache.containsKey(url)) {
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
        val chapters = try {
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
     * Handles formats like: "01", "8.5", "98v2", "100.1v2", "107 e 108", "201V3"
     */
    private fun parseChapterNumber(chapterNumber: String): Double {
        // Extract the numeric part before any version suffix (v2, v3, etc.) or text
        val numericPart = chapterNumber
            .replace(Regex("[vV]\\d+"), "") // Remove version suffixes like v2, V3
            .replace(Regex("\\s+e\\s+.*"), "") // Remove " e ..." parts
            .trim()

        return numericPart.toDoubleOrNull() ?: 0.0
    }

    fun fetchMangaMetadata(mangaId: String): MangaMetadata? {
        logger.info("Fetching metadata for: {}", mangaId)
        val url = "$baseUrl/manga/$mangaId/"

        val html = if (htmlCache.containsKey(url)) {
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

        val html = if (htmlCache.containsKey(url)) {
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
        playwrightClient.close()
    }
}
