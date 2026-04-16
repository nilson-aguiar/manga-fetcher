package com.mangafetcher.downloader.infrastructure.conversion

import com.mangafetcher.downloader.domain.model.MangaMetadata
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

object ComicInfoGenerator {
    fun generate(metadata: MangaMetadata): String {
        val dbf = DocumentBuilderFactory.newInstance()
        val db = dbf.newDocumentBuilder()
        val doc = db.newDocument()

        val root = doc.createElement("ComicInfo")
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
        root.setAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema")
        doc.appendChild(root)

        fun appendTag(
            name: String,
            value: String?,
        ) {
            if (!value.isNullOrEmpty()) {
                val element = doc.createElement(name)
                element.appendChild(doc.createTextNode(value))
                root.appendChild(element)
            }
        }

        appendTag("Series", metadata.series)
        appendTag("Writer", metadata.writer)
        appendTag("Penciller", metadata.penciller)
        appendTag("Genre", metadata.genre)
        appendTag("Summary", metadata.summary)
        appendTag("AlternateSeries", metadata.alternateSeries)
        appendTag("Volume", metadata.volume)
        appendTag("LanguageISO", metadata.languageIso)
        appendTag("Number", metadata.number)
        appendTag("Title", metadata.title)
        appendTag("ScanInformation", metadata.scanInformation)
        metadata.pageCount?.let { appendTag("PageCount", it.toString()) }
        appendTag("Web", metadata.web)

        val transformerFactory = TransformerFactory.newInstance()
        val transformer = transformerFactory.newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")

        val sw = StringWriter()
        transformer.transform(DOMSource(doc), StreamResult(sw))
        return sw.toString().trim()
    }
}
