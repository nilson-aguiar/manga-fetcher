package com.mangafetcher.downloader.infrastructure.scraper

import com.mangafetcher.downloader.domain.model.MangaMetadata
import com.mangafetcher.downloader.domain.model.MangaMetadataProvider
import com.mangafetcher.downloader.domain.port.MangaScraperPort
import java.io.File

data class MangaResult(
    val title: String,
    val id: String,
)

data class ChapterResult(
    val number: String,
    val id: String,
    val volume: String? = null,
)

data class MangaDetails(
    val title: String,
    val authors: String,
    val artists: String,
    val description: String,
    val tags: String,
    val coverUrl: String,
)

/**
 * MangaLivre web scraper - thin coordinator that delegates to focused components.
 * Uses PlaywrightClient for browser operations, HtmlParser for parsing, and ImageDownloader for downloads.
 */
class MangaLivreScraper(
    private val baseUrl: String = "https://mangalivre.to",
    private val playwrightClient: PlaywrightClient = PlaywrightClient(),
    private val htmlParser: HtmlParser = HtmlParser(),
    private val imageDownloader: ImageDownloader = ImageDownloader(playwrightClient, htmlParser),
) : MangaScraperPort,
    MangaMetadataProvider,
    AutoCloseable {
    override fun getMetadata(
        title: String,
        chapter: String?,
        volume: String?,
    ): MangaMetadata? = fetchMangaMetadata(title)?.copy(number = chapter, volume = volume)

    override fun search(title: String): List<MangaResult> {
        val html = playwrightClient.fetchPage("$baseUrl/?s=$title&post_type=wp-manga")
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

    // Keep these methods for backward compatibility with tests
    @Deprecated("Use HtmlParser directly for testing", ReplaceWith("htmlParser.parseSearchResults(html)"))
    fun parseSearchResults(html: String): List<MangaResult> = htmlParser.parseSearchResults(html)

    @Deprecated("Use HtmlParser directly for testing", ReplaceWith("htmlParser.parseChapters(html)"))
    fun parseChapters(html: String): List<ChapterResult> = htmlParser.parseChapters(html)

    override fun close() {
        playwrightClient.close()
    }
}
