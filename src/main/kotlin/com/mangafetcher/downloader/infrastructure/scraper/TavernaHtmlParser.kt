package com.mangafetcher.downloader.infrastructure.scraper

import com.mangafetcher.downloader.domain.model.MangaMetadata
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory

data class TavernaChapterLink(
    val number: String,
    val id: String,
    val volume: String? = null,
    val downloadUrl: String,
    val rawLabel: String,
)

/**
 * Pure HTML parser for Taverna de Quadrinhos (tavernadequadrinhos.com.br).
 * Handles WordPress + Elementor + JetEngine DOM structure.
 */
class TavernaHtmlParser {
    private val logger = LoggerFactory.getLogger(TavernaHtmlParser::class.java)

    companion object {
        private val SECTION_HEADINGS =
            setOf(
                "compartilhe:",
                "tags",
                "deixe o seu comentário",
                "última edição lançada",
                "nome original",
                "editora",
                "tipo",
                "escritor",
                "artista",
                "tradução & diagramação",
                "mangás",
                "você também pode gostar",
                "mais quadrinhos da",
                "outros quadrinhos que",
            )

        private val MEDIAFIRE_KEY_REGEX =
            Regex("""mediafire\.com/(?:file|file_premium|download)/([a-zA-Z0-9]+)""", RegexOption.IGNORE_CASE)
    }

    /**
     * Parses search results HTML from `/?s=<query>`.
     * Scopes strictly to `.jet-listing-grid .jet-listing-grid__item` so recommendation cards
     * in `.elementor-widget-loop-grid` at the bottom of the page are excluded.
     */
    fun parseSearchResults(html: String): List<MangaResult> {
        val doc = Jsoup.parse(html)
        val results = mutableListOf<MangaResult>()
        val seenIds = mutableSetOf<String>()

        for (item in doc.select(".jet-listing-grid .jet-listing-grid__item")) {
            val link =
                item
                    .select("a[href*='/quadrinho/']")
                    .firstOrNull { it.text().isNotBlank() }
                    ?: continue

            val title = link.text().trim()
            val href = link.attr("href").trim().trimEnd('/')
            val id = extractSlugFromUrl(href)
            if (id.isNotEmpty() && title.isNotEmpty() && seenIds.add(id)) {
                results.add(MangaResult(title = title, id = id))
            }
        }

        return results
    }

    /**
     * Parses chapter/issue download cards from a comic page (`/quadrinho/{mangaId}/`).
     */
    fun parseChapterLinks(html: String): List<TavernaChapterLink> {
        val doc = Jsoup.parse(html)
        val mainTitle = extractTitle(doc)
        val links = mutableListOf<TavernaChapterLink>()

        for ((index, item) in doc.select(".jet-listing-grid__item").withIndex()) {
            // Only chapter items have a download button (recommendations grid does not)
            val downloadAnchor =
                item.selectFirst("a.elementor-button-link[href], a[href*='mediafire.com']")
                    ?: continue

            val rawHref = downloadAnchor.attr("href").trim()
            if (rawHref.isEmpty() || rawHref.contains("/quadrinho/")) continue

            val rawLabel =
                item.selectFirst(".jet-listing-dynamic-field__content")?.text()?.trim()
                    ?: item.selectFirst("img[alt]")?.attr("alt")?.trim()
                    ?: ""

            val number = extractChapterNumber(rawLabel, mainTitle, rawHref, index + 1)
            val volume = extractVolumeNumber(rawLabel, rawHref)
            val id = deriveChapterId(rawHref, number)

            links.add(
                TavernaChapterLink(
                    number = number,
                    id = id,
                    volume = volume,
                    downloadUrl = rawHref,
                    rawLabel = rawLabel.ifEmpty { "Ch. $number" },
                ),
            )
        }

        return links
            .distinctBy { it.id }
            .sortedWith(compareBy({ parseNumericValue(it.number) }, { it.number }))
    }

    /**
     * Parses chapter list as domain [ChapterResult] objects.
     */
    fun parseChapters(html: String): List<ChapterResult> =
        parseChapterLinks(html).map { link ->
            ChapterResult(
                number = link.number,
                id = link.id,
                volume = link.volume,
            )
        }

    /**
     * Parses comic/manga details from `/quadrinho/{mangaId}/`.
     */
    fun parseMangaDetails(html: String): MangaDetails {
        val doc = Jsoup.parse(html)

        val title = extractTitle(doc)
        val authors = extractScopedTerms(doc, "Escritor")
        val artists = extractScopedTerms(doc, "Artista")
        val tags = extractScopedTerms(doc, "Tags")
        val description = extractDescription(doc)
        val coverUrl = extractCoverUrl(doc)

        return MangaDetails(
            title = title,
            authors = authors,
            artists = artists,
            description = description,
            tags = tags,
            coverUrl = coverUrl,
        )
    }

    /**
     * Parses [MangaMetadata] for `ComicInfo.xml` generation (`languageIso = "pt-br"`).
     */
    fun parseMangaMetadata(
        html: String,
        baseUrl: String,
        mangaId: String,
    ): MangaMetadata? {
        val doc = Jsoup.parse(html)
        val series = extractTitle(doc)
        if (series.isEmpty()) return null

        val writer = extractScopedTerms(doc, "Escritor")
        val penciller = extractScopedTerms(doc, "Artista")
        val genres =
            extractScopedTerms(doc, "Tags")
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .joinToString(",")
        val summary = extractDescription(doc)
        val alternateSeries = extractOriginalName(doc)
        val normalizedBase = baseUrl.trimEnd('/')

        return MangaMetadata(
            series = series,
            writer = writer.ifEmpty { null },
            penciller = penciller.ifEmpty { null },
            genre = genres.ifEmpty { null },
            summary = summary.ifEmpty { null },
            alternateSeries = alternateSeries.ifEmpty { null },
            languageIso = "pt-br",
            web = "$normalizedBase/quadrinho/$mangaId/",
        )
    }

    private fun extractTitle(doc: Document): String {
        // First try primary H1 heading before metadata/share headings
        val h1Title =
            doc
                .select("h1.elementor-heading-title")
                .map { it.text().trim() }
                .firstOrNull { text ->
                    text.isNotEmpty() && text.lowercase() !in SECTION_HEADINGS
                }
        if (!h1Title.isNullOrEmpty()) {
            return h1Title
        }

        // Fallback to OpenGraph title
        val ogTitle = doc.selectFirst("meta[property=og:title]")?.attr("content")?.trim() ?: ""
        if (ogTitle.isNotEmpty()) {
            return ogTitle
                .substringBefore(" - Taverna de Quadrinhos")
                .replace(Regex("""\s*\(Volume\s+\d+\)\s*$""", RegexOption.IGNORE_CASE), "")
                .trim()
        }

        return ""
    }

    /**
     * Scopes term extraction to the specific `.e-con` container that holds the given heading
     * (e.g., "Escritor", "Artista", "Tags") so unrelated links in recommendation grids
     * (such as the "Finalizada" badge linking to `/_artistas/`) are never included.
     */
    private fun extractScopedTerms(
        doc: Document,
        headingLabel: String,
    ): String {
        val heading =
            doc.select("h1.elementor-heading-title, h2.elementor-heading-title, h3.elementor-heading-title").firstOrNull {
                it.text().trim().equals(headingLabel, ignoreCase = true)
            } ?: return ""

        val container = findSectionContainer(heading) ?: return ""

        val terms =
            container
                .select("a.elementor-post-info__terms-list-item, .elementor-post-info__terms-list a")
                .map { it.text().trim() }
                .filter { it.isNotEmpty() }
                .distinct()

        return terms.joinToString(", ")
    }

    private fun extractOriginalName(doc: Document): String {
        val heading =
            doc.select("h1.elementor-heading-title, h2.elementor-heading-title").firstOrNull {
                it.text().trim().equals("Nome Original", ignoreCase = true)
            } ?: return ""

        val container = findSectionContainer(heading) ?: return ""
        val valueHeading =
            container.select("h2.elementor-heading-title, .elementor-heading-title").firstOrNull {
                !it.text().trim().equals("Nome Original", ignoreCase = true)
            }
        return valueHeading?.text()?.trim() ?: ""
    }

    private fun findSectionContainer(heading: Element): Element? {
        var current: Element? = heading.parent()
        while (current != null) {
            if (current.hasClass("e-con")) {
                return current
            }
            current = current.parent()
        }
        return heading.parent()?.parent()
    }

    private fun extractDescription(doc: Document): String {
        val editorText =
            doc
                .select(".elementor-widget-text-editor")
                .map { it.text().replace('\u00A0', ' ').trim() }
                .firstOrNull { it.isNotEmpty() }
        if (!editorText.isNullOrEmpty()) {
            return editorText
        }

        return doc
            .selectFirst("meta[property=og:description]")
            ?.attr("content")
            ?.replace('\u00A0', ' ')
            ?.trim() ?: ""
    }

    private fun extractCoverUrl(doc: Document): String {
        val ogImage = doc.selectFirst("meta[property=og:image]")?.attr("content")?.trim() ?: ""
        if (ogImage.isNotEmpty()) return ogImage

        val firstCardImg =
            doc.selectFirst(".jet-listing-dynamic-image img")?.let { img ->
                img.attr("data-src").trim().ifEmpty { img.attr("src").trim() }
            } ?: ""
        return if (firstCardImg.startsWith("data:")) "" else firstCardImg
    }

    private fun extractChapterNumber(
        rawLabel: String,
        mainTitle: String,
        downloadUrl: String,
        fallbackIndex: Int,
    ): String {
        val strippedLabel =
            if (mainTitle.isNotEmpty() && rawLabel.startsWith(mainTitle, ignoreCase = true)) {
                rawLabel.substring(mainTitle.length).trim()
            } else {
                rawLabel.trim()
            }

        // 1. Explicit #<number> (e.g., "Claymore #1", "Batman #0.5")
        val hashMatch = Regex("""#\s*0*(\d+(?:\.\d+)?)""").find(rawLabel)
        if (hashMatch != null) {
            return normalizeNumberString(hashMatch.groupValues[1])
        }

        // 2. Explicit Capítulo / Cap / Edição / Vol in stripped label
        val keywordMatch =
            Regex("""(?:cap[íi]tulo|cap\.?|edi[çc][ãa]o|vol(?:ume)?\.?)\s*#?\s*0*(\d+(?:\.\d+)?)""", RegexOption.IGNORE_CASE)
                .find(strippedLabel)
        if (keywordMatch != null) {
            return normalizeNumberString(keywordMatch.groupValues[1])
        }

        // 3. Trailing number in stripped label
        val trailingMatch = Regex("""(\d+(?:\.\d+)?)\s*$""").find(strippedLabel)
        if (trailingMatch != null) {
            return normalizeNumberString(trailingMatch.groupValues[1])
        }

        // 4. Number in decoded download URL filename (e.g., "Claymore_%25231.cbr", "Claymore_vol._26.cbr")
        val decodedUrl =
            try {
                java.net.URLDecoder.decode(java.net.URLDecoder.decode(downloadUrl, "UTF-8"), "UTF-8")
            } catch (e: Exception) {
                downloadUrl
            }
        val urlMatch =
            Regex("""(?:#|vol\.?\s*|cap\.?\s*|_)0*(\d+(?:\.\d+)?)(?=\.[a-z0-9]{2,4}(?:/file)?$)""", RegexOption.IGNORE_CASE)
                .find(decodedUrl)
        if (urlMatch != null) {
            return normalizeNumberString(urlMatch.groupValues[1])
        }

        return fallbackIndex.toString()
    }

    private fun extractVolumeNumber(
        rawLabel: String,
        downloadUrl: String,
    ): String? {
        val labelMatch = Regex("""(?:vol(?:ume)?\.?)\s*0*(\d+)""", RegexOption.IGNORE_CASE).find(rawLabel)
        if (labelMatch != null) {
            return labelMatch.groupValues[1]
        }
        return null
    }

    private fun deriveChapterId(
        downloadUrl: String,
        chapterNumber: String,
    ): String {
        val mfMatch = MEDIAFIRE_KEY_REGEX.find(downloadUrl)
        if (mfMatch != null) {
            return mfMatch.groupValues[1]
        }
        val slug =
            downloadUrl
                .substringBeforeLast("/file")
                .substringAfterLast("/")
                .replace(Regex("[^a-zA-Z0-9._-]"), "-")
                .trim('-')
        return slug.ifEmpty { "ch-$chapterNumber" }
    }

    private fun normalizeNumberString(numStr: String): String {
        val num = numStr.toDoubleOrNull() ?: return numStr
        return if (num % 1.0 == 0.0) num.toInt().toString() else num.toString()
    }

    private fun parseNumericValue(number: String): Double =
        Regex("""(\d+(?:\.\d+)?)""").find(number)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0

    private fun extractSlugFromUrl(url: String): String {
        val afterQuadrinho = url.substringAfter("/quadrinho/", "")
        if (afterQuadrinho.isNotEmpty()) {
            return afterQuadrinho.substringBefore("/").substringBefore("?").substringBefore("#").trim()
        }
        return url.trimEnd('/').substringAfterLast("/")
    }
}
