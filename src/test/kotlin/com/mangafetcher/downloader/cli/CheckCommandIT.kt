package com.mangafetcher.downloader.cli

import com.mangafetcher.downloader.infrastructure.persistence.SqliteDownloadRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import picocli.CommandLine
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CheckCommandIT {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun `should detect missing files and remove them from db`() {
        val dbFile = File(tempDir, "download.db")
        val mangaId = "test-manga"
        val chapterId = "chap-1"
        val chapterNum = "1"

        // 1. Setup DB entry
        SqliteDownloadRepository(dbFile).use { tracker ->
            tracker.markDownloaded(mangaId, chapterId, chapterNum)
        }

        // 2. Run check - should find missing file
        val app = DownloaderApplication()
        val cmd = CommandLine(app)
        
        // Use -d to delete missing entries automatically
        val result = cmd.execute("check", "-o", tempDir.absolutePath, "-d")
        assertEquals(0, result)

        // 3. Verify entry was deleted
        SqliteDownloadRepository(dbFile).use { tracker ->
            assertTrue(tracker.getAllDownloads().isEmpty(), "Database should be empty after deleting missing file entry")
        }
    }

    @Test
    fun `should keep entries when files exist`() {
        val dbFile = File(tempDir, "download.db")
        val mangaId = "test-manga"
        val chapterId = "chap-1"
        val chapterNum = "1"

        // 1. Setup DB entry and file
        SqliteDownloadRepository(dbFile).use { tracker ->
            tracker.markDownloaded(mangaId, chapterId, chapterNum)
        }
        val chapterFile = File(tempDir, "Ch. 1.cbz")
        chapterFile.writeText("fake content")

        // 2. Run check
        val app = DownloaderApplication()
        val cmd = CommandLine(app)
        val result = cmd.execute("check", "-o", tempDir.absolutePath)
        assertEquals(0, result)

        // 3. Verify entry still exists
        SqliteDownloadRepository(dbFile).use { tracker ->
            val all = tracker.getAllDownloads()
            assertEquals(1, all.size)
            assertEquals(chapterId, all[0].chapterId)
        }
    }
}
