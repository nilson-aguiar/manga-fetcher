package com.mangafetcher.downloader.infrastructure.scraper

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.LoadState

/**
 * Manages Playwright browser lifecycle and provides page fetching capabilities.
 * Handles browser initialization, page navigation, and resource cleanup.
 */
class PlaywrightClient : AutoCloseable {
    private val playwright: Playwright = Playwright.create()
    private val browser: Browser =
        playwright.chromium().launch(
            BrowserType.LaunchOptions()
                .setHeadless(true)
                .setArgs(
                    listOf(
                        "--no-sandbox",
                        "--disable-setuid-sandbox",
                        "--disable-dev-shm-usage",
                        "--disable-gpu",
                    ),
                ),
        )
    private val context =
        browser.newContext(
            Browser.NewContextOptions().setUserAgent(
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36",
            ),
        )

    /**
     * Fetches the HTML content of a page at the given URL.
     * Creates a new page, navigates to the URL, waits for DOM to load, and returns the HTML.
     */
    fun fetchPage(url: String): String {
        val page = context.newPage()
        try {
            page.navigate(url)
            page.waitForLoadState(LoadState.DOMCONTENTLOADED)
            return page.content()
        } finally {
            page.close()
        }
    }

    /**
     * Downloads a file from the given URL and returns the bytes.
     */
    fun downloadFile(url: String): ByteArray? {
        val page = context.newPage()
        try {
            val response = page.request().get(url)
            return if (response.ok()) response.body() else null
        } finally {
            page.close()
        }
    }

    /**
     * Fetches images from a chapter page with lazy loading support.
     * Scrolls to trigger lazy loading and waits for network to be idle.
     */
    fun fetchChapterPageWithImages(url: String): String {
        val page = context.newPage()
        try {
            page.navigate(url)
            page.waitForLoadState(LoadState.DOMCONTENTLOADED)

            try {
                page.waitForSelector(
                    ".reading-content img, .page-break img, img.wp-manga-chapter-img",
                    Page.WaitForSelectorOptions().setTimeout(5000.0),
                )
            } catch (e: Exception) {
                // Ignore timeout - some pages might not have images
            }

            // Scroll to trigger lazy loading
            page.evaluate("window.scrollTo(0, document.body.scrollHeight)")
            page.waitForLoadState(LoadState.NETWORKIDLE)

            return page.content()
        } finally {
            page.close()
        }
    }

    /**
     * Downloads an image using Playwright's request API.
     */
    fun downloadImage(url: String): ByteArray? {
        val page = context.newPage()
        try {
            val response = page.request().get(url)
            return if (response.ok()) response.body() else null
        } catch (e: Exception) {
            return null
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
