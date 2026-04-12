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
class SearchCommand : Callable<Int> {
    @CommandLine.Parameters(index = "0", description = ["Title to search for"])
    lateinit var title: String

    override fun call(): Int {
        println("Searching for: $title")
        return 0
    }
}

@Command(name = "download", description = ["Download a specific chapter of a manga"])
class DownloadCommand : Callable<Int> {
    @CommandLine.Parameters(index = "0", description = ["Manga ID"])
    lateinit var mangaId: String

    @CommandLine.Parameters(index = "1", description = ["Chapter ID"])
    lateinit var chapterId: String

    @CommandLine.Option(names = ["-o", "--output"], description = ["Output directory"], defaultValue = ".")
    lateinit var output: String

    override fun call(): Int {
        println("Downloading $mangaId chapter $chapterId to $output")
        return 0
    }
}

fun main(args: Array<String>) {
    exitProcess(CommandLine(DownloaderApplication()).execute(*args))
}
