package com.mangafetcher.downloader

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.util.zip.ZipFile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CbzConverterTest {
    private lateinit var tempDir: File

    @BeforeEach
    fun setUp() {
        tempDir = Files.createTempDirectory("manga-fetcher-test").toFile()
    }

    @AfterEach
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `should create cbz from list of images`() {
        val image1 = File(tempDir, "01.jpg").apply { writeText("image1") }
        val image2 = File(tempDir, "02.jpg").apply { writeText("image2") }
        val outputFile = File(tempDir, "chapter.cbz")

        val converter = CbzConverter()
        converter.convert(listOf(image1, image2), outputFile)

        assertTrue(outputFile.exists())
        ZipFile(outputFile).use { zip ->
            assertEquals(2, zip.size())
            assertNotNull(zip.getEntry("01.jpg"))
            assertNotNull(zip.getEntry("02.jpg"))
        }
    }
}

// Minimal assertNotNull if needed
fun assertNotNull(any: Any?) {
    assertTrue(any != null)
}
