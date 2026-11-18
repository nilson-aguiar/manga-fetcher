package com.mangafetcher.downloader.infrastructure.download

import org.junit.jupiter.api.Test
import java.util.Base64
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MediaFireHtmlParserTest {
    private val parser = MediaFireHtmlParser()

    @Test
    fun `should extract direct link from downloadButton href`() {
        val directLink = "https://download1350.mediafire.com/token123/pb9varqib6cuj6n/Claymore+%231.cbr"
        val html =
            """
            <html>
            <body>
                <a class="input popsok"
                   aria-label="Download file"
                   href="$directLink"
                   id="downloadButton"
                   rel="nofollow">
                    Download (7.04MB)
                </a>
            </body>
            </html>
            """.trimIndent()

        assertEquals(directLink, parser.extractDirectDownloadUrl(html))
    }

    @Test
    fun `should decode base64 data-scrambled-url from downloadButton`() {
        val directLink = "https://download1999.mediafire.com/xyz987/pb9varqib6cuj6n/Claymore+%231.cbr"
        val scrambled = Base64.getEncoder().encodeToString(directLink.toByteArray(Charsets.UTF_8))
        val html =
            """
            <html>
            <body>
                <a class="input popsok"
                   aria-label="Download file"
                   href="javascript:void(0)"
                   data-scrambled-url="$scrambled"
                   id="downloadButton">
                    Download
                </a>
            </body>
            </html>
            """.trimIndent()

        assertEquals(directLink, parser.extractDirectDownloadUrl(html))
    }

    @Test
    fun `should fallback to aria-label Download file when downloadButton id is absent`() {
        val directLink = "https://download1500.mediafire.com/abc/gmegccktvhvkcj9/Akira_1.cbr"
        val html =
            """
            <html>
            <body>
                <div id="download_link">
                    <a aria-label="Download file" href="$directLink">Download</a>
                </div>
            </body>
            </html>
            """.trimIndent()

        assertEquals(directLink, parser.extractDirectDownloadUrl(html))
    }

    @Test
    fun `should identify single file vs folder URLs and normalize http to https`() {
        val httpFileUrl = "http://www.mediafire.com/file/gmegccktvhvkcj9/file"
        val httpsFileUrl = "https://www.mediafire.com/file/pb9varqib6cuj6n/Claymore_%25231.cbr/file"
        val folderUrl = "https://www.mediafire.com/folder/abc123def456/Claymore"

        assertEquals("https://www.mediafire.com/file/gmegccktvhvkcj9/file", parser.normalizeUrl(httpFileUrl))
        assertTrue(parser.isSingleFileUrl(httpFileUrl))
        assertTrue(parser.isSingleFileUrl(httpsFileUrl))
        assertFalse(parser.isSingleFileUrl(folderUrl))
        assertTrue(parser.isFolderUrl(folderUrl))
        assertEquals("pb9varqib6cuj6n", parser.extractFileKey(httpsFileUrl))
        assertNull(parser.extractFileKey(folderUrl))
    }
}
