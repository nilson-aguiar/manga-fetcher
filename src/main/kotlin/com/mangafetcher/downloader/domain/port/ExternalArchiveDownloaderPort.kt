package com.mangafetcher.downloader.domain.port

import java.io.File

/**
 * Port interface for downloading and extracting comic/manga page images
 * from external file-hosting platforms (e.g., MediaFire, Google Drive, Mega).
 */
interface ExternalArchiveDownloaderPort {
    /**
     * Checks whether this downloader can handle the given external URL.
     */
    fun supports(url: String): Boolean

    /**
     * Resolves, downloads, and extracts page images from the given external URL
     * into [outputDir], returning the extracted image files sorted in reading order.
     */
    fun downloadAndExtractImages(
        url: String,
        outputDir: File,
    ): List<File>
}
