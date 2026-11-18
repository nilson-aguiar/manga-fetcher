package com.mangafetcher.downloader.infrastructure.download

import com.mangafetcher.downloader.domain.port.ExternalArchiveDownloaderPort
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Composite implementation of [ExternalArchiveDownloaderPort] that delegates to the first
 * registered downloader supporting the target URL (e.g., MediaFire now, Google Drive / Mega in the future).
 */
class CompositeExternalArchiveDownloader(
    private val downloaders: List<ExternalArchiveDownloaderPort>,
) : ExternalArchiveDownloaderPort {
    private val logger = LoggerFactory.getLogger(CompositeExternalArchiveDownloader::class.java)

    override fun supports(url: String): Boolean = downloaders.any { it.supports(url) }

    override fun downloadAndExtractImages(
        url: String,
        outputDir: File,
    ): List<File> {
        val handler = downloaders.firstOrNull { it.supports(url) }
        if (handler == null) {
            logger.warn("No external archive downloader supports URL: {}", url)
            return emptyList()
        }
        return handler.downloadAndExtractImages(url, outputDir)
    }
}
