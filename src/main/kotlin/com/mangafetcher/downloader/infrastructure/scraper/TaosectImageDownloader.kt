package com.mangafetcher.downloader.infrastructure.scraper

import com.mangafetcher.downloader.domain.port.ImageDownloaderPort
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Handles downloading and saving images from Taosect manga reader.
 * Taosect hosts images on Google Drive, so this downloader handles
 * the specific URL format used by their reader.
 */
class TaosectImageDownloader(
    private val playwrightClient: PlaywrightClient,
    private val htmlParser: TaosectHtmlParser,
) : ImageDownloaderPort {

    private val logger = LoggerFactory.getLogger(TaosectImageDownloader::class.java)
    /**
     * Downloads all images from a Taosect chapter page.
     * Chapter URL format: https://taosect.com/leitor-online/projeto/{manga-id}/{chapter-id}/
     * Returns a list of downloaded image files.
     */
    override fun downloadChapterImages(
        baseUrl: String,
        mangaId: String,
        chapterId: String,
        outputDir: File,
    ): List<File> {
        // Taosect uses a different URL pattern for the chapter reader
        val url = "$baseUrl/leitor-online/projeto/$mangaId/$chapterId/"
        logger.info("Opening chapter reader: {}", url)
        logger.info("Extracting image URLs...")
        val html = playwrightClient.fetchPage(url)
        val imageUrls = htmlParser.extractImageUrls(html)
        logger.info("Found {} images to download", imageUrls.size)

        outputDir.mkdirs()

        return imageUrls.mapIndexedNotNull { index, imgUrl ->
            try {
                logger.info("Downloading image {}/{}", index + 1, imageUrls.size)
                val bytes = playwrightClient.downloadImage(imgUrl)
                if (bytes == null) {
                    logger.warn("Failed to download image {}/{}", index + 1, imageUrls.size)
                    return@mapIndexedNotNull null
                }

                // Determine file extension from URL or default to jpg
                val extension = when {
                    imgUrl.contains(".png", ignoreCase = true) -> "png"
                    imgUrl.contains(".webp", ignoreCase = true) -> "webp"
                    imgUrl.contains(".jpeg", ignoreCase = true) -> "jpeg"
                    else -> "jpg"
                }

                val file = File(outputDir, "%03d.$extension".format(index + 1))
                file.writeBytes(bytes)
                logger.info("Downloaded image {}/{} ({} KB)", index + 1, imageUrls.size, bytes.size / 1024)
                file
            } catch (e: Exception) {
                logger.error("Error downloading image {}/{}: {}", index + 1, imageUrls.size, e.message)
                null
            }
        }
    }

    /**
     * Downloads a single file from a URL.
     */
    override fun downloadFile(
        url: String,
        outputFile: File,
    ) {
        logger.info("Downloading file: {}", outputFile.name)
        val bytes = playwrightClient.downloadFile(url)
        if (bytes != null) {
            outputFile.writeBytes(bytes)
            logger.info("Downloaded {} KB", bytes.size / 1024)
        } else {
            logger.warn("Failed to download file")
        }
    }
}
