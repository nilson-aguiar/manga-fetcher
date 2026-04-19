package com.mangafetcher.downloader.application

import com.mangafetcher.downloader.domain.model.DownloadRequest
import com.mangafetcher.downloader.domain.model.DownloadResult
import com.mangafetcher.downloader.domain.model.MangaMetadata
import com.mangafetcher.downloader.domain.model.MangaMetadataProvider
import com.mangafetcher.downloader.domain.port.DownloadTrackerPort
import com.mangafetcher.downloader.domain.port.FileConverterPort
import com.mangafetcher.downloader.domain.port.MangaDownloadProvider
import com.mangafetcher.downloader.domain.service.ChapterNamingUtils
import com.mangafetcher.downloader.infrastructure.conversion.CbzConverter
import com.mangafetcher.downloader.infrastructure.conversion.ComicInfoGenerator
import com.mangafetcher.downloader.infrastructure.download.CompositeDownloadProvider
import com.mangafetcher.downloader.infrastructure.download.MangaLivreDownloadProvider
import com.mangafetcher.downloader.infrastructure.download.TaosectDownloadProvider
import com.mangafetcher.downloader.infrastructure.metadata.CompositeMetadataProvider
import com.mangafetcher.downloader.infrastructure.metadata.MangaDexMetadataProvider
import com.mangafetcher.downloader.infrastructure.metadata.MangaLivreMetadataProvider
import com.mangafetcher.downloader.infrastructure.metadata.TaosectMetadataProvider
import com.mangafetcher.downloader.infrastructure.persistence.SqliteDownloadRepository
import com.mangafetcher.downloader.infrastructure.scraper.MangaDetails
import com.mangafetcher.downloader.infrastructure.scraper.MangaLivreScraper
import com.mangafetcher.downloader.infrastructure.scraper.PlaywrightClient
import org.slf4j.LoggerFactory
import java.io.File

class MangaDownloadService(
    // Share a single PlaywrightClient across all components to prevent browser crashes
    private val sharedPlaywrightClient: PlaywrightClient = PlaywrightClient(),
    private val downloadProvider: MangaDownloadProvider =
        CompositeDownloadProvider(
            listOf(
                MangaLivreDownloadProvider(playwrightClient = sharedPlaywrightClient),
                TaosectDownloadProvider(sharedPlaywrightClient = sharedPlaywrightClient),
            ),
        ),
    private val converter: FileConverterPort = CbzConverter(),
    private val metadataProvider: MangaMetadataProvider =
        CompositeMetadataProvider(
            listOf(
                MangaDexMetadataProvider(),
                MangaLivreMetadataProvider(MangaLivreScraper(playwrightClient = sharedPlaywrightClient)),
                TaosectMetadataProvider(sharedPlaywrightClient = sharedPlaywrightClient),
            ),
        ),
) : AutoCloseable {
    private val logger = LoggerFactory.getLogger(MangaDownloadService::class.java)

    fun downloadManga(request: DownloadRequest): DownloadResult {
        request.outputDir.mkdirs()

        val dbFile = File(request.outputDir, "download.db")
        val dbTracker: DownloadTrackerPort = SqliteDownloadRepository(dbFile)

        val tempDir =
            java.nio.file.Files
                .createTempDirectory("manga-fetcher-download")
                .toFile()

        var successCount = 0
        var skippedCount = 0
        var failedCount = 0

        try {
            val details = downloadProvider.fetchMangaDetails(request.mangaId)
            saveMangaInfo(details, request.outputDir)
            downloadCover(details.coverUrl, request.outputDir)

            val allChapters = downloadProvider.fetchChapters(request.mangaId)
            if (allChapters.isEmpty()) {
                logger.warn("No chapters found for manga {}", request.mangaId)
                return DownloadResult(0, 0, 0)
            }

            val chaptersToDownload =
                filterChapters(
                    allChapters,
                    request.chapterNumber,
                    request.fromChapter,
                )

            if (chaptersToDownload.isEmpty()) {
                val requested = request.chapterNumber ?: request.fromChapter ?: "unknown"
                logger.warn("No chapters found matching the criteria")
                println("\n❌ Chapter '$requested' not found!")
                println("📚 Total chapters available: ${allChapters.size}")

                // Show some available options
                if (allChapters.isNotEmpty()) {
                    val samplesToShow = 10
                    println("\n📖 Available chapters:")

                    // If we have many chapters, show first 5 and last 5
                    val samples =
                        if (allChapters.size > samplesToShow) {
                            allChapters.take(5) + allChapters.takeLast(5)
                        } else {
                            allChapters
                        }

                    samples.forEach { chapter ->
                        val volumeInfo = chapter.volume?.let { " (Vol. $it)" } ?: ""
                        println("  • Chapter ${chapter.number}$volumeInfo")
                    }

                    if (allChapters.size > samplesToShow) {
                        println("  ... and ${allChapters.size - samplesToShow} more")
                    }

                    // Show closest matches if user specified a chapter number
                    request.fromChapter?.let { fromChapter ->
                        val requestedNum = extractNumber(fromChapter)
                        val closest =
                            allChapters
                                .map { it to kotlin.math.abs(extractNumber(it.number) - requestedNum) }
                                .sortedBy { it.second }
                                .take(3)

                        if (closest.isNotEmpty() && closest[0].second > 0) {
                            println("\n💡 Closest matches:")
                            closest.forEach { (chapter, _) ->
                                val volumeInfo = chapter.volume?.let { " (Vol. $it)" } ?: ""
                                println("  • Chapter ${chapter.number}$volumeInfo")
                            }
                        }
                    }
                }

                return DownloadResult(0, 0, 0)
            }

            logger.info("Found {} chapters to process", chaptersToDownload.size)

            dbTracker.use { tracker ->
                for (chapter in chaptersToDownload) {
                    val cId = chapter.id
                    val cNum = chapter.number
                    val volume = chapter.volume

                    // Standardize existing file naming
                    if (ChapterNamingUtils.ensureCorrectNaming(
                            request.outputDir,
                            request.mangaId,
                            cId,
                            cNum,
                            volume,
                            withVolume = request.withVolume,
                        )
                    ) {
                        logger.debug("Standardized filename for chapter {}", cNum)
                    }

                    // Check if chapter is already downloaded
                    val existingFile =
                        ChapterNamingUtils.findExistingFile(
                            request.outputDir,
                            cNum,
                            request.mangaId,
                            cId,
                        )
                    if (existingFile != null || tracker.isDownloaded(request.mangaId, cId)) {
                        logger.info("Skipping chapter {} (already exists or in database)", cNum)
                        skippedCount++
                        continue
                    }

                    logger.info("Downloading images for {} chapter {}", request.mangaId, cNum)
                    val images = downloadProvider.downloadChapterImages(request.mangaId, cId, tempDir)
                    if (images.isEmpty()) {
                        logger.error("No images found or failed to download chapter {}", cNum)
                        failedCount++
                        continue
                    }

                    // Metadata fetching and generation - use actual manga title for better matching
                    val metadata = getMetadata(request.mangaId, cNum, volume)
                    val metadataXml =
                        metadata?.let {
                            ComicInfoGenerator.generate(it.copy(pageCount = images.size))
                        }

                    val finalName = ChapterNamingUtils.getFileName(cNum, volume, request.withVolume)
                    val outputFile = File(request.outputDir, finalName)

                    logger.info("Converting to {}", outputFile.absolutePath)
                    converter.convertToCbz(images, outputFile, metadataXml)
                    tracker.markDownloaded(request.mangaId, cId, cNum)
                    logger.info("Successfully downloaded and converted to {}", outputFile.name)
                    successCount++

                    tempDir.listFiles()?.forEach { it.delete() }
                }
            }
        } finally {
            tempDir.deleteRecursively()
        }

        return DownloadResult(successCount, skippedCount, failedCount)
    }

    private fun filterChapters(
        allChapters: List<com.mangafetcher.downloader.infrastructure.scraper.ChapterResult>,
        chapterNumber: String?,
        fromChapter: String?,
    ): List<com.mangafetcher.downloader.infrastructure.scraper.ChapterResult> =
        if (chapterNumber != null) {
            val targetNum = extractNumber(chapterNumber)
            allChapters.filter { extractNumber(it.number) == targetNum }
        } else if (fromChapter != null) {
            val fromNum = extractNumber(fromChapter)
            allChapters.filter { extractNumber(it.number) >= fromNum }
        } else {
            emptyList()
        }

    private fun getMetadata(
        mangaId: String,
        chapter: String,
        volume: String?,
    ): MangaMetadata? =
        try {
            metadataProvider.getMetadata(mangaId, chapter, volume)
        } catch (e: Exception) {
            null
        }

    private fun saveMangaInfo(
        details: MangaDetails,
        outputDir: File,
    ) {
        val infoFile = File(outputDir, "manga_info.csv")
        if (infoFile.exists()) return

        val header = "title,authors,artists,description,tags"
        val row =
            listOf(
                details.title,
                details.authors,
                details.artists,
                details.description,
                details.tags,
            ).joinToString(",") { escapeCsv(it) }

        infoFile.writeText("$header\n$row")
        logger.info("Saved manga metadata to manga_info.csv")
    }

    private fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            "\"$escaped\""
        } else {
            escaped
        }
    }

    private fun downloadCover(
        coverUrl: String,
        outputDir: File,
    ) {
        val coverFile = File(outputDir, "cover.jpg")
        if (coverFile.exists() || coverUrl.isEmpty()) return

        logger.info("Downloading cover image...")
        try {
            downloadProvider.downloadFile(coverUrl, coverFile)
            logger.info("Saved cover image to cover.jpg")
        } catch (e: Exception) {
            logger.warn("Failed to download cover image: {}", e.message)
        }
    }

    private fun extractNumber(s: String): Double {
        val match = """(\d+(\.\d+)?)""".toRegex().find(s)
        return match?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
    }

    override fun close() {
        try {
            downloadProvider.close()
        } catch (e: Exception) {
            logger.warn("Error closing download provider: {}", e.message)
        }
        try {
            (metadataProvider as? AutoCloseable)?.close()
        } catch (e: Exception) {
            logger.warn("Error closing metadata provider: {}", e.message)
        }
        try {
            sharedPlaywrightClient.close()
        } catch (e: Exception) {
            logger.warn("Error closing shared Playwright client: {}", e.message)
        }
    }
}
