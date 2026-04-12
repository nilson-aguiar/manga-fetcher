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
        DownloadCommand::class,
    ],
)
class DownloaderApplication : Callable<Int> {
    override fun call(): Int = 0
}

@Command(name = "search", description = ["Search for manga by title"])
class SearchCommand(
    private val scraper: MangaLivreScraper = MangaLivreScraper(),
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

@Command(name = "download", description = ["Download chapters of a manga"])
class DownloadCommand(
    private val scraper: MangaLivreScraper = MangaLivreScraper(),
    private val converter: CbzConverter = CbzConverter(),
) : Callable<Int> {
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

    override fun call(): Int {
        val outputDir = java.io.File(output)
        outputDir.mkdirs()

        val tempDir =
            java.nio.file.Files
                .createTempDirectory("manga-fetcher-download")
                .toFile()

        try {
            scraper.use { s ->
                val allChapters = s.fetchChapters(mangaId)
                if (allChapters.isEmpty()) {
                    println("No chapters found for manga $mangaId.")
                    return 0
                }

                val chaptersToDownload =
                    if (selection.chapterNumber != null) {
                        val targetNum = extractNumber(selection.chapterNumber!!)
                        allChapters.filter { extractNumber(it.number) == targetNum }
                    } else {
                        val fromNum = extractNumber(selection.fromChapter!!)
                        allChapters.filter { extractNumber(it.number) >= fromNum }
                    }

                if (chaptersToDownload.isEmpty()) {
                    println("No chapters found matching the criteria.")
                    return 0
                }

                println("Found ${chaptersToDownload.size} chapters to process.")

                for (chapter in chaptersToDownload) {
                    val cId = chapter.id
                    val cNum = chapter.number
                    val volume = chapter.volume

                    // Standardize existing file naming (handles old formats and volume additions)
                    if (ChapterNamingUtils.ensureCorrectNaming(outputDir, mangaId, cId, cNum, volume)) {
                        println("Standardized filename for chapter $cNum")
                    }

                    // Check if chapter is already downloaded (now standardized)
                    val existingFile = ChapterNamingUtils.findExistingFile(outputDir, cNum, mangaId, cId)
                    if (existingFile != null) {
                        println("Skipping chapter $cNum (already exists as ${existingFile.name}).")
                        continue
                    }

                    println("Downloading images for $mangaId chapter $cNum...")
                    val images = s.downloadImages(mangaId, cId, tempDir)
                    if (images.isEmpty()) {
                        println("Error: No images found or failed to download chapter $cNum.")
                        continue
                    }

                    val finalName = ChapterNamingUtils.getFileName(cNum, volume)
                    val outputFile = java.io.File(outputDir, finalName)

                    println("Converting to ${outputFile.absolutePath}...")
                    converter.convert(images, outputFile)
                    println("Successfully downloaded and converted to ${outputFile.name}")
                    tempDir.listFiles()?.forEach { it.delete() }
                }
            }
        } finally {
            tempDir.deleteRecursively()
        }
        return 0
    }

    private fun extractNumber(s: String): Double {
        val match = """(\d+(\.\d+)?)""".toRegex().find(s)
        return match?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
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
