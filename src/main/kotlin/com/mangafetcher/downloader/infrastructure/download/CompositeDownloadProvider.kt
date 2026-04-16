package com.mangafetcher.downloader.infrastructure.download

import com.mangafetcher.downloader.domain.port.MangaDownloadProvider
import com.mangafetcher.downloader.infrastructure.scraper.ChapterResult
import com.mangafetcher.downloader.infrastructure.scraper.MangaDetails
import java.io.File

/**
 * Composite download provider that tries multiple providers in order.
 * If the first provider fails, it tries the next one.
 */
class CompositeDownloadProvider(
    private val providers: List<MangaDownloadProvider>,
) : MangaDownloadProvider,
    AutoCloseable {
    override fun fetchMangaDetails(mangaId: String): MangaDetails {
        var lastException: Exception? = null
        for (provider in providers) {
            try {
                return provider.fetchMangaDetails(mangaId)
            } catch (e: Exception) {
                lastException = e
                continue
            }
        }
        throw lastException ?: IllegalStateException("No providers available")
    }

    override fun fetchChapters(mangaId: String): List<ChapterResult> {
        var lastException: Exception? = null
        for (provider in providers) {
            try {
                val chapters = provider.fetchChapters(mangaId)
                if (chapters.isNotEmpty()) {
                    return chapters
                }
            } catch (e: Exception) {
                lastException = e
                continue
            }
        }
        // If all providers returned empty lists or failed, return empty list
        return emptyList()
    }

    override fun downloadChapterImages(
        mangaId: String,
        chapterId: String,
        outputDir: File,
    ): List<File> {
        var lastException: Exception? = null
        for (provider in providers) {
            try {
                val images = provider.downloadChapterImages(mangaId, chapterId, outputDir)
                if (images.isNotEmpty()) {
                    return images
                }
            } catch (e: Exception) {
                lastException = e
                continue
            }
        }
        // If all providers returned empty lists or failed, return empty list
        return emptyList()
    }

    override fun downloadFile(
        url: String,
        outputFile: File,
    ) {
        var lastException: Exception? = null
        for (provider in providers) {
            try {
                provider.downloadFile(url, outputFile)
                return
            } catch (e: Exception) {
                lastException = e
                continue
            }
        }
        throw lastException ?: IllegalStateException("No providers available")
    }

    override fun close() {
        providers.forEach { provider ->
            try {
                provider.close()
            } catch (e: Exception) {
                // Ignore close errors
            }
        }
    }
}
