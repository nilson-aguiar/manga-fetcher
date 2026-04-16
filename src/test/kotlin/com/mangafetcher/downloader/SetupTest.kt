package com.mangafetcher.downloader

import com.mangafetcher.downloader.cli.DownloaderApplication
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test
import picocli.CommandLine
import kotlin.test.assertNotNull

class SetupTest {
    @Test
    fun `should have picocli in classpath`() {
        val cmd = CommandLine(DownloaderApplication())
        assertNotNull(cmd)
    }

    @Test
    fun `should have okhttp in classpath`() {
        val client = OkHttpClient()
        assertNotNull(client)
    }
}
