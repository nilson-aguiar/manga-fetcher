package com.mangafetcher.downloader

import org.junit.jupiter.api.Test
import picocli.CommandLine
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.file.Files
import kotlin.test.assertTrue

class CLIIntegrationIT {
    @Test
    fun `should search and download using CLI`() {
        val app = DownloaderApplication()
        val cmd = CommandLine(app)

        val sw = StringWriter()
        val pw = PrintWriter(sw)

        // Capture System.out
        val oldOut = System.out
        System.setOut(
            java.io.PrintStream(
                object : java.io.OutputStream() {
                    override fun write(b: Int) {
                        sw.write(b)
                        oldOut.write(b) // Still print to real out for debugging
                    }
                },
            ),
        )

        try {
            // 1. Search
            println("Testing CLI search...")
            val searchResult = cmd.execute("search", "solo-leveling")
            val outputAfterSearch = sw.toString()
            assertTrue(searchResult == 0, "Search should return 0 exit code")
            assertTrue(outputAfterSearch.contains("Solo Leveling"), "Search output should contain 'Solo Leveling'")

            // Get a real chapter ID to ensure download works
            val scraper = MangaLivreScraper()
            val chapters = scraper.use { it.fetchChapters("solo-leveling") }
            assertTrue(chapters.isNotEmpty(), "Should find chapters for solo-leveling")
            val firstChapterId = chapters.last().id
            println("Found real chapter ID: $firstChapterId")

            // 2. Download
            val tempDir = Files.createTempDirectory("cli-integration-it").toFile()
            try {
                println("Testing CLI download...")
                val downloadResult = cmd.execute("download", "solo-leveling", firstChapterId, "-o", tempDir.absolutePath)
                val outputAfterDownload = sw.toString()

                assertTrue(downloadResult == 0, "Download should return 0 exit code")
                assertTrue(outputAfterDownload.contains("Successfully downloaded"), "Download output should contain success message")

                val chapter = chapters.last()
                val expectedName = ChapterNamingUtils.getFileName(chapter.number, chapter.volume)
                val expectedFile = tempDir.resolve(expectedName)
                
                assertTrue(expectedFile.exists(), "Expected .cbz file should exist at ${expectedFile.absolutePath}")
                assertTrue(expectedFile.length() > 0, "Expected .cbz file should not be empty")
            } finally {
                tempDir.deleteRecursively()
            }
        } finally {
            System.setOut(oldOut)
        }
    }
}
