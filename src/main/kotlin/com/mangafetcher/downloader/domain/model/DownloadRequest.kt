package com.mangafetcher.downloader.domain.model

import java.io.File

data class DownloadRequest(
    val mangaId: String,
    val outputDir: File,
    val chapterNumber: String? = null,
    val fromChapter: String? = null,
    val withVolume: Boolean = false,
)
