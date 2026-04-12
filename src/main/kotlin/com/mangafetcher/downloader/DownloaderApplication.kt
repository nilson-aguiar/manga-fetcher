package com.mangafetcher.downloader

import picocli.CommandLine
import picocli.CommandLine.Command
import java.util.concurrent.Callable
import kotlin.system.exitProcess

@Command(name = "manga-fetcher", mixinStandardHelpOptions = true, version = ["0.0.1-SNAPSHOT"],
         description = ["Download manga and convert to .cbz files."])
class DownloaderApplication : Callable<Int> {

    override fun call(): Int {
        return 0
    }
}

fun main(args: Array<String>) {
    exitProcess(CommandLine(DownloaderApplication()).execute(*args))
}
