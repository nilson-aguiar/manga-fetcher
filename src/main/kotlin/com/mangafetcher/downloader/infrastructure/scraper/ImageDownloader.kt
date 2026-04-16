package com.mangafetcher.downloader.infrastructure.scraper

import com.mangafetcher.downloader.domain.port.ImageDownloaderPort
import java.io.File

/**
 * Handles downloading and saving images from URLs.
 */
class ImageDownloader(
    private val playwrightClient: PlaywrightClient,
    private val htmlParser: HtmlParser,
) : ImageDownloaderPort {
    /**
     * Downloads all images from a chapter page.
     * Returns a list of downloaded image files.
     */
    override fun downloadChapterImages(
        baseUrl: String,
        mangaId: String,
        chapterId: String,
        outputDir: File,
    ): List<File> {
        val url = "$baseUrl/manga/$mangaId/$chapterId/"
        val html = playwrightClient.fetchChapterPageWithImages(url)
        val imageUrls = htmlParser.extractImageUrls(html)

        outputDir.mkdirs()

        return imageUrls.mapIndexedNotNull { index, imgUrl ->
            try {
                val bytes = playwrightClient.downloadImage(imgUrl) ?: return@mapIndexedNotNull null

                val extension = imgUrl.substringAfterLast(".", "jpg").substringBefore("?")
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
