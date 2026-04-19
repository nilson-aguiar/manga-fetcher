package com.mangafetcher.downloader.cli
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import picocli.CommandLine
import java.io.PrintStream
import java.io.StringWriter
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CLIIntegrationIT {
    private val logger = LoggerFactory.getLogger(CLIIntegrationIT::class.java)

    @Test
    fun `should search and download using CLI`() {
        val app = DownloaderApplication()
        val cmd = CommandLine(app)

        val sw = StringWriter()
        val printStream = PrintStream(object : java.io.OutputStream() {
            override fun write(b: Int) {
                sw.write(b)
            }
        })

        // Capture System.out and System.err since SLF4J SimpleLogger usually prints to System.err
        val oldOut = System.out
        val oldErr = System.err
        System.setOut(printStream)
        System.setErr(printStream)

        try {
            // 1. Search
            logger.info("Testing CLI search...")
            val searchResult = cmd.execute("search", "solo-leveling")
            val outputAfterSearch = sw.toString()
            assertEquals(searchResult, 0, "Search should return 0 exit code. Output: $outputAfterSearch")
            assertTrue(outputAfterSearch.contains("Solo Leveling"), "Search output should contain 'Solo Leveling'. Output: $outputAfterSearch")

            // 2. Download
            // We use a known chapter number that is likely to exist and be stable
            val chapterToDownload = "00"
            val tempDir = Files.createTempDirectory("cli-integration-it").toFile()
            try {
                logger.info("Testing CLI download...")
                // Use the new -c flag
                val downloadResult = cmd.execute("download", "solo-leveling", "-c", chapterToDownload, "-o", tempDir.absolutePath)
                val outputAfterDownload = sw.toString()

                assertEquals(downloadResult, 0, "Download should return 0 exit code. Output: $outputAfterDownload")
                assertTrue(outputAfterDownload.contains("Successfully downloaded"), "Download output should contain success message. Output: $outputAfterDownload")

                val cbzFiles = tempDir.listFiles { _, name -> name.endsWith(".cbz") }
                assertTrue(cbzFiles != null && cbzFiles.isNotEmpty(), "At least one .cbz file should exist in ${tempDir.absolutePath}")
                assertTrue(cbzFiles[0].length() > 0, "The .cbz file should not be empty")
            } finally {
                tempDir.deleteRecursively()
            }
        } finally {
            System.setOut(oldOut)
            System.setErr(oldErr)
        }
    }
}
