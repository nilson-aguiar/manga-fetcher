package com.mangafetcher.downloader.infrastructure.download

import net.sf.sevenzipjbinding.ExtractOperationResult
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.util.zip.ZipFile

/**
 * Extracts comic/manga page images from archive files (`.cbz`, `.zip`, `.cbr`, `.rar`, `.7z`).
 *
 * - Uses standard `java.util.zip.ZipFile` for ZIP/CBZ archives.
 * - Uses `SevenZipJBinding` (`RandomAccessFileInStream`) for RAR4, RAR5 (`.cbr`/`.rar`), and `.7z` archives.
 * - Sorts image entries in natural numeric reading order and outputs sequential filenames (`001.jpg`, `002.jpg`, ...)
 *   into the target directory to avoid collisions from nested folders.
 */
class ArchiveExtractor {
    private val logger = LoggerFactory.getLogger(ArchiveExtractor::class.java)

    companion object {
        private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "gif", "bmp")
        private val IGNORED_PATTERNS = listOf("__macosx", ".ds_store", "thumbs.db", "comicinfo.xml")
        private val NATURAL_TOKEN_REGEX = Regex("""(\d+)|(\D+)""")

        val NATURAL_PATH_COMPARATOR: Comparator<String> =
            Comparator { a, b ->
                compareNatural(a, b)
            }

        internal fun compareNatural(
            a: String,
            b: String,
        ): Int {
            val tokensA = NATURAL_TOKEN_REGEX.findAll(a.lowercase()).map { it.value }.toList()
            val tokensB = NATURAL_TOKEN_REGEX.findAll(b.lowercase()).map { it.value }.toList()
            val minSize = minOf(tokensA.size, tokensB.size)

            for (i in 0 until minSize) {
                val tA = tokensA[i]
                val tB = tokensB[i]
                if (tA == tB) continue

                val numA = tA.toBigIntegerOrNull()
                val numB = tB.toBigIntegerOrNull()
                val cmp =
                    if (numA != null && numB != null) {
                        val numCmp = numA.compareTo(numB)
                        if (numCmp != 0) numCmp else tA.length.compareTo(tB.length)
                    } else {
                        tA.compareTo(tB)
                    }
                if (cmp != 0) return cmp
            }
            return tokensA.size.compareTo(tokensB.size)
        }
    }

    enum class ArchiveType {
        ZIP,
        RAR_OR_7Z,
        PDF,
        UNKNOWN,
    }

    /**
     * Detects archive format using file magic bytes first, falling back to file extension.
     */
    fun detectArchiveType(
        archiveFile: File,
        fileNameHint: String? = null,
    ): ArchiveType {
        if (archiveFile.exists() && archiveFile.length() >= 4) {
            val header = ByteArray(8)
            val read = archiveFile.inputStream().use { it.read(header) }
            if (read >= 4) {
                // ZIP: PK\x03\x04
                if (header[0] == 0x50.toByte() &&
                    header[1] == 0x4B.toByte() &&
                    header[2] == 0x03.toByte() &&
                    header[3] == 0x04.toByte()
                ) {
                    return ArchiveType.ZIP
                }
                // RAR4 / RAR5: Rar!\x1A\x07
                if (read >= 6 &&
                    header[0] == 0x52.toByte() &&
                    header[1] == 0x61.toByte() &&
                    header[2] == 0x72.toByte() &&
                    header[3] == 0x21.toByte() &&
                    header[4] == 0x1A.toByte() &&
                    header[5] == 0x07.toByte()
                ) {
                    return ArchiveType.RAR_OR_7Z
                }
                // 7z: 37 7A BC AF 27 1C
                if (read >= 6 &&
                    header[0] == 0x37.toByte() &&
                    header[1] == 0x7A.toByte() &&
                    header[2] == 0xBC.toByte() &&
                    header[3] == 0xAF.toByte() &&
                    header[4] == 0x27.toByte() &&
                    header[5] == 0x1C.toByte()
                ) {
                    return ArchiveType.RAR_OR_7Z
                }
                // PDF: %PDF
                if (header[0] == 0x25.toByte() &&
                    header[1] == 0x50.toByte() &&
                    header[2] == 0x44.toByte() &&
                    header[3] == 0x46.toByte()
                ) {
                    return ArchiveType.PDF
                }
            }
        }

        val nameToInspect = (fileNameHint ?: archiveFile.name).lowercase()
        return when {
            nameToInspect.endsWith(".cbz") || nameToInspect.endsWith(".zip") -> ArchiveType.ZIP
            nameToInspect.endsWith(".cbr") || nameToInspect.endsWith(".rar") || nameToInspect.endsWith(".7z") -> ArchiveType.RAR_OR_7Z
            nameToInspect.endsWith(".pdf") -> ArchiveType.PDF
            else -> ArchiveType.UNKNOWN
        }
    }

    /**
     * Extracts image files from [archiveFile] into [outputDir] in natural sorted order.
     */
    fun extractImages(
        archiveFile: File,
        outputDir: File,
        fileNameHint: String? = null,
        startIndex: Int = 1,
    ): List<File> {
        outputDir.mkdirs()

        return when (val type = detectArchiveType(archiveFile, fileNameHint)) {
            ArchiveType.ZIP -> {
                extractFromZip(archiveFile, outputDir, startIndex)
            }

            ArchiveType.RAR_OR_7Z -> {
                extractWithSevenZip(archiveFile, outputDir, startIndex)
            }

            ArchiveType.PDF -> {
                logger.warn("PDF archives are not supported for CBZ image extraction: {}", fileNameHint ?: archiveFile.name)
                emptyList()
            }

            ArchiveType.UNKNOWN -> {
                logger.debug("Unknown archive magic bytes for {}, attempting 7-Zip extraction", fileNameHint ?: archiveFile.name)
                extractWithSevenZip(archiveFile, outputDir, startIndex)
            }
        }
    }

    private fun extractFromZip(
        archiveFile: File,
        outputDir: File,
        startIndex: Int,
    ): List<File> {
        ZipFile(archiveFile).use { zip ->
            val imageEntries =
                zip
                    .entries()
                    .asSequence()
                    .filter { !it.isDirectory && isSupportedImageEntry(it.name) }
                    .sortedWith(compareBy(NATURAL_PATH_COMPARATOR) { it.name })
                    .toList()

            val extractedFiles = mutableListOf<File>()
            for ((offset, entry) in imageEntries.withIndex()) {
                val extension = entry.name.substringAfterLast('.', "jpg").lowercase()
                val outFile = File(outputDir, "%03d.%s".format(startIndex + offset, extension))
                zip.getInputStream(entry).use { input ->
                    outFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                extractedFiles.add(outFile)
            }
            logger.info("Extracted {} images from ZIP/CBZ archive {}", extractedFiles.size, archiveFile.name)
            return extractedFiles
        }
    }

    private fun extractWithSevenZip(
        archiveFile: File,
        outputDir: File,
        startIndex: Int,
    ): List<File> {
        RandomAccessFile(archiveFile, "r").use { raf ->
            RandomAccessFileInStream(raf).use { inStream ->
                val inArchive = SevenZip.openInArchive(null, inStream)
                try {
                    val simpleInterface = inArchive.simpleInterface
                    val imageItems =
                        simpleInterface.archiveItems
                            .filter { !it.isFolder && isSupportedImageEntry(it.path) }
                            .sortedWith(compareBy(NATURAL_PATH_COMPARATOR) { it.path })

                    val extractedFiles = mutableListOf<File>()
                    for ((offset, item) in imageItems.withIndex()) {
                        val extension = item.path.substringAfterLast('.', "jpg").lowercase()
                        val outFile = File(outputDir, "%03d.%s".format(startIndex + offset, extension))
                        FileOutputStream(outFile).use { fos ->
                            val result =
                                item.extractSlow { data ->
                                    fos.write(data)
                                    data.size
                                }
                            if (result != ExtractOperationResult.OK) {
                                logger.warn("Failed to extract item {} from {}: {}", item.path, archiveFile.name, result)
                                outFile.delete()
                            } else {
                                extractedFiles.add(outFile)
                            }
                        }
                    }
                    logger.info("Extracted {} images from archive {}", extractedFiles.size, archiveFile.name)
                    return extractedFiles
                } finally {
                    try {
                        inArchive.close()
                    } catch (e: Exception) {
                        logger.debug("Error closing 7-Zip archive: {}", e.message)
                    }
                }
            }
        }
    }

    internal fun isSupportedImageEntry(entryPath: String): Boolean {
        val normalized = entryPath.replace('\\', '/').trim()
        if (normalized.isEmpty() || normalized.contains("..")) return false

        val lower = normalized.lowercase()
        if (IGNORED_PATTERNS.any { lower.contains(it) }) return false

        val fileName = lower.substringAfterLast('/')
        if (fileName.startsWith(".")) return false

        val ext = fileName.substringAfterLast('.', "")
        return ext in IMAGE_EXTENSIONS
    }
}
