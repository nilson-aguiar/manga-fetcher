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
        assertTrue(help.contains("Download chapters of a manga"))
    }

    @Test
    fun `download command should have -c and --chapter options`() {
        val app = DownloaderApplication()
        val cmd = CommandLine(app)
        val sw = StringWriter()
        cmd.out = PrintWriter(sw)
        cmd.err = PrintWriter(sw)

        cmd.execute("download", "--help")
        val help = sw.toString()

        assertTrue(help.contains("-c, --chapter"), "Should have -c/--chapter option")
    }

    @Test
    fun `download command should not accept positional chapter ID`() {
        val app = DownloaderApplication()
        val cmd = CommandLine(app)
        val sw = StringWriter()
        cmd.out = PrintWriter(sw)
        cmd.err = PrintWriter(sw)

        // Currently index 1 is positional chapterId, so this should fail once refactored
        val exitCode = cmd.execute("download", "manga-id", "chapter-id")
        
        // After refactor, this should return non-zero because of unmatched argument
        assertTrue(exitCode != 0, "Should not accept positional chapter ID")
    }

    @Test
    fun `download command should have -c and --from as mutually exclusive`() {
        val app = DownloaderApplication()
        val cmd = CommandLine(app)
        val sw = StringWriter()
        cmd.out = PrintWriter(sw)
        cmd.err = PrintWriter(sw)

        // Should fail if both are provided
        val exitCode = cmd.execute("download", "manga-id", "-c", "1", "--from", "1")
        
        assertTrue(exitCode != 0, "Should not accept both -c and --from")
    }
}
