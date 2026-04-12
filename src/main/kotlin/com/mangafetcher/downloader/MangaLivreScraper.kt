package com.mangafetcher.downloader

import org.jsoup.Jsoup

data class MangaResult(val title: String, val id: String)

class MangaLivreScraper(
    private val client: CoreHttpClient,
    private val baseUrl: String = "https://mangalivre.to"
) {
    fun search(title: String): List<MangaResult> {
        val url = "$baseUrl/search?q=$title"
        val html = client.get(url)
        val doc = Jsoup.parse(html)
        
        return doc.select(".seriesList li a").map { element ->
            val mangaTitle = element.select(".series-title").text()
            val href = element.attr("href")
            val id = href.substringAfterLast("/")
            MangaResult(mangaTitle, id)
        }
    }
}
