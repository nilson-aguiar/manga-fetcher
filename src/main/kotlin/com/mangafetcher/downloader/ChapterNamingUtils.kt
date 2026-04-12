package com.mangafetcher.downloader

import java.io.File

object ChapterNamingUtils {
    fun getChapterLabel(chapterNumber: String): String {
        return if (chapterNumber.startsWith("Capítulo ")) {
            chapterNumber.substringAfter("Capítulo ")
        } else {
            chapterNumber
        }
    }

    fun getFileName(chapterNumber: String, volume: String? = null): String {
        val label = getChapterLabel(chapterNumber)
        return if (volume != null) {
            "$volume Ch. $label.cbz"
        } else {
            "Ch. $label.cbz"
        }
    }

    fun findExistingFile(
        outputDir: File,
        chapterNumber: String,
        mangaId: String? = null,
        chapterId: String? = null,
    ): File? {
        val label = getChapterLabel(chapterNumber)
        val files = outputDir.listFiles() ?: return null

        // Match new format: Ch. X.cbz OR Vol. Y Ch. X.cbz
        val newFormatMatch = files.find { it.name.endsWith("Ch. $label.cbz") }
        if (newFormatMatch != null) return newFormatMatch

        // Match old format: mangaId-chapterId.cbz
        if (mangaId != null && chapterId != null) {
            val oldFormatMatch = files.find { it.name == "$mangaId-$chapterId.cbz" }
            if (oldFormatMatch != null) return oldFormatMatch
        }

        return null
    }

    fun ensureCorrectNaming(
        outputDir: File,
        mangaId: String,
        chapterId: String,
        chapterNumber: String,
        volume: String?,
    ): Boolean {
        val existingFile = findExistingFile(outputDir, chapterNumber, mangaId, chapterId) ?: return false
        val correctName = getFileName(chapterNumber, volume)

        if (existingFile.name == correctName) return false

        val newFile = File(outputDir, correctName)
        // Ensure we don't overwrite if by some chance both formats exist (unlikely)
        if (newFile.exists()) return false

        return existingFile.renameTo(newFile)
    }
}
