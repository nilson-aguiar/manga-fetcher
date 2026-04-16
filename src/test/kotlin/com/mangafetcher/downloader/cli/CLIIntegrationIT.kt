package com.mangafetcher.downloader.cli
import com.mangafetcher.downloader.domain.service.ChapterNamingUtils
import com.mangafetcher.downloader.infrastructure.scraper.MangaLivreScraper
import org.junit.jupiter.api.Test
import picocli.CommandLine
import java.io.StringWriter
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CLIIntegrationIT {
    @Test
    fun `should search and download using CLI`() {
        val app = DownloaderApplication()
        val cmd = CommandLine(app)

        val sw = StringWriter()

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
            assertEquals(searchResult, 0, "Search should return 0 exit code")
            assertTrue(outputAfterSearch.contains("Solo Leveling"), "Search output should contain 'Solo Leveling'")

            // Get a real chapter to ensure download works
            val scraper = MangaLivreScraper()
            val chapters = scraper.use { it.fetchChapters("solo-leveling") }
            assertTrue(chapters.isNotEmpty(), "Should find chapters for solo-leveling")
            val firstChapter = chapters.last() // Usually last is the first chapter
            val firstChapterNum = firstChapter.number
            println("Found real chapter number: $firstChapterNum")

            // 2. Download
            val tempDir = Files.createTempDirectory("cli-integration-it").toFile()
            try {
                println("Testing CLI download...")
                // Use the new -c flag
                val downloadResult = cmd.execute("download", "solo-leveling", "-c", firstChapterNum, "-o", tempDir.absolutePath)
                val outputAfterDownload = sw.toString()

                assertEquals(downloadResult, 0, "Download should return 0 exit code")
                assertTrue(outputAfterDownload.contains("Successfully downloaded"), "Download output should contain success message")

                val expectedName = ChapterNamingUtils.getFileName(firstChapter.number, firstChapter.volume)
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
