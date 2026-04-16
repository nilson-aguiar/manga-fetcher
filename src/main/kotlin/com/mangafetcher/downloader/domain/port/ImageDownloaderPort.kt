package com.mangafetcher.downloader.domain.port

import java.io.File

/**
 * Port interface for downloading chapter images.
 * Implementations handle the actual download and storage of manga images.
 */
interface ImageDownloaderPort {
    /**
     * Downloads all images for a chapter and returns the list of image files.
     */
    fun downloadChapterImages(
        baseUrl: String,
        mangaId: String,
        chapterId: String,
        outputDir: File,
    ): List<File>

    /**
     * Downloads a single file (e.g., cover image).
     */
    fun downloadFile(
        url: String,
        outputFile: File,
    )
}
