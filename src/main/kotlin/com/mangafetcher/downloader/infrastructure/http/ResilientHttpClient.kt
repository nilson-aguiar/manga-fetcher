package com.mangafetcher.downloader.infrastructure.http

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

data class DownloadedFileInfo(
    val file: File,
    val finalUrl: String,
    val contentDisposition: String? = null,
    val contentType: String? = null,
)

class ResilientHttpClient(
    private val maxRetries: Int = 3,
    private val rateLimitMs: Long = 0,
    private val defaultUserAgent: String = "MangaFetcher/0.0.1",
) {
    companion object {
        const val BROWSER_USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"
    }

    private val client =
        OkHttpClient
            .Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()
    private var lastRequestTime = 0L

    fun get(
        url: String,
        headers: Map<String, String> = emptyMap(),
    ): String {
        var lastException: Exception? = null

        repeat(maxRetries) { attempt ->
            try {
                enforceRateLimit()

                val request = buildRequest(url, headers)

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        return response.body.string()
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

    fun getBytes(
        url: String,
        headers: Map<String, String> = emptyMap(),
    ): ByteArray {
        var lastException: Exception? = null

        repeat(maxRetries) { attempt ->
            try {
                enforceRateLimit()

                val request = buildRequest(url, headers)

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        return response.body.bytes()
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

    /**
     * Streams a remote file directly to [outputFile] without buffering the entire payload in memory.
     */
    fun downloadToFile(
        url: String,
        outputFile: File,
        headers: Map<String, String> = emptyMap(),
    ): DownloadedFileInfo {
        var lastException: Exception? = null

        repeat(maxRetries) { attempt ->
            try {
                enforceRateLimit()

                val request = buildRequest(url, headers)

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        outputFile.parentFile?.mkdirs()
                        response.body.byteStream().use { input ->
                            outputFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        return DownloadedFileInfo(
                            file = outputFile,
                            finalUrl = response.request.url.toString(),
                            contentDisposition = response.header("Content-Disposition"),
                            contentType = response.header("Content-Type"),
                        )
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

        throw lastException ?: IOException("Failed to download $url after $maxRetries attempts")
    }

    private fun buildRequest(
        url: String,
        headers: Map<String, String>,
    ): Request {
        val builder =
            Request
                .Builder()
                .url(url)
                .header("User-Agent", defaultUserAgent)

        headers.forEach { (name, value) ->
            builder.header(name, value)
        }
        return builder.build()
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
