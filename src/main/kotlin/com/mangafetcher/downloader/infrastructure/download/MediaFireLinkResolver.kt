package com.mangafetcher.downloader.infrastructure.download

import com.mangafetcher.downloader.infrastructure.http.ResilientHttpClient
import com.mangafetcher.downloader.infrastructure.scraper.PlaywrightClient
import org.slf4j.LoggerFactory

data class MediaFireResolvedFile(
    val directUrl: String,
    val fileName: String? = null,
)

/**
 * Strategy interface for resolving MediaFire links into one or more direct downloadable file URLs.
 * Designed so single-file links (`/file/...`) are handled by [MediaFireSingleFileResolver] now,
 * and folder links (`/folder/...`) can be added via a future `MediaFireFolderResolver` implementation
 * without changing [MediaFireDownloader] or [TavernaDownloadProvider].
 */
interface MediaFireLinkResolver {
    fun supports(url: String): Boolean

    fun resolveFiles(url: String): List<MediaFireResolvedFile>
}

/**
 * Resolves single-file MediaFire sharing links (`/file/<key>/...`, `/file_premium/<key>/...`, `/download/<key>/...`).
 */
class MediaFireSingleFileResolver(
    private val httpClient: ResilientHttpClient =
        ResilientHttpClient(defaultUserAgent = ResilientHttpClient.BROWSER_USER_AGENT),
    private val htmlParser: MediaFireHtmlParser = MediaFireHtmlParser(),
    private val playwrightFallback: (() -> PlaywrightClient)? = null,
) : MediaFireLinkResolver {
    private val logger = LoggerFactory.getLogger(MediaFireSingleFileResolver::class.java)

    override fun supports(url: String): Boolean = htmlParser.isSingleFileUrl(url)

    override fun resolveFiles(url: String): List<MediaFireResolvedFile> {
        val normalizedUrl = htmlParser.normalizeUrl(url)
        logger.debug("Resolving MediaFire single-file link: {}", normalizedUrl)

        var directUrl: String? = null

        try {
            val html = httpClient.get(normalizedUrl)
            directUrl = htmlParser.extractDirectDownloadUrl(html)
        } catch (e: Exception) {
            logger.debug("HTTP fetch for MediaFire page failed ({}): {}", normalizedUrl, e.message)
        }

        if (directUrl == null && playwrightFallback != null) {
            logger.info("Falling back to Playwright to resolve MediaFire link: {}", normalizedUrl)
            try {
                val html = playwrightFallback.invoke().fetchPage(normalizedUrl)
                directUrl = htmlParser.extractDirectDownloadUrl(html)
            } catch (e: Exception) {
                logger.warn("Playwright fallback failed for MediaFire link {}: {}", normalizedUrl, e.message)
            }
        }

        if (directUrl == null) {
            logger.warn("Could not extract direct download link from MediaFire URL: {}", normalizedUrl)
            return emptyList()
        }

        val fileName = extractFileNameFromDirectUrl(directUrl)
        return listOf(MediaFireResolvedFile(directUrl = directUrl, fileName = fileName))
    }

    private fun extractFileNameFromDirectUrl(directUrl: String): String? {
        val rawSegment =
            directUrl
                .substringBefore("?")
                .substringBefore("#")
                .trimEnd('/')
                .substringAfterLast("/", "")
        if (rawSegment.isEmpty()) return null
        return try {
            java.net.URLDecoder.decode(rawSegment, "UTF-8")
        } catch (e: Exception) {
            rawSegment
        }
    }
}
