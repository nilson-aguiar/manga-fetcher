package com.mangafetcher.downloader.infrastructure.persistence

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SqliteDownloadRepositoryTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun `test database tracking`() {
        val dbFile = File(tempDir, "test.db")
        SqliteDownloadRepository(dbFile).use { tracker ->
            val mangaId = "one-punch-man"
            val chapterId = "chapter-1"
            val chapterNum = "1"

            assertFalse(tracker.isDownloaded(mangaId, chapterId))
            tracker.markDownloaded(mangaId, chapterId, chapterNum)
            assertTrue(tracker.isDownloaded(mangaId, chapterId))
        }
    }
}
