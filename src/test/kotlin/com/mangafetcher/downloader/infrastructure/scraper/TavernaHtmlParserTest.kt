package com.mangafetcher.downloader.infrastructure.scraper

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TavernaHtmlParserTest {
    private val parser = TavernaHtmlParser()

    private val sampleSearchHtml =
        """
        <!DOCTYPE html>
        <html>
        <body>
            <div class="elementor-widget-jet-listing-grid">
                <div class="jet-listing-grid jet-listing">
                    <div class="jet-listing-grid__items">
                        <div class="jet-listing-grid__item" data-post-id="86886">
                            <div class="jet-engine-listing-overlay-wrap" data-url="https://tavernadequadrinhos.com.br/quadrinho/claymore/">
                                <h2 class="elementor-heading-title">
                                    <a href="https://tavernadequadrinhos.com.br/quadrinho/claymore/">Claymore</a>
                                </h2>
                                <a class="jet-engine-listing-overlay-link" href="https://tavernadequadrinhos.com.br/quadrinho/claymore/"></a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <!-- Bottom recommendation loop grid that must be ignored -->
            <div class="elementor-widget-loop-grid">
                <div class="elementor-loop-container elementor-grid">
                    <div class="e-loop-item">
                        <h2 class="elementor-heading-title">
                            <a href="https://tavernadequadrinhos.com.br/quadrinho/mulher-aranha-v8/">Mulher-Aranha</a>
                        </h2>
                    </div>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()

    private val sampleClaymoreHtml =
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta property="og:title" content="Claymore - Taverna de Quadrinhos" />
            <meta property="og:description" content="Short OG description" />
            <meta property="og:image" content="https://tavernadequadrinhos.com.br/wp-content/uploads/Claymore-27.jpg" />
        </head>
        <body>
            <div class="elementor-element e-con">
                <div class="elementor-widget-heading">
                    <h1 class="elementor-heading-title">Claymore</h1>
                </div>
                <div class="elementor-widget-text-editor">
                    A história se passa num mundo medieval aterrorizado por “Yomas”. Clare passa por diversos desafios.
                </div>
            </div>

            <!-- Chapter listing grid -->
            <div class="jet-listing-grid__items">
                <div class="jet-listing-grid__item">
                    <div class="jet-listing-dynamic-image">
                        <img alt="Claymore #2" data-src="https://tavernadequadrinhos.com.br/wp-content/uploads/Claymore-2.jpg" />
                    </div>
                    <div class="elementor-widget-button">
                        <a class="elementor-button elementor-button-link" href="https://www.mediafire.com/file/mb2duxwx097s9gc/Claymore_%25232.cbr/file">
                            <span class="elementor-button-text">Baixar</span>
                        </a>
                    </div>
                    <div class="jet-listing-dynamic-field__content">Claymore #2</div>
                </div>
                <div class="jet-listing-grid__item">
                    <div class="jet-listing-dynamic-image">
                        <img alt="Claymore #1" data-src="https://tavernadequadrinhos.com.br/wp-content/uploads/Claymore-1.jpg" />
                    </div>
                    <div class="elementor-widget-button">
                        <a class="elementor-button elementor-button-link" href="https://www.mediafire.com/file/pb9varqib6cuj6n/Claymore_%25231.cbr/file">
                            <span class="elementor-button-text">Baixar</span>
                        </a>
                    </div>
                    <div class="jet-listing-dynamic-field__content">Claymore #1</div>
                </div>
                <div class="jet-listing-grid__item">
                    <div class="elementor-widget-button">
                        <a class="elementor-button elementor-button-link" href="https://www.mediafire.com/file/un9l32wlumjoqg4/Claymore_vol._26.cbr/file">
                            <span class="elementor-button-text">Baixar</span>
                        </a>
                    </div>
                    <div class="jet-listing-dynamic-field__content">Claymore #26</div>
                </div>
            </div>

            <!-- Metadata sections -->
            <div class="elementor-element e-con-full e-flex e-con e-child">
                <div class="elementor-widget-heading">
                    <h1 class="elementor-heading-title">Tags</h1>
                </div>
                <div class="elementor-widget-post-info">
                    <span class="elementor-post-info__terms-list">
                        <a class="elementor-post-info__terms-list-item" href="https://tavernadequadrinhos.com.br/_tags/claymore/">Claymore</a>,
                        <a class="elementor-post-info__terms-list-item" href="https://tavernadequadrinhos.com.br/_tags/mangas/">Mangás</a>
                    </span>
                </div>
            </div>

            <div class="elementor-element e-con-full e-flex e-con e-child">
                <div class="elementor-widget-heading">
                    <h1 class="elementor-heading-title">Nome Original</h1>
                </div>
                <div class="elementor-widget-heading">
                    <h2 class="elementor-heading-title">Kureimoa</h2>
                </div>
            </div>

            <div class="elementor-element e-con-full e-flex e-con e-child">
                <div class="elementor-widget-heading">
                    <h1 class="elementor-heading-title">Escritor</h1>
                </div>
                <div class="elementor-widget-post-info">
                    <span class="elementor-post-info__terms-list">
                        <a class="elementor-post-info__terms-list-item" href="https://tavernadequadrinhos.com.br/_escritores/norihiro-yagi/">Norihiro Yagi</a>
                    </span>
                </div>
            </div>

            <div class="elementor-element e-con-full e-flex e-con e-child">
                <div class="elementor-widget-heading">
                    <h1 class="elementor-heading-title">Artista</h1>
                </div>
                <div class="elementor-widget-post-info">
                    <span class="elementor-post-info__terms-list">
                        <a class="elementor-post-info__terms-list-item" href="https://tavernadequadrinhos.com.br/_artistas/norihiro-yagi/">Norihiro Yagi</a>
                    </span>
                </div>
            </div>

            <!-- Related comics grid at bottom with erroneous /_artistas/ link on Finalizada badge -->
            <div class="jet-listing-grid__items">
                <div class="jet-listing-grid__item">
                    <h2 class="elementor-heading-title">
                        <a href="https://tavernadequadrinhos.com.br/quadrinho/akira/">Akira</a>
                    </h2>
                    <h2 class="elementor-heading-title">
                        <a href="https://tavernadequadrinhos.com.br/_artistas/yoshihiro-togashi/">Finalizada</a>
                    </h2>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()

    @Test
    fun `should parse search results and ignore bottom recommendation loop grid`() {
        val results = parser.parseSearchResults(sampleSearchHtml)

        assertEquals(1, results.size)
        assertEquals("Claymore", results[0].title)
        assertEquals("claymore", results[0].id)
    }

    @Test
    fun `should parse chapters sorted numerically and extract MediaFire IDs`() {
        val chapters = parser.parseChapterLinks(sampleClaymoreHtml)

        assertEquals(3, chapters.size)
        assertEquals("1", chapters[0].number)
        assertEquals("pb9varqib6cuj6n", chapters[0].id)
        assertEquals("https://www.mediafire.com/file/pb9varqib6cuj6n/Claymore_%25231.cbr/file", chapters[0].downloadUrl)

        assertEquals("2", chapters[1].number)
        assertEquals("mb2duxwx097s9gc", chapters[1].id)

        assertEquals("26", chapters[2].number)
        assertEquals("un9l32wlumjoqg4", chapters[2].id)
    }

    @Test
    fun `should parse manga details without leaking recommendation grid artist links`() {
        val details = parser.parseMangaDetails(sampleClaymoreHtml)

        assertEquals("Claymore", details.title)
        assertEquals("Norihiro Yagi", details.authors)
        assertEquals("Norihiro Yagi", details.artists)
        assertFalse(details.artists.contains("Finalizada"))
        assertEquals("Claymore, Mangás", details.tags)
        assertTrue(details.description.contains("mundo medieval aterrorizado"))
        assertEquals("https://tavernadequadrinhos.com.br/wp-content/uploads/Claymore-27.jpg", details.coverUrl)
    }

    @Test
    fun `should parse manga metadata with pt-br language and original title`() {
        val metadata = parser.parseMangaMetadata(sampleClaymoreHtml, "https://tavernadequadrinhos.com.br", "claymore")

        assertNotNull(metadata)
        assertEquals("Claymore", metadata.series)
        assertEquals("Norihiro Yagi", metadata.writer)
        assertEquals("Norihiro Yagi", metadata.penciller)
        assertEquals("Claymore,Mangás", metadata.genre)
        assertEquals("Kureimoa", metadata.alternateSeries)
        assertEquals("pt-br", metadata.languageIso)
        assertEquals("https://tavernadequadrinhos.com.br/quadrinho/claymore/", metadata.web)
    }
}
