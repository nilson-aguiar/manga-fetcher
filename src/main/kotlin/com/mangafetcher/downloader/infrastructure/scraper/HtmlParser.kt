package com.mangafetcher.downloader.infrastructure.scraper

import com.mangafetcher.downloader.domain.model.MangaMetadata
import org.jsoup.Jsoup

/**
 * Pure HTML parser with no I/O operations.
 * All methods take HTML strings as input and return parsed data.
 * Fully testable with HTML fixtures.
 */
class HtmlParser {
    /**
     * Parses search results HTML and extracts manga listings.
     */
    fun parseSearchResults(html: String): List<MangaResult> {
        val doc = Jsoup.parse(html)
        return doc.select(".tab-summary .post-title a, .c-tabs-item__content .post-title a").map { element ->
            val mangaTitle = element.text().trim()
            val href = element.attr("href").removeSuffix("/")
            val id = href.substringAfterLast("/")
            MangaResult(mangaTitle, id)
        }
    }

    /**
     * Parses chapter list HTML and extracts chapter information.
     */
    fun parseChapters(html: String): List<ChapterResult> {
        val doc = Jsoup.parse(html)
        return doc.select(".chapter-box a, li.wp-manga-chapter a").map { element ->
            val number = element.text().trim()
            val href = element.attr("href").removeSuffix("/")
            val id = href.substringAfterLast("/")

            // Attempt to find volume info in the parent or siblings
            val parent = element.parent()
            val volume =
                parent
                    ?.select(".vol, .volume")
                    ?.first()
                    ?.text()
                    ?.trim()

            ChapterResult(number, id, volume)
        }
    }

    /**
     * Parses manga details page and extracts full information.
     */
    fun parseMangaDetails(html: String): MangaDetails {
        val doc = Jsoup.parse(html)

        val title =
            doc
                .select(".post-title h1, .post-title h3")
                .first()
                ?.text()
                ?.trim() ?: ""
        val authors = doc.select(".author-content a").joinToString(", ") { it.text().trim() }
        val artists = doc.select(".artist-content a").joinToString(", ") { it.text().trim() }
        val description = doc.select(".description-summary, .manga-excerpt").text().trim()
        val tags = doc.select(".genres-content a").joinToString(", ") { it.text().trim() }
        val coverUrl =
            doc.select(".summary_image img").attr("data-src").ifEmpty {
                doc.select(".summary_image img").attr("src")
            }

        return MangaDetails(title, authors, artists, description, tags, coverUrl)
    }

    /**
     * Parses manga metadata from manga page HTML.
     */
    fun parseMangaMetadata(
        html: String,
        baseUrl: String,
        mangaId: String,
    ): MangaMetadata? {
        val doc = Jsoup.parse(html)

        val series = doc.select(".post-title h1").text().trim()
        if (series.isEmpty()) return null

        val writer = doc.select(".author-content a").joinToString(", ") { it.text().trim() }
        val penciller = doc.select(".artist-content a").joinToString(", ") { it.text().trim() }
        val genres = doc.select(".genres-content a").joinToString(",") { it.text().trim() }
        val summary = doc.select(".description-summary, .summary__content").text().trim()
        val alternate = doc.select(".post-content_item:contains(Alternative) .summary-content").text().trim()

        return MangaMetadata(
            series = series,
            writer = if (writer.isNotEmpty()) writer else null,
            penciller = if (penciller.isNotEmpty()) penciller else null,
            genre = if (genres.isNotEmpty()) genres else null,
            summary = if (summary.isNotEmpty()) summary else null,
            alternateSeries = if (alternate.isNotEmpty()) alternate else null,
            web = "$baseUrl/manga/$mangaId/",
        )
    }

    /**
     * Extracts image URLs from a chapter page HTML.
     */
    fun extractImageUrls(html: String): List<String> {
        val doc = Jsoup.parse(html)
        val imageElements = doc.select(".reading-content img, .page-break img, img.wp-manga-chapter-img")

        return imageElements.mapNotNull { element ->
            var imgUrl = element.attr("data-src").trim()
            if (imgUrl.isEmpty()) imgUrl = element.attr("src").trim()
            if (imgUrl.isEmpty()) null else imgUrl
        }
    }
}
