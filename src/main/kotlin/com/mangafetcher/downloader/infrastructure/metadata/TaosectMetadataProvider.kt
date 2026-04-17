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
    sharedPlaywrightClient: PlaywrightClient? = null,
) : MangaMetadataProvider,
    AutoCloseable {
    private val client: PlaywrightClient = sharedPlaywrightClient ?: PlaywrightClient()
    private val isClientOwned = sharedPlaywrightClient == null
    private val htmlParser = TaosectHtmlParser()
    private val imageDownloader = TaosectImageDownloader(client, htmlParser)
    private val scraper = TaosectScraper(baseUrl, client, htmlParser, imageDownloader)

    override fun getMetadata(
        title: String,
        chapter: String?,
        volume: String?,
    ): MangaMetadata? = scraper.getMetadata(title, chapter, volume)

    override fun close() {
        // Only close the client if we own it (not shared)
        if (isClientOwned) {
            scraper.close()
        }
    }
}
