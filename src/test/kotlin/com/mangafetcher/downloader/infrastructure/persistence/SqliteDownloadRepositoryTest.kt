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

    @Test
    fun `test get all downloads`() {
        val dbFile = File(tempDir, "test-list.db")
        SqliteDownloadRepository(dbFile).use { tracker ->
            tracker.markDownloaded("manga1", "chap1", "1")
            tracker.markDownloaded("manga1", "chap2", "2")
            tracker.markDownloaded("manga2", "chap1", "1")

            val all = tracker.getAllDownloads()
            kotlin.test.assertEquals(3, all.size)
            assertTrue(all.any { it.mangaId == "manga1" && it.chapterId == "chap1" && it.chapterNumber == "1" })
        }
    }

    @Test
    fun `test remove download`() {
        val dbFile = File(tempDir, "test-remove.db")
        SqliteDownloadRepository(dbFile).use { tracker ->
            tracker.markDownloaded("manga1", "chap1", "1")
            assertTrue(tracker.isDownloaded("manga1", "chap1"))

            tracker.removeDownload("manga1", "chap1")
            assertFalse(tracker.isDownloaded("manga1", "chap1"))
            assertTrue(tracker.getAllDownloads().isEmpty())
        }
    }
}
