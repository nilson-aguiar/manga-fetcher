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
import java.io.File

class MangaDownloadService(
    private val downloadProvider: MangaDownloadProvider =
        CompositeDownloadProvider(
            listOf(
                MangaLivreDownloadProvider(),
                TaosectDownloadProvider(),
            ),
        ),
    private val converter: FileConverterPort = CbzConverter(),
    private val metadataProvider: MangaMetadataProvider =
        CompositeMetadataProvider(
            listOf(
                MangaDexMetadataProvider(),
                MangaLivreMetadataProvider(),
                TaosectMetadataProvider(),
            ),
        ),
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
            downloadProvider.use { provider ->
                val details = provider.fetchMangaDetails(request.mangaId)
                saveMangaInfo(details, request.outputDir)
                downloadCover(details.coverUrl, request.outputDir)

                val allChapters = provider.fetchChapters(request.mangaId)
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
                        val images = provider.downloadChapterImages(request.mangaId, cId, tempDir)
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
            downloadProvider.downloadFile(coverUrl, coverFile)
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
