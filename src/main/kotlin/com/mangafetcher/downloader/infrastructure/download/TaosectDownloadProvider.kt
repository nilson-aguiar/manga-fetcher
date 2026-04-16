package com.mangafetcher.downloader.infrastructure.download

import com.mangafetcher.downloader.domain.port.MangaDownloadProvider
import com.mangafetcher.downloader.infrastructure.scraper.ChapterResult
import com.mangafetcher.downloader.infrastructure.scraper.MangaDetails
import com.mangafetcher.downloader.infrastructure.scraper.PlaywrightClient
import com.mangafetcher.downloader.infrastructure.scraper.TaosectHtmlParser
import com.mangafetcher.downloader.infrastructure.scraper.TaosectImageDownloader
import com.mangafetcher.downloader.infrastructure.scraper.TaosectScraper
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
        scraper.close()
    }
}
