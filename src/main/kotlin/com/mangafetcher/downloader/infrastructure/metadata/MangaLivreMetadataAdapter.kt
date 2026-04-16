package com.mangafetcher.downloader.infrastructure.metadata

import com.mangafetcher.downloader.domain.model.MangaMetadata
import com.mangafetcher.downloader.domain.model.MangaMetadataProvider
import com.mangafetcher.downloader.infrastructure.scraper.MangaLivreScraper

/**
 * Metadata provider adapter for MangaLivre.
 * Delegates to MangaLivreScraper for metadata fetching.
 */
class MangaLivreMetadataAdapter(
    private val scraper: MangaLivreScraper = MangaLivreScraper(),
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
