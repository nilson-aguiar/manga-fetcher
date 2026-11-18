package com.mangafetcher.downloader.infrastructure.scraper

import com.mangafetcher.downloader.domain.model.MangaMetadata
import com.mangafetcher.downloader.domain.model.MangaMetadataProvider
import com.mangafetcher.downloader.domain.port.MangaScraperPort
import com.mangafetcher.downloader.infrastructure.http.ResilientHttpClient
import org.slf4j.LoggerFactory
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Taverna de Quadrinhos scraper.
 * Uses HTTP first (via ResilientHttpClient with a standard browser User-Agent) and falls back
 * lazily to PlaywrightClient only if HTTP is blocked or returns an empty response.
 */
class TavernaScraper(
    private val baseUrl: String = "https://tavernadequadrinhos.com.br",
    private val httpClient: ResilientHttpClient =
        ResilientHttpClient(defaultUserAgent = ResilientHttpClient.BROWSER_USER_AGENT),
    private val htmlParser: TavernaHtmlParser = TavernaHtmlParser(),
    private val sharedPlaywrightClient: PlaywrightClient? = null,
    private val enablePlaywrightFallback: Boolean = true,
) : MangaScraperPort,
    MangaMetadataProvider,
    AutoCloseable {
    private val logger = LoggerFactory.getLogger(TavernaScraper::class.java)
    private var ownedPlaywrightClient: PlaywrightClient? = null
    private val htmlCache = mutableMapOf<String, String>()

    override fun search(title: String): List<MangaResult> {
        logger.info("Searching Taverna de Quadrinhos for: {}", title)
        val encodedQuery = URLEncoder.encode(title, StandardCharsets.UTF_8)
        val url = "${baseUrl.trimEnd('/')}/?s=$encodedQuery"
        val html = fetchHtml(url)
        val results = htmlParser.parseSearchResults(html)
        logger.info("Found {} search results on Taverna de Quadrinhos", results.size)
        return results
    }

    override fun fetchChapters(mangaId: String): List<ChapterResult> =
        fetchChapterLinks(mangaId).map { link ->
            ChapterResult(
                number = link.number,
                id = link.id,
                volume = link.volume,
            )
        }

    fun fetchChapterLinks(mangaId: String): List<TavernaChapterLink> {
        val slug = normalizeMangaId(mangaId)
        logger.info("Fetching chapters for comic: {}", slug)
        val url = "${baseUrl.trimEnd('/')}/quadrinho/$slug/"
        val html = fetchCachedHtml(url)
        val chapters = htmlParser.parseChapterLinks(html)
        logger.info("Found {} chapters available for {}", chapters.size, slug)
        return chapters
    }

    override fun fetchMangaDetails(mangaId: String): MangaDetails {
        val slug = normalizeMangaId(mangaId)
        logger.info("Fetching comic details for: {}", slug)
        val url = "${baseUrl.trimEnd('/')}/quadrinho/$slug/"
        val html = fetchCachedHtml(url)
        val details = htmlParser.parseMangaDetails(html)
        logger.info("Comic title: {}", details.title)
        return details
    }

    override fun getMetadata(
        title: String,
        chapter: String?,
        volume: String?,
    ): MangaMetadata? {
        logger.info("Searching Taverna de Quadrinhos metadata for: {}", title)
        return try {
            val slug = normalizeMangaId(title)
            val metadata = fetchMangaMetadata(slug)?.copy(number = chapter, volume = volume)
            if (metadata != null) {
                logger.info("Taverna: Found match for '{}'", title)
            } else {
                logger.info("Taverna: No results found for '{}'", title)
            }
            metadata
        } catch (e: Exception) {
            logger.warn("Taverna: Error fetching metadata for '{}': {}", title, e.message)
            null
        }
    }

    fun fetchMangaMetadata(mangaId: String): MangaMetadata? {
        val slug = normalizeMangaId(mangaId)
        val url = "${baseUrl.trimEnd('/')}/quadrinho/$slug/"
        val html = fetchCachedHtml(url)
        return htmlParser.parseMangaMetadata(html, baseUrl.trimEnd('/'), slug)
    }

    private fun fetchCachedHtml(url: String): String =
        htmlCache.getOrPut(url) {
            fetchHtml(url)
        }

    private fun fetchHtml(url: String): String {
        try {
            val html = httpClient.get(url)
            if (html.isNotBlank() && !isCloudflareChallenge(html)) {
                return html
            }
            logger.debug("HTTP response for {} was empty or challenged, considering Playwright fallback", url)
        } catch (e: Exception) {
            logger.debug("HTTP fetch failed for {}: {}", url, e.message)
            if (!enablePlaywrightFallback) {
                throw e
            }
        }

        if (!enablePlaywrightFallback) {
            return ""
        }

        val client = getOrCreatePlaywrightClient()
        return client.fetchPage(url)
    }

    private fun isCloudflareChallenge(html: String): Boolean =
        html.contains("Just a moment...", ignoreCase = true) &&
            html.contains("cf-browser-verification", ignoreCase = true)

    private fun getOrCreatePlaywrightClient(): PlaywrightClient {
        sharedPlaywrightClient?.let { return it }
        ownedPlaywrightClient?.let { return it }
        return PlaywrightClient().also { ownedPlaywrightClient = it }
    }

    private fun normalizeMangaId(raw: String): String =
        raw
            .trim()
            .trim('/')
            .substringAfterLast("/")
            .lowercase()
            .replace(Regex("""\s+"""), "-")

    override fun close() {
        try {
            ownedPlaywrightClient?.close()
        } catch (e: Exception) {
            logger.warn("Error closing Taverna Playwright client: {}", e.message)
        } finally {
            ownedPlaywrightClient = null
        }
    }
}
