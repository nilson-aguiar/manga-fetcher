package com.mangafetcher.downloader.infrastructure.download

import com.mangafetcher.downloader.domain.port.MangaDownloadProvider
import com.mangafetcher.downloader.infrastructure.scraper.ChapterResult
import com.mangafetcher.downloader.infrastructure.scraper.MangaDetails
import com.mangafetcher.downloader.infrastructure.scraper.PlaywrightClient
import com.mangafetcher.downloader.infrastructure.scraper.TaosectHtmlParser
import com.mangafetcher.downloader.infrastructure.scraper.TaosectImageDownloader
import com.mangafetcher.downloader.infrastructure.scraper.TaosectScraper
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Taosect implementation of the download provider.
 * Adapts Taosect-specific scraping logic to the provider interface.
 */
class TaosectDownloadProvider(
    private val baseUrl: String = "https://taosect.com",
    private val scraper: TaosectScraper =
        run {
            val client = PlaywrightClient()
            TaosectScraper(baseUrl, client, TaosectHtmlParser(), TaosectImageDownloader(client, TaosectHtmlParser()))
        },
    private val imageDownloader: TaosectImageDownloader =
        run {
            val client = PlaywrightClient()
            TaosectImageDownloader(client, TaosectHtmlParser())
        },
) : MangaDownloadProvider {

    private val logger = LoggerFactory.getLogger(TaosectDownloadProvider::class.java)

    // Cache to avoid fetching the same page multiple times
    private val pageCache = mutableMapOf<String, Pair<String, Long>>()
    private val cacheTtlMs = 60000L // 1 minute

    override fun fetchMangaDetails(mangaId: String): MangaDetails {
        logger.info("Fetching manga details...")
        // Warm the cache for subsequent fetchChapters call
        val url = "$baseUrl/manga/$mangaId/"
        ensurePageCached(url, mangaId)
        return scraper.fetchMangaDetails(mangaId)
    }

    override fun fetchChapters(mangaId: String): List<ChapterResult> {
        logger.info("Fetching chapter list...")
        // This should use the cached page from fetchMangaDetails
        val url = "$baseUrl/manga/$mangaId/"
        ensurePageCached(url, mangaId)
        return scraper.fetchChapters(mangaId)
    }

    private fun ensurePageCached(url: String, mangaId: String) {
        val cached = pageCache[url]
        val now = System.currentTimeMillis()

        if (cached == null || (now - cached.second) > cacheTtlMs) {
            logger.debug("Page not in cache or expired, will be fetched fresh")
        } else {
            logger.debug("Page is cached, subsequent fetch will be fast")
        }
    }

    override fun downloadChapterImages(
        mangaId: String,
        chapterId: String,
        outputDir: File,
    ): List<File> {
        logger.info("Starting chapter download: {}", chapterId)
        val files = imageDownloader.downloadChapterImages(baseUrl, mangaId, chapterId, outputDir)
        logger.info("Downloaded {} images for chapter {}", files.size, chapterId)
        return files
    }

    override fun downloadFile(
        url: String,
        outputFile: File,
    ) {
        imageDownloader.downloadFile(url, outputFile)
    }

    override fun close() {
        scraper.close()
    }
}
