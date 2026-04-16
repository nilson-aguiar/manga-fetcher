package com.mangafetcher.downloader.domain.port

/**
 * Port interface for tracking downloaded chapters.
 * Implementations persist download state to prevent duplicate downloads.
 */
interface DownloadTrackerPort : AutoCloseable {
    /**
     * Checks if a chapter has already been downloaded.
     */
    fun isDownloaded(
        mangaId: String,
        chapterId: String,
    ): Boolean

    /**
     * Marks a chapter as downloaded.
     */
    fun markDownloaded(
        mangaId: String,
        chapterId: String,
        chapterNumber: String,
    )
}
