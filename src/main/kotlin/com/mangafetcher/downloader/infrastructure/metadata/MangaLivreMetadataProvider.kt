package com.mangafetcher.downloader.infrastructure.metadata

import com.mangafetcher.downloader.domain.model.MangaMetadata
import com.mangafetcher.downloader.domain.model.MangaMetadataProvider
import com.mangafetcher.downloader.infrastructure.scraper.MangaLivreScraper

/**
 * Metadata provider for MangaLivre.
 * Delegates to MangaLivreScraper for metadata fetching.
 */
class MangaLivreMetadataProvider(
    scraper: MangaLivreScraper? = null,
) : MangaMetadataProvider,
    AutoCloseable {
    private val ownScraper = scraper == null
    private val scraper: MangaLivreScraper = scraper ?: MangaLivreScraper()

    override fun getMetadata(
        title: String,
        chapter: String?,
        volume: String?,
    ): MangaMetadata? = scraper.getMetadata(title, chapter, volume)

    override fun close() {
        if (ownScraper) {
            scraper.close()
        }
    }
}
