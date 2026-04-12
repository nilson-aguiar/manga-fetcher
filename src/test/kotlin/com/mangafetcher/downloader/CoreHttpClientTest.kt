package com.mangafetcher.downloader

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoreHttpClientTest {
    private val server = MockWebServer()

    @BeforeEach
    fun setUp() {
        server.start()
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `should fetch content from server`() {
        server.enqueue(MockResponse().setBody("hello world"))

        val client = CoreHttpClient()
        val response = client.get(server.url("/").toString())

        assertEquals("hello world", response)
    }

    @Test
    fun `should fetch binary content from server`() {
        val data = byteArrayOf(1, 2, 3, 4)
        server.enqueue(MockResponse().setBody(okio.Buffer().write(data)))

        val client = CoreHttpClient()
        val response = client.getBytes(server.url("/").toString())

        assertTrue(data.contentEquals(response))
    }

    @Test
    fun `should retry on 5xx error`() {
        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(MockResponse().setBody("success after retry"))

        val client = CoreHttpClient(maxRetries = 3)
        val response = client.get(server.url("/").toString())

        assertEquals("success after retry", response)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `should respect rate limit`() {
        server.enqueue(MockResponse().setBody("1"))
        server.enqueue(MockResponse().setBody("2"))

        val rateLimitMs = 100L
        val client = CoreHttpClient(rateLimitMs = rateLimitMs)

        val start = System.currentTimeMillis()
        client.get(server.url("/1").toString())
        client.get(server.url("/2").toString())
        val end = System.currentTimeMillis()

        assertTrue(end - start >= rateLimitMs, "Should have delayed for at least $rateLimitMs ms")
    }
}
