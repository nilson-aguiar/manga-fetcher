package com.mangafetcher.downloader.infrastructure.metadata

import com.mangafetcher.downloader.domain.model.MangaMetadata
import com.mangafetcher.downloader.domain.model.MangaMetadataProvider
import com.mangafetcher.downloader.infrastructure.http.ResilientHttpClient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URLEncoder

class MangaDexMetadataProvider(
    private val client: ResilientHttpClient = ResilientHttpClient(rateLimitMs = 1000),
) : MangaMetadataProvider {
    private val baseUrl = "https://api.mangadex.org"

    override fun getMetadata(
        title: String,
        chapter: String?,
        volume: String?,
    ): MangaMetadata? {
        return try {
            val encodedTitle = URLEncoder.encode(title, "UTF-8")
            val url = "$baseUrl/manga?title=$encodedTitle&limit=1&includes[]=author&includes[]=artist"
            val response = client.get(url)
            val json = Json.parseToJsonElement(response).jsonObject
            val data = json["data"]?.jsonArray?.firstOrNull()?.jsonObject ?: return null

            val attributes = data["attributes"]?.jsonObject ?: return null
            val mTitle =
                attributes["title"]
                    ?.jsonObject
                    ?.values
                    ?.firstOrNull()
                    ?.jsonPrimitive
                    ?.content ?: title
            val description =
                attributes["description"]
                    ?.jsonObject
                    ?.get("en")
                    ?.jsonPrimitive
                    ?.content
                    ?: attributes["description"]
                        ?.jsonObject
                        ?.values
                        ?.firstOrNull()
                        ?.jsonPrimitive
                        ?.content

            val altTitles =
                attributes["altTitles"]
                    ?.jsonArray
                    ?.mapNotNull {
                        it.jsonObject.values
                            .firstOrNull()
                            ?.jsonPrimitive
                            ?.content
                    }?.joinToString(",")

            val genres =
                attributes["tags"]
                    ?.jsonArray
                    ?.mapNotNull {
                        it.jsonObject["attributes"]
                            ?.jsonObject
                            ?.get("name")
                            ?.jsonObject
                            ?.get("en")
                            ?.jsonPrimitive
                            ?.content
                    }?.joinToString(",")

            val relationships = data["relationships"]?.jsonArray ?: emptyList()
            val authors =
                relationships
                    .filter { it.jsonObject["type"]?.jsonPrimitive?.content == "author" }
                    .mapNotNull {
                        it.jsonObject["attributes"]
                            ?.jsonObject
                            ?.get("name")
                            ?.jsonPrimitive
                            ?.content
                    }.joinToString(", ")
            val artists =
                relationships
                    .filter { it.jsonObject["type"]?.jsonPrimitive?.content == "artist" }
                    .mapNotNull {
                        it.jsonObject["attributes"]
                            ?.jsonObject
                            ?.get("name")
                            ?.jsonPrimitive
                            ?.content
                    }.joinToString(", ")

            MangaMetadata(
                series = mTitle,
                writer = authors.ifBlank { null },
                penciller = artists.ifBlank { null },
                genre = genres?.ifBlank { null },
                summary = description?.ifBlank { null },
                alternateSeries = altTitles?.ifBlank { null },
                volume = volume?.ifBlank { null },
                number = chapter?.ifBlank { null },
                web = "https://mangadex.org/manga/${data["id"]?.jsonPrimitive?.content}",
            )
        } catch (e: Exception) {
            null
        }
    }
}
