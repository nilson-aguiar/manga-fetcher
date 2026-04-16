package com.mangafetcher.downloader.infrastructure.metadata

import com.mangafetcher.downloader.domain.model.MangaMetadata
import com.mangafetcher.downloader.domain.model.MangaMetadataProvider
import com.mangafetcher.downloader.infrastructure.scraper.PlaywrightClient
import com.mangafetcher.downloader.infrastructure.scraper.TaosectHtmlParser
import com.mangafetcher.downloader.infrastructure.scraper.TaosectImageDownloader
import com.mangafetcher.downloader.infrastructure.scraper.TaosectScraper

/**
 * Taosect-specific metadata provider.
 * Fetches manga metadata from Taosect manga site.
 */
class TaosectMetadataProvider(
    private val baseUrl: String = "https://taosect.com",
    private val scraper: TaosectScraper =
        run {
            val client = PlaywrightClient()
            TaosectScraper(baseUrl, client, TaosectHtmlParser(), TaosectImageDownloader(client, TaosectHtmlParser()))
        },
) : MangaMetadataProvider,
    AutoCloseable {
    override fun getMetadata(
        title: String,
        chapter: String?,
        volume: String?,
    ): MangaMetadata? = scraper.getMetadata(title, chapter, volume)

    override fun close() {
        scraper.close()
    }
}
