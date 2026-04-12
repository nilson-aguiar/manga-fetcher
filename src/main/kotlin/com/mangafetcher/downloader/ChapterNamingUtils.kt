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

    fun findExistingFile(outputDir: File, chapterNumber: String): File? {
        val label = getChapterLabel(chapterNumber)
        val files = outputDir.listFiles() ?: return null
        return files.find { it.name.endsWith("Ch. $label.cbz") }
    }

    fun renameIfVolumeAvailable(outputDir: File, chapterNumber: String, volume: String?): Boolean {
        if (volume == null) return false
        val existingFile = findExistingFile(outputDir, chapterNumber) ?: return false
        
        val newName = getFileName(chapterNumber, volume)
        if (existingFile.name == newName) return false

        val newFile = File(outputDir, newName)
        return existingFile.renameTo(newFile)
    }
}
