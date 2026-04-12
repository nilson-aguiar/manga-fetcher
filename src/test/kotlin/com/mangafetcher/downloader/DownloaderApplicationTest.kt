package com.mangafetcher.downloader

import picocli.CommandLine
import org.junit.jupiter.api.Test
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.test.assertTrue

class DownloaderApplicationTest {

    @Test
    fun `should show help message`() {
        val app = DownloaderApplication()
        val cmd = CommandLine(app)
        val sw = StringWriter()
        cmd.out = PrintWriter(sw)

        cmd.execute("--help")
        val help = sw.toString()

        assertTrue(help.contains("Usage: manga-fetcher"))
        assertTrue(help.contains("Download manga and convert to .cbz files."))
    }
}
