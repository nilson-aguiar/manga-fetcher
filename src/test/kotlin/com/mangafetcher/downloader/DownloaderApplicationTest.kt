package com.mangafetcher.downloader

import org.junit.jupiter.api.Test
import picocli.CommandLine
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.test.assertTrue

class DownloaderApplicationTest {
    @Test
    fun `should have search subcommand`() {
        val app = DownloaderApplication()
        val cmd = CommandLine(app)
        val sw = StringWriter()
        cmd.out = PrintWriter(sw)
        cmd.err = PrintWriter(sw)

        cmd.execute("search", "--help")
        val help = sw.toString()

        assertTrue(help.contains("Usage: manga-fetcher search"))
        assertTrue(help.contains("Search for manga by title"))
    }

    @Test
    fun `should have download subcommand`() {
        val app = DownloaderApplication()
        val cmd = CommandLine(app)
        val sw = StringWriter()
        cmd.out = PrintWriter(sw)
        cmd.err = PrintWriter(sw)

        cmd.execute("download", "--help")
        val help = sw.toString()

        assertTrue(help.contains("Usage: manga-fetcher download"))
        assertTrue(help.contains("Download a specific chapter of a manga"))
    }
}
