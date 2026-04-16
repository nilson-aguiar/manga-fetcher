package com.mangafetcher.downloader.domain.model

data class MangaMetadata(
    val series: String,
    val writer: String? = null,
    val penciller: String? = null,
    val genre: String? = null,
    val summary: String? = null,
    val alternateSeries: String? = null,
    val volume: String? = null,
    val languageIso: String? = "pt-br",
    val number: String? = null,
    val title: String? = null,
    val scanInformation: String? = null,
    val pageCount: Int? = null,
    val web: String? = null,
)

interface MangaMetadataProvider {
    fun getMetadata(
        title: String,
        chapter: String? = null,
        volume: String? = null,
    ): MangaMetadata?
}
