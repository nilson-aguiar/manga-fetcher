package com.mangafetcher.downloader.infrastructure.scraper

import com.mangafetcher.downloader.domain.model.MangaMetadata
import com.mangafetcher.downloader.domain.model.MangaMetadataProvider
import com.mangafetcher.downloader.domain.port.MangaScraperPort
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
    override fun getMetadata(
        title: String,
        chapter: String?,
        volume: String?,
    ): MangaMetadata? = fetchMangaMetadata(title)?.copy(number = chapter, volume = volume)

    override fun search(title: String): List<MangaResult> {
        val html = playwrightClient.fetchPage("$baseUrl/?s=$title")
        return htmlParser.parseSearchResults(html)
    }

    override fun fetchChapters(mangaId: String): List<ChapterResult> {
        val html = playwrightClient.fetchPage("$baseUrl/manga/$mangaId/")
        return htmlParser.parseChapters(html)
    }

    fun fetchMangaMetadata(mangaId: String): MangaMetadata? {
        val html = playwrightClient.fetchPage("$baseUrl/manga/$mangaId/")
        return htmlParser.parseMangaMetadata(html, baseUrl, mangaId)
    }

    override fun fetchMangaDetails(mangaId: String): MangaDetails {
        val html = playwrightClient.fetchPage("$baseUrl/manga/$mangaId/")
        return htmlParser.parseMangaDetails(html)
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
