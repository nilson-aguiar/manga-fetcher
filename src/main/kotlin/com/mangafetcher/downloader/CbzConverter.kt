package com.mangafetcher.downloader

import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class CbzConverter {
    fun convert(images: List<File>, outputFile: File) {
        outputFile.parentFile?.mkdirs()
        ZipOutputStream(FileOutputStream(outputFile)).use { zipOut ->
            images.forEach { image ->
                val entry = ZipEntry(image.name)
                zipOut.putNextEntry(entry)
                image.inputStream().use { input ->
                    input.copyTo(zipOut)
                }
                zipOut.closeEntry()
            }
        }
    }
}
