package com.mangafetcher.downloader.infrastructure.scraper

import com.mangafetcher.downloader.domain.port.ImageDownloaderPort
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
        val html = playwrightClient.fetchPage(url)
        val imageUrls = htmlParser.extractImageUrls(html)

        outputDir.mkdirs()

        return imageUrls.mapIndexedNotNull { index, imgUrl ->
            try {
                val bytes = playwrightClient.downloadImage(imgUrl) ?: return@mapIndexedNotNull null

                // Determine file extension from URL or default to jpg
                val extension = when {
                    imgUrl.contains(".png", ignoreCase = true) -> "png"
                    imgUrl.contains(".webp", ignoreCase = true) -> "webp"
                    imgUrl.contains(".jpeg", ignoreCase = true) -> "jpeg"
                    else -> "jpg"
                }

                val file = File(outputDir, "%03d.$extension".format(index + 1))
                file.writeBytes(bytes)
                file
            } catch (e: Exception) {
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
        val bytes = playwrightClient.downloadFile(url)
        if (bytes != null) {
            outputFile.writeBytes(bytes)
        }
    }
}
