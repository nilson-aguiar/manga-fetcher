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
    override fun call(): Int = 0
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

    override fun call(): Int {
        val service =
            com.mangafetcher.downloader.application
                .MangaSearchService()

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
        defaultValue = "composite"
    )
    lateinit var provider: String

    override fun call(): Int {
        val downloadProvider = createDownloadProvider(provider)
        val metadataProvider = createMetadataProvider(provider)

        val service =
            com.mangafetcher.downloader.application
                .MangaDownloadService(
                    downloadProvider = downloadProvider,
                    metadataProvider = metadataProvider
                )

        return try {
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

    private fun createDownloadProvider(providerName: String): com.mangafetcher.downloader.domain.port.MangaDownloadProvider {
        return when (providerName.lowercase()) {
            "composite" -> com.mangafetcher.downloader.infrastructure.download.CompositeDownloadProvider(
                listOf(
                    com.mangafetcher.downloader.infrastructure.download.MangaLivreDownloadProvider(),
                    com.mangafetcher.downloader.infrastructure.download.TaosectDownloadProvider(),
                )
            )
            "mangalivre" -> com.mangafetcher.downloader.infrastructure.download.MangaLivreDownloadProvider()
            "taosect" -> com.mangafetcher.downloader.infrastructure.download.TaosectDownloadProvider()
            else -> throw IllegalArgumentException("Unknown provider: $providerName. Valid options: composite, mangalivre, taosect")
        }
    }

    private fun createMetadataProvider(providerName: String): com.mangafetcher.downloader.domain.model.MangaMetadataProvider {
        return when (providerName.lowercase()) {
            "composite" -> com.mangafetcher.downloader.infrastructure.metadata.CompositeMetadataProvider(
                listOf(
                    com.mangafetcher.downloader.infrastructure.metadata.MangaDexMetadataProvider(),
                    com.mangafetcher.downloader.infrastructure.metadata.MangaLivreMetadataProvider(),
                    com.mangafetcher.downloader.infrastructure.metadata.TaosectMetadataProvider(),
                )
            )
            "mangalivre" -> com.mangafetcher.downloader.infrastructure.metadata.MangaLivreMetadataProvider()
            "taosect" -> com.mangafetcher.downloader.infrastructure.metadata.TaosectMetadataProvider()
            else -> throw IllegalArgumentException("Unknown provider: $providerName. Valid options: composite, mangalivre, taosect")
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

    exitProcess(CommandLine(DownloaderApplication()).execute(*args))
}
