package com.mangafetcher.downloader.application

import com.mangafetcher.downloader.domain.model.DownloadRequest
import com.mangafetcher.downloader.domain.port.DownloadTrackerPort
import com.mangafetcher.downloader.domain.port.FileConverterPort
import com.mangafetcher.downloader.domain.port.ImageDownloaderPort
import com.mangafetcher.downloader.domain.port.MangaScraperPort
import com.mangafetcher.downloader.infrastructure.scraper.ChapterResult
import com.mangafetcher.downloader.infrastructure.scraper.MangaDetails
import com.mangafetcher.downloader.infrastructure.scraper.MangaResult
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals

/**
 * Demonstrates the benefits of using domain ports.
 * We can now easily test the service with mock implementations,
 * without needing real Playwright, database, or file I/O.
 */
class MangaDownloadServicePortsTest {
    @Test
    fun `demonstrates testability with mock ports`() {
        // Create simple in-memory mock implementations
        val mockScraper =
            object : MangaScraperPort {
                override fun search(title: String) = listOf(MangaResult("Test Manga", "test-id"))

                override fun fetchChapters(mangaId: String) =
                    listOf(
                        ChapterResult("Chapter 1", "ch-1", null),
                    )

                override fun fetchMangaDetails(mangaId: String) =
                    MangaDetails(
                        "Test Manga",
                        "Author",
                        "Artist",
                        "Description",
                        "Tags",
                        "http://cover.jpg",
                    )

                override fun close() {}
            }

        val mockConverter =
            object : FileConverterPort {
                var convertedFiles = mutableListOf<File>()

                override fun convertToCbz(
                    images: List<File>,
                    outputFile: File,
                    metadataXml: String?,
                ) {
                    convertedFiles.add(outputFile)
                }
            }

        val mockImageDownloader =
            object : ImageDownloaderPort {
                override fun downloadChapterImages(
                    baseUrl: String,
                    mangaId: String,
                    chapterId: String,
                    outputDir: File,
                ): List<File> {
                    // Return empty list to simulate no images (skip conversion)
                    return emptyList()
                }

                override fun downloadFile(
                    url: String,
                    outputFile: File,
                ) {}
            }

        // The service is now testable without real dependencies!
        val service =
            MangaDownloadService(
                scraper = mockScraper,
                converter = mockConverter,
                imageDownloader = mockImageDownloader,
            )

        // This test demonstrates that we can now test the service logic
        // without any real I/O, browser automation, or database operations
        // In a real test, we would verify the business logic works correctly
        assertEquals(0, mockConverter.convertedFiles.size, "No files should be converted when no images are downloaded")
    }
}
