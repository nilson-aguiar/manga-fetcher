package com.mangafetcher.downloader.infrastructure.persistence

import com.mangafetcher.downloader.domain.port.DownloadTrackerPort
import java.io.File
import java.sql.Connection
import java.sql.DriverManager

class SqliteDownloadRepository(
    private val dbFile: File,
) : DownloadTrackerPort,
    AutoCloseable {
    private val connection: Connection by lazy {
        DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}")
    }

    init {
        setup()
    }

    private fun setup() {
        connection.createStatement().use { statement ->
            statement.execute(
                """
                CREATE TABLE IF NOT EXISTS downloads (
                    manga_id TEXT,
                    chapter_id TEXT,
                    chapter_number TEXT,
                    downloaded_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY(manga_id, chapter_id)
                )
                """.trimIndent(),
            )
        }
    }

    override fun isDownloaded(
        mangaId: String,
        chapterId: String,
    ): Boolean {
        val sql = "SELECT 1 FROM downloads WHERE manga_id = ? AND chapter_id = ?"
        return connection.prepareStatement(sql).use { statement ->
            statement.setString(1, mangaId)
            statement.setString(2, chapterId)
            statement.executeQuery().use { it.next() }
        }
    }

    override fun markDownloaded(
        mangaId: String,
        chapterId: String,
        chapterNumber: String,
    ) {
        val sql = "INSERT OR REPLACE INTO downloads (manga_id, chapter_id, chapter_number) VALUES (?, ?, ?)"
        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, mangaId)
            statement.setString(2, chapterId)
            statement.setString(3, chapterNumber)
            statement.executeUpdate()
        }
    }

    override fun close() {
        if (!connection.isClosed) {
            connection.close()
        }
    }
}
