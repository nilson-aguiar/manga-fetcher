package com.mangafetcher.downloader.infrastructure.download

import com.mangafetcher.downloader.domain.port.ExternalArchiveDownloaderPort
import com.mangafetcher.downloader.infrastructure.http.ResilientHttpClient
import com.mangafetcher.downloader.infrastructure.scraper.PlaywrightClient
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Downloads and extracts comic archives hosted on MediaFire.
 *
 * Uses a list of [MediaFireLinkResolver] strategies (currently [MediaFireSingleFileResolver] for `/file/...`,
 * extensible to a future `MediaFireFolderResolver` for `/folder/...`) to resolve direct file URLs,
 * streams each file to a temporary archive on disk, and extracts ordered page images via [ArchiveExtractor].
 */
class MediaFireDownloader(
    private val httpClient: ResilientHttpClient =
        ResilientHttpClient(defaultUserAgent = ResilientHttpClient.BROWSER_USER_AGENT),
    private val htmlParser: MediaFireHtmlParser = MediaFireHtmlParser(),
    private val archiveExtractor: ArchiveExtractor = ArchiveExtractor(),
    playwrightFallback: (() -> PlaywrightClient)? = null,
    private val linkResolvers: List<MediaFireLinkResolver> =
        listOf(
            MediaFireSingleFileResolver(
                httpClient = httpClient,
                htmlParser = htmlParser,
                playwrightFallback = playwrightFallback,
            ),
        ),
) : ExternalArchiveDownloaderPort {
    private val logger = LoggerFactory.getLogger(MediaFireDownloader::class.java)

    override fun supports(url: String): Boolean = url.contains("mediafire.com", ignoreCase = true)

    override fun downloadAndExtractImages(
        url: String,
        outputDir: File,
    ): List<File> {
        outputDir.mkdirs()

        if (htmlParser.isFolderUrl(url) && linkResolvers.none { it.supports(url) }) {
            logger.warn("MediaFire folder links are not supported yet: {}", url)
            return emptyList()
        }

        val resolver = linkResolvers.firstOrNull { it.supports(url) }
        if (resolver == null) {
            logger.warn("No MediaFire link resolver supports URL: {}", url)
            return emptyList()
        }

        val resolvedFiles = resolver.resolveFiles(url)
        if (resolvedFiles.isEmpty()) {
            logger.warn("No downloadable files resolved from MediaFire URL: {}", url)
            return emptyList()
        }

        val allExtractedImages = mutableListOf<File>()

        for ((index, resolvedFile) in resolvedFiles.withIndex()) {
            val tempArchive = File(outputDir, ".temp-mediafire-archive-$index.tmp")
            try {
                logger.info("Downloading archive from MediaFire: {}", resolvedFile.fileName ?: resolvedFile.directUrl)
                val downloadInfo = httpClient.downloadToFile(resolvedFile.directUrl, tempArchive)
                val fileNameHint =
                    extractFileNameFromContentDisposition(downloadInfo.contentDisposition)
                        ?: resolvedFile.fileName
                        ?: downloadInfo.finalUrl.substringAfterLast("/")

                logger.info(
                    "Downloaded archive ({} KB), extracting images...",
                    tempArchive.length() / 1024,
                )

                val extracted =
                    archiveExtractor.extractImages(
                        archiveFile = tempArchive,
                        outputDir = outputDir,
                        fileNameHint = fileNameHint,
                        startIndex = allExtractedImages.size + 1,
                    )
                allExtractedImages.addAll(extracted)
            } catch (e: Exception) {
                logger.error("Failed to download or extract MediaFire archive {}: {}", resolvedFile.directUrl, e.message)
            } finally {
                if (tempArchive.exists()) {
                    tempArchive.delete()
                }
            }
        }

        return allExtractedImages
    }

    private fun extractFileNameFromContentDisposition(contentDisposition: String?): String? {
        if (contentDisposition.isNullOrBlank()) return null
        val match = Regex("""filename\*?=(?:UTF-8'')?"?([^";]+)"?""", RegexOption.IGNORE_CASE).find(contentDisposition)
        return match?.groupValues?.get(1)?.trim()
    }
}
