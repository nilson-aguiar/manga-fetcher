package com.mangafetcher.downloader.infrastructure.metadata

import com.mangafetcher.downloader.domain.model.MangaMetadata
import com.mangafetcher.downloader.domain.model.MangaMetadataProvider

/**
 * Composite metadata provider that tries multiple providers in order.
 * If the first provider returns null or fails, it tries the next one.
 */
class CompositeMetadataProvider(
    private val providers: List<MangaMetadataProvider>,
) : MangaMetadataProvider,
    AutoCloseable {
    override fun getMetadata(
        title: String,
        chapter: String?,
        volume: String?,
    ): MangaMetadata? {
        for (provider in providers) {
            try {
                val metadata = provider.getMetadata(title, chapter, volume)
                if (metadata != null) {
                    return metadata
                }
            } catch (e: Exception) {
                // Continue to next provider
                continue
            }
        }
        return null
    }

    override fun close() {
        providers.forEach { provider ->
            if (provider is AutoCloseable) {
                try {
                    provider.close()
                } catch (e: Exception) {
                    // Ignore close errors
                }
            }
        }
    }
}
