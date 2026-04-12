package com.mangafetcher.downloader

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class CoreHttpClient(
    private val maxRetries: Int = 3,
    private val rateLimitMs: Long = 0,
) {
    private val client = OkHttpClient()
    private var lastRequestTime = 0L

    fun get(url: String): String {
        var lastException: Exception? = null

        repeat(maxRetries) { attempt ->
            try {
                enforceRateLimit()

                val request =
                    Request
                        .Builder()
                        .url(url)
                        .header("User-Agent", "MangaFetcher/0.0.1")
                        .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        return response.body?.string() ?: ""
                    }
                    if (response.code < 500) {
                        throw IOException("Unexpected code $response")
                    }
                    // For 5xx, we retry
                    lastException = IOException("Server error $response")
                }
            } catch (e: Exception) {
                lastException = e
            }

            // Wait before retry
            if (attempt < maxRetries - 1) {
                Thread.sleep(100L * (attempt + 1))
            }
        }

        throw lastException ?: IOException("Failed to fetch $url after $maxRetries attempts")
    }

    fun getBytes(url: String): ByteArray {
        var lastException: Exception? = null

        repeat(maxRetries) { attempt ->
            try {
                enforceRateLimit()

                val request =
                    Request
                        .Builder()
                        .url(url)
                        .header("User-Agent", "MangaFetcher/0.0.1")
                        .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        return response.body?.bytes() ?: ByteArray(0)
                    }
                    if (response.code < 500) {
                        throw IOException("Unexpected code $response")
                    }
                    lastException = IOException("Server error $response")
                }
            } catch (e: Exception) {
                lastException = e
            }

            if (attempt < maxRetries - 1) {
                Thread.sleep(100L * (attempt + 1))
            }
        }

        throw lastException ?: IOException("Failed to fetch $url after $maxRetries attempts")
    }

    private fun enforceRateLimit() {
        if (rateLimitMs > 0) {
            val now = System.currentTimeMillis()
            val elapsed = now - lastRequestTime
            if (elapsed < rateLimitMs) {
                Thread.sleep(rateLimitMs - elapsed)
            }
            lastRequestTime = System.currentTimeMillis()
        }
    }
}
