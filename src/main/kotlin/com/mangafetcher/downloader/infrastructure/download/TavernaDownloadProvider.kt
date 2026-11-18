package com.mangafetcher.downloader.infrastructure.download

import com.mangafetcher.downloader.domain.port.ExternalArchiveDownloaderPort
import com.mangafetcher.downloader.domain.port.MangaDownloadProvider
import com.mangafetcher.downloader.infrastructure.http.ResilientHttpClient
import com.mangafetcher.downloader.infrastructure.scraper.ChapterResult
import com.mangafetcher.downloader.infrastructure.scraper.MangaDetails
import com.mangafetcher.downloader.infrastructure.scraper.PlaywrightClient
import com.mangafetcher.downloader.infrastructure.scraper.TavernaHtmlParser
import com.mangafetcher.downloader.infrastructure.scraper.TavernaScraper
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Taverna de Quadrinhos implementation of [MangaDownloadProvider].
 * Scrapes comic details and chapter links via [TavernaScraper] and downloads/extracts
 * external archives (e.g., MediaFire `.cbr`/`.cbz`) via [ExternalArchiveDownloaderPort].
 */
class TavernaDownloadProvider(
    baseUrl: String = "https://tavernadequadrinhos.com.br",
    private val httpClient: ResilientHttpClient =
        ResilientHttpClient(defaultUserAgent = ResilientHttpClient.BROWSER_USER_AGENT),
    htmlParser: TavernaHtmlParser = TavernaHtmlParser(),
    private val sharedPlaywrightClient: PlaywrightClient? = null,
    enablePlaywrightFallback: Boolean = true,
    private val scraper: TavernaScraper =
        TavernaScraper(
            baseUrl = baseUrl,
            httpClient = httpClient,
            htmlParser = htmlParser,
            sharedPlaywrightClient = sharedPlaywrightClient,
            enablePlaywrightFallback = enablePlaywrightFallback,
        ),
    externalDownloader: ExternalArchiveDownloaderPort? = null,
) : MangaDownloadProvider {
    private val logger = LoggerFactory.getLogger(TavernaDownloadProvider::class.java)
    private var ownedPlaywrightClient: PlaywrightClient? = null

    private val externalArchiveDownloader: ExternalArchiveDownloaderPort =
        externalDownloader
            ?: CompositeExternalArchiveDownloader(
                listOf(
                    MediaFireDownloader(
                        httpClient = httpClient,
                        playwrightFallback =
                            if (enablePlaywrightFallback) {
                                { getOrCreatePlaywrightClient() }
                            } else {
                                null
                            },
                    ),
                ),
            )

    // Maps (normalizedMangaId, chapterId) -> external download URL (e.g. MediaFire link)
    private val chapterUrlCache = mutableMapOf<Pair<String, String>, String>()

    override fun fetchMangaDetails(mangaId: String): MangaDetails = scraper.fetchMangaDetails(mangaId)

    override fun fetchChapters(mangaId: String): List<ChapterResult> {
        val normalizedId = normalizeMangaId(mangaId)
        val links = scraper.fetchChapterLinks(normalizedId)
        links.forEach { link ->
            chapterUrlCache[normalizedId to link.id] = link.downloadUrl
        }
        return links.map { link ->
            ChapterResult(
                number = link.number,
                id = link.id,
                volume = link.volume,
            )
        }
    }

    override fun downloadChapterImages(
        mangaId: String,
        chapterId: String,
        outputDir: File,
    ): List<File> {
        val normalizedId = normalizeMangaId(mangaId)
        var downloadUrl = chapterUrlCache[normalizedId to chapterId]

        if (downloadUrl == null) {
            // Populate cache if downloadChapterImages was invoked without prior fetchChapters
            fetchChapters(normalizedId)
            downloadUrl = chapterUrlCache[normalizedId to chapterId]
        }

        if (downloadUrl == null) {
            logger.warn("Could not find download link for manga '{}' chapter '{}'", mangaId, chapterId)
            return emptyList()
        }

        logger.info("Downloading chapter {} from external host: {}", chapterId, downloadUrl)
        return externalArchiveDownloader.downloadAndExtractImages(downloadUrl, outputDir)
    }

    override fun downloadFile(
        url: String,
        outputFile: File,
    ) {
        logger.info("Downloading file: {}", outputFile.name)
        try {
            httpClient.downloadToFile(url, outputFile)
            logger.info("Downloaded {} KB", outputFile.length() / 1024)
        } catch (e: Exception) {
            logger.warn("HTTP download failed for {}, trying Playwright fallback: {}", url, e.message)
            val bytes = getOrCreatePlaywrightClient().downloadFile(url)
            if (bytes != null) {
                outputFile.parentFile?.mkdirs()
                outputFile.writeBytes(bytes)
                logger.info("Downloaded {} KB via Playwright", bytes.size / 1024)
            } else {
                logger.warn("Failed to download file: {}", url)
            }
        }
    }

    private fun getOrCreatePlaywrightClient(): PlaywrightClient {
        sharedPlaywrightClient?.let { return it }
        ownedPlaywrightClient?.let { return it }
        return PlaywrightClient().also { ownedPlaywrightClient = it }
    }

    private fun normalizeMangaId(raw: String): String =
        raw
            .trim()
            .trim('/')
            .substringAfterLast("/")
            .lowercase()
            .replace(Regex("""\s+"""), "-")

    override fun close() {
        try {
            scraper.close()
        } catch (e: Exception) {
            logger.warn("Error closing TavernaScraper: {}", e.message)
        }
        try {
            ownedPlaywrightClient?.close()
        } catch (e: Exception) {
            logger.warn("Error closing owned PlaywrightClient: {}", e.message)
        } finally {
            ownedPlaywrightClient = null
        }
    }
}
