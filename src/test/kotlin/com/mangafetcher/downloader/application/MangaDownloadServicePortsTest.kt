package com.mangafetcher.downloader.application

import com.mangafetcher.downloader.domain.model.DownloadRequest
import com.mangafetcher.downloader.domain.port.FileConverterPort
import com.mangafetcher.downloader.domain.port.MangaDownloadProvider
import com.mangafetcher.downloader.infrastructure.scraper.ChapterResult
import com.mangafetcher.downloader.infrastructure.scraper.MangaDetails
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Demonstrates the benefits of using domain ports.
 * We can now easily test the service with mock implementations,
 * without needing real Playwright, database, or file I/O.
 */
class MangaDownloadServicePortsTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun `downloads and converts chapters successfully`() {
        // Track interactions with mocks
        val providerCalls = mutableListOf<String>()
        val downloadedChapters = mutableListOf<String>()
        val convertedChapters = mutableListOf<Pair<File, Int>>()

        val mockProvider =
            object : MangaDownloadProvider {
                override fun fetchChapters(mangaId: String): List<ChapterResult> {
                    providerCalls.add("fetchChapters:$mangaId")
                    return listOf(
                        ChapterResult("1", "ch-1", null),
                        ChapterResult("2", "ch-2", null),
                    )
                }

                override fun fetchMangaDetails(mangaId: String): MangaDetails {
                    providerCalls.add("fetchMangaDetails:$mangaId")
                    return MangaDetails(
                        "Test Manga",
                        "Test Author",
                        "Test Artist",
                        "Test Description",
                        "Action, Adventure",
                        "http://example.com/cover.jpg",
                    )
                }

                override fun downloadChapterImages(
                    mangaId: String,
                    chapterId: String,
                    outputDir: File,
                ): List<File> {
                    downloadedChapters.add(chapterId)
                    return listOf(
                        File(outputDir, "page1.jpg").apply { writeText("mock image 1") },
                        File(outputDir, "page2.jpg").apply { writeText("mock image 2") },
                        File(outputDir, "page3.jpg").apply { writeText("mock image 3") },
                    )
                }

                override fun downloadFile(
                    url: String,
                    outputFile: File,
                ) {
                    outputFile.writeText("mock cover")
                }

                override fun close() {}
            }

        val mockConverter =
            object : FileConverterPort {
                override fun convertToCbz(
                    images: List<File>,
                    outputFile: File,
                    metadataXml: String?,
                ) {
                    convertedChapters.add(outputFile to images.size)
                    // Simulate file creation
                    outputFile.writeText("mock cbz content")
                }
            }

        val service =
            MangaDownloadService(
                downloadProvider = mockProvider,
                converter = mockConverter,
            )

        val request =
            DownloadRequest(
                mangaId = "test-manga",
                outputDir = tempDir,
                chapterNumber = "1",
            )

        // Execute the download
        val result = service.downloadManga(request)

        // Verify the service orchestrated all dependencies correctly
        assertEquals(1, result.successCount, "Should successfully download 1 chapter")
        assertEquals(0, result.failedCount, "Should have no failures")

        // Verify provider was called
        assertTrue(providerCalls.contains("fetchMangaDetails:test-manga"), "Should fetch manga details")
        assertTrue(providerCalls.contains("fetchChapters:test-manga"), "Should fetch chapters")

        // Verify images were downloaded
        assertTrue(downloadedChapters.contains("ch-1"), "Should download chapter 1 images")

        // Verify conversion happened
        assertEquals(1, convertedChapters.size, "Should convert 1 chapter")
        assertEquals(3, convertedChapters[0].second, "Should convert 3 images")

        // Verify output file was created
        val outputFile = File(tempDir, "Ch. 1.cbz")
        assertTrue(outputFile.exists(), "CBZ file should be created")

        // Verify manga info was saved
        val infoFile = File(tempDir, "manga_info.csv")
        assertTrue(infoFile.exists(), "Manga info should be saved")
        val infoContent = infoFile.readText()
        assertTrue(infoContent.contains("Test Manga"), "Info should contain manga title")

        // Verify cover was downloaded
        val coverFile = File(tempDir, "cover.jpg")
        assertTrue(coverFile.exists(), "Cover should be downloaded")
    }

    @Test
    fun `skips chapters when images fail to download`() {
        val mockProvider =
            object : MangaDownloadProvider {
                override fun fetchChapters(mangaId: String) =
                    listOf(ChapterResult("1", "ch-1", null))

                override fun fetchMangaDetails(mangaId: String) =
                    MangaDetails("Test Manga", "Author", "Artist", "Desc", "Tags", "")

                override fun downloadChapterImages(
                    mangaId: String,
                    chapterId: String,
                    outputDir: File,
                ): List<File> {
                    // Return empty list to simulate download failure
                    return emptyList()
                }

                override fun downloadFile(
                    url: String,
                    outputFile: File,
                ) {}

                override fun close() {}
            }

        val mockConverter =
            object : FileConverterPort {
                override fun convertToCbz(
                    images: List<File>,
                    outputFile: File,
                    metadataXml: String?,
                ) {
                    error("Should not be called when download fails")
                }
            }

        val service =
            MangaDownloadService(
                downloadProvider = mockProvider,
                converter = mockConverter,
            )

        val request =
            DownloadRequest(
                mangaId = "test-manga",
                outputDir = tempDir,
                chapterNumber = "1",
            )

        val result = service.downloadManga(request)

        // Verify failure was handled correctly
        assertEquals(0, result.successCount, "Should have no successful downloads")
        assertEquals(1, result.failedCount, "Should report 1 failed chapter")
        assertEquals(0, result.skippedCount, "Should have no skipped chapters")
    }

    @Test
    fun `downloads multiple chapters when using fromChapter`() {
        val convertedFiles = mutableListOf<File>()

        val mockProvider =
            object : MangaDownloadProvider {
                override fun fetchChapters(mangaId: String) =
                    listOf(
                        ChapterResult("1", "ch-1", null),
                        ChapterResult("2", "ch-2", null),
                        ChapterResult("3", "ch-3", null),
                    )

                override fun fetchMangaDetails(mangaId: String) =
                    MangaDetails("Test Manga", "Author", "Artist", "Desc", "Tags", "")

                override fun downloadChapterImages(
                    mangaId: String,
                    chapterId: String,
                    outputDir: File,
                ): List<File> =
                    listOf(
                        File(outputDir, "page1.jpg").apply { writeText("mock") },
                    )

                override fun downloadFile(
                    url: String,
                    outputFile: File,
                ) {}

                override fun close() {}
            }

        val mockConverter =
            object : FileConverterPort {
                override fun convertToCbz(
                    images: List<File>,
                    outputFile: File,
                    metadataXml: String?,
                ) {
                    convertedFiles.add(outputFile)
                    outputFile.writeText("mock cbz")
                }
            }

        val service =
            MangaDownloadService(
                downloadProvider = mockProvider,
                converter = mockConverter,
            )

        val request =
            DownloadRequest(
                mangaId = "test-manga",
                outputDir = tempDir,
                fromChapter = "2",
            )

        val result = service.downloadManga(request)

        // Should download chapters 2 and 3
        assertEquals(2, result.successCount, "Should download 2 chapters (2 and 3)")
        assertEquals(2, convertedFiles.size, "Should convert 2 chapters")
    }
}
