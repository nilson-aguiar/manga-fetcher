package com.mangafetcher.downloader

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChapterNamingTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun `should extract chapter label`() {
        assertEquals("200", ChapterNamingUtils.getChapterLabel("Capítulo 200"))
        assertEquals("200", ChapterNamingUtils.getChapterLabel("200"))
    }

    @Test
    fun `should format file name`() {
        assertEquals("Ch. 200.cbz", ChapterNamingUtils.getFileName("200"))
        assertEquals("Vol. 34 Ch. 200.cbz", ChapterNamingUtils.getFileName("200", "Vol. 34"))
    }

    @Test
    fun `should find existing file`() {
        val file = File(tempDir, "Ch. 200.cbz")
        file.createNewFile()

        val found = ChapterNamingUtils.findExistingFile(tempDir, "Capítulo 200")
        assertNotNull(found)
        assertEquals("Ch. 200.cbz", found.name)

        val fileWithVol = File(tempDir, "Vol. 34 Ch. 201.cbz")
        fileWithVol.createNewFile()
        val foundWithVol = ChapterNamingUtils.findExistingFile(tempDir, "201")
        assertNotNull(foundWithVol)
        assertEquals("Vol. 34 Ch. 201.cbz", foundWithVol.name)

        assertNull(ChapterNamingUtils.findExistingFile(tempDir, "202"))
    }

    @Test
    fun `should rename if volume becomes available`() {
        val file = File(tempDir, "Ch. 200.cbz")
        file.createNewFile()

        val renamed = ChapterNamingUtils.renameIfVolumeAvailable(tempDir, "200", "Vol. 34")
        assertTrue(renamed)

        assertFalse(File(tempDir, "Ch. 200.cbz").exists())
        assertTrue(File(tempDir, "Vol. 34 Ch. 200.cbz").exists())
    }

    @Test
    fun `should not rename if volume matches existing file`() {
        val file = File(tempDir, "Vol. 34 Ch. 200.cbz")
        file.createNewFile()

        val renamed = ChapterNamingUtils.renameIfVolumeAvailable(tempDir, "200", "Vol. 34")
        assertFalse(renamed)
        assertTrue(File(tempDir, "Vol. 34 Ch. 200.cbz").exists())
    }

    @Test
    fun `should not rename if volume is null`() {
        val file = File(tempDir, "Ch. 200.cbz")
        file.createNewFile()

        val renamed = ChapterNamingUtils.renameIfVolumeAvailable(tempDir, "200", null)
        assertFalse(renamed)
    }
}
