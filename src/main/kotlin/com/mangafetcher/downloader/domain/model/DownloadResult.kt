package com.mangafetcher.downloader.domain.model

data class DownloadResult(
    val successCount: Int,
    val skippedCount: Int,
    val failedCount: Int,
)
