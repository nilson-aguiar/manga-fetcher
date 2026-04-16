package com.mangafetcher.downloader.infrastructure.conversion

import com.mangafetcher.downloader.domain.model.MangaMetadata
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.zip.ZipFile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ComicInfoTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun `test ComicInfo generation`() {
        val metadata =
            MangaMetadata(
                series = "One Punch-Man",
                writer = "ONE",
                penciller = "Murata Yuusuke",
                genre = "Action,Comedy",
                summary = "Saitama is a hero for fun.",
                volume = "27",
                number = "136",
                title = "Persistence",
                scanInformation = "Tao Sect",
                pageCount = 37,
                web = "https://mangadex.org/chapter/123",
            )

        val xml = ComicInfoGenerator.generate(metadata)

        assertTrue(xml.contains("<Series>One Punch-Man</Series>"))
        assertTrue(xml.contains("<Writer>ONE</Writer>"))
        assertTrue(xml.contains("<Penciller>Murata Yuusuke</Penciller>"))
        assertTrue(xml.contains("<Volume>27</Volume>"))
        assertTrue(xml.contains("<Number>136</Number>"))
        assertTrue(xml.contains("<Title>Persistence</Title>"))
        assertTrue(xml.contains("<PageCount>37</PageCount>"))
    }

    @Test
    fun `test CBZ injection`() {
        val converter = CbzConverter()
        val outputFile = File(tempDir, "test.cbz")
        val images = listOf(File(tempDir, "1.jpg").apply { writeText("image1") })
        val metadataXml = "<ComicInfo><Series>Test</Series></ComicInfo>"

        converter.convert(images, outputFile, metadataXml)

        assertTrue(outputFile.exists())
        ZipFile(outputFile).use { zip ->
            val entry = zip.getEntry("ComicInfo.xml")
            assertNotNull(entry)
            val content = zip.getInputStream(entry).bufferedReader().readText()
            assertEquals(metadataXml, content)

            assertNotNull(zip.getEntry("1.jpg"))
        }
    }
}
