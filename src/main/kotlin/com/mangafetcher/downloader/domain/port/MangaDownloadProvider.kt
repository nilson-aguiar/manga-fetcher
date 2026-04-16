package com.mangafetcher.downloader.domain.port

import com.mangafetcher.downloader.infrastructure.scraper.ChapterResult
import com.mangafetcher.downloader.infrastructure.scraper.MangaDetails
import java.io.File

/**
 * Port interface for manga download operations.
 * Implementations provide source-specific download capabilities.
 */
interface MangaDownloadProvider : AutoCloseable {
    /**
     * Fetches detailed information about a manga.
     */
    fun fetchMangaDetails(mangaId: String): MangaDetails

    /**
     * Fetches the list of chapters for a manga.
     */
    fun fetchChapters(mangaId: String): List<ChapterResult>

    /**
     * Downloads all images for a specific chapter.
     * @return List of downloaded image files
     */
    fun downloadChapterImages(
        mangaId: String,
        chapterId: String,
        outputDir: File,
    ): List<File>

    /**
     * Downloads a file from a URL.
     */
    fun downloadFile(
        url: String,
        outputFile: File,
    )
}
