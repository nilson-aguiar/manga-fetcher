package com.mangafetcher.downloader.infrastructure.metadata

import com.mangafetcher.downloader.infrastructure.scraper.MangaDetails
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MetadataGenerationTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun `test csv escaping`() {
        val details =
            MangaDetails(
                title = "Manga, Title",
                authors = "Author \"One\"",
                artists = "Artist\nTwo",
                description = "Description with , and \" and \n",
                tags = "Tag1, Tag2",
                coverUrl = "http://example.com/cover.jpg",
            )

        saveMangaInfo(details, tempDir)

        val infoFile = File(tempDir, "manga_info.csv")
        assertTrue(infoFile.exists())

        val content = infoFile.readText()
        assertTrue(content.contains("\"Manga, Title\""))
        assertTrue(content.contains("\"Author \"\"One\"\"\""))
        assertTrue(content.contains("\"Artist\nTwo\""))
        assertTrue(content.contains("\"Description with , and \"\" and \n\""))
        assertTrue(content.contains("\"Tag1, Tag2\""))
    }

    private fun saveMangaInfo(
        details: MangaDetails,
        outputDir: File,
    ) {
        val infoFile = File(outputDir, "manga_info.csv")
        val header = "title,authors,artists,description,tags"
        val row =
            listOf(
                details.title,
                details.authors,
                details.artists,
                details.description,
                details.tags,
            ).joinToString(",") { escapeCsv(it) }

        infoFile.writeText("$header\n$row")
    }

    private fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            "\"$escaped\""
        } else {
            escaped
        }
    }
}
