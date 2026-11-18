package com.mangafetcher.downloader.infrastructure.download

import com.mangafetcher.downloader.application.MangaDownloadService
import com.mangafetcher.downloader.domain.model.DownloadRequest
import com.mangafetcher.downloader.domain.port.MangaDownloadProvider
import com.mangafetcher.downloader.infrastructure.metadata.TavernaMetadataProvider
import com.mangafetcher.downloader.infrastructure.scraper.ChapterResult
import com.mangafetcher.downloader.infrastructure.scraper.MangaDetails
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TavernaDownloadProviderTest {
    private val server = MockWebServer()

    @TempDir
    lateinit var tempDir: File

    @BeforeEach
    fun setUp() {
        server.start()
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `should download from Taverna and MediaFire and package into standardized CBZ folder format`() {
        val baseUrl = server.url("").toString().trimEnd('/')
        val archiveBytes = createSampleArchiveBytes()

        server.dispatcher =
            object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse =
                    when (request.path) {
                        "/quadrinho/claymore/" -> {
                            MockResponse()
                                .setResponseCode(200)
                                .setBody(
                                    """
                                    <html>
                                    <head>
                                        <meta property="og:title" content="Claymore - Taverna de Quadrinhos" />
                                        <meta property="og:image" content="$baseUrl/uploads/Claymore-27.jpg" />
                                    </head>
                                    <body>
                                        <div class="elementor-element e-con">
                                            <h1 class="elementor-heading-title">Claymore</h1>
                                            <div class="elementor-widget-text-editor">Sinopse em português de Claymore.</div>
                                        </div>
                                        <div class="jet-listing-grid__items">
                                            <div class="jet-listing-grid__item">
                                                <a class="elementor-button-link" href="$baseUrl/mediafire.com/file/pb9varqib6cuj6n/Claymore_1.cbr/file">Baixar</a>
                                                <div class="jet-listing-dynamic-field__content">Claymore #1</div>
                                            </div>
                                        </div>
                                        <div class="e-con">
                                            <h1 class="elementor-heading-title">Escritor</h1>
                                            <a class="elementor-post-info__terms-list-item" href="$baseUrl/_escritores/norihiro-yagi/">Norihiro Yagi</a>
                                        </div>
                                    </body>
                                    </html>
                                    """.trimIndent(),
                                )
                        }

                        "/uploads/Claymore-27.jpg" -> {
                            MockResponse()
                                .setResponseCode(200)
                                .setBody("fake-cover-bytes")
                        }

                        "/mediafire.com/file/pb9varqib6cuj6n/Claymore_1.cbr/file" -> {
                            MockResponse()
                                .setResponseCode(200)
                                .setBody(
                                    """
                                    <html>
                                    <body>
                                        <a id="downloadButton" href="$baseUrl/direct/pb9varqib6cuj6n/Claymore_1.cbz">Download</a>
                                    </body>
                                    </html>
                                    """.trimIndent(),
                                )
                        }

                        "/direct/pb9varqib6cuj6n/Claymore_1.cbz" -> {
                            MockResponse()
                                .setResponseCode(200)
                                .addHeader("Content-Disposition", "attachment; filename=\"Claymore_1.cbz\"")
                                .setBody(Buffer().write(archiveBytes))
                        }

                        else -> {
                            MockResponse().setResponseCode(404)
                        }
                    }
            }

        val tavernaProvider =
            TavernaDownloadProvider(
                baseUrl = baseUrl,
                enablePlaywrightFallback = false,
            )
        val tavernaMetadata =
            TavernaMetadataProvider(
                baseUrl = baseUrl,
                enablePlaywrightFallback = false,
            )

        val outputDir = File(tempDir, "claymore-output")

        val details = tavernaProvider.fetchMangaDetails("claymore")
        assertEquals("Claymore", details.title)
        assertEquals("Norihiro Yagi", details.authors)

        val chapters = tavernaProvider.fetchChapters("claymore")
        assertEquals(1, chapters.size)
        assertEquals("1", chapters[0].number)
        assertEquals("pb9varqib6cuj6n", chapters[0].id)

        val imagesDir = File(tempDir, "images-test")
        val extractedImages = tavernaProvider.downloadChapterImages("claymore", "pb9varqib6cuj6n", imagesDir)
        assertEquals(2, extractedImages.size)
        assertEquals("001.jpg", extractedImages[0].name)
        assertEquals("002.jpg", extractedImages[1].name)

        // Verify metadata provider returns pt-br metadata
        val metadata = tavernaMetadata.getMetadata("claymore", "1", null)
        assertNotNull(metadata)
        assertEquals("Claymore", metadata.series)
        assertEquals("pt-br", metadata.languageIso)
    }

    @Test
    fun `CompositeDownloadProvider should skip provider that returns blank title`() {
        val emptyProvider =
            object : MangaDownloadProvider {
                override fun fetchMangaDetails(mangaId: String) = MangaDetails("", "", "", "", "", "")

                override fun fetchChapters(mangaId: String) = emptyList<ChapterResult>()

                override fun downloadChapterImages(
                    mangaId: String,
                    chapterId: String,
                    outputDir: File,
                ) = emptyList<File>()

                override fun downloadFile(
                    url: String,
                    outputFile: File,
                ) {}

                override fun close() {}
            }

        val validProvider =
            object : MangaDownloadProvider {
                override fun fetchMangaDetails(mangaId: String) =
                    MangaDetails("Claymore", "Norihiro Yagi", "Norihiro Yagi", "Desc", "Mangás", "")

                override fun fetchChapters(mangaId: String) = listOf(ChapterResult("1", "pb9varqib6cuj6n", null))

                override fun downloadChapterImages(
                    mangaId: String,
                    chapterId: String,
                    outputDir: File,
                ) = emptyList<File>()

                override fun downloadFile(
                    url: String,
                    outputFile: File,
                ) {}

                override fun close() {}
            }

        val composite = CompositeDownloadProvider(listOf(emptyProvider, validProvider))
        val details = composite.fetchMangaDetails("claymore")

        assertEquals("Claymore", details.title)
        assertEquals("Norihiro Yagi", details.authors)
    }

    private fun createSampleArchiveBytes(): ByteArray {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zipOut ->
            zipOut.putNextEntry(ZipEntry("Claymore #1/page2.jpg"))
            zipOut.write("page-2-bytes".toByteArray())
            zipOut.closeEntry()

            zipOut.putNextEntry(ZipEntry("Claymore #1/page1.jpg"))
            zipOut.write("page-1-bytes".toByteArray())
            zipOut.closeEntry()
        }
        return baos.toByteArray()
    }
}
