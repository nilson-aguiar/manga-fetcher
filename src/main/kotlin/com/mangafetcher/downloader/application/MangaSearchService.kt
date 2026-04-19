package com.mangafetcher.downloader.application

import com.mangafetcher.downloader.domain.port.MangaScraperPort
import com.mangafetcher.downloader.infrastructure.scraper.MangaLivreScraper
import com.mangafetcher.downloader.infrastructure.scraper.MangaResult

class MangaSearchService(
    private val scraper: MangaScraperPort = MangaLivreScraper(),
) : AutoCloseable {
    fun search(title: String): List<MangaResult> = scraper.search(title)
    
    override fun close() {
        scraper.close()
    }
}
