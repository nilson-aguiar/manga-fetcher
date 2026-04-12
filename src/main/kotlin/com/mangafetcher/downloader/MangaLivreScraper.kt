package com.mangafetcher.downloader

import org.jsoup.Jsoup

data class MangaResult(val title: String, val id: String)
data class ChapterResult(val number: String, val id: String)

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

    fun fetchChapters(mangaId: String): List<ChapterResult> {
        val url = "$baseUrl/manga/$mangaId"
        val html = client.get(url)
        val doc = Jsoup.parse(html)
        
        return doc.select(".chapterList li a").map { element ->
            val number = element.select(".chapter-number").text()
            val href = element.attr("href")
            val id = href.substringAfterLast("/")
            ChapterResult(number, id)
        }
    }

    fun downloadImages(mangaId: String, chapterId: String, outputDir: java.io.File): List<java.io.File> {
        val url = "$baseUrl/manga/$mangaId/$chapterId"
        val html = client.get(url)
        val doc = Jsoup.parse(html)
        
        outputDir.mkdirs()
        
        return doc.select(".manga-pages img").mapIndexed { index, element ->
            val imgUrl = element.attr("src")
            val bytes = client.getBytes(imgUrl)
            val file = java.io.File(outputDir, "%02d.jpg".format(index + 1))
            file.writeBytes(bytes)
            file
        }
    }
}
