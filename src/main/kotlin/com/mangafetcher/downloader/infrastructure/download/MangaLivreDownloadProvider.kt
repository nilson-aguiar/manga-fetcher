package com.mangafetcher.downloader.infrastructure.download

import com.mangafetcher.downloader.domain.port.MangaDownloadProvider
import com.mangafetcher.downloader.infrastructure.scraper.ChapterResult
import com.mangafetcher.downloader.infrastructure.scraper.HtmlParser
import com.mangafetcher.downloader.infrastructure.scraper.ImageDownloader
import com.mangafetcher.downloader.infrastructure.scraper.MangaDetails
import com.mangafetcher.downloader.infrastructure.scraper.MangaLivreScraper
import com.mangafetcher.downloader.infrastructure.scraper.PlaywrightClient
import java.io.File

/**
 * MangaLivre implementation of the download provider.
 * Adapts MangaLivre-specific scraping logic to the provider interface.
 */
class MangaLivreDownloadProvider(
    private val baseUrl: String = "https://mangalivre.to",
    playwrightClient: PlaywrightClient? = null,
    htmlParser: HtmlParser = HtmlParser(),
) : MangaDownloadProvider {
    private val ownClient = playwrightClient == null
    private val playwrightClient: PlaywrightClient = playwrightClient ?: PlaywrightClient()
    private val scraper: MangaLivreScraper = MangaLivreScraper(baseUrl, this.playwrightClient, htmlParser)
    private val imageDownloader: ImageDownloader = ImageDownloader(this.playwrightClient, htmlParser)

    override fun fetchMangaDetails(mangaId: String): MangaDetails = scraper.fetchMangaDetails(mangaId)

    override fun fetchChapters(mangaId: String): List<ChapterResult> = scraper.fetchChapters(mangaId)

    override fun downloadChapterImages(
        mangaId: String,
        chapterId: String,
        outputDir: File,
    ): List<File> = imageDownloader.downloadChapterImages(baseUrl, mangaId, chapterId, outputDir)

    override fun downloadFile(
        url: String,
        outputFile: File,
    ) {
        imageDownloader.downloadFile(url, outputFile)
    }

    override fun close() {
        if (ownClient) {
            playwrightClient.close()
        }
    }
}
