package com.mangafetcher.downloader

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
        DownloadCommand::class
    ]
)
class DownloaderApplication : Callable<Int> {
    override fun call(): Int = 0
}

@Command(name = "search", description = ["Search for manga by title"])
class SearchCommand(
    private val scraper: MangaLivreScraper = MangaLivreScraper()
) : Callable<Int> {
    @CommandLine.Parameters(index = "0", description = ["Title to search for"])
    lateinit var title: String

    override fun call(): Int {
        scraper.use { s ->
            val results = s.search(title)
            if (results.isEmpty()) {
                println("No results found for '$title'")
                return 0
            }
            println("Search results for '$title':")
            results.forEach { println("- ${it.title} (ID: ${it.id})") }
        }
        return 0
    }
}

@Command(name = "download", description = ["Download a specific chapter of a manga"])
class DownloadCommand(
    private val scraper: MangaLivreScraper = MangaLivreScraper(),
    private val converter: CbzConverter = CbzConverter()
) : Callable<Int> {
    @CommandLine.Parameters(index = "0", description = ["Manga ID"])
    lateinit var mangaId: String

    @CommandLine.Parameters(index = "1", description = ["Chapter ID"])
    lateinit var chapterId: String

    @CommandLine.Option(names = ["-o", "--output"], description = ["Output directory"], defaultValue = ".")
    lateinit var output: String

    override fun call(): Int {
        val outputDir = java.io.File(output)
        val tempDir = java.nio.file.Files.createTempDirectory("manga-fetcher-download").toFile()
        
        try {
            scraper.use { s ->
                println("Downloading images for $mangaId chapter $chapterId...")
                val images = s.downloadImages(mangaId, chapterId, tempDir)
                if (images.isEmpty()) {
                    println("Error: No images found or failed to download.")
                    return 1
                }
                
                val outputFile = java.io.File(outputDir, "$mangaId-$chapterId.cbz")
                println("Converting to ${outputFile.absolutePath}...")
                converter.convert(images, outputFile)
                println("Successfully downloaded and converted to ${outputFile.name}")
            }
        } finally {
            tempDir.deleteRecursively()
        }
        return 0
    }
}

fun main(args: Array<String>) {
    exitProcess(CommandLine(DownloaderApplication()).execute(*args))
}
