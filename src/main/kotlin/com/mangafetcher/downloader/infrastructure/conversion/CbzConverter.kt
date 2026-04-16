package com.mangafetcher.downloader.infrastructure.conversion

import com.mangafetcher.downloader.domain.port.FileConverterPort
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class CbzConverter : FileConverterPort {
    // Backward compatibility wrapper
    fun convert(
        images: List<File>,
        outputFile: File,
        metadataXml: String? = null,
    ) = convertToCbz(images, outputFile, metadataXml)

    override fun convertToCbz(
        images: List<File>,
        outputFile: File,
        metadataXml: String?,
    ) {
        outputFile.parentFile?.mkdirs()
        ZipOutputStream(FileOutputStream(outputFile)).use { zipOut ->
            if (metadataXml != null) {
                val comicInfoEntry = ZipEntry("ComicInfo.xml")
                zipOut.putNextEntry(comicInfoEntry)
                zipOut.write(metadataXml.toByteArray())
                zipOut.closeEntry()
            }
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
