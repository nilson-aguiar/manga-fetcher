package com.mangafetcher.downloader.cli

import picocli.CommandLine
import picocli.CommandLine.Command
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
    @CommandLine.Parameters(index = "0", description = ["Manga ID"])
    lateinit var mangaId: String

    @CommandLine.Option(names = ["-o", "--output", "--output-dir"], description = ["Output directory"], defaultValue = ".")
    lateinit var output: String

    override fun call(): Int {
        val service =
            com.mangafetcher.downloader.application
                .ChapterRenameService()

        return try {
            val outputDir = java.io.File(output)
            val renamedCount = service.renameChapters(mangaId, outputDir)
            println("Renamed $renamedCount files.")
            0
        } catch (e: IllegalArgumentException) {
            println(e.message)
            1
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            1
        }
    }
}

@Command(name = "search", description = ["Search for manga by title"])
class SearchCommand : Callable<Int> {
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
        val service =
            com.mangafetcher.downloader.application
                .MangaSearchService(scraper = scraper)

        return try {
            val results = service.search(title)
            if (results.isEmpty()) {
                println("No results found for '$title'")
            } else {
                println("Search results for '$title':")
                results.forEach { println("- ${it.title} (ID: ${it.id})") }
            }
            0
        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            1
        }
    }

    private fun createScraper(providerName: String): com.mangafetcher.downloader.domain.port.MangaScraperPort =
        when (providerName.lowercase()) {
            "mangalivre" -> {
                com.mangafetcher.downloader.infrastructure.scraper
                    .MangaLivreScraper()
            }

            "taosect" -> {
                com.mangafetcher.downloader.infrastructure.scraper
                    .TaosectScraper()
            }

            else -> {
                throw IllegalArgumentException("Unknown provider: $providerName. Valid options: mangalivre, taosect")
            }
        }
}

@Command(name = "download", description = ["Download chapters of a manga"])
class DownloadCommand : Callable<Int> {
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
        val metadataProvider = createMetadataProvider(provider, sharedClient)

        return com.mangafetcher.downloader.application
            .MangaDownloadService(
                sharedPlaywrightClient = sharedClient,
                downloadProvider = downloadProvider,
                metadataProvider = metadataProvider,
            ).use { service ->
                try {
                    val request =
                        com.mangafetcher.downloader.domain.model.DownloadRequest(
                            mangaId = mangaId,
                            outputDir = java.io.File(output),
                            chapterNumber = selection.chapterNumber,
                            fromChapter = selection.fromChapter,
                            withVolume = withVolume,
                        )

                    val result = service.downloadManga(request)

                    println("\n=== Download Summary ===")
                    println("Successfully downloaded: ${result.successCount} chapters")
                    println("Skipped (already exists): ${result.skippedCount} chapters")
                    println("Failed: ${result.failedCount} chapters")

                    0
                } catch (e: Exception) {
                    System.err.println("Error: ${e.message}")
                    1
                }
            }
    }

    private fun createDownloadProvider(
        providerName: String,
        sharedClient: com.mangafetcher.downloader.infrastructure.scraper.PlaywrightClient,
    ): com.mangafetcher.downloader.domain.port.MangaDownloadProvider =
        when (providerName.lowercase()) {
            "composite" -> {
                com.mangafetcher.downloader.infrastructure.download.CompositeDownloadProvider(
                    listOf(
                        com.mangafetcher.downloader.infrastructure.download
                            .MangaLivreDownloadProvider(),
                        com.mangafetcher.downloader.infrastructure.download.TaosectDownloadProvider(
                            sharedPlaywrightClient = sharedClient,
                        ),
                    ),
                )
            }

            "mangalivre" -> {
                com.mangafetcher.downloader.infrastructure.download
                    .MangaLivreDownloadProvider()
            }

            "taosect" -> {
                com.mangafetcher.downloader.infrastructure.download.TaosectDownloadProvider(
                    sharedPlaywrightClient = sharedClient,
                )
            }

            else -> {
                throw IllegalArgumentException("Unknown provider: $providerName. Valid options: composite, mangalivre, taosect")
            }
        }

    private fun createMetadataProvider(
        providerName: String,
        sharedClient: com.mangafetcher.downloader.infrastructure.scraper.PlaywrightClient,
    ): com.mangafetcher.downloader.domain.model.MangaMetadataProvider =
        when (providerName.lowercase()) {
            "composite" -> {
                com.mangafetcher.downloader.infrastructure.metadata.CompositeMetadataProvider(
                    listOf(
                        com.mangafetcher.downloader.infrastructure.metadata
                            .MangaDexMetadataProvider(),
                        com.mangafetcher.downloader.infrastructure.metadata
                            .MangaLivreMetadataProvider(),
                        com.mangafetcher.downloader.infrastructure.metadata.TaosectMetadataProvider(
                            sharedPlaywrightClient = sharedClient,
                        ),
                    ),
                )
            }

            "mangalivre" -> {
                com.mangafetcher.downloader.infrastructure.metadata
                    .MangaLivreMetadataProvider()
            }

            "taosect" -> {
                com.mangafetcher.downloader.infrastructure.metadata.TaosectMetadataProvider(
                    sharedPlaywrightClient = sharedClient,
                )
            }

            else -> {
                throw IllegalArgumentException("Unknown provider: $providerName. Valid options: composite, mangalivre, taosect")
            }
        }
}

fun main(args: Array<String>) {
    try {
        val uri = java.net.URI.create("resource:/")
        try {
            java.nio.file.FileSystems
                .getFileSystem(uri)
        } catch (e: java.nio.file.FileSystemNotFoundException) {
            java.nio.file.FileSystems
                .newFileSystem(uri, emptyMap<String, Any>())
        }
    } catch (e: Exception) {
        // Ignore initialization errors if we are not in a native image or it fails
    }

    // Extract log level from args before creating CommandLine (to configure logging early)
    val logLevel =
        args.indexOf("--log-level").let { index ->
            if (index >= 0 && index + 1 < args.size) args[index + 1] else "info"
        }
    DownloaderApplication.configureLogging(logLevel)

    exitProcess(CommandLine(DownloaderApplication()).execute(*args))
}
