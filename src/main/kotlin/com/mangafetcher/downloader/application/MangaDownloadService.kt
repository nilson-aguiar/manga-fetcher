package com.mangafetcher.downloader.application

import com.mangafetcher.downloader.domain.model.DownloadRequest
import com.mangafetcher.downloader.domain.model.DownloadResult
import com.mangafetcher.downloader.domain.model.MangaMetadata
import com.mangafetcher.downloader.domain.model.MangaMetadataProvider
import com.mangafetcher.downloader.domain.port.DownloadTrackerPort
import com.mangafetcher.downloader.domain.port.FileConverterPort
import com.mangafetcher.downloader.domain.port.ImageDownloaderPort
import com.mangafetcher.downloader.domain.port.MangaScraperPort
import com.mangafetcher.downloader.domain.service.ChapterNamingUtils
import com.mangafetcher.downloader.infrastructure.conversion.CbzConverter
import com.mangafetcher.downloader.infrastructure.conversion.ComicInfoGenerator
import com.mangafetcher.downloader.infrastructure.metadata.MangaDexMetadataProvider
import com.mangafetcher.downloader.infrastructure.persistence.SqliteDownloadRepository
import com.mangafetcher.downloader.infrastructure.scraper.HtmlParser
import com.mangafetcher.downloader.infrastructure.scraper.ImageDownloader
import com.mangafetcher.downloader.infrastructure.scraper.MangaDetails
import com.mangafetcher.downloader.infrastructure.scraper.MangaLivreScraper
import com.mangafetcher.downloader.infrastructure.scraper.PlaywrightClient
import java.io.File

class MangaDownloadService(
    private val scraper: MangaScraperPort = MangaLivreScraper(),
    private val converter: FileConverterPort = CbzConverter(),
    private val metadataFallback: MangaMetadataProvider = MangaDexMetadataProvider(),
    private val imageDownloader: ImageDownloaderPort =
        run {
            val client = PlaywrightClient()
            ImageDownloader(client, HtmlParser())
        },
) {
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
            scraper.use { s ->
                val details = s.fetchMangaDetails(request.mangaId)
                saveMangaInfo(details, request.outputDir)
                downloadCover(details.coverUrl, request.outputDir)

                val allChapters = s.fetchChapters(request.mangaId)
                if (allChapters.isEmpty()) {
                    println("No chapters found for manga ${request.mangaId}.")
                    return DownloadResult(0, 0, 0)
                }

                val chaptersToDownload =
                    filterChapters(
                        allChapters,
                        request.chapterNumber,
                        request.fromChapter,
                    )

                if (chaptersToDownload.isEmpty()) {
                    println("No chapters found matching the criteria.")
                    return DownloadResult(0, 0, 0)
                }

                println("Found ${chaptersToDownload.size} chapters to process.")

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
                            println("Standardized filename for chapter $cNum")
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
                            println("Skipping chapter $cNum (already exists or in database).")
                            skippedCount++
                            continue
                        }

                        println("Downloading images for ${request.mangaId} chapter $cNum...")
                        val images = imageDownloader.downloadChapterImages("https://mangalivre.to", request.mangaId, cId, tempDir)
                        if (images.isEmpty()) {
                            println("Error: No images found or failed to download chapter $cNum.")
                            failedCount++
                            continue
                        }

                        // Metadata fetching and generation
                        val metadata = getMetadata(request.mangaId, cNum, volume)
                        val metadataXml =
                            metadata?.let {
                                ComicInfoGenerator.generate(it.copy(pageCount = images.size))
                            }

                        val finalName = ChapterNamingUtils.getFileName(cNum, volume, request.withVolume)
                        val outputFile = File(request.outputDir, finalName)

                        println("Converting to ${outputFile.absolutePath}...")
                        converter.convertToCbz(images, outputFile, metadataXml)
                        tracker.markDownloaded(request.mangaId, cId, cNum)
                        println("Successfully downloaded and converted to ${outputFile.name}")
                        successCount++

                        tempDir.listFiles()?.forEach { it.delete() }
                    }
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
    ): MangaMetadata? {
        val primary = (scraper as? MangaMetadataProvider)?.getMetadata(mangaId, chapter, volume)
        if (primary != null && primary.writer != null) return primary

        // Fallback to MangaDex
        return try {
            val title = primary?.series ?: mangaId
            metadataFallback.getMetadata(title, chapter, volume) ?: primary
        } catch (e: Exception) {
            primary
        }
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
        println("Saved manga metadata to manga_info.csv")
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

        println("Downloading cover image...")
        try {
            imageDownloader.downloadFile(coverUrl, coverFile)
            println("Saved cover image to cover.jpg")
        } catch (e: Exception) {
            println("Failed to download cover image: ${e.message}")
        }
    }

    private fun extractNumber(s: String): Double {
        val match = """(\d+(\.\d+)?)""".toRegex().find(s)
        return match?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
    }
}
