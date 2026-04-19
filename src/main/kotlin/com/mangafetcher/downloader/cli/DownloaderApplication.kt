package com.mangafetcher.downloader.cli

import com.mangafetcher.downloader.application.ChapterRenameService
import com.mangafetcher.downloader.application.MangaSearchService
import com.mangafetcher.downloader.domain.model.DownloadRequest
import com.mangafetcher.downloader.domain.port.MangaDownloadProvider
import com.mangafetcher.downloader.domain.port.MangaScraperPort
import com.mangafetcher.downloader.infrastructure.download.MangaLivreDownloadProvider
import com.mangafetcher.downloader.infrastructure.download.TaosectDownloadProvider
import com.mangafetcher.downloader.infrastructure.scraper.MangaLivreScraper
import com.mangafetcher.downloader.infrastructure.scraper.PlaywrightClient
import com.mangafetcher.downloader.infrastructure.scraper.TaosectScraper
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command
import java.io.File
import java.util.concurrent.Callable
import kotlin.system.exitProcess

@Command(
    name = "manga-fetcher",
    mixinStandardHelpOptions = true,
    version = ["0.0.1-SNAPSHOT"],
    description = ["Download manga and convert to .cbz files."],
    subcommands = [
        SearchCommand::class,
        DownloadCommand::class,
        RenameCommand::class,
        CheckCommand::class,
    ],
)
class DownloaderApplication : Callable<Int> {
    @CommandLine.Option(
        names = ["--log-level"],
        description = ["Log level: trace, debug, info, warn, error (default: info)"],
        defaultValue = "info",
    )
    var logLevel: String = "info"

    override fun call(): Int {
        configureLogging(logLevel)
        return 0
    }

    companion object {
        fun configureLogging(level: String) {
            val normalizedLevel = level.lowercase()

            // Set log level
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", normalizedLevel)

            // Enable timestamp with clean format
            System.setProperty("org.slf4j.simpleLogger.showDateTime", "true")
            System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "HH:mm:ss.SSS")

            // Show level in output (e.g., [INFO], [DEBUG])
            System.setProperty("org.slf4j.simpleLogger.showLogName", "true")
            System.setProperty("org.slf4j.simpleLogger.showShortLogName", "true")

            // Don't show thread name (cleaner output)
            System.setProperty("org.slf4j.simpleLogger.showThreadName", "false")

            // Format: [HH:mm:ss.SSS] [LEVEL] ShortClassName - message
            System.setProperty("org.slf4j.simpleLogger.levelInBrackets", "true")
        }
    }
}

@Command(name = "rename", description = ["Retroactively rename files to include volume information"])
class RenameCommand : Callable<Int> {
    private val logger = LoggerFactory.getLogger(RenameCommand::class.java)

    @CommandLine.Parameters(index = "0", description = ["Manga ID"])
    lateinit var mangaId: String

    @CommandLine.Option(names = ["-o", "--output", "--output-dir"], description = ["Output directory"], defaultValue = ".")
    lateinit var output: String

    override fun call(): Int {
        val service = ChapterRenameService()

        return try {
            val outputDir = File(output)
            val renamedCount = service.renameChapters(mangaId, outputDir)
            logger.info("Renamed {} files.", renamedCount)
            0
        } catch (e: IllegalArgumentException) {
            logger.error(e.message)
            1
        } catch (e: Exception) {
            logger.error("Error: {}", e.message)
            1
        }
    }
}

@Command(name = "search", description = ["Search for manga by title"])
class SearchCommand : Callable<Int> {
    private val logger = LoggerFactory.getLogger(SearchCommand::class.java)

    @CommandLine.Parameters(index = "0", description = ["Title to search for"])
    lateinit var title: String

    @CommandLine.Option(
        names = ["-p", "--provider"],
        description = ["Search provider to use: mangalivre, taosect"],
        defaultValue = "mangalivre",
    )
    lateinit var provider: String

    override fun call(): Int {
        val scraper = createScraper(provider)

        return try {
            MangaSearchService(scraper = scraper).use { service ->
                val results = service.search(title)
                if (results.isEmpty()) {
                    logger.info("No results found for '{}'", title)
                } else {
                    logger.info("Search results for '{}':", title)
                    results.forEach { logger.info("- {} (ID: {})", it.title, it.id) }
                }
                0
            }
        } catch (e: Exception) {
            logger.error("Error: {}", e.message)
            1
        }
    }

    private fun createScraper(providerName: String): MangaScraperPort =
        when (providerName.lowercase()) {
            "mangalivre" -> {
                MangaLivreScraper()
            }

            "taosect" -> {
                TaosectScraper()
            }

            else -> {
                throw IllegalArgumentException("Unknown provider: $providerName. Valid options: mangalivre, taosect")
            }
        }
}

@Command(name = "download", description = ["Download chapters of a manga"])
class DownloadCommand : Callable<Int> {
    private val logger = LoggerFactory.getLogger(DownloadCommand::class.java)

    @CommandLine.Parameters(index = "0", description = ["Manga ID"])
    lateinit var mangaId: String

    @CommandLine.ArgGroup(multiplicity = "1")
    lateinit var selection: Selection

    class Selection {
        @CommandLine.Option(names = ["-c", "--chapter"], description = ["Specific Chapter Number"])
        var chapterNumber: String? = null

        @CommandLine.Option(names = ["--from"], description = ["Download all chapters starting from this one"])
        var fromChapter: String? = null
    }

    @CommandLine.Option(names = ["-o", "--output", "--output-dir"], description = ["Output directory"], defaultValue = ".")
    lateinit var output: String

    @CommandLine.Option(names = ["--with-volume"], description = ["Include volume in the filename (e.g., Vol. 1 Ch. 1.cbz)"])
    var withVolume: Boolean = false

    @CommandLine.Option(
        names = ["-p", "--provider"],
        description = ["Download provider to use: composite, mangalivre, taosect"],
        defaultValue = "composite",
    )
    lateinit var provider: String

    override fun call(): Int {
        // Create shared PlaywrightClient for Taosect to prevent browser crashes
        val sharedClient =
            com.mangafetcher.downloader.infrastructure.scraper
                .PlaywrightClient()

        val downloadProvider = createDownloadProvider(provider, sharedClient)

        return com.mangafetcher.downloader.application
            .MangaDownloadService(
                sharedPlaywrightClient = sharedClient,
                downloadProvider = downloadProvider,
            ).use { service ->
                try {
                    val request =
                        DownloadRequest(
                            mangaId = mangaId,
                            outputDir = File(output),
                            chapterNumber = selection.chapterNumber,
                            fromChapter = selection.fromChapter,
                            withVolume = withVolume,
                        )

                    val result = service.downloadManga(request)

                    logger.info("\n=== Download Summary ===")
                    logger.info("Successfully downloaded: {} chapters", result.successCount)
                    logger.info("Skipped (already exists): {} chapters", result.skippedCount)
                    logger.info("Failed: {} chapters", result.failedCount)

                    0
                } catch (e: Exception) {
                    logger.error("Error: {}", e.message)
                    1
                }
            }
    }

    private fun createDownloadProvider(
        providerName: String,
        sharedClient: PlaywrightClient,
    ): MangaDownloadProvider =
        when (providerName.lowercase()) {
            "composite" -> {
                com.mangafetcher.downloader.infrastructure.download.CompositeDownloadProvider(
                    listOf(
                        MangaLivreDownloadProvider(playwrightClient = sharedClient),
                        TaosectDownloadProvider(
                            sharedPlaywrightClient = sharedClient,
                        ),
                    ),
                )
            }

            "mangalivre" -> MangaLivreDownloadProvider(playwrightClient = sharedClient)


            "taosect" -> TaosectDownloadProvider(sharedPlaywrightClient = sharedClient)


            else -> {
                throw IllegalArgumentException("Unknown provider: $providerName. Valid options: composite, mangalivre, taosect")
            }
        }
}

@Command(name = "check", description = ["Check if database entries match existing files"])
class CheckCommand : Callable<Int> {
    private val logger = LoggerFactory.getLogger(CheckCommand::class.java)

    @CommandLine.Option(
        names = ["-o", "--output", "--output-dir"],
        description = ["Output directory"],
        defaultValue = ".",
    )
    lateinit var output: String

    @CommandLine.Option(
        names = ["-d", "--delete-missing"],
        description = ["Automatically delete missing entries from the database"],
    )
    var deleteMissing: Boolean = false

    override fun call(): Int {
        val outputDir = File(output)
        if (!outputDir.exists() || !outputDir.isDirectory) {
            logger.error("Output directory does not exist or is not a directory: $output")
            return 1
        }

        val dbFile = File(outputDir, "download.db")
        if (!dbFile.exists()) {
            logger.error("Database file not found at: ${dbFile.absolutePath}")
            return 1
        }

        return com.mangafetcher.downloader.infrastructure.persistence.SqliteDownloadRepository(dbFile).use { tracker ->
            val entries = tracker.getAllDownloads()
            if (entries.isEmpty()) {
                logger.info("No entries found in the database.")
                return 0
            }

            logger.info("Checking {} entries in the database...", entries.size)

            val missingEntries = mutableListOf<com.mangafetcher.downloader.domain.port.DownloadEntry>()

            for (entry in entries) {
                val existingFile =
                    com.mangafetcher.downloader.domain.service.ChapterNamingUtils.findExistingFile(
                        outputDir,
                        entry.chapterNumber,
                        entry.mangaId,
                        entry.chapterId,
                    )
                if (existingFile == null) {
                    missingEntries.add(entry)
                }
            }

            if (missingEntries.isEmpty()) {
                logger.info("All database entries match existing files.")
                return 0
            }

            logger.warn("Found {} missing files.", missingEntries.size)
            for (entry in missingEntries) {
                logger.warn("  - Manga ID: {}, Chapter: {}", entry.mangaId, entry.chapterNumber)
            }

            var deletedCount = 0

            if (deleteMissing) {
                logger.info("Automatically deleting missing entries from database...")
                for (entry in missingEntries) {
                    tracker.removeDownload(entry.mangaId, entry.chapterId)
                    deletedCount++
                }
            } else {
                print("\nDo you want to remove these entries from the database? [y/N]: ")
                val response = readlnOrNull()?.trim()?.lowercase() ?: ""
                if (response == "y" || response == "yes") {
                    logger.info("Deleting entries...")
                    for (entry in missingEntries) {
                        tracker.removeDownload(entry.mangaId, entry.chapterId)
                        deletedCount++
                    }
                } else {
                    logger.info("Skipped deleting entries.")
                }
            }

            logger.info("\n=== Check Summary ===")
            logger.info("Total entries checked: {}", entries.size)
            logger.info("Missing files found: {}", missingEntries.size)
            logger.info("Entries deleted: {}", deletedCount)

            0
        }
    }
}

fun main(args: Array<String>) {
    // Extract log level from args before creating CommandLine (to configure logging early)
    val logLevel =
        args.indexOf("--log-level").let { index ->
            if (index >= 0 && index + 1 < args.size) args[index + 1] else "info"
        }
    DownloaderApplication.configureLogging(logLevel)

    exitProcess(CommandLine(DownloaderApplication()).execute(*args))
}
