package com.mangafetcher.downloader.application

import com.mangafetcher.downloader.domain.port.MangaScraperPort
import com.mangafetcher.downloader.domain.service.ChapterNamingUtils
import com.mangafetcher.downloader.infrastructure.scraper.MangaLivreScraper
import org.slf4j.LoggerFactory
import java.io.File

class ChapterRenameService(
    private val scraper: MangaScraperPort = MangaLivreScraper(),
) {
    private val logger = LoggerFactory.getLogger(ChapterRenameService::class.java)

    fun renameChapters(
        mangaId: String,
        outputDir: File,
    ): Int {
        if (!outputDir.exists()) {
            throw IllegalArgumentException("Directory ${outputDir.path} does not exist.")
        }

        return scraper.use { s ->
            val allChapters = s.fetchChapters(mangaId)
            if (allChapters.isEmpty()) {
                logger.warn("No chapters found for manga {}", mangaId)
                return@use 0
            }

            var renamedCount = 0
            for (chapter in allChapters) {
                if (chapter.volume != null) {
                    if (ChapterNamingUtils.ensureCorrectNaming(
                            outputDir,
                            mangaId,
                            chapter.id,
                            chapter.number,
                            chapter.volume,
                            withVolume = true,
                        )
                    ) {
                        renamedCount++
                    }
                }
            }
            renamedCount
        }
    }
}
