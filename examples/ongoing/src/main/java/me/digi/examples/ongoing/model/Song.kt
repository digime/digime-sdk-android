package me.digi.examples.ongoing.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

class Song(
    val entityId: String,
    val genres: List<String>,
    val createdDate: Long
) {

    class Adapter : JsonDeserializer<Song> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): Song {
            val root = json.let { it as? JsonObject } ?: throw IllegalArgumentException()
            val track = root.getAsJsonObject("track")
            val artists = track.getAsJsonArray("artists")
            val genres = artists.mapNotNull { (it as? JsonObject)?.getAsJsonArray("genres")?.map { it.asJsonPrimitive.asString } }.flatten()
            val entityId = root.getAsJsonPrimitive("entityid").asString
            val createdDate = root.getAsJsonPrimitive("createddate").asLong
            return Song(entityId, genres, createdDate)
        }
    }
}