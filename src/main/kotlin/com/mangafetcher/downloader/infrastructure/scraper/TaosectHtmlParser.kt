package com.mangafetcher.downloader.infrastructure.scraper

import com.mangafetcher.downloader.domain.model.MangaMetadata
import org.jsoup.Jsoup

/**
 * HTML parser for Taosect manga site.
 * Parses search results, chapter listings, manga details, and chapter images.
 */
class TaosectHtmlParser {
    /**
     * Parses search results HTML and extracts manga listings.
     * Taosect uses WordPress-based manga theme with standard post listings.
     */
    fun parseSearchResults(html: String): List<MangaResult> {
        val doc = Jsoup.parse(html)
        // Search results typically use post-title links
        return doc.select(".post-title a, .projeto-titulo a").map { element ->
            val mangaTitle = element.text().trim()
            val href = element.attr("href").removeSuffix("/")
            // Extract manga ID from URLs like /manga/one-punch-man/ or /projeto/one-punch-man/
            val id = href.substringAfterLast("/")
            MangaResult(mangaTitle, id)
        }
    }

    /**
     * Parses chapter list HTML and extracts chapter information.
     * Chapters are in table rows with links to the reader.
     */
    fun parseChapters(html: String): List<ChapterResult> {
        val doc = Jsoup.parse(html)
        // Chapter links are in the format: /leitor-online/projeto/{manga-id}/cap-tulo-{number}/
        val chapters = mutableListOf<ChapterResult>()

        doc.select("a[href*='/leitor-online/projeto/']").forEach { element ->
            val href = element.attr("href").removeSuffix("/")
            val chapterId = href.substringAfterLast("/")

            // Extract chapter number from the text or URL
            val text = element.text().trim()
            val number = when {
                text.contains("Capítulo", ignoreCase = true) -> text.substringAfter("Capítulo").trim()
                text.contains("Cap", ignoreCase = true) -> text.substringAfter("Cap").trim()
                chapterId.contains("cap-tulo-") -> chapterId.substringAfter("cap-tulo-").replace("-", ".")
                else -> text
            }

            if (chapterId.isNotEmpty() && number.isNotEmpty()) {
                chapters.add(ChapterResult(number, chapterId, null))
            }
        }

        return chapters.distinctBy { it.id }
    }

    /**
     * Parses manga details page and extracts full information.
     * Taosect uses a table-based layout for manga information.
     */
    fun parseMangaDetails(html: String): MangaDetails {
        val doc = Jsoup.parse(html)

        val title = doc.select("h1.titulo-projeto").text().trim()

        // Extract metadata from the info table
        val table = doc.select("table.tabela-projeto tr")
        var artists = ""
        var authors = ""
        var description = ""
        var tags = ""

        table.forEach { row ->
            val header = row.select("td strong").text().trim()
            val value = row.select("td").getOrNull(1)?.text()?.trim() ?: ""

            when {
                header.contains("Arte", ignoreCase = true) -> artists = value
                header.contains("Roteiro", ignoreCase = true) -> authors = value
            }
        }

        // Description is in a td with colspan=2 inside the table
        description = doc.select("table.tabela-projeto td[colspan='2'].tabela-projeto-conteudo p")
            .text().trim()

        // Tags/genres are links with class "link_genero"
        tags = doc.select("a.link_genero").joinToString(", ") { it.text().trim() }

        // Cover image - first volume cover
        val coverUrl = doc.select("img.imagem-volume-projeto").firstOrNull()?.attr("src") ?: ""

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

        val series = doc.select("h1.titulo-projeto").text().trim()
        if (series.isEmpty()) return null

        // Extract metadata from the info table
        val table = doc.select("table.tabela-projeto tr")
        var penciller = ""
        var writer = ""

        table.forEach { row ->
            val header = row.select("td strong").text().trim()
            val value = row.select("td").getOrNull(1)?.text()?.trim() ?: ""

            when {
                header.contains("Arte", ignoreCase = true) -> penciller = value
                header.contains("Roteiro", ignoreCase = true) -> writer = value
            }
        }

        val summary = doc.select("table.tabela-projeto td[colspan='2'].tabela-projeto-conteudo p")
            .text().trim()
        val genres = doc.select("a.link_genero").joinToString(",") { it.text().trim() }
        val alternate = doc.select("h3.titulo-original").text().trim()

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
     * Taosect uses img tags with class "pagina_capitulo" for chapter images.
     */
    fun extractImageUrls(html: String): List<String> {
        val doc = Jsoup.parse(html)
        // Taosect chapter images have class "pagina_capitulo"
        val imageElements = doc.select("img.pagina_capitulo")

        return imageElements.mapNotNull { element ->
            var imgUrl = element.attr("src").trim()
            if (imgUrl.isEmpty()) imgUrl = element.attr("data-src").trim()
            if (imgUrl.isEmpty()) null else imgUrl
        }
    }
}
