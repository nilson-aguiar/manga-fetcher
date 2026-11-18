package com.mangafetcher.downloader.infrastructure.metadata

import com.mangafetcher.downloader.domain.model.MangaMetadata
import com.mangafetcher.downloader.domain.model.MangaMetadataProvider
import com.mangafetcher.downloader.infrastructure.http.ResilientHttpClient
import com.mangafetcher.downloader.infrastructure.scraper.PlaywrightClient
import com.mangafetcher.downloader.infrastructure.scraper.TavernaHtmlParser
import com.mangafetcher.downloader.infrastructure.scraper.TavernaScraper

/**
 * Taverna de Quadrinhos metadata provider.
 * Fetches Portuguese (`pt-br`) comic/manga metadata from tavernadequadrinhos.com.br.
 */
class TavernaMetadataProvider(
    baseUrl: String = "https://tavernadequadrinhos.com.br",
    httpClient: ResilientHttpClient =
        ResilientHttpClient(defaultUserAgent = ResilientHttpClient.BROWSER_USER_AGENT),
    htmlParser: TavernaHtmlParser = TavernaHtmlParser(),
    sharedPlaywrightClient: PlaywrightClient? = null,
    enablePlaywrightFallback: Boolean = true,
    private val scraper: TavernaScraper =
        TavernaScraper(
            baseUrl = baseUrl,
            httpClient = httpClient,
            htmlParser = htmlParser,
            sharedPlaywrightClient = sharedPlaywrightClient,
            enablePlaywrightFallback = enablePlaywrightFallback,
        ),
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
