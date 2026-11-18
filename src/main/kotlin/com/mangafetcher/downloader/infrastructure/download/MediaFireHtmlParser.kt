package com.mangafetcher.downloader.infrastructure.download

import org.jsoup.Jsoup
import java.util.Base64

/**
 * Pure HTML parser for MediaFire file pages.
 * Extracts direct download URLs from static `#downloadButton` attributes,
 * Base64-encoded `data-scrambled-url` attributes, or fallback selectors.
 */
class MediaFireHtmlParser {
    companion object {
        private val FILE_KEY_REGEX =
            Regex("""mediafire\.com/(?:file|file_premium|download)/([a-zA-Z0-9]+)""", RegexOption.IGNORE_CASE)
        private val FOLDER_KEY_REGEX =
            Regex("""mediafire\.com/folder/([a-zA-Z0-9]+)""", RegexOption.IGNORE_CASE)
        private val DIRECT_DOWNLOAD_REGEX =
            Regex("""https?://download\d*\.mediafire\.com/[^"'\s<>]+""", RegexOption.IGNORE_CASE)
    }

    /**
     * Normalizes a MediaFire URL (e.g., upgrades `http://` to `https://`).
     */
    fun normalizeUrl(url: String): String {
        val trimmed = url.trim()
        return if (
            trimmed.startsWith("http://www.mediafire.com", ignoreCase = true) ||
            trimmed.startsWith("http://mediafire.com", ignoreCase = true)
        ) {
            "https://" + trimmed.substring("http://".length)
        } else {
            trimmed
        }
    }

    /**
     * Returns true if the URL points to a single MediaFire file (`/file/...`, `/file_premium/...`, or `/download/...`).
     */
    fun isSingleFileUrl(url: String): Boolean = FILE_KEY_REGEX.containsMatchIn(url)

    /**
     * Returns true if the URL points to a MediaFire folder (`/folder/...`).
     */
    fun isFolderUrl(url: String): Boolean = FOLDER_KEY_REGEX.containsMatchIn(url)

    /**
     * Extracts the MediaFire file key from a `/file/<key>/...` URL.
     */
    fun extractFileKey(url: String): String? = FILE_KEY_REGEX.find(url)?.groupValues?.get(1)

    /**
     * Extracts the direct download link (`https://downloadXXXX.mediafire.com/...`) from a MediaFire file HTML page.
     */
    fun extractDirectDownloadUrl(html: String): String? {
        if (html.isBlank()) return null

        val doc = Jsoup.parse(html)

        // 1. Check #downloadButton data-scrambled-url (Base64 encoded direct URL)
        val downloadBtn = doc.selectFirst("a#downloadButton, #downloadButton")
        if (downloadBtn != null) {
            val scrambled = downloadBtn.attr("data-scrambled-url").trim()
            if (scrambled.isNotEmpty()) {
                decodeBase64Url(scrambled)?.let { return it }
            }

            // 2. Check #downloadButton href
            val href = downloadBtn.attr("href").trim()
            if (isValidHttpUrl(href)) {
                return href
            }
        }

        // 3. Fallback: aria-label="Download file" or a.input.popsok
        val fallbackAnchors =
            doc.select(
                "#download_link a[aria-label='Download file'], a[aria-label='Download file'], a.input.popsok",
            )
        for (anchor in fallbackAnchors) {
            val scrambled = anchor.attr("data-scrambled-url").trim()
            if (scrambled.isNotEmpty()) {
                decodeBase64Url(scrambled)?.let { return it }
            }
            val href = anchor.attr("href").trim()
            if (isValidHttpUrl(href)) {
                return href
            }
        }

        // 4. Regex fallback in raw HTML/scripts
        val regexMatch = DIRECT_DOWNLOAD_REGEX.find(html)
        if (regexMatch != null) {
            return regexMatch.value
        }

        return null
    }

    private fun decodeBase64Url(scrambled: String): String? =
        try {
            val decoded = String(Base64.getDecoder().decode(scrambled), Charsets.UTF_8).trim()
            if (isValidHttpUrl(decoded)) decoded else null
        } catch (e: Exception) {
            null
        }

    private fun isValidHttpUrl(url: String): Boolean =
        (url.startsWith("https://", ignoreCase = true) || url.startsWith("http://", ignoreCase = true)) &&
            !url.contains("javascript:", ignoreCase = true)
}
