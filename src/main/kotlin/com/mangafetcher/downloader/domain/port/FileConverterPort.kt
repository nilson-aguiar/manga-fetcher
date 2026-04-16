package com.mangafetcher.downloader.domain.port

import java.io.File

/**
 * Port interface for converting images to archive formats.
 * Implementations handle file format conversion (e.g., images to CBZ).
 */
interface FileConverterPort {
    /**
     * Converts a list of image files to CBZ format with optional metadata.
     */
    fun convertToCbz(
        images: List<File>,
        outputFile: File,
        metadataXml: String? = null,
    )
}
