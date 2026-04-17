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
    private val logger = org.slf4j.LoggerFactory.getLogger(CompositeMetadataProvider::class.java)

    override fun getMetadata(
        title: String,
        chapter: String?,
        volume: String?,
    ): MangaMetadata? {
        logger.debug("CompositeMetadataProvider: Trying {} providers for '{}'", providers.size, title)
        for ((index, provider) in providers.withIndex()) {
            try {
                logger.debug(
                    "CompositeMetadataProvider: Trying provider {} of {} ({})",
                    index + 1,
                    providers.size,
                    provider::class.simpleName,
                )
                val metadata = provider.getMetadata(title, chapter, volume)
                if (metadata != null) {
                    logger.info("CompositeMetadataProvider: Successfully got metadata from {} for '{}'", provider::class.simpleName, title)
                    return metadata
                }
                logger.debug("CompositeMetadataProvider: Provider {} returned null", provider::class.simpleName)
            } catch (e: Exception) {
                logger.debug("CompositeMetadataProvider: Provider {} threw exception: {}", provider::class.simpleName, e.message)
                // Continue to next provider
                continue
            }
        }
        logger.warn("CompositeMetadataProvider: No metadata found for '{}' from any provider", title)
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
