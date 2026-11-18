package com.mangafetcher.downloader.infrastructure.download

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArchiveExtractorTest {
    private val extractor = ArchiveExtractor()

    @TempDir
    lateinit var tempDir: File

    @Test
    fun `should extract and naturally sort images from nested zip or cbz while ignoring metadata and system files`() {
        val archiveFile = File(tempDir, "sample.cbz")
        ZipOutputStream(FileOutputStream(archiveFile)).use { zipOut ->
            fun addEntry(
                name: String,
                content: String,
            ) {
                zipOut.putNextEntry(ZipEntry(name))
                zipOut.write(content.toByteArray())
                zipOut.closeEntry()
            }

            addEntry("ComicInfo.xml", "<ComicInfo/>")
            addEntry("__MACOSX/Claymore #1/._page1.jpg", "junk")
            addEntry("Claymore #1/.DS_Store", "junk")
            addEntry("Claymore #1/page10.jpg", "image-10")
            addEntry("Claymore #1/page2.jpg", "image-2")
            addEntry("Claymore #1/page1.png", "image-1")
        }

        val extractDir = File(tempDir, "extracted")
        val images = extractor.extractImages(archiveFile, extractDir)

        assertEquals(3, images.size)
        assertEquals("001.png", images[0].name)
        assertEquals("image-1", images[0].readText())
        assertEquals("002.jpg", images[1].name)
        assertEquals("image-2", images[1].readText())
        assertEquals("003.jpg", images[2].name)
        assertEquals("image-10", images[2].readText())
    }

    @Test
    fun `should detect PDF magic bytes and return empty list gracefully`() {
        val pdfFile = File(tempDir, "sample.pdf")
        pdfFile.writeBytes("%PDF-1.7 fake pdf content".toByteArray())

        assertEquals(ArchiveExtractor.ArchiveType.PDF, extractor.detectArchiveType(pdfFile))
        val extractDir = File(tempDir, "extracted-pdf")
        val images = extractor.extractImages(pdfFile, extractDir)
        assertTrue(images.isEmpty())
    }

    @Test
    fun `should detect RAR5 magic bytes`() {
        val rar5Header = byteArrayOf(0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x01, 0x00)
        val rarFile = File(tempDir, "sample.cbr")
        rarFile.writeBytes(rar5Header)

        assertEquals(ArchiveExtractor.ArchiveType.RAR_OR_7Z, extractor.detectArchiveType(rarFile))
    }

    @Test
    fun `should extract real RAR5 cbr archive using SevenZipJBinding when fixture is present`() {
        val sampleCbr = File("/tmp/claymore1.cbr").takeIf { it.exists() } ?: return
        val extractDir = File(tempDir, "extracted-rar5")

        val images = extractor.extractImages(sampleCbr, extractDir)

        assertTrue(images.isNotEmpty(), "Expected extracted images from RAR5 .cbr archive")
        assertEquals("001.jpg", images.first().name)
        assertTrue(images.first().length() > 1000)
    }
}
