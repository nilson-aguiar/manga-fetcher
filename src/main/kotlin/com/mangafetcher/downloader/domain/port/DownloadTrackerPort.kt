package com.mangafetcher.downloader.domain.port

/**
 * Data model for a downloaded chapter entry.
 */
data class DownloadEntry(
    val mangaId: String,
    val chapterId: String,
    val chapterNumber: String,
)

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

    /**
     * Retrieves all recorded downloads.
     */
    fun getAllDownloads(): List<DownloadEntry>

    /**
     * Removes a download record.
     */
    fun removeDownload(
        mangaId: String,
        chapterId: String,
    )
}
