package com.mangafetcher.downloader

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.LoadState
import org.jsoup.Jsoup

data class MangaResult(
    val title: String,
    val id: String,
)

data class ChapterResult(
    val number: String,
    val id: String,
)

class MangaLivreScraper(
    private val baseUrl: String = "https://mangalivre.to",
) : AutoCloseable {
    private val playwright: Playwright = Playwright.create()
    private val browser: Browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(true))
    private val context =
        browser.newContext(
            Browser
                .NewContextOptions()
                .setUserAgent(
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36",
                ),
        )

    private fun getPageContent(url: String): String {
        val page = context.newPage()
        try {
            page.navigate(url)
            page.waitForLoadState(LoadState.DOMCONTENTLOADED)
            return page.content()
        } finally {
            page.close()
        }
    }

    fun search(title: String): List<MangaResult> {
        val html = getPageContent("$baseUrl/?s=$title&post_type=wp-manga")
        return parseSearchResults(html)
    }

    fun parseSearchResults(html: String): List<MangaResult> {
        val doc = Jsoup.parse(html)
        return doc.select(".tab-summary .post-title a, .c-tabs-item__content .post-title a").map { element ->
            val mangaTitle = element.text().trim()
            val href = element.attr("href").removeSuffix("/")
            val id = href.substringAfterLast("/")
            MangaResult(mangaTitle, id)
        }
    }

    fun fetchChapters(mangaId: String): List<ChapterResult> {
        val html = getPageContent("$baseUrl/manga/$mangaId/")
        return parseChapters(html)
    }

    fun parseChapters(html: String): List<ChapterResult> {
        val doc = Jsoup.parse(html)
        return doc.select(".chapter-box a, li.wp-manga-chapter a").map { element ->
            val number = element.text().trim()
            val href = element.attr("href").removeSuffix("/")
            val id = href.substringAfterLast("/")
            ChapterResult(number, id)
        }
    }

    fun downloadImages(
        mangaId: String,
        chapterId: String,
        outputDir: java.io.File,
    ): List<java.io.File> {
        val page = context.newPage()
        try {
            val url = "$baseUrl/manga/$mangaId/$chapterId/"
            page.navigate(url)
            page.waitForLoadState(LoadState.DOMCONTENTLOADED)

            try {
                page.waitForSelector(
                    ".reading-content img, .page-break img, img.wp-manga-chapter-img",
                    Page.WaitForSelectorOptions().setTimeout(5000.0),
                )
            } catch (e: Exception) {
                // Ignore
            }

            // Scroll down to trigger lazy loading if any
            page.evaluate("window.scrollTo(0, document.body.scrollHeight)")
            page.waitForLoadState(LoadState.NETWORKIDLE)

            val html = page.content()
            val doc = Jsoup.parse(html)

            outputDir.mkdirs()

            val imageElements = doc.select(".reading-content img, .page-break img, img.wp-manga-chapter-img")

            return imageElements
                .mapIndexed { index, element ->
                    var imgUrl = element.attr("data-src").trim()
                    if (imgUrl.isEmpty()) imgUrl = element.attr("src").trim()
                    if (imgUrl.isEmpty()) return@mapIndexed null

                    try {
                        val response = page.request().get(imgUrl)
                        if (response.ok()) {
                            val bytes = response.body()
                            val extension = imgUrl.substringAfterLast(".", "jpg").substringBefore("?")
                            val file = java.io.File(outputDir, "%03d.$extension".format(index + 1))
                            file.writeBytes(bytes)
                            file
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null
                    }
                }.filterNotNull()
        } finally {
            page.close()
        }
    }

    override fun close() {
        context.close()
        browser.close()
        playwright.close()
    }
}
