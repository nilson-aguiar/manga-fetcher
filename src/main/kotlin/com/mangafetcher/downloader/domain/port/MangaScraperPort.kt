package com.mangafetcher.downloader.domain.port

import com.mangafetcher.downloader.infrastructure.scraper.ChapterResult
import com.mangafetcher.downloader.infrastructure.scraper.MangaDetails
import com.mangafetcher.downloader.infrastructure.scraper.MangaResult

/**
 * Port interface for manga scraping operations.
 * Implementations provide access to manga catalogs and chapter listings.
 */
interface MangaScraperPort : AutoCloseable {
    /**
     * Searches for manga by title.
     */
    fun search(title: String): List<MangaResult>

    /**
     * Fetches the list of chapters for a manga.
     */
    fun fetchChapters(mangaId: String): List<ChapterResult>

    /**
     * Fetches detailed information about a manga.
     */
    fun fetchMangaDetails(mangaId: String): MangaDetails
}
