package com.mangafetcher.downloader.domain

import com.mangafetcher.downloader.domain.service.ChapterNamingUtils
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
        assertEquals("200", ChapterNamingUtils.getChapterLabel("Capitulo 200"))
        assertEquals("200", ChapterNamingUtils.getChapterLabel("Chapter 200"))
        assertEquals("200", ChapterNamingUtils.getChapterLabel("200"))
        assertEquals("1", ChapterNamingUtils.getChapterLabel("01"))
        assertEquals("0", ChapterNamingUtils.getChapterLabel("00"))
        assertEquals("0.5", ChapterNamingUtils.getChapterLabel("00.5"))
    }

    @Test
    fun `should format file name`() {
        assertEquals("Ch. 200.cbz", ChapterNamingUtils.getFileName("200"))
        assertEquals("Vol. 34 Ch. 200.cbz", ChapterNamingUtils.getFileName("200", "Vol. 34", withVolume = true))
        assertEquals("Vol. 34 Ch. 200.cbz", ChapterNamingUtils.getFileName("200", "Volume 34", withVolume = true))
        assertEquals("Ch. 1.cbz", ChapterNamingUtils.getFileName("01"))
    }

    @Test
    fun `should normalize volume label`() {
        assertEquals("Vol. 34", ChapterNamingUtils.getVolumeLabel("Volume 34"))
        assertEquals("Vol. 34", ChapterNamingUtils.getVolumeLabel("Vol. 34"))
        assertEquals("Vol. 34", ChapterNamingUtils.getVolumeLabel("Vol 34"))
        assertEquals("Vol. 34", ChapterNamingUtils.getVolumeLabel("34"))
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

        val renamed = ChapterNamingUtils.ensureCorrectNaming(tempDir, "manga-id", "chapter-id", "200", "Vol. 34", withVolume = true)
        assertTrue(renamed)

        assertFalse(File(tempDir, "Ch. 200.cbz").exists())
        assertTrue(File(tempDir, "Vol. 34 Ch. 200.cbz").exists())
    }

    @Test
    fun `should not rename if volume matches existing file`() {
        val file = File(tempDir, "Vol. 34 Ch. 200.cbz")
        file.createNewFile()

        val renamed = ChapterNamingUtils.ensureCorrectNaming(tempDir, "manga-id", "chapter-id", "200", "Vol. 34", withVolume = true)
        assertFalse(renamed)
        assertTrue(File(tempDir, "Vol. 34 Ch. 200.cbz").exists())
    }

    @Test
    fun `should rename if volume is null`() {
        val file = File(tempDir, "Ch. 200.cbz")
        file.createNewFile()

        val renamed = ChapterNamingUtils.ensureCorrectNaming(tempDir, "manga-id", "chapter-id", "200", null)
        assertFalse(renamed, "Should not rename if already in correct default format")
    }

    @Test
    fun `should rename from old format to new format`() {
        val oldFile = File(tempDir, "solo-leveling-capitulo-00.cbz")
        oldFile.createNewFile()

        val renamed = ChapterNamingUtils.ensureCorrectNaming(tempDir, "solo-leveling", "capitulo-00", "00", null)
        assertTrue(renamed, "Should rename old format to new format")
        assertFalse(File(tempDir, "solo-leveling-capitulo-00.cbz").exists())
        assertTrue(File(tempDir, "Ch. 0.cbz").exists())
    }
}
