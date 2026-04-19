package com.mangafetcher.downloader.infrastructure.scraper

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.LoadState
import org.slf4j.LoggerFactory

/**
 * Manages Playwright browser lifecycle and provides page fetching capabilities.
 * Handles browser initialization, page navigation, and resource cleanup.
 */
class PlaywrightClient(
    useFirefox: Boolean? = null,
) : AutoCloseable {
    companion object {
        private val logger = LoggerFactory.getLogger(PlaywrightClient::class.java)
        
        // Singleton Playwright instance to avoid starting/stopping the driver process multiple times
        private val playwrightInstance: Playwright by lazy {
            logger.info("Creating singleton Playwright instance")
            Playwright.create()
        }
    }

    // Allow override via environment variable: PLAYWRIGHT_BROWSER=firefox
    private val shouldUseFirefox = useFirefox ?: (System.getenv("PLAYWRIGHT_BROWSER")?.lowercase() == "firefox")

    private val browser: Browser =
        if (shouldUseFirefox) {
            logger.info("Using Firefox browser")
            playwrightInstance.firefox().launch(
                BrowserType
                    .LaunchOptions()
                    .setHeadless(true)
                    .setTimeout(60000.0),
            )
        } else {
            logger.info("Using Chromium browser")
            playwrightInstance.chromium().launch(
                BrowserType
                    .LaunchOptions()
                    .setHeadless(true)
                    .setArgs(
                        listOf(
                            "--no-sandbox",
                            "--disable-setuid-sandbox",
                            "--disable-dev-shm-usage",
                            "--disable-gpu",
                            "--disable-software-rasterizer",
                            "--disable-extensions",
                            "--disable-background-timer-throttling",
                            "--disable-backgrounding-occluded-windows",
                            "--disable-renderer-backgrounding",
                            "--disable-blink-features=AutomationControlled",
                            "--disable-web-security",
                            "--disable-features=IsolateOrigins,site-per-process,VizDisplayCompositor",
                            // Stability improvements to prevent crashes
                            "--no-zygote",
                            "--disable-accelerated-2d-canvas",
                            "--disable-crash-reporter",
                            "--disable-site-isolation-trials",
                            "--disable-features=AudioServiceOutOfProcess",
                            "--js-flags=--max-old-space-size=512",
                            // Additional memory and performance settings
                            "--disable-canvas-aa",
                            "--disable-2d-canvas-clip-aa",
                            "--disable-gl-drawing-for-tests",
                        ),
                    ).setTimeout(60000.0),
            )
        }
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
        logger.debug("Navigating to: {}", url)
        val page = context.newPage()
        try {
            logger.debug("Created new page")
            val response = page.navigate(url)
            val status = response?.status() ?: -1
            println("DEBUG: Navigation to $url status code: $status")

            if (status != 200) {
                println("DEBUG: Response headers: ${response?.headers()}")
                // In case of error (like 403), still try to grab content to see if it's a Cloudflare challenge
                val errorContent = page.content()
                println("DEBUG: Error page content (first 500 chars): ${errorContent.take(500)}")
            }

            page.waitForLoadState(LoadState.DOMCONTENTLOADED)
            logger.debug("Page loaded successfully (DOMCONTENTLOADED)")

            // Extract content IMMEDIATELY after DOM loads, before any heavy JS can crash the page
            logger.debug("Extracting content immediately after DOM load...")
            val content =
                try {
                    page.content()
                } catch (e: Exception) {
                    logger.warn("Immediate page.content() failed: {}, trying evaluate", e.message)
                    page.evaluate("() => document.documentElement.outerHTML") as String
                }

            logger.debug("Content extracted, length: {} bytes", content.length)
            return content
        } catch (e: Exception) {
            logger.error("Failed to load page: {}", e.message, e)
            throw e
        } finally {
            logger.debug("Closing page...")
            try {
                page.close()
                logger.debug("Page closed successfully")
            } catch (e: Exception) {
                logger.warn("Error closing page: {}", e.message)
            }
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
        logger.debug("Loading chapter page with images: {}", url)
        val page = context.newPage()
        try {
            page.navigate(url)
            page.waitForLoadState(LoadState.DOMCONTENTLOADED)

            try {
                logger.debug("Waiting for images to load...")
                page.waitForSelector(
                    ".reading-content img, .page-break img, img.wp-manga-chapter-img",
                    Page.WaitForSelectorOptions().setTimeout(5000.0),
                )
            } catch (e: Exception) {
                // Ignore timeout - some pages might not have images
                logger.debug("Image selector timeout (may be normal)")
            }

            // Scroll to trigger lazy loading
            logger.debug("Scrolling to trigger lazy loading...")
            page.evaluate("window.scrollTo(0, document.body.scrollHeight)")
            page.waitForLoadState(LoadState.NETWORKIDLE)
            logger.debug("All images loaded")

            return page.content()
        } catch (e: Exception) {
            logger.error("Failed to load chapter page: {}", e.message)
            throw e
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
        // We DO NOT close playwrightInstance here because it's a shared singleton
    }
}
